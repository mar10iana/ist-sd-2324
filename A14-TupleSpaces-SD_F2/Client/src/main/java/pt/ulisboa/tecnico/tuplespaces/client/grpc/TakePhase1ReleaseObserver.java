package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;

public class TakePhase1ReleaseObserver implements StreamObserver<TakePhase1ReleaseResponse> {

    ResponseCollector collector;

    public TakePhase1ReleaseObserver (ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(TakePhase1ReleaseResponse r) {
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}