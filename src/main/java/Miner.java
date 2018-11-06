import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Miner {
    private Twitter twitter;
    private List<University> universities;
    private HashMap<University, List<Status>> universityTweets;

    public Miner() {
        universities = new ArrayList<>();
        universityTweets = new HashMap<>();
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
    public void searchForUniversities() {
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
}
