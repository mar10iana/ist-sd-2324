package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.*;

import pt.ulisboa.tecnico.nameserver.contract.NameServerServiceGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerServiceGrpc.NameServerServiceBlockingStub;

import pt.ulisboa.tecnico.nameserver.contract.NameServer.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class ClientService2 {

    OrderedDelayer delayer;

    private static boolean debug = false;
    private static int numServers;
    private static int clientId;
    private String nameServerTarget = "localhost:5001";
    private static String serviceName = "TupleSpaces";
    private static String qualifierA = "A";
    private static String qualifierB = "B";
    private static String qualifierC = "C";

    // NameServer
    private ManagedChannel nameServerChannel;
    private static NameServerServiceGrpc.NameServerServiceBlockingStub nameServerStub;
    
    // TupleSpaces
    private ArrayList<String> serverTargets = new ArrayList<String>();
    private ManagedChannel[] channels;
    private TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[] stubs;

    // Response collector
    private ResponseCollector c = new ResponseCollector();

    public ClientService2(int numServers, int clientId, boolean debug) {

        this.debug = debug;
        this.numServers = numServers;
        this.clientId = clientId;

        channels = new ManagedChannel[numServers];
        stubs = new TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[numServers];

        // Initialize channel and stub for NameServer
        initNamingServerChannelAndStub();
        
        // Lookup TupleSpaces service address
        lookupService();

        // Initialize channels and stubs for TupleSpaces
        initServiceChannelsAndStubs();
    
        /* The delayer can be used to inject delays to the sending of requests to the 
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
    }

    /* Initialize gRPC channel and stub for NameServer */
    private void initNamingServerChannelAndStub() {
        nameServerChannel = ManagedChannelBuilder.forTarget(nameServerTarget).usePlaintext().build();
        nameServerStub = NameServerServiceGrpc.newBlockingStub(nameServerChannel);
    }

    /* Lookup the address of the TupleSpaces service using NameServer */
    private void lookupService() {

        try {
            // Create the request and response for the lookup
            LookupRequest requestA = LookupRequest.newBuilder().setName(serviceName).setQualifier(qualifierA).build();
            LookupResponse responseA = nameServerStub.lookup(requestA);

            if (responseA.getServersCount() == 0) {
                System.out.println("No servers found for service " + serviceName + " with qualifier " + qualifierA);
                System.exit(1);
            }
            serverTargets.add(responseA.getServersList().get(0));

            LookupRequest requestB = LookupRequest.newBuilder().setName(serviceName).setQualifier(qualifierB).build();
            LookupResponse responseB = nameServerStub.lookup(requestB);

            if (responseB.getServersCount() == 0) {
                System.out.println("No servers found for service " + serviceName + " with qualifier " + qualifierB);
                System.exit(1);
            }
            serverTargets.add(responseB.getServersList().get(0));

            LookupRequest requestC = LookupRequest.newBuilder().setName(serviceName).setQualifier(qualifierC).build();
            LookupResponse responseC = nameServerStub.lookup(requestC);

            if (responseC.getServersCount() == 0) {
                System.out.println("No servers found for service " + serviceName + " with qualifier " + qualifierC);
                System.exit(1);
            }
            serverTargets.add(responseC.getServersList().get(0));

        } catch (StatusRuntimeException e) {
            if (debug)
                System.err.println("Exception in lookupService method");

            System.out.println(e.getStatus().getDescription());

        }

        if(debug)
            System.err.println("NameServer lookup was successful");
    }

    private void initServiceChannelsAndStubs() {
        for (int i = 0; i < serverTargets.size(); i++) {
            channels[i] = ManagedChannelBuilder.forTarget(serverTargets.get(i)).usePlaintext().build();
            stubs[i] = TupleSpacesReplicaGrpc.newStub(channels[i]);
        }
    }

    /* This method allows the command processor to set the request delay assigned to a given server */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

    /* Put a tuple into the TupleSpaces */
    public void put(String tuple) {

        // Create the request
        PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();

        for (Integer id : delayer) {
            // Attempt to send the request
            try {
                stubs[id].put(request, new PutObserver(c));
            } catch (StatusRuntimeException e) {
                if(debug)
                    System.err.println("Exception in put method for tuple: " + tuple);
                System.out.println(e.getStatus().getDescription());
            }
        }

        System.out.println("OK");
        
        try {
            if(debug)
                System.err.println("Waiting for all responses...");

            c.waitUntilAllReceived(numServers);
        } catch (InterruptedException e) {
            System.out.println("Error waiting for all responses");
        }

        if(debug)
            System.err.println("Tuple: " + tuple + " added successfully");

        c.clear();
    }

    /* Read a tuple from the TupleSpaces */
    public void read(String tuple) {

        // Create the request
        ReadRequest request = ReadRequest.newBuilder().setSearchPattern(tuple).build();

        for (Integer id : delayer) {
            // Attempt to send the request
            try {
                stubs[id].read(request, new ReadObserver(c));
            } catch (StatusRuntimeException e) {
                if(debug)
                    System.err.println("Exception in read method for tuple: " + tuple);
                System.out.println(e.getStatus().getDescription());
            }
        }

        try {
            if(debug)
                System.err.println("Waiting for first response...");

            c.waitUntilFirstResponse();
        } catch (InterruptedException e) {
            System.out.println("Error waiting for first response");
        }

        if(debug)
            System.err.println("Tuple: " + tuple + " read successfully");
        
        System.out.println("OK");
        System.out.println(c.getStrings());
        c.clear();

    }

    public void takePhase1Release() {

        TakePhase1ReleaseRequest request = TakePhase1ReleaseRequest.newBuilder().setClientId(clientId).build();

        if(debug)
            System.err.println("Releasing lock...");

        try {
            for (Integer id : delayer) {
                stubs[id].takePhase1Release(request, new TakePhase1ReleaseObserver(c));
                if(debug)
                    System.err.println("Lock released successfully");
            }
        } catch (StatusRuntimeException e) {
            if(debug)
                System.err.println("Exception in releasePhase1 method");
            System.out.println(e.getStatus().getDescription());
        }
    }

    public String takePhase1(String tuple) {
        boolean retry;

        do {
            retry = false;
            int emptyLists;

            if(debug)
                System.err.println("Starting take phase 1...");

            TakePhase1Request request = TakePhase1Request.newBuilder().setSearchPattern(tuple).setClientId(clientId).build();
            
            if(debug)
                System.err.println("Starting take phase 1...");

            try {
                for (Integer id : delayer) {
                    stubs[id].takePhase1(request, new TakePhase1Observer(c));
                }
            } catch (StatusRuntimeException e) {
                if(debug)
                    System.err.println("Exception in takePhase1 method for tuple: " + tuple);
                System.out.println(e.getStatus().getDescription());
            }

            try {
                if(debug)
                    System.err.println("Waiting for all responses...");

                c.waitUntilAllReceived(numServers);
            } catch (InterruptedException e) {
                System.out.println("Error waiting for all responses");
            }

            emptyLists = c.getEmptyListsCount();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error waiting after empty list count");
            }
            if(debug)
                System.err.println("Empty lists: " + emptyLists);

            if (emptyLists == 0) {

                Set<String> commonTuples = new HashSet<>(c.getTakePhase1Responses().get(0));

                for (List<String> serverTuples : c.getTakePhase1Responses()) {
                    commonTuples.retainAll(serverTuples);
                }
    
                if (!commonTuples.isEmpty()) {
                    if(debug)
                        System.err.println("Common tuple: " + commonTuples.iterator().next());
    
                    c.clear();
                    return commonTuples.iterator().next();
    
                } else {
                    if(debug)
                        System.err.println("No common tuple");
    
                    if(debug)
                        System.err.println("Retrying take phase 1...");
    
                    c.clear();
                    commonTuples.clear();
                    retry = true;
                }
            }
            else if(emptyLists <= numServers/2) {
                if(debug)
                    System.err.println("Retrying take phase 1...");
    
                c.clear();
                retry = true;
            }
            else if(emptyLists > numServers/2) {
                if(debug)
                    System.err.println("Unlocking tuples...");
    
                c.clear();
                takePhase1Release();
    
                if(debug)
                    System.err.println("Retrying take phase 1...");
    
                retry = true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error waiting before retrying");
            }
        } while (retry);
        
        c.clear();
        return null;
    }

    public void takePhase2(String tuple) {
        // Create the request
        TakePhase2Request request = TakePhase2Request.newBuilder().setClientId(clientId).setTuple(tuple).build();

        if(debug)
            System.err.println("Starting take phase 2...");

        for (Integer id : delayer) {
            // Attempt to send the request
            try {
                stubs[id].takePhase2(request, new TakePhase2Observer(c));
            } catch (StatusRuntimeException e) {
                if(debug)
                    System.err.println("Exception in takePhase2 method");
                System.out.println(e.getStatus().getDescription());
            }
        }

        try {
            if(debug)
                System.err.println("Waiting for all responses...");

            c.waitUntilAllReceived(numServers);
        } catch (InterruptedException e) {
            System.err.println("Error waiting for all responses");
        }

        if(debug)
            System.err.println("Take Phase 2 was successful");

        System.out.println("OK");
        System.out.println(c.getFirstString());

        c.clear();
    }

    /* Take a tuple from the TupleSpaces */
    public void take(String tuple) {
        String tupleToTake;

        tupleToTake = takePhase1(tuple);

        if(tupleToTake == null) {
            System.out.println("Error in take phase 1");
            return;
        }

        takePhase2(tupleToTake);
        takePhase1Release();
    }

    /* Get the state of the TupleSpaces */
    public void getTupleSpacesState(String qualifier) {
        // Create the request
        getTupleSpacesStateRequest request = getTupleSpacesStateRequest.newBuilder().build();

        // Attempt to send the request
        try {
            switch (qualifier) {
                case "A":
                    stubs[0].getTupleSpacesState(request, new getTupleSpacesStateObserver(c));
                    break;
                case "B":
                    stubs[1].getTupleSpacesState(request, new getTupleSpacesStateObserver(c));
                    break;
                case "C":
                    stubs[2].getTupleSpacesState(request, new getTupleSpacesStateObserver(c));
                    break;
                default:
                    System.out.println("Invalid qualifier");
                    return;
            }
            System.out.println("OK");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error waiting before retrying");
            }
            System.out.println(c.getStrings());
        } catch (StatusRuntimeException e) {
            if(debug)
                System.err.println("Exception in getTupleSpacesState method");
            System.out.println(e.getStatus().getDescription());
        }

        if(debug)
            System.err.println("Get Tuple Spaces State " + qualifier + " was successful");
    
        c.clear();
    }

    /* Close gRPC channels */
    public void serviceShutdown() {
        nameServerChannel.shutdownNow();
        for (int i = 0; i < serverTargets.size(); i++) {
            channels[i].shutdownNow();
        }
    }
        
}