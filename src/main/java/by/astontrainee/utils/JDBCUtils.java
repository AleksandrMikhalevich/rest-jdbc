package by.astontrainee.utils;

import java.util.ResourceBundle;

/**
 * @author Alex Mikhalevich
 */
public class JDBCUtils {

    public static final String DATABASE_RESOURCE = "restjdbc";
    public static final String URL_KEY = "url";
    public static final String USER_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String DRIVER = "driver";

    private static final ResourceBundle RESOURCE = ResourceBundle.getBundle(DATABASE_RESOURCE);

    public static String getValue(String key) {
        return RESOURCE.getString(key);
    }
}
