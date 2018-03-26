package ca.bc.gov.nrs.cmdb.features.computenode.crawl;

import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;
import ca.bc.gov.nrs.cmdb.service.crawler.ServerCrawlManager;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

public class GetAll
{
    public static class Query {}

    public class Model
    {

    }

    @Component("getCrawl")
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public class Handler
    {
        private final ServerCrawlManager crawlManager;

        public Handler(ServerCrawlManager crawlManager)
        {

            this.crawlManager = crawlManager;
        }

        public RestModel<Model> handle(Query message)
        {

            Model model = new Model();

            RestModel<Model> result = new RestModel<>();
            result.setModel(model);
            return result;
        }

    }
}
