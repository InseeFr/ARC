package fr.insee.arc_essnet.utils.dao;

import java.sql.Connection;

/**
 * Marker
 */
public interface IQueryHandler extends IWrapper<Connection>, IQueryExecutor, AutoCloseable
{
    // Nothing here
}
