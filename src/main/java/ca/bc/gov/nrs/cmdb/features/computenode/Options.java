package ca.bc.gov.nrs.cmdb.features.computenode;

import ca.bc.gov.nrs.cmdb.model.vertices.Credential;
import ca.bc.gov.nrs.cmdb.model.vertices.Region;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
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

import java.util.*;


public class Options
{
    public static class Query
    {

    }

    @JsonAutoDetect
    public static class Model
    {
        private List<String> regions = new ArrayList<>();

        public List<String> getRegions()
        {
            return regions;
        }

        public void setRegions(List<String> regions)
        {
            this.regions = regions;
        }

        private List<CredentialDto> credentials = new ArrayList<>();

        public List<CredentialDto> getCredentials()
        {
            return credentials;
        }

        public void setCredentials(List<CredentialDto> credntials)
        {
            this.credentials = credntials;
        }
    }

    @JsonAutoDetect
    public static class CredentialDto
    {
        private String id;
        private String description;
        private String username;

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
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


        public Model handle(Query query)
        {
            OrientGraphNoTx odb = this.factory.getNoTx();
            try
            {
                Set<String> regionNames = new TreeSet<>();
                Iterable<Vertex> regionVertices = odb.getVerticesOfClass(Region.CLASS_NAME);
                for (Vertex v : regionVertices)
                {
                    String regionName = v.getProperty(Region.NAME_ATTRIBUTE);
                    if (regionName == null)
                    {
                        log.warn("Unable to determine the name for Region vertex {}", v.getId());
                    }
                    else
                    {
                        regionNames.add(regionName);
                    }
                }


                Set<CredentialDto> credentials = new HashSet<>();
                Iterable<Vertex> credVerts = odb.getVerticesOfClass(Credential.CLASS_NAME);
                for (Vertex cred : credVerts)
                {
                    String id = cred.getId().toString();
                    String description = cred.getProperty(Credential.ATTR_DESCRIPTION);
                    String username = cred.getProperty(Credential.ATTR_USERNAME);
                    if (id != null && description != null && username != null)
                    {
                        CredentialDto c = new CredentialDto();
                        c.setDescription(description);
                        c.setId(id);
                        c.setUsername(username);
                        credentials.add(c);
                    }
                }


                Model model = new Model();
                model.setRegions(new ArrayList(regionNames));
                model.setCredentials(new ArrayList(credentials));
                return model;
            }
            finally
            {
                odb.shutdown();
            }
        }
    }
}
