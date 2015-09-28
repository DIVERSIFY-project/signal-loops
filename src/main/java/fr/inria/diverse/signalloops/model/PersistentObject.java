package fr.inria.diverse.signalloops.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by marodrig on 02/09/2015.
 */
public interface PersistentObject {

    /**
     * Return a query to insert the object in the DB
     * @return
     */
    String getInsertQuery();

    /**
     * Return a query to retrieve the object in the DB
     * @return
     */
    String getRetrieveQuery();

    /**
     * Import the object from a result set of a SELECT query
     * @param set Set to return
     * @throws java.sql.SQLException
     */
    void fromResultSet(ResultSet set) throws SQLException;

}
