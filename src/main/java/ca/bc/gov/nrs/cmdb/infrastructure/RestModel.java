package ca.bc.gov.nrs.cmdb.infrastructure;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class RestModel<T>
{
    private HttpStatus statusCode = HttpStatus.OK;
    private Map<String,String> headers = new HashMap<>();

    private T model;

    public HttpStatus getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode)
    {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void addHeader(String headerName, String headerValue)
    {
        this.headers.put(headerName, headerValue);
    }

    public T getModel()
    {
        return model;
    }

    public void setModel(T model)
    {
        this.model = model;
    }
}
