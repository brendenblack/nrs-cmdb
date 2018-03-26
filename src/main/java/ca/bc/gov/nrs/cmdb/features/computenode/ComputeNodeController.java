package ca.bc.gov.nrs.cmdb.features.computenode;

import ca.bc.gov.nrs.cmdb.features.computenode.crawl.DoCrawl;
import ca.bc.gov.nrs.cmdb.infrastructure.RestException;
import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/computenode")
public class ComputeNodeController
{

    private static final Logger log = LoggerFactory.getLogger(ComputeNodeController.class);

    private final GetAll.Handler getAllHandler;
    private final Create.Handler createDeviceHandler;
    private final Options.Handler optionsHandler;
    private final Delete.Handler deleteHandler;
    private final DoCrawl.Handler crawlHandler;
    private final SimpMessagingTemplate template;
    private final OrientGraphFactory factory;


    @Autowired
    public ComputeNodeController(GetAll.Handler getAllHandler,
                                 Create.Handler createDeviceHandler,
                                 Options.Handler optionsHandler,
                                 Delete.Handler deleteHandler,
                                 DoCrawl.Handler crawlHandler,
                                 SimpMessagingTemplate template,
                                 OrientGraphFactory factory)
    {
        this.createDeviceHandler = createDeviceHandler;
        this.getAllHandler = getAllHandler;
        this.optionsHandler = optionsHandler;
        this.deleteHandler = deleteHandler;
        this.crawlHandler = crawlHandler;
        this.template = template;
        this.factory = factory;
    }

    @PostMapping("/seed")
    public void doSeed()
    {
        List<String> fqdns = Arrays.asList(
                "pepsi.bcgov",
                "cocacola.bcgov",
                "drpepper.bcgov",
                "rootbeer.bcgov"
        );

        for (String fqdn : fqdns)
        {
            try
            {
                Create.Command server = new Create.Command();
                server.setFqdn(fqdn);
                this.createDeviceHandler.handle(server);
            }
            catch (RestException e)
            {
                // this means the server already exists, no-op
            }
        }
    }

    @PostMapping("/test")
    public String doTest()
    {
        OrientGraphNoTx odb = this.factory.getNoTx();
        try
        {
            OrientVertexType s = odb.getVertexType("Server");
            if (s == null)
            {
                log.info("Creating a vertex with type 'Server'");
                OrientVertexType server = odb.createVertexType("Server");
                server.createProperty("id", OType.INTEGER);
                server.createProperty("fqdn", OType.STRING);
                odb.commit();
                return server.getName();
            }
            else
            {
                log.info("Server already exists");
                return "";
            }

        }
        finally
        {
            odb.shutdown();
        }
    }


    @RequestMapping(method = RequestMethod.OPTIONS)
    public Options.Model options()
    {
        return this.optionsHandler.handle(new Options.Query());
    }


    @PostMapping
    public Create.Model create(@RequestBody Create.Command message, HttpServletResponse response)
    {
        RestModel<Create.Model> result = this.createDeviceHandler.handle(message);
        for (String key : result.getHeaders().keySet())
        {
            response.addHeader(key, result.getHeaders().get(key));
        }
        response.setStatus(result.getStatusCode().value());

        return result.getModel();
    }

    @GetMapping
    public GetAll.Model getAll()
    {
        return this.getAllHandler.handle(new GetAll.Query());
    }

    @DeleteMapping
    public void delete(@RequestBody Delete.Command message, HttpServletResponse response)
    {
        this.deleteHandler.handle(message);
    }

    @GetMapping("crawl")
    public void doCrawl(HttpServletResponse response)
    {
        ca.bc.gov.nrs.cmdb.features.computenode.crawl.GetAll.Query message = new ca.bc.gov.nrs.cmdb.features.computenode.crawl.GetAll.Query();

        //RestModel result = this.crawlGetAllHandler.handle(message);

    }


    @PutMapping("crawl")
    public void doCrawl(@RequestBody DoCrawl.Command message, HttpServletResponse response)
    {
        RestModel result = this.crawlHandler.handle(message);

        response.setStatus(result.getStatusCode().value());
        for (Object okey : result.getHeaders().keySet())
        {
            response.addHeader(okey.toString(), result.getHeaders().get(okey).toString());
        }
    }

    @MessageMapping("/send/message")
    public void onReceivedMessage(String message)
    {
        this.template.convertAndSend("/crawl", new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - " + message);
    }
}
