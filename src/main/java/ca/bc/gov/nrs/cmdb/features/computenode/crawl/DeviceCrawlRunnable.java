package ca.bc.gov.nrs.cmdb.features.computenode.crawl;

import com.pastdev.jsch.DefaultSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class DeviceCrawlRunnable implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(DeviceCrawlRunnable.class);
    private final String crawlId;
    private final DefaultSessionFactory sessionFactory;
    private final SimpMessagingTemplate template;

    public DeviceCrawlRunnable(String crawlId, DefaultSessionFactory sessionFactory, SimpMessagingTemplate template)
    {
        this.crawlId = crawlId;
        this.sessionFactory = sessionFactory;
        this.template = template;
    }



    @Override
    public void run()
    {

    }




//    public void error(String message)
//    {
//        ServerCrawlStatusMessage m = new ServerCrawlStatusMessage(this.server, message);
//        m.setStatus(ServerCrawlStatusMessage.Phase.FINISHED);
//        m.setStatusClass("danger");
//
//        sendProgress(m);
//    }
//
//    public void info(String message)
//    {
//        ServerCrawlStatusMessage m = new ServerCrawlStatusMessage(this.server, message);
//        m.setStatus(ServerCrawlStatusMessage.Phase.IN_PROGRESS);
//        m.setStatusClass("info");
//
//        sendProgress(m);
//    }
//
//    public void warn(String message)
//    {
//        ServerCrawlStatusMessage m = new ServerCrawlStatusMessage(this.server, message);
//        m.setStatus(ServerCrawlStatusMessage.Phase.IN_PROGRESS);
//        m.setStatusClass("warn");
//
//        sendProgress(m);
//    }
//
//    public void finish(String message)
//    {
//        ServerCrawlStatusMessage m = new ServerCrawlStatusMessage(this.server, message);
//        m.setStatus(ServerCrawlStatusMessage.Phase.FINISHED);
//        m.setStatusClass("success");
//
//        sendProgress(m);
//    }

    public void sendProgress(DeviceCrawlMessage message)
    {
        // this.status = message;
        template.convertAndSend("/topic/devicecrawl/" + this.crawlId, message);
    }

    public class DeviceCrawlMessage
    {

    }
}

