package ca.bc.gov.nrs.cmdb.infrastructure;

import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;
import ca.bc.gov.nrs.cmdb.model.vertices.OperatingSystem;
import ca.bc.gov.nrs.cmdb.model.vertices.Region;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Repository
public class NaiveRepository
{
    private final OrientGraphFactory factory;

    public static final Predicate<String> NULL_OR_EMPTY = (in) -> null == in || "".equals(in);

    @Autowired
    public NaiveRepository(OrientGraphFactory factory)
    {
        this.factory = factory;
    }


    public OrientVertex getVertex(String id)
    {
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {
            OrientVertex vertex = odb.getVertex(id);
            return vertex;
        }
        finally
        {
            odb.shutdown();
        }
    }


    public Vertex saveServer(ComputeNode server)
    {

        OrientVertex vertex = getVertex(server.getId());

        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {

            if (vertex == null)
            {
                Map<String,String> props = new HashMap<>();
                props.put("fqdn", server.getFqdn().toLowerCase());
                vertex = odb.addVertex("class:" + ComputeNode.CLASS_NAME, props);
            }

            vertex.setProperty(ComputeNode.ATTRIBUTE_IP_ADDRESS, server.getIpAddress().toString());

            if (server.getOperatingSystem() != null)
            {
                Vertex vOs;
                addOrUpdateOperatingSystem(server.getOperatingSystem());
            }
        }
        finally
        {
            odb.shutdown();
        }


    }

    public OperatingSystem addOrUpdateOperatingSystem(OperatingSystem os)
    {
        OrientVertex vertex = getVertex(os.getId());
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {

            if (vertex == null)
            {
                Map<String,String> props = new HashMap<>();
                os.get
            }
        }
        finally
        {
            odb.shutdown();
        }
    }



    public ComputeNode getServer(String fqdn)
    {
        OrientVertex vertex = getServerVertex(fqdn);
        if (vertex != null)
        {

            ComputeNode server = new ComputeNode();
            server.setId(vertex.getId().toString());
            Object ofqdn = vertex.getProperty(ComputeNode.ATTRIBUTE_FQDN);
            if (ofqdn != null)
            {
                server.setFqdn(ofqdn.toString());
            }
            else
            {
                log.error("FQDN of vertex {} was not readable", vertex.getId());
                throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "FQDN of vertex " + vertex.getId() + " was not readable");
            }

            for (Edge e : vertex.getEdges(Direction.OUT, ComputeNode.LINK_IS_LOCATED_IN))
            {
                Vertex region = e.getVertex(Direction.OUT);
                Object oname = region.getProperty(Region.NAME_ATTRIBUTE);
                if (oname != null)
                {
                    server.setRegion(this.regionService.getRegion(oname.toString()));
                }
            }


            return server;
        }
        else
        {
            return null;
        }
    }



    public OrientVertex getServerVertex(String fqdn)
    {
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {
            OrientVertex vertex = odb.getVertex(ComputeNode.CLASS_NAME + "." + ComputeNode.ATTRIBUTE_FQDN);
            return vertex;
        }
        finally
        {
            odb.shutdown();
        }
    }



}
