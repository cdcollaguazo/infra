package com.cdcollaguazo.infra.construct;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.List;

import static com.cdcollaguazo.infra.config.EnvironmentConfig.ROOT_DB_USER;
import static software.amazon.awscdk.services.rds.PostgresInstanceEngineProps.builder;

public class DataConstruct extends Construct {

    public DataConstruct(Construct scope, String id, Vpc vpc, SecurityGroup rdsSg) {
        super(scope, id);

        // RDS
        DatabaseInstance.Builder.create(this, "Rds")
                .engine(DatabaseInstanceEngine.postgres(
                        builder()
                                .version(PostgresEngineVersion.VER_18)
                                .build()
                ))
                .credentials(Credentials.fromGeneratedSecret(ROOT_DB_USER,
                        CredentialsBaseOptions.builder()
                                .secretName("cdcollaguazo-rds")
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
    }

}
