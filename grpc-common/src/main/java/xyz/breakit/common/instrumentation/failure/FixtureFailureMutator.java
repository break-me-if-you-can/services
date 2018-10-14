package xyz.breakit.common.instrumentation.failure;

/**
 * Allows injecting fixture failures.
 *
 * @see FixtureFailureProvider
 */
public interface FixtureFailureMutator {

    void setFullFixtureEnabled(boolean enabled);

}
