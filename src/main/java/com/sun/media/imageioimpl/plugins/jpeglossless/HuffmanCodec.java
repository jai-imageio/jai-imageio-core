package com.sun.media.imageioimpl.plugins.jpeglossless;

import java.util.HashMap;

/**
 * This class implements Huffman decoding.
 */
public class HuffmanCodec {
    private static final int LEAVES_OFFSET = 16;
    private int leafCounter;

    private HashMap<short[], Decoder> cachedDecoders = new HashMap<short[], Decoder>();

    public int getSample(BitBuffer bb, CodecOptions options) {
        if (bb == null) {
            throw new IllegalArgumentException("No data to handle.");
        }
        if (options == null || !(options instanceof HuffmanCodecOptions)) {
            throw new IllegalArgumentException("Options must be an instance of HuffmanCodecOptions.");
        }

        HuffmanCodecOptions huffman = (HuffmanCodecOptions) options;
        Decoder decoder = cachedDecoders.get(huffman.table);
        if (decoder == null) {
            decoder = new Decoder(huffman.table);
            cachedDecoders.put(huffman.table, decoder);
        }

        int bitCount = decoder.decode(bb);
        if (bitCount == 16) {
            return 0x8000;
        }
        if (bitCount < 0)
            bitCount = 0;
        int v = bb.getBits(bitCount) & ((int) Math.pow(2, bitCount) - 1);
        if ((v & (1 << (bitCount - 1))) == 0) {
            v -= (1 << bitCount) - 1;
        }

        return v;
    }

    private class Decoder {
        private Decoder[] branch = new Decoder[2];
        private int leafValue = -1;

        private Decoder() {
        }

        private Decoder(short[] source) {
            leafCounter = 0;
            createDecoder(this, source, 0, 0);
        }

        private Decoder createDecoder(short[] source, int start, int level) {
            Decoder dest = new Decoder();
            createDecoder(dest, source, start, level);
            return dest;
        }

        private void createDecoder(Decoder dest, short[] source, int start, int level) {
            int next = 0;
            int i = 0;
            while (i <= leafCounter && next < LEAVES_OFFSET) {
                i += source[start + next++] & 0xff;
            }

            if (level < next && next < LEAVES_OFFSET) {
                dest.branch[0] = createDecoder(source, start, level + 1);
                dest.branch[1] = createDecoder(source, start, level + 1);
            } else {
                i = start + LEAVES_OFFSET + leafCounter++;
                if (i < source.length) {
                    dest.leafValue = source[i] & 0xff;
                }
            }
        }

        private int decode(BitBuffer bb) {
            Decoder d = this;
            while (d.branch[0] != null) {
                int v = bb.getBits(1);
                if (v < 0)
                    break; // eof
                d = d.branch[v];
            }
            return d.leafValue;
        }
    }
}
