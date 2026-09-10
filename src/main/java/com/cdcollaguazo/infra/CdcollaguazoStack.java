package com.cdcollaguazo.infra;

import com.cdcollaguazo.infra.construct.ComputeConstruct;
import com.cdcollaguazo.infra.construct.DataConstruct;
import com.cdcollaguazo.infra.construct.IngressConstruct;
import com.cdcollaguazo.infra.construct.NetworkConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class CdcollaguazoStack extends Stack {

    public CdcollaguazoStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        NetworkConstruct networkConstruct = new NetworkConstruct(this, "Network");
        new DataConstruct(this, "Data", networkConstruct.getVpc(), networkConstruct.getRdsSg());
        ComputeConstruct computeConstruct = new ComputeConstruct(this, "Compute", networkConstruct.getVpc(),
                networkConstruct.getAlbSg());
        new IngressConstruct(this, "Ingress", computeConstruct.getAlb());
    }

}
