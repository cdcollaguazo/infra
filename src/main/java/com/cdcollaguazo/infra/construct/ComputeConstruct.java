package com.cdcollaguazo.infra.construct;

import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerInsights;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.constructs.Construct;

public class ComputeConstruct extends Construct {

    private final ApplicationLoadBalancer alb;

    public ComputeConstruct(Construct scope, String id, Vpc vpc, SecurityGroup albSg) {
        super(scope, id);

        // Cluster
        Cluster.Builder.create(this, "EcsCluster")
                .clusterName("cdcollaguazo")
                .vpc(vpc)
                .containerInsightsV2(ContainerInsights.ENHANCED)
                .build();

        // Load Balancer
        alb = ApplicationLoadBalancer.Builder.create(this, "Alb")
                .loadBalancerName("cdcollaguazo")
                .internetFacing(false)
                .ipAddressType(IpAddressType.IPV4)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build())
                .securityGroup(albSg)
                .build();

        alb.addListener("HttpListener",
                ApplicationListenerProps.builder()
                        .loadBalancer(alb)
                        .protocol(ApplicationProtocol.HTTP)
                        .port(80)
                        .open(false)
                        .defaultAction(
                                ListenerAction.fixedResponse(
                                        404,
                                        FixedResponseOptions.builder()
                                                .contentType("text/plain")
                                                .messageBody("Not Found")
                                                .build()
                                )
                        )
                        .build());
    }

    public ApplicationLoadBalancer getAlb() {
        return alb;
    }

}
