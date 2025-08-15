package cs321.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

import cs321.btree.BTree;

public class Cache {
    // single map supports both key types (String and Long)
    private final LinkedHashMap<Object, Object> map;
    private final int capacity;
    private int refCount;
    private int hitCount;

    public Cache(Integer size) {
        int cap = (size == null || size <= 0) ? 1 : size;
        this.capacity = cap;
        this.map = new LinkedHashMap<Object, Object>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                return size() > Cache.this.capacity; // LRU eviction
            }
        };
        this.refCount = 0;
        this.hitCount = 0;
    }

    // -------- String-key API (used by SSHSearchBTree) --------
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public String[] get(String key) {
        refCount++;
        Object v = map.get(key);
        if (v != null)
            hitCount++;
        return (String[]) v;
    }

    public void put(String key, String[] value) {
        map.put(key, value);
    }

    // -------- long-key API (for BTree node caching) --------
    public boolean contains(long address) {
        return map.containsKey(address);
    }

    public BTree.Node get(long address) {
        refCount++;
        Object v = map.get(address);
        if (v != null)
            hitCount++;
        return (BTree.Node) v;
    }

    // kept for compatibility with older code
    public BTree.Node add(long address, BTree.Node value) {
        Object prev = map.put(address, value);
        return (BTree.Node) prev;
    }

    public BTree.Node remove(long address) {
        Object prev = map.remove(address);
        return (BTree.Node) prev;
    }

    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        double hitPercent = refCount > 0 ? (hitCount * 100.0) / refCount : 0.0;
        return "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "cs321.Cache.Cache with " + capacity + " entries has been created\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "Total number of references:        " + refCount + "\n"
                + "Total number of cache hits:        " + hitCount + "\n"
                + "cs321.Cache.Cache hit percent:                 "
                + String.format("%.2f", hitPercent) + "%\n";
    }
}