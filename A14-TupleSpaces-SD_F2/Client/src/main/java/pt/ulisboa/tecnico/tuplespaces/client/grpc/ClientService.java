package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*; // Message Classes

import pt.ulisboa.tecnico.nameserver.contract.NameServerServiceGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerServiceGrpc.NameServerServiceBlockingStub;

import pt.ulisboa.tecnico.nameserver.contract.NameServer.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;

public class ClientService {
  
  private static boolean debug = false;
  private String nameServerTarget = "localhost:5001";
  private static String serviceName = "TupleSpaces";
  private static String qualifier = "A";

  private ManagedChannel nameServerChannel;
  private static NameServerServiceGrpc.NameServerServiceBlockingStub nameServerStub;

  TupleSpacesGrpc.TupleSpacesBlockingStub serviceStub;
  ManagedChannel serviceChannel;

  private int delay[] = new int[3];

  /* Constructor to initialize client with debug flag */
  public ClientService(boolean debug) {
    this.debug = debug;

    // Initialize channel and stub for NameServer
    initNamingServerChannelAndStub();
    // Lookup TupleSpaces service address
    lookupService();
  }

  /* Initialize gRPC channel and stub for NameServer */
  private void initNamingServerChannelAndStub() {
    nameServerChannel = ManagedChannelBuilder.forTarget(nameServerTarget).usePlaintext().build();
    nameServerStub = NameServerServiceGrpc.newBlockingStub(nameServerChannel);
  }

  /* Lookup the address of the TupleSpaces service using NameServer */
  private void lookupService() {

    String target = null;

    try {
      // Create the request and response for the lookup
      LookupRequest request = LookupRequest.newBuilder().setName(serviceName).setQualifier(qualifier).build();
      LookupResponse response = nameServerStub.lookup(request);

      // Get first server address from response
      target = response.getServersList().get(0);
    } 
    catch (StatusRuntimeException e) {
      if(debug)
        System.err.println("Exception in lookupService method");
      System.out.println(e.getStatus().getDescription());
    }
    initServiceChannelAndStub(target);

    if(debug)
      System.err.println("NameServer lookup was successful");
  }

  /* Initialize gRPC channel and stub for TupleSpaces service */
  private void initServiceChannelAndStub(String target) {
    serviceChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    serviceStub = TupleSpacesGrpc.newBlockingStub(serviceChannel);
  }
  
  /* Close gRPC channels */
  public void serviceShutdown() {
    nameServerChannel.shutdownNow();
    serviceChannel.shutdownNow();
  }

  /* Put a tuple into the TupleSpaces */
  public void put(String tuple) {

    // Create the request
    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    
    // Attempt to send the request
    try {
      serviceStub.put(request);
      System.out.println("OK");
    } 
    catch (StatusRuntimeException e) {
      if(debug)
        System.err.println("Exception in put method for tuple: " + tuple);
      System.out.println(e.getStatus().getDescription());
    }

    if(debug)
      System.err.println("Tuple: " + tuple + " added successfully");

  }

  /* Read a tuple from the TupleSpaces */
  public void read(String tuple) {

    // Create the request
    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(tuple).build();

    // Attempt to send the request
    try {
      ReadResponse response = serviceStub.read(request);
      System.out.println("OK");
      System.out.println(response.getResult().toString());
    } 
    catch (StatusRuntimeException e) {
      if(debug)
        System.err.println("Exception in read method for tuple: " + tuple);
      System.out.println(e.getStatus().getDescription());
    }

    if(debug)
      System.err.println("Tuple: " + tuple + " read successfully");

  }

  /* Take a tuple from the TupleSpaces */
  public void take(String tuple) {

    // Create the request
    TakeRequest request = TakeRequest.newBuilder().setSearchPattern(tuple).build();

    // Attempt to send the request
    try {
      TakeResponse response = serviceStub.take(request);
      System.out.println("OK");
      System.out.println(response.getResult().toString());
    }
    catch (StatusRuntimeException e) {
      if(debug)
        System.err.println("Exception in take method for tuple: " + tuple);
      System.out.println(e.getStatus().getDescription());
    }

    if(debug)
      System.err.println("Tuple: " + tuple + " removed successfully");

  }

  /* Get the state of the TupleSpaces */
  public void getTupleSpacesState(String qualifier) {

    // Create the request
    getTupleSpacesStateRequest request = getTupleSpacesStateRequest.newBuilder().build();

    // Attempt to send the request
    try {
      getTupleSpacesStateResponse response = serviceStub.getTupleSpacesState(request);
      System.out.println("OK");
      System.out.println(response.getTupleList().toString());
    }
    catch (StatusRuntimeException e) {
      if(debug)
        System.err.println("Exception in getTupleSpacesState method");
      System.out.println(e.getStatus().getDescription());
    }

    if(debug)
      System.err.println("Get Tuple Spaces State " + qualifier + " was successful");
  }

  /* Set the delay for a specific qualifier */
  public void setDelay(String qualifier, int delay) {
    switch (qualifier) {
      case "A":
        this.delay[0] = delay;
        break;
      case "B":
        this.delay[1] = delay;
        break;
      case "C":
        this.delay[2] = delay;
        break;
      default:
        if(debug)
          System.err.println("Delay not set for qualifier: " + qualifier);
        System.out.println("Invalid qualifier, please try again");
        return;
    }
    if(debug)
      System.err.println("Delay set for qualifier: " + qualifier + " successfully");
  }
}
