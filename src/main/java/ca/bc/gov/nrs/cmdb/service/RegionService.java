package ca.bc.gov.nrs.cmdb.service;

import ca.bc.gov.nrs.cmdb.model.vertices.Region;
import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegionService
{
    private static final Logger log = LoggerFactory.getLogger(RegionService.class);
    private final OrientGraphFactory factory;

    @Autowired
    public RegionService(OrientGraphFactory factory)
    {
        this.factory = factory;
    }

    public Region getRegion(String name)
    {
        OrientVertex vertex = getRegionVertex(name);
        if (vertex == null)
        {
            return null;
        }

        Region region = new Region();
        Object oname = vertex.getProperty(Region.NAME_ATTRIBUTE);
        if (oname == null)
        {
            log.warn("Warning about regions and stuff");
        }


        region.setName(oname.toString());
        return region;
    }

    public OrientVertex getRegionVertex(String name)
    {
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {
            return odb.getVertex(ComputeNode.CLASS_NAME + "." + ComputeNode.ATTRIBUTE_FQDN);
        }
        finally
        {
            odb.shutdown();
        }
    }


}
