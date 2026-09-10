package com.cdcollaguazo.infra.construct;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.cloudfront.origins.VpcOrigin;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget;
import software.amazon.awscdk.services.s3.*;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static com.cdcollaguazo.infra.config.EnvironmentConfig.*;

public class IngressConstruct extends Construct {

    public IngressConstruct(Construct scope, String id, ApplicationLoadBalancer alb) {
        super(scope, id);

        // S3 Bucket
        Bucket bucket = Bucket.Builder.create(this, "S3")
                .bucketName("cdcollaguazo")
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .publicReadAccess(false)
                .versioned(false)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // Alb behavior
        BehaviorOptions albOptions = BehaviorOptions.builder()
                .origin(VpcOrigin.withApplicationLoadBalancer(alb))
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .build();

        // Certificate
        ICertificate certificate = Certificate.fromCertificateArn(this, "Certificate",
                CERTIFICATE_ARN);

        // CloudFront Distribution
        Distribution cfDistribution = Distribution.Builder.create(this, "CfDistribution")
                .domainNames(List.of("www." + MAIN_HOST, MAIN_HOST))
                .certificate(certificate)
                .defaultRootObject("index.html")
                .defaultBehavior(BehaviorOptions.builder()
                        .origin(S3BucketOrigin.withOriginAccessControl(bucket))
                        .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                        .build())
                .additionalBehaviors(Map.of(
                        "/auth/*", albOptions,
                        "*/api/*", albOptions
                ))
                .build();

        // Hosted zone
        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone",
                HostedZoneAttributes.builder()
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .zoneName(MAIN_HOST)
                        .build());

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
    }

}
