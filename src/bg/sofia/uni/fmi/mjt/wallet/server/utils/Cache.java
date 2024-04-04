package bg.sofia.uni.fmi.mjt.wallet.server.utils;

public interface Cache<K, V> {

    /**
     * Returns the value for the @key
     */
    V get(K key);

    /**
     * Adds an entry with @key and @value
     */
    void put(K key, V value);
}