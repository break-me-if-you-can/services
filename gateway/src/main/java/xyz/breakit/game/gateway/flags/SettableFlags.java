package xyz.breakit.game.gateway.flags;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Simple in-memory mutable implementation of {@link Flags}.
 */
@ThreadSafe
public final class SettableFlags implements Flags {

    private volatile boolean partialDegradationEnabled;
    private volatile boolean retryEnabled;
    private volatile boolean diverseGeeseEnabled;


    @Override
    public boolean isPartialDegradationEnabled() {
        return partialDegradationEnabled;
    }

    @Override
    public boolean isRetryEnabled() {
        return retryEnabled;
    }

    public void setPartialDegradationEnabled(boolean enabled) {
        this.partialDegradationEnabled = enabled;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
    }

    @Override
    public boolean isDiverseGeeseEnabled() {
        return diverseGeeseEnabled;
    }

    public void setDiverseGeeseEnabled(boolean diverseGeeseEnabled) {
        this.diverseGeeseEnabled = diverseGeeseEnabled;
    }
}
