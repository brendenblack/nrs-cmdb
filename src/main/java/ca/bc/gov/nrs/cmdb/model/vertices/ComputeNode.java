package ca.bc.gov.nrs.cmdb.model.vertices;


import com.tinkerpop.blueprints.Vertex;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComputeNode
{
    public static final String CLASS_NAME = "ComputeNode";

    public static final String ATTRIBUTE_ARCHITECTURE = "architecture";
    public static final String ATTRIBUTE_IP_ADDRESS = "ipaddress";
    public static final String ATTRIBUTE_BIOS_DATE = "biosdate";
    public static final String ATTRIBUTE_BIOS_VERSION = "biosversion";
    public static final String ATTRIBUTE_HOST_NAME = "hostname";
    public static final String ATTRIBUTE_FQDN = "fqdn";
    public static final String ATTRIBUTE_MEMORY_GB = "memtotalgb";

    public static final List<String> ATTRIBUTES = Arrays.asList(
            ATTRIBUTE_ARCHITECTURE,
            ATTRIBUTE_BIOS_DATE,
            ATTRIBUTE_BIOS_VERSION,
            ATTRIBUTE_HOST_NAME,
            ATTRIBUTE_FQDN,
            ATTRIBUTE_MEMORY_GB
    );

    public static final String LINK_IS_LOCATED_IN = "Is_Located_In";
    public static final String LINK_HAS_OS = "Has_OS";




    private String id;
    private String fqdn;
    private Region region;
    private String name;
    private InetAddress ipAddress;
    private List<String> labels = new ArrayList<>();
    private OperatingSystem os;


    public ComputeNode() {}

    public ComputeNode(Vertex v)
    {
        this.id = v.getId().toString();

        for (String attr : ATTRIBUTES)
        {
            String val = v.getProperty(attr);
            if (val != null)
            {
                switch (attr)
                {
                    case ATTRIBUTE_FQDN:
                        this.fqdn = val;
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public String getFqdn()
    {
        return fqdn;
    }

    public void setFqdn(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public String getName()
    {
        return (this.name == null) ? "" : this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public InetAddress getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public List<String> getLabels()
    {
        return labels;
    }

    public void setLabels(List<String> labels)
    {
        this.labels = labels;
    }


    public String getOperationSystemFamily()
    {
        if (this.os != null)
        {
            return this.os.getFamily();
        }
        else
        {
            return "";
        }
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Region getRegion()
    {
        return region;
    }

    public void setRegion(Region region)
    {
        this.region = region;
    }

    
    public OperatingSystem getOperatingSystem()
    {
        return this.os;
    }

    public void setOperatingSystem(OperatingSystem os)
    {
        this.os = os;
    }
}
