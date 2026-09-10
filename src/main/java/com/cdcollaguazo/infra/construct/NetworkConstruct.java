package com.cdcollaguazo.infra.construct;

import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.List;

public class NetworkConstruct extends Construct {

    private final Vpc vpc;
    private final SecurityGroup albSg;
    private final SecurityGroup ecsSg;
    private final SecurityGroup rdsSg;

    public NetworkConstruct(Construct scope, String id) {
        super(scope, id);

        // VPC and subnets
        vpc = Vpc.Builder
                .create(this, "Vpc")
                .ipAddresses(IpAddresses.cidr("10.0.0.0/16"))
                .vpcName("cdcollaguazo")
                .maxAzs(2)
                .natGateways(2)
                .subnetConfiguration(List.of(
                        SubnetConfiguration.builder()
                                .name("cdcollaguazo-public")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build(),
                        SubnetConfiguration.builder()
                                .name("cdcollaguazo-private-egress")
                                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                                .cidrMask(18)
                                .build(),
                        SubnetConfiguration.builder()
                                .name("cdcollaguazo-private")
                                .subnetType(SubnetType.PRIVATE_ISOLATED)
                                .cidrMask(24)
                                .build()
                ))
                .build();

        // Security Groups and inbound/outbound rules for ALB, ECS and RDS.
        albSg = SecurityGroup.Builder.create(this, "AlbSg")
                .vpc(vpc)
                .securityGroupName("cdcollaguazo-alb")
                .allowAllOutbound(false)
                .description("Security group for ALB")
                .build();

        albSg.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP from everywhere");

        ecsSg = SecurityGroup.Builder.create(this, "EcsSg")
                .vpc(vpc)
                .securityGroupName("cdcollaguazo-ecs")
                .allowAllOutbound(true)
                .description("Security group for ECS")
                .build();

        albSg.addEgressRule(ecsSg, Port.tcp(8080), "Allow TCP 8080 to ECS");
        albSg.addEgressRule(ecsSg, Port.tcp(9000), "Allow TCP 9000 to ECS");

        rdsSg = SecurityGroup.Builder.create(this, "RdsSg")
                .vpc(vpc)
                .securityGroupName("cdcollaguazo-rds")
                .allowAllOutbound(false)
                .description("Security group for RDS")
                .build();

        rdsSg.addIngressRule(ecsSg, Port.tcp(5432), "Allow TCP 5432 from ECS");
    }

    public Vpc getVpc() {
        return vpc;
    }

    public SecurityGroup getAlbSg() {
        return albSg;
    }

    public SecurityGroup getRdsSg() {
        return rdsSg;
    }

    public SecurityGroup getEcsSg() {
        return ecsSg;
    }

}
