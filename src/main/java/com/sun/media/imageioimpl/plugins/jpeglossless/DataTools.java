package com.sun.media.imageioimpl.plugins.jpeglossless;

/**
 * A utility class with convenience methods for reading, writing and decoding
 * words.
 */
public final class DataTools {

    /**
     * Translates up to the first len bytes of a byte array beyond the given
     * offset to a short. If there are fewer than len bytes available, the MSBs
     * are all assumed to be zero (regardless of endianness).
     */
    public static short bytesToShort(byte[] bytes, int off, int len, boolean little) {
        if (bytes.length - off < len)
            len = bytes.length - off;
        short total = 0;
        for (int i = 0, ndx = off; i < len; i++, ndx++) {
            total |= (bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((little ? i : len - i - 1) * 8);
        }
        return total;
    }

    /**
     * Translates up to the first len bytes of a byte array beyond the given
     * offset to an int. If there are fewer than len bytes available, the MSBs
     * are all assumed to be zero (regardless of endianness).
     */
    public static int bytesToInt(byte[] bytes, int off, int len, boolean little) {
        if (bytes.length - off < len)
            len = bytes.length - off;
        int total = 0;
        for (int i = 0, ndx = off; i < len; i++, ndx++) {
            total |= (bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((little ? i : len - i - 1) * 8);
        }
        return total;
    }

    /**
     * Translates nBytes of the given long and places the result in the given
     * byte array.
     * 
     * @throws IllegalArgumentException
     *             if the specified indices fall outside the buffer
     */
    public static void unpackBytes(long value, byte[] buf, int ndx, int nBytes, boolean little) {
        if (buf.length < ndx + nBytes) {
            throw new IllegalArgumentException("Invalid indices: buf.length=" + buf.length + ", ndx=" + ndx + ", nBytes=" + nBytes);
        }
        if (little) {
            for (int i = 0; i < nBytes; i++) {
                buf[ndx + i] = (byte) ((value >> (8 * i)) & 0xff);
            }
        } else {
            for (int i = 0; i < nBytes; i++) {
                buf[ndx + i] = (byte) ((value >> (8 * (nBytes - i - 1))) & 0xff);
            }
        }
    }
}
