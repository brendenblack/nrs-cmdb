package ca.bc.gov.nrs.cmdb.service.crawler;

public interface OngoingCrawl
{
    String getId();

    String getFqdn();

    NaiveServerFactsCrawlRunnableImpl.Message getStatus();

}
