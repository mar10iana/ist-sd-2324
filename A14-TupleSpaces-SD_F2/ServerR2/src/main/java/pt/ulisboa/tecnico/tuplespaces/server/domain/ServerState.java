package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {
    private List<Tuple> tuples;

    /* Constructor to initialize the server state */
    public ServerState() {
        this.tuples = new ArrayList<>();
    }

    /* Add a tuple to the server state */
    public synchronized void put(String tupleValue) {
        Tuple tuple = new Tuple(tupleValue);
        tuples.add(tuple);
        // Notify waiting threads that a new tuple is available
        notifyAll();
    }

    /* Helper method to find a tuple matching a given pattern */
    private Tuple getMatchingTuple(String pattern) {
        for (Tuple tuple : tuples) {
            if (tuple.getValue().matches(pattern)) {
                return tuple;
            }
        }
        return null;
    }

    /* Read a tuple matching a pattern */
    public synchronized String read(String pattern) {
        Tuple tuple = getMatchingTuple(pattern);
        // Wait for a matching tuple to be added
        while (tuple == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught on ServerState -> read() -> wait(): " + e.getMessage());
            }
            tuple = getMatchingTuple(pattern);
        }
        return tuple.getValue();
    }

    /* Unlock all tuples locked by a Client */
    public synchronized void takePhase1Release(int clientId) {
        for(Tuple tuple : tuples) {
            if(tuple.getClientId() == clientId) {
                tuple.unlock();
            }
        }
    }

    /* Lock and return all tuples matching a pattern */
    public synchronized ArrayList<String> takePhase1(String pattern, int clientId) {
        ArrayList<String> tupleValues = new ArrayList<>();
        
        for(Tuple tuple : tuples) {

            // Check if the tuple matches the pattern
            if(tuple.getValue().matches(pattern)) {

                // Check if the tuple is locked by another client
                if(tuple.isLocked() && tuple.getClientId() != clientId) {
                    
                    // Unlock tuples locked by the client in this phase
                    for(Tuple t : tuples) {
                        if(t.isLocked() && t.getClientId() == clientId) {
                            t.unlock();
                        }
                    }
                    // Return empty list
                    return new ArrayList<>();
                }
                // Lock the tuple
                tuple.lock(clientId);
                tupleValues.add(tuple.getValue());
            }
        }
        return tupleValues;
    }

    /* Remove a tuple */
    public synchronized String takePhase2(String tupleValue, int clientId) {
        for(Tuple tuple : tuples) {
            if(tuple.getValue().equals(tupleValue) && tuple.getClientId() == clientId) {
                tuples.remove(tuple);
                return tuple.getValue();
            }
        }
        return null;
    }

    /* Get the current state of the server */
    public synchronized ArrayList<String> getTupleSpacesState() {
        ArrayList<String> tupleValues = new ArrayList<>();
        for (Tuple tuple : tuples) {
            tupleValues.add(tuple.getValue());
        }
        return tupleValues;
    }
}
