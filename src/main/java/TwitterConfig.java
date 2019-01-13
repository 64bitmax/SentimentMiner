import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TwitterConfig {
    private String authConsumerKey;
    private String authConsumerSecret;
    private String authAccessToken;
    private String authAccessTokenSecret;

    public TwitterConfig() throws IOException {
        loadProperties();
    }

    private void loadProperties() throws IOException {
        Properties prop = new Properties();
        String fileName = "twitter-config.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        prop.load(inputStream);
        authConsumerKey = prop.getProperty("authConsumerKey");
        authConsumerSecret = prop.getProperty("authConsumerSecret");
        authAccessToken = prop.getProperty("authAccessToken");
        authAccessTokenSecret = prop.getProperty("authAccessTokenSecret");
    }

    public String getAuthConsumerKey() {
        return authConsumerKey;
    }

    public String getAuthConsumerSecret() {
        return authConsumerSecret;
    }

    public String getAuthAccessToken() {
        return authAccessToken;
    }

    public String getAuthAccessTokenSecret() {
        return authAccessTokenSecret;
    }

}
