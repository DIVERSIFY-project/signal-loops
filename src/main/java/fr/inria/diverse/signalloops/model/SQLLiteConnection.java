package fr.inria.diverse.signalloops.model;

import java.sql.*;
import java.util.List;

/**
 * Created by marodrig on 02/09/2015.
 */
public class SQLLiteConnection {


    private final String filePath;

    public SQLLiteConnection(String filePath) {
        this.filePath = filePath;
    }

    public Connection connect() throws ClassNotFoundException, SQLException {
        Connection c = null;
        Class.forName("org.sqlite.JDBC");
        c = DriverManager.getConnection("jdbc:sqlite:" + filePath);
        c.setAutoCommit(false);
        return c;
    }

    /**
     * Retrieve all Objects from a table where some condition is meet
     *
     * @param tableName
     * @param condition
     * @return
     * @throws java.sql.SQLException
     * @throws ClassNotFoundException
     */
    public List<PersistentObject> retrieveWhere(String tableName, String condition,
                                                PersistentObjectLoader loader) throws SQLException, ClassNotFoundException {
        Connection c = connect();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE " + condition);
        List<PersistentObject> objects = loader.buildFromResultSet(rs);
        stmt.close();
        c.close();
        return objects;
    }

    /**
     * Retrieve all Objects from a table
     *
     * @param tableName Name of the table
     * @param loader    Delegate to build objects
     * @return A list of objects
     * @throws java.sql.SQLException
     * @throws ClassNotFoundException
     */
    public List<PersistentObject> retrieveAll(String tableName, PersistentObjectLoader loader) throws SQLException, ClassNotFoundException {
        Connection c = connect();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
        List<PersistentObject> objects = loader.buildFromResultSet(rs);
        stmt.close();
        c.close();
        return objects;
    }

    /**
     * Insert a persistent object
     *
     * @param insertable
     * @throws java.sql.SQLException
     * @throws ClassNotFoundException
     */
    public void insert(PersistentObject insertable) throws SQLException, ClassNotFoundException {
        Connection c = connect();
        Statement stmt = c.createStatement();
        stmt.executeUpdate(insertable.getInsertQuery());
        stmt.close();
        c.commit();
        c.close();
    }

    /**
     * Init the tables of the db
     *
     * @throws java.sql.SQLException
     * @throws ClassNotFoundException
     */
    public void initTables() throws SQLException, ClassNotFoundException {
        String loops = "CREATE TABLE Loops(" +
                "id          INT  PRIMARY KEY NOT NULL," +
                "code        TEXT NOT NULL," +
                "position    TEXT NOT NULL," +
                "project     INT  NOT NULL," +
                "totalStmnt  INT," +
                "fixedStmnt  INT," +
                "timeGain    REAL," +
                "testFails   INT," +
                "testTimeOut INT," +
                "nbTestCover INT," +
                "upFix       INT," +
                "downFix      INT," +
                "originalTime REAL," +
                "minVal       REAL," +
                "maxVal       REAL," +
                "normalizedDistortion REAL," +
                "distortion   REAL," +
                "sizeDiff     INT);";
        Connection c = connect();
        Statement stmt = c.createStatement();
        stmt.execute(loops);
        stmt.close();
        c.commit();
        c.close();
    }
}
