package by.astontrainee.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Alex Mikhalevich
 */
public class ConnectionUtils {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(JDBCUtils.getValue(JDBCUtils.DRIVER));
        } catch (ClassNotFoundException e) {
            throw new SQLException();
        }
        Connection conn;
        conn = DriverManager.getConnection(JDBCUtils.getValue(JDBCUtils.URL_KEY),
                JDBCUtils.getValue(JDBCUtils.USER_KEY),
                JDBCUtils.getValue(JDBCUtils.PASSWORD_KEY));
        return conn;
    }
}
