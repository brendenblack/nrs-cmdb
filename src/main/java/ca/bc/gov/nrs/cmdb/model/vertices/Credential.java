package ca.bc.gov.nrs.cmdb.model.vertices;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

public class Credential extends OrientVertex
{
    public static final String CLASS_NAME = "Credential";

    public static final String ATTR_DESCRIPTION = "Description";
    public static final String ATTR_USERNAME = "Username";
    public static final String ATTR_PASSWORD = "Password";

    public static final String OUT_IS_CREDENTIAL_FOR = "Is_Credential_For";

    private String username;

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
