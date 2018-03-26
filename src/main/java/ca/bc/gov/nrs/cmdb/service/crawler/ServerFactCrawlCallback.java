package ca.bc.gov.nrs.cmdb.service.crawler;

import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;

public interface ServerFactCrawlCallback
{
    void doCallback(String crawlId, ComputeNode server);
}
