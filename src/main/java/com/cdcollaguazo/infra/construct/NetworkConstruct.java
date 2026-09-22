package com.cdcollaguazo.infra.construct;

import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.List;

public class NetworkConstruct extends Construct {

    private final Vpc vpc;
    private final SecurityGroup albSg;
    private final SecurityGroup ecsSg;
    private final SecurityGroup rdsSg;
    private final String platformName;

    public NetworkConstruct(Construct scope, String id, String platformName) {
        super(scope, id);

        this.platformName = platformName;

        // VPC and subnets
        vpc = Vpc.Builder
                .create(this, "Vpc")
                .ipAddresses(IpAddresses.cidr("10.0.0.0/16"))
                .vpcName(platformName)
                .maxAzs(2)
                .natGateways(2)
                .subnetConfiguration(List.of(
                        SubnetConfiguration.builder()
                                .name(platformName + "-public")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build(),
                        SubnetConfiguration.builder()
                                .name(platformName + "-private")
                                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                                .cidrMask(18)
                                .build(),
                        SubnetConfiguration.builder()
                                .name(platformName + "-isolated")
                                .subnetType(SubnetType.PRIVATE_ISOLATED)
                                .cidrMask(24)
                                .build()
                ))
                .build();

        // Security Groups and inbound/outbound rules for ALB, ECS and RDS.
        albSg = SecurityGroup.Builder.create(this, "AlbSg")
                .vpc(vpc)
                .securityGroupName(platformName + "-alb")
                .allowAllOutbound(false)
                .description("Security group for ALB")
                .build();

        albSg.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP from everywhere");

        ecsSg = SecurityGroup.Builder.create(this, "EcsSg")
                .vpc(vpc)
                .securityGroupName(platformName + "-ecs")
                .allowAllOutbound(true)
                .description("Security group for ECS")
                .build();

        ecsSg.addIngressRule(albSg, Port.tcp(8080), "Allow TCP 8080 from ALB");
        ecsSg.addIngressRule(albSg, Port.tcp(9000), "Allow TCP 9000 from ALB");

        albSg.addEgressRule(ecsSg, Port.tcp(8080), "Allow TCP 8080 to ECS");
        albSg.addEgressRule(ecsSg, Port.tcp(9000), "Allow TCP 9000 to ECS");

        rdsSg = SecurityGroup.Builder.create(this, "RdsSg")
                .vpc(vpc)
                .securityGroupName(platformName + "-rds")
                .allowAllOutbound(false)
                .description("Security group for RDS")
                .build();

        rdsSg.addIngressRule(ecsSg, Port.tcp(5432), "Allow TCP 5432 from ECS");

        // String Parameters
        StringParameter.Builder.create(this, "VpcIdParameter")
                .parameterName(buildParameterName("vpc-id"))
                .stringValue(vpc.getVpcId())
                .build();

        StringParameter.Builder.create(this, "Az1Parameter")
                .parameterName(buildParameterName("az-1"))
                .stringValue(vpc.getAvailabilityZones().get(0))
                .build();

        StringParameter.Builder.create(this, "Az2Parameter")
                .parameterName(buildParameterName("az-2"))
                .stringValue(vpc.getAvailabilityZones().get(1))
                .build();

        List<String> privateSubnetsIds = vpc.getPrivateSubnets().stream()
                .map(ISubnet::getSubnetId)
                .toList();

        StringParameter.Builder.create(this, "PrivateSubnet1IdParameter")
                .parameterName(buildParameterName("private-subnet-1-id"))
                .stringValue(privateSubnetsIds.get(0))
                .build();

        StringParameter.Builder.create(this, "PrivateSubnet2IdParameter")
                .parameterName(buildParameterName("private-subnet-2-id"))
                .stringValue(privateSubnetsIds.get(1))
                .build();

        StringParameter.Builder.create(this, "AlbSgIdParameter")
                .parameterName(buildParameterName("alb-sg-id"))
                .stringValue(albSg.getSecurityGroupId())
                .build();

        StringParameter.Builder.create(this, "EcsSgIdParameter")
                .parameterName(buildParameterName("ecs-sg-id"))
                .stringValue(ecsSg.getSecurityGroupId())
                .build();

        StringParameter.Builder.create(this, "RdsSgIdParameter")
                .parameterName(buildParameterName("rds-sg-id"))
                .stringValue(rdsSg.getSecurityGroupId())
                .build();
    }

    private String buildParameterName(String parameter) {
        return "/" + platformName + "/vpc/" + parameter;
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
