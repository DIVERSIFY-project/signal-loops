package fr.inria.diverse.signalloops.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by marodrig on 02/09/2015.
 */
public class ProjectDescription implements PersistentObject {

    private int id;
    private String name;
    private String version;
    private String svnURL;
    private String description;

    private static String INSERT_QUERY =
            "INSERT OR REPLACE INTO Projects VALUES (%d,'%s','%s','%s','%s');";

    public String getInsertQuery() {
        return String.format(Locale.ENGLISH, INSERT_QUERY, id, name, version, svnURL, description);
    }

    @Override
    public String getRetrieveQuery() {
        return null;
    }

    @Override
    public void fromResultSet(ResultSet set) throws SQLException {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSvnURL() {
        return svnURL;
    }

    public void setSvnURL(String svnURL) {
        this.svnURL = svnURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
