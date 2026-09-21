package com.cdcollaguazo.infra.config;

public record Config(
        String platformName,
        String platformHost,
        String hostedZoneId,
        String certificateArn,
        String rootDbUser
) {
}
