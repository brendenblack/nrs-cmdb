package ca.bc.gov.nrs.cmdb.features.computenode;

import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import ca.bc.gov.nrs.cmdb.service.DeviceService;
import ca.bc.gov.nrs.cmdb.service.ServerCrawlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

public class Crawl
{
    public static class Command
    {
        private String fqdn;
        private String username;
        private String password;

        public String getFqdn()
        {
            return fqdn;
        }

        public void setFqdn(String fqdn)
        {
            this.fqdn = fqdn;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }

    public static class Model
    {

    }

    @Component
    public static class Handler
    {
        private static final Logger log = LoggerFactory.getLogger(Handler.class);
        private final DeviceService deviceService;
        private final ServerCrawlManager crawlManager;

        @Autowired
        public Handler(DeviceService deviceService, ServerCrawlManager crawlManager)
        {
            this.deviceService = deviceService;
            this.crawlManager = crawlManager;
        }

        public RestModel<Model> handle(Command message)
        {
            log.debug("Received crawl request: [fqdn: {}] [user: {}]", message.getFqdn(),
                      message.getUsername());

            String id = this.crawlManager.doFactCrawlAsync(message.getFqdn(),
                                               message.getUsername(),
                                               message.getPassword());

            RestModel<Model> model = new RestModel<>();
            model.setStatusCode(HttpStatus.ACCEPTED);
            model.addHeader("Location", ServerCrawlManager.WEBSOCKET_ROOT + "/" + id);
            return model;
        }
    }

}
