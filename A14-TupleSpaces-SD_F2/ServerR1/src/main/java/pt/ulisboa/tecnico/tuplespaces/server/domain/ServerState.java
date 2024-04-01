package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {
    private List<String> tuples;

    /* Constructor to initialize the server state */
    public ServerState() {
        this.tuples = new ArrayList<>();
    }

    /* Add a tuple to the server state */
    public synchronized void put(String tuple) {
        tuples.add(tuple);
        // Notify waiting threads that a new tuple is available
        notifyAll();
    }

    /* Helper method to find a tuple matching a given pattern */
    private String getMatchingTuple(String pattern) {
        for (String tuple : tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
            }
        }
        return null;
    }

    /* Read a tuple matching a pattern */
    public synchronized String read(String pattern) {
        String tuple = getMatchingTuple(pattern);
        // Wait for a matching tuple to be added
        while (tuple == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught on ServerState -> read() -> wait(): " + e.getMessage());
            }
            tuple = getMatchingTuple(pattern);
        }
        return tuple;
    }

    /* Remove a tuple matching a pattern */
    public synchronized String take(String pattern) {
        String tuple = getMatchingTuple(pattern);
        // Wait for a matching tuple to be added
        while (tuple == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught on ServerState -> take() -> wait(): " + e.getMessage());
            }
            tuple = getMatchingTuple(pattern);
        }
        tuples.remove(tuple);
        return tuple;
    }

    /* Get the current state of the server */
    public synchronized ArrayList<String> getTupleSpacesState() {
        return new ArrayList<>(tuples);
    }
}
