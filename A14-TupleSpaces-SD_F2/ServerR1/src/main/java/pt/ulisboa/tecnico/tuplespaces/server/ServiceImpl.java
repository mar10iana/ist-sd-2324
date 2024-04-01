package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState; // ServerState
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc; // TupleSpacesGrpc
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*; // Message Classes

import io.grpc.stub.StreamObserver;

import static io.grpc.Status.INVALID_ARGUMENT;

import java.util.ArrayList;

public class ServiceImpl extends TupleSpacesGrpc.TupleSpacesImplBase {

    // Initialize the server state
    private ServerState serverState = new ServerState();

    /** Handle put requests */
    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {

        // Get the tuple from the request
        String tuple = request.getNewTuple();

        // Check for invalid tuple
        if (tuple == null) {
            // Respond with error
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple").asRuntimeException());
        } else {
            // Add the tuple to the server state
            serverState.put(tuple);
            
            // Create and send a success response
            PutResponse response = PutResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /** Handle read requests */
    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {

        // Get the search pattern from the request
        String searchPattern = request.getSearchPattern();

        // Search for the tuple in the server state
        String tuple = serverState.read(searchPattern);

        // Check for invalid search pattern or no matching tuple
        if (tuple == null) {
            // Respond with error
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid search pattern").asRuntimeException());
        } else {
            // Create and send a success response with the found tuple
            ReadResponse response = ReadResponse.newBuilder().setResult(tuple).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /** Handle take requests */
    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {

        // Get the search pattern from the request
        String searchPattern = request.getSearchPattern();

        // Attempt to remove a matching tuple from the server state
        String tuple = serverState.take(searchPattern);

        // Check for invalid search pattern or no matching tuple
        if (tuple == null) {
            // Respond with error
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid search pattern").asRuntimeException());
        } else {
            // Create and send a success response with the removed tuple
            TakeResponse response = TakeResponse.newBuilder().setResult(tuple).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /** Handle getTupleSpacesState requests */
    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver) {

        // Retrieve the current state of tuple spaces
        ArrayList<String> tuples = serverState.getTupleSpacesState();

        // Check for invalid state or other errors
        if (tuples == null) {
            // Respond with error
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid request").asRuntimeException());
        } else {
            // Create and send a success response with the list of tuples
            getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder().addAllTuple(tuples).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
