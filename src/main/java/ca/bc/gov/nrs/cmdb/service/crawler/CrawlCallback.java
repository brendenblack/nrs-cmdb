package ca.bc.gov.nrs.cmdb.service.crawler;

public interface CrawlCallback
{
    void doCallback(String id, String fqdn);
}
