package ca.bc.gov.nrs.cmdb.service;

import ca.bc.gov.nrs.cmdb.infrastructure.WebSocketConfig;
import ca.bc.gov.nrs.cmdb.service.crawler.CrawlCallback;
import ca.bc.gov.nrs.cmdb.service.crawler.NaiveServerFactsCrawlRunnableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ServerCrawlManager implements CrawlCallback
{
    private final static Logger log = LoggerFactory.getLogger(ServerCrawlManager.class);

    private final SimpMessagingTemplate template;
    private Map<String, NaiveServerFactsCrawlRunnableImpl> ongoingFactCrawls = new HashMap<>();

    public static final String WEBSOCKET_ROOT = WebSocketConfig.BROKER_ROOT_PATH + "/crawl";

    public ServerCrawlManager(SimpMessagingTemplate template)
    {
        this.template = template;
    }

    public String doFactCrawlAsync(String fqdn, String username, String password)
    {
        log.debug("Beginning asynchronous crawl of {}", fqdn);
        if (this.ongoingFactCrawls.containsKey(fqdn))
        {
            log.error("Already crawling {}", fqdn);
            // fail
        }

        String crawlId = UUID.randomUUID().toString();
        log.debug("Will assign id {}", crawlId);
        NaiveServerFactsCrawlRunnableImpl crawl = new NaiveServerFactsCrawlRunnableImpl(crawlId, template, fqdn, username, password);
        crawl.setCallback(this);
        log.info("Beginning asynchronous crawl");
        new Thread(crawl).start();

        this.ongoingFactCrawls.put(fqdn, crawl);
        log.debug("Returning crawl id {}", crawlId);
        return crawlId;
    }

    @Override
    public void doCallback()
    {
        log.debug("\nCallback triggered\n");
    }




}
