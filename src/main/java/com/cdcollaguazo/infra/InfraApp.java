package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.config.Config;
import com.cdcollaguazo.infra.config.ConfigLoader;
import software.amazon.awscdk.*;

public class InfraApp {

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Add BootstraplessSynthesizer since we don't need to upload any assets
        // Only template creation is needed
        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        Config config = ConfigLoader.loadConfig();

        InfraIngressStack infraIngressStack = new InfraIngressStack(app, "InfraIngress", props, config);

        new InfraComputeStack(app, "InfraCompute", props, infraIngressStack.getCfDistribution(), config);

        app.synth();
    }

}
