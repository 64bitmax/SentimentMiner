public class Main {
    public static void main(String[] args) {
        Miner miner = new Miner();

        String command;
        do {
            System.out.print("Enter a command [SEARCH, SAVEDB]: ");
            command = Utilities.INPUT_SCANNER.nextLine();
            if(command.equals("SEARCH")) {
                miner.searchUniversityTweets("Oxford Brookes", "oxford_brookes", 10000);
            } else if(command.equals("SAVEDB")) {
                miner.fileToDatabase("ec2-3-8-1-226.eu-west-2.compute.amazonaws.com",27017, "data_miner", "original_tweets");
            }
        } while(!command.equals("STOP"));
    }
}