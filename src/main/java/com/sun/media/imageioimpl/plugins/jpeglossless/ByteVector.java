package com.sun.media.imageioimpl.plugins.jpeglossless;

/**
 * A growable array of bytes.
 */
public class ByteVector {
    private byte[] data;
    private int size;

    public ByteVector() {
        data = new byte[10];
        size = 0;
    }

    public void add(byte x) {
        while (size >= data.length) {
            doubleCapacity();
        }
        data[size++] = x;
    }

    private void doubleCapacity() {
        byte[] tmp = new byte[data.length * 2 + 1];
        System.arraycopy(data, 0, tmp, 0, data.length);
        data = tmp;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[size];
        System.arraycopy(data, 0, bytes, 0, size);
        return bytes;
    }
}
