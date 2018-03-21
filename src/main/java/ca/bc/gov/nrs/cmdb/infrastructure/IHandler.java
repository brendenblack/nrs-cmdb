package ca.bc.gov.nrs.cmdb.infrastructure;

import ca.bc.gov.nrs.cmdb.infrastructure.RestModel;

public interface IHandler<TRequest, TResponse extends RestModel>
{
    TResponse handle(TRequest message);
}
