package ca.bc.gov.nrs.cmdb.service.crawler;

import ca.bc.gov.nrs.cmdb.infrastructure.WebSocketConfig;
import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServerCrawlManager implements ServerFactCrawlCallback
{
    private final static Logger log = LoggerFactory.getLogger(ServerCrawlManager.class);

    private final SimpMessagingTemplate template;
    private static Map<String, NaiveServerFactsCrawlRunnableImpl> ongoingFactCrawls = new HashMap<>();

    public static final String WEBSOCKET_ROOT = WebSocketConfig.BROKER_ROOT_PATH + "/crawl";

    public ServerCrawlManager(SimpMessagingTemplate template)
    {
        this.template = template;
    }

    public String doFactCrawlAsync(ComputeNode server, String username, String password)
    {
        log.debug("Beginning asynchronous crawl of {}", server.getFqdn());
        if (ongoingFactCrawls.containsKey(server.getFqdn()))
        {
            log.error("Already crawling {}", server.getFqdn());
            // fail
        }

        String crawlId = UUID.randomUUID().toString();
        log.debug("Will assign id {}", crawlId);

        CrawlUpdateMessage m = new CrawlUpdateMessage();
        m.setCrawlId(crawlId);
        m.setFqdn(server.getFqdn());
        m.setStatus("Beginning");
        sendMessage(m);

        NaiveServerFactsCrawlRunnableImpl crawl = new NaiveServerFactsCrawlRunnableImpl(crawlId, template, server, username, password);
        crawl.setCallback(this);
        log.info("Beginning asynchronous crawl");
        new Thread(crawl).start();

        ongoingFactCrawls.put(server.getFqdn(), crawl);
        log.debug("Returning crawl id {}", crawlId);
        return crawlId;
    }

    public List<OngoingCrawl> getOngoingFactCrawls()
    {
        List<OngoingCrawl> crawls = new ArrayList<>();

        for (String id : ongoingFactCrawls.keySet())
        {
            NaiveServerFactsCrawlRunnableImpl runnable = ongoingFactCrawls.get(id);
            crawls.add(runnable);
        }

        return crawls;
    }


    @Override
    public void doCallback(String crawlId, ComputeNode server)
    {
        log.debug("\nCallback triggered\n");
        CrawlUpdateMessage m = new CrawlUpdateMessage();
        m.setCrawlId(crawlId);
        m.setFqdn(server.getFqdn());
        m.setStatus("Ending");
        sendMessage(m);
    }

    public void sendMessage(CrawlUpdateMessage message)
    {
        log.info("Sending message {}: {}", message.getCrawlId(), message.getStatus());
        template.convertAndSend(ServerCrawlManager.WEBSOCKET_ROOT, message);
    }




    @JsonAutoDetect
    public class CrawlUpdateMessage
    {
        private String crawlId;
        private String fqdn;
        private String status;

        public String getCrawlId()
        {
            return crawlId;
        }

        public void setCrawlId(String crawlId)
        {
            this.crawlId = crawlId;
        }

        public String getFqdn()
        {
            return fqdn;
        }

        public void setFqdn(String fqdn)
        {
            this.fqdn = fqdn;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }
    }





}
