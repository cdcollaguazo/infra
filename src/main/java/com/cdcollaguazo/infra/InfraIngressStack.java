package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.config.Config;
import com.cdcollaguazo.infra.construct.IngressConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.constructs.Construct;

public class InfraIngressStack extends Stack {

    public InfraIngressStack(Construct scope, String id, StackProps props, ApplicationLoadBalancer alb, Config config) {
        super(scope, id, props);

        // Hosted Zone
        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone",
                HostedZoneAttributes.builder()
                        .hostedZoneId(config.hostedZoneId())
                        .zoneName(config.platformHost())
                        .build());

        // Certificate
        ICertificate certificate = Certificate.fromCertificateArn(this, "Certificate", config.certificateArn());

        new IngressConstruct(this, "Ingress", hostedZone, certificate, alb, config);
    }

}
