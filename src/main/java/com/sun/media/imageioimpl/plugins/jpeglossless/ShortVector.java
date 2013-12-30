package com.sun.media.imageioimpl.plugins.jpeglossless;

/**
 * A growable array of shorts.
 */
public class ShortVector {
    private short[] data;
    private int size;

    public ShortVector() {
        data = new short[10];
        size = 0;
    }

    public void add(short x) {
        while (size >= data.length) {
            doubleCapacity();
        }
        data[size++] = x;
    }

    private void doubleCapacity() {
        short[] tmp = new short[data.length * 2 + 1];
        System.arraycopy(data, 0, tmp, 0, data.length);
        data = tmp;
    }

    public short[] toShortArray() {
        short[] shorts = new short[size];
        System.arraycopy(data, 0, shorts, 0, size);
        return shorts;
    }
}
