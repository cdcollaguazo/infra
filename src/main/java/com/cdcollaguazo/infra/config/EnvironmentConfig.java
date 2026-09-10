package com.cdcollaguazo.infra.config;

public class EnvironmentConfig {

    public static final String ROOT_DB_USER = System.getenv("ROOT_DB_USER");
    public static final String HOSTED_ZONE_ID = System.getenv("HOSTED_ZONE_ID");
    public static final String CERTIFICATE_ARN = System.getenv("CERTIFICATE_ARN");
    public static final String MAIN_HOST = System.getenv("MAIN_HOST");

}
