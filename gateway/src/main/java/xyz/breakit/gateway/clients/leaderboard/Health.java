package xyz.breakit.gateway.clients.leaderboard;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Health {
    private boolean broken;

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("broken", broken)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Health health = (Health) o;
        return broken == health.broken;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(broken);
    }
}
