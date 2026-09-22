package com.cdcollaguazo.infra.config;

public record Config(
        String domain,
        String hostedZoneId,
        String certificateArn,
        String rootDbUser
) {
}
