package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.*;

import io.grpc.stub.StreamObserver;

import static io.grpc.Status.INVALID_ARGUMENT;

import java.util.ArrayList;

public class ServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

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

    /** Handle release requests */
    @Override
    public void takePhase1Release(TakePhase1ReleaseRequest request, StreamObserver<TakePhase1ReleaseResponse> responseObserver) {

        // Get the client id from the request
        int clientId = request.getClientId();

        // Release the reserved tuples for the client
        serverState.takePhase1Release(clientId);

        // Create and send a success response
        TakePhase1ReleaseResponse response = TakePhase1ReleaseResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /** Handle takePhase1 requests */
    @Override
    public void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver) {

        // Get the search pattern and client id from the request
        String searchPattern = request.getSearchPattern();
        int clientId = request.getClientId();

        ArrayList<String> tuple = serverState.takePhase1(searchPattern, clientId);

        // Create and send a success response with the removed tuple
        TakePhase1Response response = TakePhase1Response.newBuilder().addAllReservedTuples(tuple).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /** Handle takePhase2 requests */
    @Override
    public void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver) {

        // Get the tuple value and client id from the request
        String tupleValue = request.getTuple();
        int clientId = request.getClientId();

        // Attempt to remove a matching tuple from the server state
        String tuple = serverState.takePhase2(tupleValue, clientId);

        // Check for invalid search pattern or no matching tuple
        if (tuple == null) {
            // Respond with error
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid search pattern").asRuntimeException());
        } else {
            // Create and send a success response with the removed tuple
            TakePhase2Response response = TakePhase2Response.newBuilder().setResult(tuple).build();
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
