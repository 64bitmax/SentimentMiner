public class Main {
    public static void main(String[] args) {
        Miner miner = new Miner();
        System.out.print("Enter a command [SEARCH, ...]: ");
        String command = Utilities.INPUT_SCANNER.nextLine();
        if(command.equals("SEARCH")) {
            // miner.searchForUniversities();
        }
    }
}