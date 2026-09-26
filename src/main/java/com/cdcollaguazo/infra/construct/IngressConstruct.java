package com.cdcollaguazo.infra.construct;

import com.cdcollaguazo.infra.config.Config;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.cloudfront.origins.VpcOrigin;
import software.amazon.awscdk.services.cloudfront.origins.VpcOriginWithEndpointProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class IngressConstruct extends Construct {

    private final String platformName;

    public IngressConstruct(Construct scope, String id, IHostedZone hostedZone, ICertificate certificate,
                            ApplicationLoadBalancer alb, Config config) {
        super(scope, id);

        this.platformName = config.platformName();

        // S3 Bucket
        Bucket bucket = Bucket.Builder.create(this, "S3")
                .bucketName(platformName)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .publicReadAccess(false)
                .versioned(false)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // CloudFront Redirect Function
        Function cfRedirectFunction = Function.Builder.create(this, "CfRedirectFunction")
                .code(FunctionCode.fromInline(
                                """
                                function handler(event) {
                                    var request = event.request;
                                    var host = request.headers.host.value;
                                    
                                    if (host === '%s') {
                                        return {
                                            statusCode: 301,
                                            statusDescription: 'Moved Permanently',
                                            headers: {
                                                location: {
                                                    value: 'https://%s' + request.uri
                                                }
                                            }
                                        };
                                    }
                                    
                                    return request;
                                }
                                """.formatted(config.platformHost(), "www." + config.platformHost())
                        )
                )
                .runtime(FunctionRuntime.JS_2_0)
                .build();

        FunctionAssociation cfRedirectFunctionAssociation = FunctionAssociation.builder()
                .function(cfRedirectFunction)
                .eventType(FunctionEventType.VIEWER_REQUEST)
                .build();

        // Alb Behavior
        BehaviorOptions albOptions = BehaviorOptions.builder()
                .origin(VpcOrigin.withApplicationLoadBalancer(
                                alb, VpcOriginWithEndpointProps.builder()
                                        .protocolPolicy(OriginProtocolPolicy.HTTP_ONLY)
                                        .httpPort(80)
                                        .build()
                        )
                )
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .functionAssociations(List.of(cfRedirectFunctionAssociation))
                .build();

        // CloudFront Distribution
        Distribution cfDistribution = Distribution.Builder.create(this, "CfDistribution")
                .domainNames(List.of("www." + config.platformHost(), config.platformHost()))
                .certificate(certificate)
                .defaultRootObject("index.html")
                .defaultBehavior(BehaviorOptions.builder()
                        .origin(S3BucketOrigin.withOriginAccessControl(bucket))
                        .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                        .functionAssociations(List.of(cfRedirectFunctionAssociation))
                        .build())
                .additionalBehaviors(Map.of(
                        "/auth", albOptions,
                        "/auth/*", albOptions,
                        "*/api/*", albOptions
                ))
                .build();

        // WWW Record
        new ARecord(this, "WwwRecord", ARecordProps.builder()
                .zone(hostedZone)
                .recordName("www")
                .target(RecordTarget.fromAlias(new CloudFrontTarget(cfDistribution)))
                .build());

        // Root Record
        new ARecord(this, "RootRecord", ARecordProps.builder()
                .zone(hostedZone)
                .recordName("")
                .target(RecordTarget.fromAlias(new CloudFrontTarget(cfDistribution)))
                .build());

        // String Parameters
        StringParameter.Builder.create(this, "S3BucketNameParameter")
                .parameterName(buildParameterName("s3", "bucket-name"))
                .stringValue(bucket.getBucketName())
                .build();

        StringParameter.Builder.create(this, "CfDistributionIdParameter")
                .parameterName(buildParameterName("cf", "distribution-id"))
                .stringValue(cfDistribution.getDistributionId())
                .build();
    }

    private String buildParameterName(String module, String parameter) {
        return "/" + platformName + "/" + module + "/" + parameter;
    }

}
