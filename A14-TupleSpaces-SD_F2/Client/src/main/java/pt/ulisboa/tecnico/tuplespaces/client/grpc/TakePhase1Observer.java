package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;

public class TakePhase1Observer implements StreamObserver<TakePhase1Response> {

    ResponseCollector collector;

    public TakePhase1Observer (ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(TakePhase1Response r) {
        collector.addString(r.getReservedTuplesList().toString());
        collector.addResponseTakePhase1(r.getReservedTuplesList());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}