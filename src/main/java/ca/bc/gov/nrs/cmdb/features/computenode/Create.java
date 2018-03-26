package ca.bc.gov.nrs.cmdb.features.computenode;

import ca.bc.gov.nrs.cmdb.infrastructure.RestException;
import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import ca.bc.gov.nrs.cmdb.model.vertices.Region;
import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;
import ca.bc.gov.nrs.cmdb.service.DeviceService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.jcraft.jsch.JSchException;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.command.CommandRunner;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Create
{
    public static class Command
    {
        private String fqdn;
        private String regionId;
        private String otherRegionName;

        public String getFqdn()
        {
            return fqdn;
        }

        public void setFqdn(String fqdn)
        {
            this.fqdn = fqdn;
        }


        public String getRegionId()
        {
            return regionId;
        }

        public void setRegionId(String regionId)
        {
            this.regionId = regionId;
        }

        public String getOtherRegionName()
        {
            return otherRegionName;
        }

        public void setOtherRegionName(String otherRegionName)
        {
            this.otherRegionName = otherRegionName;
        }
    }

    @JsonAutoDetect
    public static class Model
    {
        private String deviceId;
        private boolean crawling;
        private String crawlStatusUrl;

        public String getDeviceId()
        {
            return deviceId;
        }

        public void setDeviceId(String deviceId)
        {
            this.deviceId = deviceId;
        }
    }

    @Component
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public static class Handler
    {
        private static final Logger log = LoggerFactory.getLogger(Handler.class);

        private final OrientGraphFactory factory;
        private final DeviceService deviceService;

        @Autowired
        public Handler(OrientGraphFactory factory,
                       DeviceService deviceService)
        {
            this.factory = factory;
            this.deviceService = deviceService;
        }


        public RestModel<Model> handle(Command command)
        {
            ComputeNode server = this.deviceService.getServer(command.getFqdn());



            if (server != null)
            {
                log.info("A vertex with fqdn {} already exists with id {}",
                         command.getFqdn(),
                         server.getId());

                throw new RestException(HttpStatus.CONFLICT, "A vertex already exists for address " + command.getFqdn() + " with id " + server.getId());
            }




            log.trace("Checking to see if {} already exists", command.getFqdn());
            OrientGraphNoTx odb = this.factory.getNoTx();
            try
            {

                log.info("Creating a new vertex to represent {}", command.getFqdn());
                Map<String,String> props = new HashMap<>();
                props.put("fqdn", command.getFqdn().toLowerCase());
                Vertex deviceVertex = odb.addVertex("class:" + ComputeNode.CLASS_NAME, props);
                log.info("Created vertex {}", deviceVertex.getId());

                if (command.getRegionId() != null && command.getRegionId().length() > 0)
                {
                    Iterable<Vertex> regionVertices = odb.getVertices(Region.CLASS_NAME + "." + Region.NAME_ATTRIBUTE, command.getRegionId());
                    Vertex regionVertex;
                    if (regionVertices.iterator().hasNext())
                    {
                        regionVertex = regionVertices.iterator().next();
                    }
                    else
                    {
                        Map<String,String> regionProperties = new HashMap<>();
                        regionProperties.put(Region.NAME_ATTRIBUTE, command.getRegionId());
                        regionVertex = odb.addVertex(Region.CLASS_NAME, regionProperties);
                    }

                    if (regionVertex != null)
                    {
                        deviceVertex.addEdge(ComputeNode.LINK_IS_LOCATED_IN, regionVertex);
                    }
                    else
                    {
                        log.warn("Something went wrong with regionId");
                    }

                }
                else if (command.getRegionId().equals("other"))
                {
                    // TODO: create region?
                }

                odb.commit();

                String deviceId = deviceVertex.getId().toString();
                RestModel<Model> result = new RestModel<Model>();
                result.setStatusCode(HttpStatus.CREATED);
                result.addHeader("Location", "/api/computenode/" + deviceId); // TODO: generate the link

                Model model = new Model();
                model.setDeviceId(deviceId);

                result.setModel(model);
                return result;

            }
            finally
            {
                odb.shutdown();
            }
        }

        public String doServerCrawl(String fqdn, String username, String password)
        {
            // TODO: mask password
            log.debug("Will attempt to crawl server {} with username {} and password {}", fqdn, username, password);
            try
            {
                DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory("nrscdua", fqdn, 22);
                defaultSessionFactory.setPassword(password);
                defaultSessionFactory.setConfig("StrictHostKeyChecking", "no");


                CommandRunner commandRunner = new CommandRunner(defaultSessionFactory);
                CommandRunner.ExecuteResult unameResult = commandRunner.execute("uname -s");

                switch (unameResult.getStdout())
                {
                    case "Linux":
                        break;
                    case "SunOS":
                        log.warn("Host {} is SunOS, property discovery is not yet implemented", fqdn);
                        break;
                    default:
                        log.warn("Unrecognized OS: {}", unameResult.getStdout());
                        break;
                }




            }
            catch (JSchException e)
            {
                log.error("An error occurred while attempting to create a session factory to {} with username {}", fqdn, username, e);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return "";
        }

        public String getOsName(DefaultSessionFactory sessionFactory)
        {
            String os = "";
            CommandRunner commandRunner = new CommandRunner(sessionFactory);

            try
            {
                CommandRunner.ExecuteResult unameResult = commandRunner.execute("uname -s");
                os = unameResult.getStdout().trim();
            }
            catch (JSchException |IOException e)
            {
                log.error("An error occurred fetching the OS name of this server", e);
            }

            return os;
        }


        public void readLinuxProperties(Properties p)
        {

        }

        public OrientVertexType createVertexType(OrientGraphNoTx odb)
        {
            OrientVertexType serverType = odb.createVertexType(ComputeNode.CLASS_NAME);
            serverType.createProperty(ComputeNode.ATTRIBUTE_FQDN, OType.STRING).setMandatory(true).setNotNull(true);
            serverType.createIndex("fqdnIdx", OClass.INDEX_TYPE.UNIQUE, ComputeNode.ATTRIBUTE_FQDN);

            odb.commit();

            return serverType;
        }



        public Properties getLinuxProperties(DefaultSessionFactory sessionFactory)
        {
            Properties p = new Properties();
            CommandRunner commandRunner = new CommandRunner(sessionFactory);
            try
            {
                CommandRunner.ExecuteResult result = commandRunner.execute("cat /etc/*release");

                String[] lines = result.getStdout().split("\n");
                for (String line : lines)
                {
                    if (line.contains("="))
                    {
                        p.load(new StringReader(line));
                    }
                }
                log.info("------------- results ------------------");
                for (Object k : p.keySet())
                {
                    log.info("{} = {}", k, p.get(k));
                }
            }

            catch (JSchException | IOException e)
            {
                log.error("An error occurred attemping to get properties of target Linux OS", e);
            }

            return p;
        }
    }
}
