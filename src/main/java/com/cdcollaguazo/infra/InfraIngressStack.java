package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.config.Config;
import com.cdcollaguazo.infra.construct.IngressConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.constructs.Construct;

public class InfraIngressStack extends Stack {

    private final Distribution cfDistribution;

    public InfraIngressStack(Construct scope, String id, StackProps props, Config config) {
        super(scope, id, props);

        // Hosted Zone
        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone",
                HostedZoneAttributes.builder()
                        .hostedZoneId(config.hostedZoneId())
                        .zoneName(config.platformHost())
                        .build());

        IngressConstruct ingressConstruct = new IngressConstruct(this, "Ingress", hostedZone, config);

        cfDistribution = ingressConstruct.getCfDistribution();
    }

    public Distribution getCfDistribution() {
        return cfDistribution;
    }

}
