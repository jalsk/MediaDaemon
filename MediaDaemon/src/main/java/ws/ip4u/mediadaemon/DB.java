package ws.ip4u.mediadaemon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author jalsk
 */
public class DB
{
	private Connection conn;
//	private Log log = LogFactory.getLog(DB.class);
	private static final String DB_TBL_EPISODES = "episodes";
	private static final String DB_TBL_SEASON = "season";
	private static final String DB_TBL_SERIES = "series";

	private DB()
	{
	}
	
	public DB(String dbPath) throws SQLException
	{
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		createTables();
	}

	private void createTables() throws SQLException
	{
		Statement stmt = null;

		try
		{
			stmt = conn.createStatement();

			stmt.executeUpdate("create table " + DB_TBL_SERIES + " (id, name, path, noRename);");
			stmt.executeUpdate("create table " + DB_TBL_SEASON + " (id, seriesid, number, path);");
			stmt.executeUpdate("create table " + DB_TBL_EPISODES + " (id, seasonid, name, number, season, path, filename);");
		}
		finally
		{
			if(stmt != null)
			{
				stmt.close();
			}
		}
	}
}
