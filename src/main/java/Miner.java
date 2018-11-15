import com.mongodb.client.MongoDatabase;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class Miner {
    private Twitter twitter;
    private List<University> universities;
    private HashMap<University, List<Status>> universityTweets;
    private File file;

    Miner() {
        universities = new ArrayList<>();
        universityTweets = new HashMap<>();
        file = new File("/Users/Max/Desktop/universityTweets.txt");
        initialize();
    }

    private void initialize() {
        authenticateApplication();
        loadUniversityNames();
    }

    /**
     * Authenticates the Twitter application using Twitter developer authentication keys
     */
    private void authenticateApplication() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("aedewbUEnQXzSKt5Te1eWMoMy")
                .setOAuthConsumerSecret("832VQRuOeCiKfcniXGq7jy7BTRDe4RHEkWE4dsQrZW9ABlMfnh")
                .setOAuthAccessToken("1057730086252756997-NUdLXOOehBTkdIPwF9La2iySMbY3OR")
                .setOAuthAccessTokenSecret("SnbOvE7LvYxE5ePK6ytAf6Toi6x8sGkMtPyXCm1cIsI61");
        TwitterFactory twitterFactory = new TwitterFactory(cb.build());
        twitter = twitterFactory.getInstance();
    }

    /**
     * Parses university information from file and loads into the list of universities
     */
    private void loadUniversityNames() {
        try {
            File universityFile = new File(this.getClass().getResource( "/Universities.txt" ).toURI() );
            Scanner scanner = new Scanner(universityFile);
            while(scanner.hasNextLine()) {
                universities.add(new University(scanner.nextLine()));
            }
        } catch (FileNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Queries Twitter for tweets about  UK universities from the list on the National Student Survey
     * NOTE: May be resource consuming
     * RESTRICTION: Run a minimum number of times during development due to quota
     *              - 250 for past 30 days
     *              - 50 for full archive
     */
    void searchForUniversities() {
        for(University university : universities) {
            try {
                Query query = new Query(university.getName());
                QueryResult result;
                do {
                    result = twitter.search(query);
                    universityTweets.put(university, result.getTweets());
                } while ((query = result.nextQuery()) != null);
            } catch (TwitterException e) {
                e.printStackTrace();
                System.out.println("Failure to search for tweets: " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    void searchOxfordBrookes(String universityName, String universityHandle) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            Query query = new Query(universityName);
            query.setCount(100);
            query.setLang("en");
            QueryResult result;
            do {
                result = twitter.search(query);
                for(Status tweet : result.getTweets()) {
                    if(!tweet.getUser().getScreenName().equals(universityName) && !tweet.getUser().getName().equals(universityName) ||
                            !tweet.getUser().getName().equals(universityHandle) && !tweet.getUser().getScreenName().equals(universityHandle)) {
                        System.out.println("Tweet information: " + tweet.getText());
                        writer.write(tweet.getText());
                        writer.newLine();
                    }
                }
                universityTweets.put(new University(universityName), result.getTweets());
            } while ((query = result.nextQuery()) != null);
            writer.close();
        } catch (TwitterException e) {
            e.printStackTrace();
            System.out.println("Twitter search failed for " + universityName + ": " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void fileToDatabase(String address, int port, String databaseName, String tableName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            DBConnector connector = new DBConnector(address, port);
            MongoDatabase database = connector.getDatabase(databaseName);
            connector.createTable(database, tableName);

            String tweet;
            while((tweet = reader.readLine()) != null) {
                connector.addDocument(database, tableName, "tweet", tweet);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
