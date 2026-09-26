package com.cdcollaguazo.infra.construct;

import com.cdcollaguazo.infra.config.Config;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.efs.FileSystem;
import software.amazon.awscdk.services.efs.PerformanceMode;
import software.amazon.awscdk.services.efs.ThroughputMode;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.List;

import static software.amazon.awscdk.services.rds.PostgresInstanceEngineProps.builder;

public class DataConstruct extends Construct {

    private final String platformName;

    public DataConstruct(Construct scope, String id, Vpc vpc, SecurityGroup rdsSg, SecurityGroup efsSg, Config config) {
        super(scope, id);

        platformName = config.platformName();

        // RDS
        DatabaseInstance rds = DatabaseInstance.Builder.create(this, "Rds")
                .engine(DatabaseInstanceEngine.postgres(
                        builder()
                                .version(PostgresEngineVersion.VER_18)
                                .build()
                ))
                .credentials(Credentials.fromGeneratedSecret(config.rootDbUser(),
                        CredentialsBaseOptions.builder()
                                .secretName(platformName + "-rds")
                                .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .allocatedStorage(30)
                .multiAz(false)
                .networkType(NetworkType.IPV4)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build())
                .securityGroups(List.of(rdsSg))
                .caCertificate(CaCertificate.RDS_CA_RSA2048_G1)
                .publiclyAccessible(false)
                .port(5432)
                .databaseInsightsMode(DatabaseInsightsMode.STANDARD)
                .enablePerformanceInsights(true)
                .performanceInsightRetention(PerformanceInsightRetention.DEFAULT)
                .databaseName("postgres")
                .backupRetention(Duration.days(1))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // EFS
        FileSystem efs = FileSystem.Builder.create(this, "Efs")
                .fileSystemName("bsn")
                .oneZone(false)
                .enableAutomaticBackups(true)
                .throughputMode(ThroughputMode.ELASTIC)
                .performanceMode(PerformanceMode.GENERAL_PURPOSE)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroup(efsSg)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // String Parameters
        StringParameter.Builder.create(this, "RdsInstanceHostParameter")
                .parameterName(buildParameterName("rds", "instance-host"))
                .stringValue(rds.getDbInstanceEndpointAddress())
                .build();

        StringParameter.Builder.create(this, "RdsInstancePortParameter")
                .parameterName(buildParameterName("rds", "instance-port"))
                .stringValue(rds.getDbInstanceEndpointPort())
                .build();

        StringParameter.Builder.create(this, "RdsSecretArnParameter")
                .parameterName(buildParameterName("rds", "secret-arn"))
                .stringValue(rds.getSecret().getSecretArn())
                .build();

        StringParameter.Builder.create(this, "EfsFileSystemIdParameter")
                .parameterName(buildParameterName("efs", "file-system-id"))
                .stringValue(efs.getFileSystemId())
                .build();
    }

    private String buildParameterName(String module, String parameter) {
        return "/" + platformName + "/" + module + "/" + parameter;
    }

}
