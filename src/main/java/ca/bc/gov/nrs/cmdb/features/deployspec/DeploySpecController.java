package ca.bc.gov.nrs.cmdb.features.deployspec;

import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

public class DeploySpecController
{
    private static final Logger log = LoggerFactory.getLogger(DeploySpecController.class);
    private final Create.Handler createHandler;

    @Autowired
    public DeploySpecController(Create.Handler createHandler)
    {
        this.createHandler = createHandler;
    }

    @PostMapping
    public Create.Model create(Create.Query message)
    {
        RestModel<Create.Model> result = this.createHandler.handle(message);


        return result.getModel();
    }

}
