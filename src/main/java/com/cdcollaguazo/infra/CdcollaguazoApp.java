package com.cdcollaguazo.infra;

import software.amazon.awscdk.*;

public class CdcollaguazoApp {

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Add BootstraplessSynthesizer since we don't need to upload any assets
        // Only template creation is needed
        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new CdcollaguazoStack(app, "Cdcollaguazo", props);

        System.out.println("Synthesizing cdcollaguazo infrastructure...");
        app.synth();
    }

}
