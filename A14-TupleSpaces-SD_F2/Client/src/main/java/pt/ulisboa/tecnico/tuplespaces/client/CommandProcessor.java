package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService2;

import java.lang.InterruptedException;

import java.util.Scanner;

public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";

    private final ClientService2 clientService;

    /* Constructor to initialize the CommandProcessor with a ClientService */
    public CommandProcessor(ClientService2 clientService) {
        this.clientService = clientService;
    }

    /* Parse the input from the user */
    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        // Loop to process commands until 'exit' command is issued
        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);

             switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState(split);
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case SET_DELAY:
                    this.setdelay(split);
                    break;

                case EXIT:
                    // Set flag to terminate command processing loop
                    exit = true;
                    // Close scanner to release resources
                    scanner.close();
                    // Shutdown client service to close any open resources
                    clientService.serviceShutdown();
                    break;

                default:
                    this.printUsage();
                    break;
             }
        }
    }

    /* Process the put command */
    private void put(String[] split){

        // Validate command format
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        
        // Extract tuple from command arguments
        String tuple = split[1];

        // Invoke 'put' operation on client service with the tuple
        clientService.put(tuple);
    }

    /* Process the read command */
    private void read(String[] split){
        
        // Validate command format
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        
        // Extract tuple from command arguments
        String tuple = split[1];

        // Invoke 'read' operation on client service with the tuple
        clientService.read(tuple);
    }

    /* Process the take command */
    private void take(String[] split){

        // Validate command format
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        
        // Extract tuple from command arguments
        String tuple = split[1];

        // Invoke 'take' operation on client service with the tuple
        clientService.take(tuple);
    }

    /* Process the getTupleSpacesState command */
    private void getTupleSpacesState(String[] split){

        // Validate command format
        if (split.length != 2){
            this.printUsage();
            return;
        }
        String qualifier = split[1];

        // Invoke 'getTupleSpacesState' operation on client service with the qualifier
        clientService.getTupleSpacesState(qualifier);

    }

    /* Process the sleep command */
    private void sleep(String[] split) {
        
        // Validate command format
        if (split.length != 2){
            this.printUsage();
            return;
        }
        
        Integer time;
        
        // Attempt to parse sleep duration from command arguments
        try {
            time = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }

        // Pause execution for specified duration
        try {
            Thread.sleep(time*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setdelay(String[] split) {

        // Validate command format
        if (split.length != 3){
            this.printUsage();
            return;
        }

        int qualifier = indexOfServerQualifier(split[1]);
        if (qualifier == -1)
            System.out.println("Invalid server qualifier");

        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }
        // register delay <time> for when calling server <qualifier>
        clientService.setDelay(qualifier, time);
    }

    /* Print the usage of the command line interface */
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- setdelay <server> <integer>\n" +
                "- exit\n");
    }

    private int indexOfServerQualifier(String qualifier) {
        switch (qualifier) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            default:
                return -1;
        }
    }

    /* Validate the input format */
    private boolean inputIsValid(String[] input){
        if (input.length < 2 
            ||
            !input[1].substring(0,1).equals(BGN_TUPLE) 
            || 
            !input[1].endsWith(END_TUPLE)
            || 
            input.length > 2
            ) {
            this.printUsage();
            return false;
        }
        else {
            return true;
        }
    }
}
