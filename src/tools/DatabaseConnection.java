package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import constants.ServerConstants;
import io.Config;
import net.server.Server;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster (some modifications to this beautiful code)
 */
public class DatabaseConnection {
        
    private static ThreadLocal<Connection> con = new ThreadLocalConnection();

    public static Connection getConnection() {
        Connection c = con.get();
        try {
            c.getMetaData();
        } catch (SQLException e) { // connection is dead, therefore discard old object 5ever
            con.remove();
            c = con.get();
        }
        return c;
    }

    private static class ThreadLocalConnection extends ThreadLocal<Connection> {

        @Override
        protected Connection initialValue() {
            try {
                Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
            } catch (ClassNotFoundException e) {
                System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
                e.printStackTrace();
                return null;
            }
            try {
                Config config = Server.getInstance().getConfig();
                return DriverManager.getConnection(config.getString("DatabaseURL"), config.getString("DatabaseUsername"), config.getString("DatabasePassword"));
            } catch (SQLException e) {
                System.out.println("[SEVERE] Unable to make database connection.");
                e.printStackTrace();
                return null;
            }
        }
    }
}
