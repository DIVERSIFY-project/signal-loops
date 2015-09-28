package fr.inria.diverse.signalloops.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by marodrig on 03/09/2015.
 */
public interface PersistentObjectLoader {
    List<PersistentObject> buildFromResultSet(ResultSet set) throws SQLException;
}
