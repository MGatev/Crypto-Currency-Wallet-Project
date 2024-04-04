package bg.sofia.uni.fmi.mjt.wallet.server.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TimedCache<K, V> implements Cache<K, V>, Serializable {

    private final long minutes;
    private final Map<K, TimedObject<V>> map;

    public TimedCache(long minutes) {
        this.minutes = minutes;
        this.map = new HashMap<>();
    }

    @Override
    public V get(K key) {
        TimedObject<V> timedObject = map.get(key);
        if (timedObject == null) {
            return null;
        }

        if (timedObject.minutesSinceCreation() > minutes) {
            map.remove(key);
            return null;
        } else {
            return timedObject.getObject();
        }
    }

    @Override
    public void put(K key, V value) {
        map.put(key, new TimedObject<>(value));
    }
}
