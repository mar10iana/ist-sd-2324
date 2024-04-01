package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService2;

public class ClientMain {

    static final int numServers = 3;
    private static final String debugFlag = "-debug";
    private static boolean debug = false;
    private static int clientId;

    public static void main(String[] args) {

        System.out.println(ClientMain.class.getSimpleName());

        // Parse command line arguments
        if (args.length == 1) {
            try {
                clientId = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Client ID must be an integer");
                System.exit(1);
            }
        }
        else if (args.length == 2) {
            try {
                clientId = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Client ID must be an integer");
                System.exit(1);
            }
            if (args[1].equals(debugFlag)) {
                debug = true;
                System.err.println("Debug mode enabled");
            }
        }

        // Create a new CommandProcessor and start parsing input
        CommandProcessor parser = new CommandProcessor(new ClientService2(ClientMain.numServers, clientId, debug));
        parser.parseInput();
    }
}
