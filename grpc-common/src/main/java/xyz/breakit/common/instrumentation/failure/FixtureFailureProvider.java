package xyz.breakit.common.instrumentation.failure;

/**
 * Provides injected fixture failure.
 */
public interface FixtureFailureProvider {
    /**
     * @return {@code true} when full fixture failure enabled. Game
     * is not playable since entire game fixture is covered with elements.
     */
    boolean isFullFixtureEnabled();
}
