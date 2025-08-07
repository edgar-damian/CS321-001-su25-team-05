package cs321.Cache;

import java.util.Iterator;
import java.util.LinkedList;

public class Cache{

}
/*
public class Cache<T extends Comparable<T>> implements Iterable<T>{
    private LinkedList<T> cache;
    private int cacheSize;
    private int refCount;
    private int hitCount;

    public Cache(Integer size){
        this.cacheSize = size;
        this.cache = new LinkedList<>();
        this.refCount = 0;
        this.hitCount = 0;
    }



    public T get(T key) {
        refCount++;
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
    }


    public T add(T value) {
        remove(value.getKey());
        T returnValue = null;
        // removes last if cache is full
        if(cache.size() >= cacheSize){
            returnValue = cache.removeLast();
        }
        cache.addFirst(value);
        return returnValue;
    }


    public T remove(K key) {
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
    }

    public boolean contains(int address){
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
    }


    public void clear() {
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
 */
