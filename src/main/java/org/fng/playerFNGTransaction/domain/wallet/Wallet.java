package org.fng.playerFNGTransaction.domain.wallet;

import org.fng.playerFNGTransaction.domain.transaction.Holder;
import org.fng.playerFNGTransaction.domain.transaction.Receiver;

public interface Wallet<C> extends Holder<C, C>, Receiver<C> {
}
