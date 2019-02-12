import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String command = "";
        do {
            try {
                TwitterConfig twitterConfig = new TwitterConfig();

                Miner miner = new Miner();
                boolean valid = miner.authenticateApplication(
                        twitterConfig.getAuthConsumerKey(),
                        twitterConfig.getAuthConsumerSecret(),
                        twitterConfig.getAuthAccessToken(),
                        twitterConfig.getAuthAccessTokenSecret());

                if(valid) {
                    System.out.println("Credentials verified successfully.\n");
                    System.out.print("Enter a command [SEARCH, SAVEDB, SAVEUNI, STREAMTWEETS]: ");
                    command = Utilities.INPUT_SCANNER.nextLine();
                    if(command.equals("SEARCH")) {
                        miner.searchUniversityTweets("Oxford Brookes", "oxford_brookes", 10000);
                    } else if(command.equals("SAVEDB")) {
                        miner.fileToDatabase( "data_miner");
                    } else if (command.equals("SAVEUNI")) {
                        miner.sendUniversitiesToDatabase("university_info", "universities_2018");
                    } else if (command.equals("STREAMTWEETS")) {
                        miner.streamTweets("Oxford Brookes", "oxford_brookes", "data_miner");
                        break;
                    }
                } else {
                    System.out.println("Failed to verify credentials.");
                    System.exit(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } while(!command.equals("STOP"));
    }
}