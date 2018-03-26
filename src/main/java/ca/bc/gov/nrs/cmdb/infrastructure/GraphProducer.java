package ca.bc.gov.nrs.cmdb.infrastructure;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@ApplicationScope
public class GraphProducer
{
    private Graph graph;

    @PostConstruct
    public void init()
    {
        String orientDBServer = System.getenv("ORIENTDB_SERVER");
        String orientDBUser = System.getenv("ORIENTDB_USER");
        String orientDBPass = System.getenv("ORIENTDB_PASS");
        String orientDBName = System.getenv("ORIENTDB_NAME");

        com.tinkerpop.blueprints.impls.orient.OrientGraphFactory factory =  new OrientGraphFactory("remote:"+ orientDBServer +"/" + orientDBName,
                                                                                                   orientDBUser, orientDBPass);

        this.graph = factory.getNoTx();
    }

    @Produces
    @ApplicationScope
    public Graph getGraph()
    {
        return graph;
    }

    public void close(@Disposes Graph graph)
    {

    }
}
