package ca.bc.gov.nrs.cmdb.service;

import ca.bc.gov.nrs.cmdb.infrastructure.RestException;
import ca.bc.gov.nrs.cmdb.model.vertices.OperatingSystem;
import ca.bc.gov.nrs.cmdb.model.vertices.OperatingSystemFamily;
import ca.bc.gov.nrs.cmdb.model.vertices.Region;
import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DeviceService
{
    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);
    private final OrientGraphFactory factory;
    private final RegionService regionService;

    @Autowired
    public DeviceService(OrientGraphFactory factory, RegionService regionService)
    {
        this.factory = factory;
        this.regionService = regionService;
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

    public void saveServer(ComputeNode server)
    {
        OrientVertex vertex = getServerVertex(server.getFqdn());
        if (vertex == null)
        {
            OrientGraphNoTx odb = this.factory.getNoTx();
            try
            {

            }
            finally
            {
                odb.shutdown();
            }
        }

    }


    public OperatingSystem getOperatingSystem(String id)
    {
        OrientVertex vertex = getOperatingSystemVertex(id);

        OperatingSystem os = new OperatingSystem();
        os.setId(vertex.getId().toString());


        Iterable<Edge> edges = vertex.getEdges(Direction.OUT, OperatingSystem.LINK_IS_MEMBER_OF);
        if (edges.iterator().hasNext())
        {
            Edge edge = edges.iterator().next();
            Vertex vfamily = edge.getVertex(Direction.OUT);
            if (vfamily != null)
            {
                Object oname = vfamily.getProperty(OperatingSystemFamily.NAME);
                if (oname != null)
                {
                    os.setFamily(oname.toString());
                }
            }
        }

        Object oname = vertex.getProperty(OperatingSystem.ATTR_NAME);
        if (oname != null)
        {
            os.setName(oname.toString());
        }
        else
        {
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        Object oprettyName = vertex.getProperty(OperatingSystem.ATTR_PRETTY_NAME);
        if (oprettyName != null)
        {
            os.setPrettyName(oprettyName.toString());
        }

        Object oversion = vertex.getProperty(OperatingSystem.ATTR_VERSION);
        if (oversion != null)
        {
            os.setVersion(oversion.toString());
        }

        Object oversionName = vertex.getProperty(OperatingSystem.ATTR_VERSION_NAME);
        if (oversionName != null)
        {
            os.setVersionName(oversionName.toString());
        }

        Object ovariant = vertex.getProperty(OperatingSystem.ATTR_VARIANT_ID);
        if (ovariant != null)
        {
            os.setVariantId(ovariant.toString());
        }

        return os;
    }


    public OrientVertex getOperatingSystemVertex(String id)
    {
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {
            return odb.getVertex(id);
        }
        finally
        {
            odb.shutdown();
        }
    }

    public OrientVertex getOperatingSystemFamilyVertex(String name)
    {
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {
            Iterable<Vertex> vertices = odb.getVertices(OperatingSystemFamily.CLASS_NAME + "." + OperatingSystemFamily.NAME, name);
            if (vertices.iterator().hasNext())
            {
                return (OrientVertex)vertices.iterator().next();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            odb.shutdown();
        }
    }


    public void doCrawlAsync(String fqdn)
    {
        OrientVertex vertex = getServerVertex(fqdn);
        if (vertex == null)
        {

        }
    }

}
