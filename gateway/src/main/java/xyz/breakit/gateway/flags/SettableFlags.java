package xyz.breakit.gateway.flags;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Simple in-memory mutable implementation of {@link Flags}.
 */
@ThreadSafe
public final class SettableFlags implements Flags {

    private volatile boolean partialDegradationEnabled;

    @Override
    public boolean isPartialDegradationEnabled() {
        return partialDegradationEnabled;
    }

    public void setPartialDegradationEnabled(boolean enabled) {
        this.partialDegradationEnabled = enabled;
    }
}
