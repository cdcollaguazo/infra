package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.config.ConfigLoader;
import software.amazon.awscdk.*;

public class InfraApp {

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Add BootstraplessSynthesizer since we don't need to upload any assets
        // Only template creation is needed
        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new InfraStack(app, "Infra", props, ConfigLoader.loadConfig());

        app.synth();
    }

}
