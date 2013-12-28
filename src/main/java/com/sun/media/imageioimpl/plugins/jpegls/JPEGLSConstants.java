package com.sun.media.imageioimpl.plugins.jpegls;

public class JPEGLSConstants {

    // Constant tables for run length codes ...

    // Order of run length codes
    public static final int[] J = new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2,
            2, 3, 3, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

    // Length of run length codes (ie. 1<<J[n])
    public static final int[] J_rm = new int[] { 1, 1, 1, 1, 2, 2, 2, 2, 4, 4,
            4, 4, 8, 8, 8, 8, 16, 16, 32, 32, 64, 64, 128, 128, 256, 512, 1024,
            2048, 4096, 8192, 16384, 32786 };

    public static final int BASIC_T1 = 3;
    public static final int BASIC_T2 = 7;
    public static final int BASIC_T3 = 21;

    // JPEG Syntax - Marker Segment stuff ....

    public static final int JPEG_MARKER = 0xff80;
    public static final int JPEG_MARKER_DNL = 0xffdc;
    public static final int JPEG_MARKER_EOI = 0xffd9;
    public static final int JPEG_MARKER_SOI = 0xffd8;
    public static final int JPEG_MARKER_SOS = 0xffda;

    // New for JPEG-LS (14495-1:1997)

    public static final int JPEG_MARKER_SOF55 = 0xfff7;
    public static final int JPEG_MARKER_LSE = 0xfff8;

    public static final byte JPEG_LSE_ID_L1 = 0x01;
    public static final byte JPEG_LSE_ID_L2 = 0x02;
    public static final byte JPEG_LSE_ID_L3 = 0x03;
    public static final byte JPEG_LSE_ID_L4 = 0x04;

    // plus two more run mode interruption contexts
    public static final int nContexts = 365;

    // Limits on values in bias correction array C
    public static final int MIN_C = -128;
    public static final int MAX_C = 127;
    
    public static final long MIN_A = 0L;
    public static final long MAX_A = (1L << 31L) - 1L;
}
