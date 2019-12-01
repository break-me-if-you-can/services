package xyz.breakit.game.gateway.flags;

/**
 * Contains flags that control configurable behaviour.
 * Flag values can change dynamically.
 */
public interface Flags {

    /**
     * @return {@code true} if partial degradation is enabled,
     * and server response can contain fallback values instead
     * of throwing an error when dependency failed.
     */
    boolean isPartialDegradationEnabled();

    /**
     * @return {@code true} if reties to upstream services are
     * enabled.
     */
    boolean isRetryEnabled();

    /**
     * @return {@code true} if diverse geese are enabled, otherwise only canada geese type is used
     * and all other geese types are ignored
     */
    boolean isDiverseGeeseEnabled();

}
