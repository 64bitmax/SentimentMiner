import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

class Miner{
    private Twitter twitter;
    private TwitterStream twitterStream;
    private List<University> universities;
    private HashMap<University, List<Status>> universityTweets;
    private File overallTeachingFile;
    private File opportunitiesFile;
    private File assessmentFeedbackFile;
    private File academicSupportFile;
    private File organisationManagementFile;
    private File learningResourcesFile;
    private File learningCommunityFile;
    private File studentVoiceFile;
    private File overallFile;


    private DBConnector dbConnector;

    Miner() throws IOException {
        dbConnector = new DBConnector();
        universities = new ArrayList<>();
        universityTweets = new HashMap<>();
        twitterStream = new TwitterStreamFactory().getInstance();
        overallTeachingFile = new File("/Users/Max/Desktop/overallTeachingTweets.txt");
        opportunitiesFile = new File("/Users/Max/Desktop/opportunitiesTweets.txt");
        assessmentFeedbackFile = new File("/Users/Max/Desktop/assessmentFeedbackTweets.txt");
        academicSupportFile = new File("/Users/Max/Desktop/academicSupportTweets.txt");
        organisationManagementFile = new File("/Users/Max/Desktop/organisationManagementTweets.txt");
        learningResourcesFile = new File("/Users/Max/Desktop/learningResourcesTweets.txt");
        learningCommunityFile = new File("/Users/Max/Desktop/learningCommunityTweets.txt");
        studentVoiceFile = new File("/Users/Max/Desktop/studentVoiceTweets.txt");
        overallFile = new File("/Users/Max/Desktop/overallTweets.txt");
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

        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setTweetModeExtended(true);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

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

    void streamTweets(String universityName, String universityHandle, String databaseName) {
        StatusListener listener = new StatusListener(){
            public void onStatus(Status tweet) {
                // Do database stuff
                if(!tweet.getUser().getScreenName().equals(universityName) && !tweet.getUser().getName().equals(universityName) ||
                        !tweet.getUser().getName().equals(universityHandle) && !tweet.getUser().getScreenName().equals(universityHandle)) {

                    if(tweet.getText().toLowerCase().contains("teaching") || tweet.getText().toLowerCase().contains("teachers") ||
                            tweet.getText().toLowerCase().contains("lecturers") || tweet.getText().toLowerCase().contains("staff")) {
                        sendDirectToDatabase(databaseName, "overall_teaching", tweet.getText());
                    }

                    if(tweet.getText().toLowerCase().contains("opportunities") || tweet.getText().toLowerCase().contains("prospects") ||
                            tweet.getText().toLowerCase().contains("opportunity")) {
                        sendDirectToDatabase(databaseName, "learning_opportunities", tweet.getText());
                    }

                    if(tweet.getText().toLowerCase().contains("assessment") || tweet.getText().toLowerCase().contains("test") ||
                            tweet.getText().toLowerCase().contains("exam") || tweet.getText().toLowerCase().contains("feedback") ||
                            tweet.getText().toLowerCase().contains("marks") || tweet.getText().toLowerCase().contains("mark")) {
                        sendDirectToDatabase(databaseName, "assessment_and_feedback", tweet.getText());
                    }

                    if(tweet.getText().contains("support") || tweet.getText().contains("advice") ||
                            tweet.getText().contains("guidance")) {
                        sendDirectToDatabase(databaseName, "academic_support", tweet.getText());
                    }

                    if(tweet.getText().contains("organised") || tweet.getText().contains("management") ||
                            tweet.getText().contains("timetable")) {
                        sendDirectToDatabase(databaseName, "organisation_and_management", tweet.getText());
                    }

                    if(tweet.getText().contains("facilities") || tweet.getText().contains("resources") ||
                            tweet.getText().contains("equipment") || tweet.getText().contains("software") ||
                            tweet.getText().contains("learning")) {
                        sendDirectToDatabase(databaseName, "learning_resources", tweet.getText());
                    }

                    if(tweet.getText().contains("community")  || tweet.getText().contains("others") ||
                            tweet.getText().contains("communities") || tweet.getText().contains("groups") ||
                            tweet.getText().contains("group") ||  tweet.getText().contains("collaboration")) {
                        sendDirectToDatabase(databaseName, "learning_community", tweet.getText());
                    }

                    if(tweet.getText().contains("voice")  || tweet.getText().contains("student") ||
                            tweet.getText().contains("union") ||  tweet.getText().contains("values")) {
                        sendDirectToDatabase(databaseName, "student_voice", tweet.getText());
                    }

                    sendDirectToDatabase(databaseName, "overall", tweet.getText());
                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            @Override
            public void onScrubGeo(long l, long l1) {

            }
            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(listener);

        String[] keywords = {"oxford brookes"};
        twitterStream.filter(new FilterQuery().track(keywords));
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
                            !tweet.getUser().getName().equals(universityHandle) && !tweet.getUser().getScreenName().equals(universityHandle)
                            && !tweet.getText().equals("")) {
                        if(tweet.isRetweet()) {
                            if(!validTweets.contains(tweet.getRetweetedStatus().getText().replaceAll("\n", "."))) {
                                String tweetText = tweet.getRetweetedStatus().getText().replaceAll("\n", ".");
                                validTweets.add(tweetText);
                                writeToFiles(tweetText);
                                tweets.add(tweet);
                            }
                        } else {
                            if(!validTweets.contains(tweet.getText().replaceAll("\n", "."))) {
                                String tweetText = tweet.getText().replaceAll("\n", ".");
                                validTweets.add(tweetText);
                                writeToFiles(tweetText);
                                tweets.add(tweet);
                            }
                        }
                    }
                }
                for (Status tweet: tweets)
                    if(tweet.getId() < endID) endID = tweet.getId();
                query.setMaxId(endID - 1);
            }
            universityTweets.put(new University(universityName), tweets);
        } catch (TwitterException e) {
            e.printStackTrace();
            System.out.println("Twitter search failed for " + universityName + ": " + e.getMessage());
            System.exit(-1);
        }
    }

    void writeToFiles(String tweet) {
        try {
            // Select tweets into categories of NSS
            if(tweet.toLowerCase().contains("teaching") || tweet.toLowerCase().contains("teachers") ||
                    tweet.toLowerCase().contains("lecturers")) {
                // Send to 'Overall_Teaching'
                BufferedWriter writer = new BufferedWriter(new FileWriter(overallTeachingFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.toLowerCase().contains("opportunities") || tweet.toLowerCase().contains("prospects") ||
                    tweet.toLowerCase().contains("opportunity")) {
                // Send to 'Opportunities'
                BufferedWriter writer = new BufferedWriter(new FileWriter(opportunitiesFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.toLowerCase().contains("assessment") || tweet.toLowerCase().contains("test") ||
                    tweet.toLowerCase().contains("exam") || tweet.toLowerCase().contains("feedback") ||
                    tweet.toLowerCase().contains("marks") || tweet.toLowerCase().contains("mark")) {
                // Send to 'Assessment_And_Feedback'
                BufferedWriter writer = new BufferedWriter(new FileWriter(assessmentFeedbackFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.contains("support") || tweet.contains("advice") ||
                    tweet.contains("guidance")) {
                // Send to 'Academic_Support'
                BufferedWriter writer = new BufferedWriter(new FileWriter(academicSupportFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.contains("organised") || tweet.contains("management") ||
                    tweet.contains("timetable")) {
                // Send to 'Organisation_And_Management'
                BufferedWriter writer = new BufferedWriter(new FileWriter(organisationManagementFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.contains("facilities") || tweet.contains("resources") ||
                    tweet.contains("equipment") || tweet.contains("software") ||
                    tweet.contains("learning")) {
                // Send to 'Learning_Resources'
                BufferedWriter writer = new BufferedWriter(new FileWriter(learningResourcesFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.contains("community")  || tweet.contains("others") ||
                    tweet.contains("communities") || tweet.contains("groups") ||
                    tweet.contains("group") ||  tweet.contains("collaboration")) {
                // Send to 'Learning_Community'
                BufferedWriter writer = new BufferedWriter(new FileWriter(learningCommunityFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            if(tweet.contains("voice")  || tweet.contains("student") ||
                    tweet.contains("union") ||  tweet.contains("values")) {
                // Send to 'Student_Voice'
                BufferedWriter writer = new BufferedWriter(new FileWriter(studentVoiceFile, true));
                writer.write(tweet);
                writer.newLine();
                writer.close();
            }

            // Send to 'Overall'
            BufferedWriter writer = new BufferedWriter(new FileWriter(overallFile, true));
            writer.write(tweet);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendDirectToDatabase(String databaseName, String tableName, String tweet) {
        MongoDatabase database = dbConnector.getDatabase(databaseName);
        dbConnector.createTable(database, tableName);
        dbConnector.addDocument(database, tableName, "tweet", tweet);
    }

    void fileToDatabase(String databaseName) {
        try {
            MongoDatabase database = dbConnector.getDatabase(databaseName);

            // Check all the tables exist, if not, create them
            dbConnector.createTable(database, "overall_teaching");
            dbConnector.createTable(database, "learning_opportunities");
            dbConnector.createTable(database, "assessment_and_feedback");
            dbConnector.createTable(database, "academic_support");
            dbConnector.createTable(database, "organisation_and_management");
            dbConnector.createTable(database, "learning_resources");
            dbConnector.createTable(database, "learning_community");
            dbConnector.createTable(database, "student_voice");
            dbConnector.createTable(database, "overall");

            BufferedReader reader = new BufferedReader(new FileReader(overallTeachingFile));
            String tweet;
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "overall_teaching", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(opportunitiesFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "learning_opportunities", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(assessmentFeedbackFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "assessment_and_feedback", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(academicSupportFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "academic_support", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(organisationManagementFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "organisation_and_management", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(learningResourcesFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "learning_resources", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(learningCommunityFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "learning_community", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(studentVoiceFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "student_voice", "tweet", tweet);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(overallFile));
            while((tweet = reader.readLine()) != null) {
                dbConnector.addDocument(database, "overall", "tweet", tweet);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MongoCommandException e) {
            System.out.println("ERROR: Try deleting the table in MongoDB before sending the file contents to the database.");
        }
    }

    void sendUniversitiesToDatabase(String databaseName, String tableName) {
        loadUniversityNames();
        MongoDatabase database = dbConnector.getDatabase(databaseName);
        dbConnector.createTable(database, tableName);

        for (University uni : universities) {
            dbConnector.addDocument(database, tableName, "university", uni.getName());
        }
    }

}
