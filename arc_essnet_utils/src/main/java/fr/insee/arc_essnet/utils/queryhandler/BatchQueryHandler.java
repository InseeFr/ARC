package fr.insee.arc_essnet.utils.queryhandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import fr.insee.arc_essnet.utils.dao.AbstractQueryHandler;
import fr.insee.arc_essnet.utils.dao.IQueryHandler;

public class BatchQueryHandler extends AbstractQueryHandler implements IQueryHandler
{
    private Connection connection;

    public BatchQueryHandler(Connection connection)
    {
        super();
        this.connection = connection;
        this.onClose = HowToClose.DO_NOT_CLOSE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.insee.siera.sqlengine.IQueryExecutor#executeQuery(java.util.function
     * .Function, java.lang.String, java.util.function.BiConsumer)
     */
    @Override
    public <T> List<T> executeQuery(Function<ResultSet, T> onRecord, String query,
            BiConsumer<Throwable, String> onException) throws SQLException
    {
        return executeQuery(onRecord, query, onException, HowToClose.DO_NOT_CLOSE);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.siera.sqlengine.IQueryExecutor#execute(java.lang.String,
     * java.util.function.BiConsumer)
     */
    @Override
    public void executeUpdate(String query, BiConsumer<Throwable, String> onException) throws Exception
    {
        execute(query, onException, HowToClose.DO_NOT_CLOSE);
    }

    @Override
    public <T> T execute(Function<ResultSet, T> onResult, String query, BiConsumer<Throwable, String> onException)
            throws SQLException
    {
        return execute(onResult, query, onException, HowToClose.DO_NOT_CLOSE);
    }

    @Override
    public Connection getWrapped()
    {
        return this.connection;
    }

    @Override
    public void setWrapped(Connection connection)
    {
        this.connection = connection;
    }
}
