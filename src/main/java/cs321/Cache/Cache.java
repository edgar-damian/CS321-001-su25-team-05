package cs321.Cache;
import java.util.LinkedHashMap;
import java.util.LinkedList;
//package cs321.btree.BTree;
import cs321.btree.BTree;

public class Cache{
    //public class LinkedHashMap<Key,Value>
    private LinkedHashMap<Long, BTree.Node> cache; //uses LinkedHashMap instead of a linked list

    //private LinkedList<Node> cache;
    private int cacheSize;
    private int refCount;
    private int hitCount;

    public Cache(Integer size){
        this.cacheSize = size;

        //Constructs an empty insertion-ordered LinkedHashMap instance with the specified initial capacity and a default load factor (0.75).
        this.cache = new LinkedHashMap<>(size);

        this.refCount = 0;
        this.hitCount = 0;
    }

    public BTree.Node get(long key) {
        /*
        Iterator<T> iterator = cache.iterator();
        while(iterator.hasNext()){
            T value = iterator.next();
            // check to see if key is equal to values ket
            if(value.getKey().equals(key)){
                // update hit count and moves it to the front of the cache
                hitCount++;
                iterator.remove();
                cache.addFirst(value);
                return value;
            }
        }
        return null;

         */
        /*
        Get a key, if a keys is null that mean it was never there.
        If
         */
        refCount++;
        BTree.Node node = cache.get(key);
        if (node != null) {
            hitCount++;
            cache.put(key, node); //MRU I think...
            return node;
        }
        return null;
    }

    public BTree.Node add(long address, BTree.Node value) {
        /*
        remove(value.getKey());
        T returnValue = null;
        // removes last if cache is full
        if(cache.size() >= cacheSize){
            returnValue = cache.removeLast();
        }
        cache.addFirst(value);
        return returnValue;
        */
        return cache.put(address, value); //does this still need to return a node?
    }

    public BTree.Node remove(long key) {
        /*
        T returnValue = null;
        Iterator<T> iterator = cache.iterator();
        while(iterator.hasNext()){
            T value = iterator.next();
            // if values key is equal to param key remove the the object
            if(value.getKey().equals(key)){
                returnValue = value;
                iterator.remove();
            }
        }
        return returnValue;
        */
        BTree.Node temp = cache.remove(key);
        if (temp == null) {
            //the node was never there, nothing to be removed
            return null;
        }
        return temp;
    }

    public boolean contains(long address){
        /*
        BTreeNode node = new BTreeNode(address);
        Iterator<T> iter = list.listIterator();
        T temp;
        while(iter.hasNext()){
            temp = iter.next();
            if(((Comparable<T>) node).compareTo(temp) == 0){
                return true;
            }
        }
        return false;
         */
        // no use to keep this a function?
        // Returns true if this map maps one or more keys to the specified value.
        return cache.containsKey(address);

    }

    public void clear() {
        // Removes all of the mappings from this map.
        cache.clear();
    }

    public String toString() {
        double hitNumber = 0.0;
        if (refCount > 0) {
            hitNumber = ((double) hitCount) / (double) refCount * 100;
        }
        return "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "cs321.Cache.Cache with " + cacheSize + " entries has been created\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "Total number of references:        " + refCount + "\n"
                + "Total number of cache hits:        " + hitCount + "\n"
                + "cs321.Cache.Cache hit percent:                 "
                + String.format("%.2f", hitNumber) + "%\n";
    }
}
