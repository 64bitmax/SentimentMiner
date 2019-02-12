import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfig {
    private String URI;

    public DBConfig() throws IOException {
        getValues();
    }

    private void getValues() throws IOException {
        Properties prop = new Properties();
        String fileName = "database-config.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        prop.load(inputStream);
        URI = prop.getProperty("uri");
    }

    public String getURI() {
        return URI;
    }

}
