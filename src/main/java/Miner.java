import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

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
        loadUniversityNames();
    }

    /**
     * Authenticates the Twitter application using Twitter developer authentication keys
     */
    boolean authenticateApplication(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setTweetModeExtended(true);
        TwitterFactory twitterFactory = new TwitterFactory(cb.build());
        twitter = twitterFactory.getInstance();

        try {
            User user = twitter.verifyCredentials();
            return true;
        } catch (Exception e) {
            return false;
        }
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
    void searchUniversityTweets(String universityName, String universityHandle, int numberTweets) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            ArrayList<Status> tweets = new ArrayList<>();
            ArrayList<String> validTweets = new ArrayList<>();

            Long endID = Long.MAX_VALUE;
            Query query = new Query(universityName);
            query.setLang("en");

            while(tweets.size() < numberTweets) {
                if (numberTweets - tweets.size() > 100) {
                    query.setCount(100);
                }
                else {
                    query.setCount(numberTweets - tweets.size());
                }
                QueryResult result = twitter.search(query);
                for(Status tweet : result.getTweets()) {
                    if(!tweet.getUser().getScreenName().equals(universityName) && !tweet.getUser().getName().equals(universityName) ||
                            !tweet.getUser().getName().equals(universityHandle) && !tweet.getUser().getScreenName().equals(universityHandle)) {
                        if(tweet.isRetweet()) {
                            if(!validTweets.contains(tweet.getRetweetedStatus().getText())) {
                                System.out.println("Tweet information: " + tweet.getRetweetedStatus().getText());
                                validTweets.add(tweet.getRetweetedStatus().getText());
                                writer.write(tweet.getRetweetedStatus().getText());
                                tweets.add(tweet);
                            }
                        } else {
                            if(!validTweets.contains(tweet.getText())) {
                                System.out.println("Tweet information: " + tweet.getText());
                                validTweets.add(tweet.getText());
                                writer.write(tweet.getText());
                                tweets.add(tweet);
                            }
                        }
                        writer.newLine();
                    }
                }
                for (Status tweet: tweets)
                    if(tweet.getId() < endID) endID = tweet.getId();
                query.setMaxId(endID - 1);
            }
            universityTweets.put(new University(universityName), tweets);
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
        } catch (MongoCommandException e) {
            System.out.println("ERROR: Try deleting the table in MongoDB before sending the file contents to the database.");
        }
    }
}
