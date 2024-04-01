package pt.ulisboa.tecnico.tuplespaces.server.domain;

public class Tuple {
    private String value;
    private boolean isLocked;
    private Integer clientId;

    public Tuple(String value) {
        this.value = value;
        this.isLocked = false;
        this.clientId = -1;
    }

    public synchronized String getValue() {
        return value;
    }

    public synchronized boolean isLocked() {
        return isLocked;
    }

    public synchronized Integer getClientId() {
        return clientId;
    }

    public synchronized void lock(Integer clientId) {
        this.isLocked = true;
        this.clientId = clientId;
    }

    public synchronized void unlock() {
        this.isLocked = false;
        this.clientId = -1;
    }
}