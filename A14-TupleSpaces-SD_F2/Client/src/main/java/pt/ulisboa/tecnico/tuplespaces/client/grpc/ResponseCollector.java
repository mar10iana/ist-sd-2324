package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    List<List<String>> takePhase1Responses;
    int emptyListsCouter = 0;

    public ResponseCollector() {
        collectedResponses = new ArrayList<String>();
        takePhase1Responses = new ArrayList<List<String>>();
    }

    synchronized public void addString(String s) {
        collectedResponses.add(s);
        notifyAll();
    }

    synchronized public void addResponseTakePhase1(List<String> response) {
        if(response.isEmpty())
            emptyListsCouter++;
        else
            takePhase1Responses.add(response);

        notifyAll();
    }

    synchronized public List<List<String>> getTakePhase1Responses() {
        return new ArrayList<>(takePhase1Responses);
    }

    synchronized public int getEmptyListsCount() {
        return emptyListsCouter;
    }

    synchronized public String getStrings() {
        String res = new String();
        for (String s : collectedResponses) {
            res = res.concat(s);
        }
        return res;
    }

    synchronized public String getFirstString() {
        return collectedResponses.get(0);
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (collectedResponses.size() < n) 
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Error in [ResponseCollector -> wait()]: " + e.getMessage());
            }
    }

    synchronized public void waitUntilFirstResponse() throws InterruptedException {
        while (collectedResponses.isEmpty()) 
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Error in [ResponseCollector -> wait()]: " + e.getMessage());
            }
    }

    synchronized public void clear() {
        collectedResponses.clear();
        takePhase1Responses.clear();
        emptyListsCouter = 0;
    }
}