package org.osmdroid.util;

public interface IterableWithSize<T> extends Iterable<T> {
    int size();
}
