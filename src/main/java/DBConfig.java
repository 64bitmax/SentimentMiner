import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfig {
    private String address;
    private int port;
    private String authDb;
    private String username;
    private String password;

    public DBConfig() throws IOException {
        getValues();
    }

    private void getValues() throws IOException {
        Properties prop = new Properties();
        String fileName = "database-config.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        prop.load(inputStream);
        address = prop.getProperty("address");
        port = Integer.valueOf(prop.getProperty("port"));
        authDb = prop.getProperty("authDb");
        username = prop.getProperty("username");
        password = prop.getProperty("password");
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getAuthDb() {
        return authDb;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
