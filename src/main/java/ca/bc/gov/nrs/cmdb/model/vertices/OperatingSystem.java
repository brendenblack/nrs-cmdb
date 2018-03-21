package ca.bc.gov.nrs.cmdb.model.vertices;

import java.util.Arrays;
import java.util.List;

public class OperatingSystem
{
    public static final String CLASS_NAME = "OperatingSystem";

    public static final String ATTR_NAME = "name";
    public static final String ATTR_PRETTY_NAME = "prettyName";
    public static final String ATTR_VARIANT_ID = "variant";
    public static final String ATTR_VERSION = "version";
    public static final String ATTR_VERSION_NAME = "versionName";

    public static final List<String> ATTRIBUTES = Arrays.asList(
            ATTR_NAME,
            ATTR_PRETTY_NAME,
            ATTR_VARIANT_ID,
            ATTR_VERSION,
            ATTR_VERSION_NAME);

    public static final String LINK_IS_MEMBER_OF = "Is_Member_Of";


    private String id;
    private String name;
    private String family;
    private String prettyName;
    private String variantId;
    private String version;
    private String versionName;




    public OperatingSystem()
    {

    }


    public String getVersionName()
    {
        return versionName;
    }

    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getVariantId()
    {
        return variantId;
    }

    public void setVariantId(String variantId)
    {
        this.variantId = variantId;
    }

    public String getPrettyName()
    {
        return prettyName;
    }

    public void setPrettyName(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFamily()
    {
        return family;
    }

    public void setFamily(String family)
    {
        this.family = family;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
