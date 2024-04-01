package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;

public class getTupleSpacesStateObserver implements StreamObserver<getTupleSpacesStateResponse> {

    ResponseCollector collector;

    public getTupleSpacesStateObserver (ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(getTupleSpacesStateResponse r) {
        for (int i = 0; i < r.getTupleList().size(); i++) {
            collector.addString(r.getTupleList().get(i));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}