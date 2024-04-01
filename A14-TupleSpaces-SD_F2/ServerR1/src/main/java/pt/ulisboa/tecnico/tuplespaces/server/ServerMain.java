package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.nameserver.contract.NameServerServiceGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerServiceGrpc.NameServerServiceBlockingStub;

import pt.ulisboa.tecnico.nameserver.contract.NameServer.*;

import java.io.IOException;
import java.util.Scanner;
import io.grpc.StatusRuntimeException;

public class ServerMain {

  private static final String debugFlag = "-debug";
  private static boolean debug = false;
  private static String qualifier;
  private static String serviceName = "TupleSpaces";
  private static String namingServerTarget = "localhost:5001";
  private static String serviceTarget;

  public static void main(String[] args) throws IOException, InterruptedException {

    System.out.println("Running: " + ServerMain.class.getSimpleName());

    // Check for the correct number of arguments
    if (args.length < 2 || args.length > 3) {
      System.out.println("Invalid number of arguments, please try again");
      return;
    }

    // Check for debug flag and enable debug mode if present
    if (args.length == 3) {
      if (debugFlag.equals(args[2])) {
        System.err.println("Server is in debug mode");
        debug = true;
      }
    }

    if (debug) {
      System.err.printf("Received %d arguments:\n", args.length);
      for (int i = 0; i < args.length; i++) {
          System.err.printf("args[%d] = %s\n", i, args[i]);
      }
    }

    // Validate the server qualifier
    if (!args[1].equals("A") && !args[1].equals("B") && !args[1].equals("C")) {
      System.out.println("Invalid qualifier, please try again");
      return;
    }

    try {

      // Parse the port number from arguments
      final int port = Integer.parseInt(args[0]);
      final String serviceTarget = "localhost:" + port;
      qualifier = args[1];

      // Create a managed channel for communication with the naming server
      final ManagedChannel channel = ManagedChannelBuilder.forTarget(namingServerTarget).usePlaintext().build();
      // Create a blocking stub for synchronous calls to the naming server
      final NameServerServiceGrpc.NameServerServiceBlockingStub stub = NameServerServiceGrpc.newBlockingStub(channel);

      // Prepare the registration request for the naming server
      RegisterRequest request = RegisterRequest.newBuilder().setName(serviceName).setQualifier(qualifier).setAddress(serviceTarget).build();

      if(debug)
        System.err.println("Registering server in the naming server");

      // Attempt to register the server with the naming server
      try {
        stub.register(request);
      } catch (StatusRuntimeException e) {
        System.out.println("Not possible to register the server");
        return;
      }

      if(debug)
        System.err.println("Server registered in the naming server");

      // Instantiate the service implementation
      final BindableService serviceImpl = new ServiceImpl();

      // Create and configure the server
      Server server = ServerBuilder.forPort(port).addService(serviceImpl).build();

      if (debug)
        System.err.println("Starting server");

      // Start the server
      server.start();

      if(debug)
        System.err.println("Server started");

      // Wait for user input to shut down the server
      System.out.println("Press ENTER to shutdown");
      System.in.read();

      // Prepare the delete request for the naming server
      DeleteRequest deleteRequest = DeleteRequest.newBuilder().setName(serviceName).setAddress(serviceTarget).build();

      // Attempt to deregister the server from the naming server
      try {
        stub.delete(deleteRequest);
      } catch (StatusRuntimeException e) {
        System.out.println("Not possible to remove the server");
        return;
      }

      // Shut down the channel and server
      channel.shutdownNow();
      server.shutdown();

    } catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
      System.out.println("Invalid port number, please try again");
      return;
    }
  }
}
