package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.config.Config;
import com.cdcollaguazo.infra.construct.ComputeConstruct;
import com.cdcollaguazo.infra.construct.DataConstruct;
import com.cdcollaguazo.infra.construct.NetworkConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class InfraComputeStack extends Stack {

    public InfraComputeStack(Construct scope, String id, StackProps props, Config config) {
        super(scope, id, props);

        NetworkConstruct networkConstruct = new NetworkConstruct(this, "Network", config.platformName());

        new ComputeConstruct(this, "Compute", networkConstruct.getVpc(),
                networkConstruct.getAlbSg(), config.platformName());

        new DataConstruct(this, "Data", networkConstruct.getVpc(), networkConstruct.getRdsSg(),
                networkConstruct.getEfsSg(), config);
    }

}
