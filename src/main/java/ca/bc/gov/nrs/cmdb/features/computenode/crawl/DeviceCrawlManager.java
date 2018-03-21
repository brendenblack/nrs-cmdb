package ca.bc.gov.nrs.cmdb.features.computenode.crawl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceCrawlManager
{
    private List<UUID> activeCrawls = new ArrayList<>();

}
