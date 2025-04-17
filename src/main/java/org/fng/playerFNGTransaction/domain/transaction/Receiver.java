package org.fng.playerFNGTransaction.domain.transaction;

public interface Receiver<K>{
    void receive(K amount);
}
