package org.fng.playerFNGTransaction.domain.transaction;

public interface Holder<T, K>{
    T retrieve(K amount);
}
