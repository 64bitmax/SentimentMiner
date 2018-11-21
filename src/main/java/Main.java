public class Main {
    public static void main(String[] args) {
        String command = "";
        do {
            System.out.println("Please validate keys before use: ");
            System.out.println("OAuthConsumerKey: ");
            String OAuthConsumerKey = Utilities.INPUT_SCANNER.nextLine();
            System.out.println("OAuthConsumerSecret: ");
            String OAuthConsumerSecret = Utilities.INPUT_SCANNER.nextLine();
            System.out.println("OAuthAccessToken: ");
            String OAuthAccessToken = Utilities.INPUT_SCANNER.nextLine();
            System.out.println("OAuthAccessTokenSecret: ");
            String OAuthAccessTokenSecret = Utilities.INPUT_SCANNER.nextLine();

            Miner miner = new Miner();
            boolean valid = miner.authenticateApplication(OAuthConsumerKey, OAuthConsumerSecret, OAuthAccessToken, OAuthAccessTokenSecret);

            if(valid) {
                System.out.println("Credentials verified successfully.\n");
                System.out.print("Enter a command [SEARCH, SAVEDB]: ");
                command = Utilities.INPUT_SCANNER.nextLine();
                if(command.equals("SEARCH")) {
                    miner.searchUniversityTweets("Oxford Brookes", "oxford_brookes", 10000);
                } else if(command.equals("SAVEDB")) {
                    miner.fileToDatabase("ec2-3-8-1-226.eu-west-2.compute.amazonaws.com",27017, "data_miner", "original_tweets");
                }
            } else {
                System.out.println("Failed to verify credentials.");
                System.exit(1);
            }
        } while(!command.equals("STOP"));
    }
}