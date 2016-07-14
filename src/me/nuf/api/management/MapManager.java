package me.nuf.api.management;

import java.util.Map;

/**
 * Created by nuf on 3/19/2016.
 */
public class MapManager<K, V> {

    protected Map<K, V> registry;

    public Map<K, V> getRegistry() {
        return registry;
    }

    public void register(K key, V value) {
        registry.put(key, value);
    }

    public void unregister(K key) {
        registry.remove(key);
    }

}
