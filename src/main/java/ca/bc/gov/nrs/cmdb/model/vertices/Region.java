package ca.bc.gov.nrs.cmdb.model.vertices;

import java.util.Arrays;
import java.util.List;

public class Region
{
    public static final String CLASS_NAME = "Region";

    public static final String NAME_ATTRIBUTE = "name";
    public static final List<String> ATTRIBUTES = Arrays.asList(NAME_ATTRIBUTE);


    private String name = "";

    public Region() {}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
