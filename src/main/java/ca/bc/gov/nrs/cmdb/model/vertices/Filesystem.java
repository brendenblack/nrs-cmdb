package ca.bc.gov.nrs.cmdb.model.vertices;

public class Filesystem
{
    private String id;
    private String name;
    private String type;
    private long size;
    private long used;
    private long available;
    private String mountedOn;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getUsed()
    {
        return used;
    }

    public int getUsagePercent()
    {
        double usage = this.used / (this.used + this.available) * 100;
        return (int)Math.ceil(usage);
    }

    public void setUsed(long used)
    {
        this.used = used;
    }

    public long getAvailable()
    {
        return available;
    }

    public void setAvailable(long available)
    {
        this.available = available;
    }

    public String getMountedOn()
    {
        return mountedOn;
    }

    public void setMountedOn(String mountedOn)
    {
        this.mountedOn = mountedOn;
    }

}
