package com.cdcollaguazo.infra.config;

public class ConfigLoader {

    private ConfigLoader() {}

    public static Config loadConfig() {
        return new Config(
                required("PLATFORM_NAME"),
                required("PLATFORM_HOST"),
                required("HOSTED_ZONE_ID"),
                required("CERTIFICATE_ARN"),
                required("ROOT_DB_USER")
        );
    }

    private static String required(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }

        return value;
    }

}
