package org.osmdroid.tileprovider.cachemanager;

public interface IterableWithSize<T> extends Iterable<T> {
    int size();
}
