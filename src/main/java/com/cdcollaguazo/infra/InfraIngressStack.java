package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.config.Config;
import com.cdcollaguazo.infra.construct.IngressConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancerAttributes;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationLoadBalancer;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

public class InfraIngressStack extends Stack {

    private final String platformName;

    public InfraIngressStack(Construct scope, String id, StackProps props, Config config) {
        super(scope, id, props);

        platformName = config.platformName();

        // Hosted Zone
        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone",
                HostedZoneAttributes.builder()
                        .hostedZoneId(config.hostedZoneId())
                        .zoneName(config.platformHost())
                        .build());

        // Certificate
        ICertificate certificate = Certificate.fromCertificateArn(this, "Certificate", config.certificateArn());

        // Alb
        String albSgId = getValueForParameter("vpc", "alb-sg-id");
        String albArn = getValueForParameter("alb", "load-balancer-arn");
        String albDnsName = getValueForParameter("alb", "load-balancer-dns-name");

        IApplicationLoadBalancer alb = ApplicationLoadBalancer.fromApplicationLoadBalancerAttributes(this, "Alb",
                ApplicationLoadBalancerAttributes.builder()
                        .loadBalancerArn(albArn)
                        .securityGroupId(albSgId)
                        .loadBalancerDnsName(albDnsName)
                        .build());

        new IngressConstruct(this, "Ingress", hostedZone, certificate, alb, config);
    }

    private String getValueForParameter(String module, String parameter) {
        return StringParameter.valueForStringParameter(this,
                "/" + platformName + "/" + module + "/" + parameter);
    }

}
