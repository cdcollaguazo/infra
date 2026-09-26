package com.cdcollaguazo.infra.construct;

import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerInsights;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

public class ComputeConstruct extends Construct {

    private final ApplicationLoadBalancer alb;
    private final String platformName;

    public ComputeConstruct(Construct scope, String id, Vpc vpc, SecurityGroup albSg, String platformName) {
        super(scope, id);

        this.platformName = platformName;

        // Cluster
        Cluster ecsCluster = Cluster.Builder.create(this, "EcsCluster")
                .clusterName(platformName)
                .vpc(vpc)
                .containerInsightsV2(ContainerInsights.ENHANCED)
                .build();

        // Load Balancer
        alb = ApplicationLoadBalancer.Builder.create(this, "Alb")
                .loadBalancerName(platformName)
                .internetFacing(false)
                .ipAddressType(IpAddressType.IPV4)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build())
                .securityGroup(albSg)
                .build();

        ApplicationListener albHttpListener = alb.addListener("AlbHttpListener",
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

        // String Parameters
        StringParameter.Builder.create(this, "EcsClusterArnParameter")
                .parameterName(buildParameterName("ecs", "cluster-arn"))
                .stringValue(ecsCluster.getClusterArn())
                .build();

        StringParameter.Builder.create(this, "EcsClusterNameParameter")
                .parameterName(buildParameterName("ecs", "cluster-name"))
                .stringValue(ecsCluster.getClusterName())
                .build();

        StringParameter.Builder.create(this, "AlbHttpListenerArnParameter")
                .parameterName(buildParameterName("alb", "http-listener-arn"))
                .stringValue(albHttpListener.getListenerArn())
                .build();
    }

    private String buildParameterName(String module, String parameter) {
        return "/" + platformName + "/" + module + "/" + parameter;
    }

    public ApplicationLoadBalancer getAlb() {
        return alb;
    }

}
