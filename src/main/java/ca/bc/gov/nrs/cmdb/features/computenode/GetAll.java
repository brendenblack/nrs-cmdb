package ca.bc.gov.nrs.cmdb.features.computenode;

import ca.bc.gov.nrs.cmdb.model.edges.EdgeConstants;
import ca.bc.gov.nrs.cmdb.model.vertices.VertexConstants;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class GetAll
{
    public static class Query
    {

    }

    @JsonAutoDetect
    public static class Model
    {
        private List<DeviceDto> devices = new ArrayList<>();

        public List<DeviceDto> getDevices()
        {
            return this.devices;
        }

        public void setDevices(List<DeviceDto> devices)
        {
            this.devices = devices;
        }
    }

    @JsonAutoDetect
    public static class DeviceDto
    {
        private String fqdn;
        private String operatingSystemFamily;

        public String getOperatingSystemFamily()
        {
            return operatingSystemFamily;
        }

        public void setOperatingSystemFamily(String operatingSystemFamily)
        {
            this.operatingSystemFamily = operatingSystemFamily;
        }

        public String getFqdn()
        {
            return fqdn;
        }

        public void setFqdn(String fqdn)
        {
            this.fqdn = fqdn;
        }
    }

    @Component
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public static class Handler
    {
        private static final Logger log = LoggerFactory.getLogger(Handler.class);
        private final OrientGraphFactory factory;

        @Autowired
        public Handler(OrientGraphFactory factory)
        {
            this.factory = factory;
        }

        public Model handle(Query message)
        {
            log.info("Getting servers");
            OrientGraphNoTx odb = this.factory.getNoTx();
            List<DeviceDto> servers = new ArrayList<>();
            try
            {
                Iterable<Vertex> vertices = odb.getVerticesOfClass(VertexConstants.SERVER_CLASS);

                for (Vertex v : vertices)
                {
                    log.info("Found vertex with id {}", v.getId());
                    for (String k : v.getPropertyKeys())
                    {
                        log.info("- {}: {}", k, v.getProperty(k));
                    }

                    for (Edge e : v.getEdges(Direction.OUT, EdgeConstants.IS_LOCATED_IN))
                    {
                        Vertex v2 = e.getVertex(Direction.OUT);
                        log.info("Edge {} points to vertex {}", e.getLabel(), v2.getId());
                        for (String k : v2.getPropertyKeys())
                        {
                            log.info("- {}: {}", k, v2.getProperty(k));
                        }
                    }


                    String fqdn = v.getProperty("fqdn");
                    if (fqdn == null)
                    {
                        log.warn("Unable to locate fqdn property for server vertex {}", v.getId());
                    }
                    else
                    {
                        DeviceDto s = new DeviceDto();
                        s.setFqdn(fqdn);
                        s.setOperatingSystemFamily(v.getProperty("osfamily").toString());
                        log.info("Created server {}", s.getFqdn());
                        servers.add(s);
                    }
                }
            }
            finally
            {
                odb.shutdown();
            }

            Model model = new Model();
            model.setDevices(servers);
            return model;
        }
    }
}
