package ca.bc.gov.nrs.cmdb.features.computenode;

import ca.bc.gov.nrs.cmdb.infrastructure.RestException;
import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

public class Delete
{
    public static class Command
    {
        private String deviceId;

        public String getDeviceId()
        {
            return deviceId;
        }

        public void setDeviceId(String deviceId)
        {
            this.deviceId = deviceId;
        }
    }

    public static class Model
    {
        private String id;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }
    }


    @Component
    public static class Handler
    {
        private Logger log = LoggerFactory.getLogger(Handler.class);

        private final OrientGraphFactory factory;

        public Handler(OrientGraphFactory factory)
        {
            this.factory = factory;
        }

        public RestModel handle(Command message)
        {

            OrientGraphNoTx odb = this.factory.getNoTx();
            try
            {
                OrientVertex vertex = odb.getVertex(message.getDeviceId());
                if (vertex == null)
                {
                    throw new RestException(HttpStatus.NOT_FOUND, "Unable to find vertex with id " + message.getDeviceId() + " to delete");
                }

                odb.removeVertex(vertex);

                RestModel<Model> result = new RestModel<>();
                Model model = new Model();
                model.setId(message.getDeviceId());
                result.setModel(model);
                result.setStatusCode(HttpStatus.NO_CONTENT);
                return result;
            }
            finally
            {
                odb.shutdown();
            }
        }
    }

}
