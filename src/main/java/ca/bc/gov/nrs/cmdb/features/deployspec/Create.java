package ca.bc.gov.nrs.cmdb.features.deployspec;

import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class Create
{
    public static class Query
    {
        private String key;
        private String version;
        private List<RequirementQuery> requirements = new ArrayList<>();
    }

    public static class RequirementQuery
    {
        private String interfaceKey;
        private String version;
        private String[] expand;
        private String resolution;
        private String runtime;
    }

    @JsonAutoDetect
    public static class Model
    {
        private String id;
    }

    @Component("createDeploySpecHandler")
    public static class Handler
    {
        private static final Logger log = LoggerFactory.getLogger(Handler.class);

        private final OrientGraphFactory graphFactory;

        @Autowired
        public Handler(OrientGraphFactory graphFactory)
        {
            this.graphFactory = graphFactory;
        }

        public RestModel<Model> handle(Query message)
        {


            RestModel<Model> result = new RestModel();
            Model model = new Model();


            result.setModel(model);
            return result;
        }
    }
}
