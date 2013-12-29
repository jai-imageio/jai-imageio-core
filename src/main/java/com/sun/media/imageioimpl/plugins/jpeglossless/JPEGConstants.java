package com.sun.media.imageioimpl.plugins.jpeglossless;

public class JPEGConstants {

    // Start of Frame markers - non-differential, Huffman coding
    public static final int SOF0 = 0xffc0; // baseline DCT
    public static final int SOF1 = 0xffc1; // extended sequential DCT
    public static final int SOF2 = 0xffc2; // progressive DCT
    public static final int SOF3 = 0xffc3; // lossless (sequential)

    // Start of Frame markers - differential, Huffman coding
    public static final int SOF5 = 0xffc5; // differential sequential DCT
    public static final int SOF6 = 0xffc6; // differential progressive DCT
    public static final int SOF7 = 0xffc7; // differential lossless (sequential)

    // Start of Frame markers - non-differential, arithmetic coding
    public static final int JPG = 0xffc8; // reserved for JPEG extensions
    public static final int SOF9 = 0xffc9; // extended sequential DCT
    public static final int SOF10 = 0xffca; // progressive DCT
    public static final int SOF11 = 0xffcb; // lossless (sequential)

    // Start of Frame markers - differential, arithmetic coding
    public static final int SOF13 = 0xffcd; // differential sequential DCT
    public static final int SOF14 = 0xffce; // differential progressive DCT
    public static final int SOF15 = 0xffcf; // differential lossless (sequential)

    public static final int DHT = 0xffc4; // define Huffman table(s)
    public static final int DAC = 0xffcc; // define arithmetic coding conditions

    // Restart interval termination
    public static final int RST_0 = 0xffd0;
    public static final int RST_1 = 0xffd1;
    public static final int RST_2 = 0xffd2;
    public static final int RST_3 = 0xffd3;
    public static final int RST_4 = 0xffd4;
    public static final int RST_5 = 0xffd5;
    public static final int RST_6 = 0xffd6;
    public static final int RST_7 = 0xffd7;

    public static final int SOI = 0xffd8; // start of image
    public static final int EOI = 0xffd9; // end of image
    public static final int SOS = 0xffda; // start of scan
    public static final int DQT = 0xffdb; // define quantization table(s)
    public static final int DNL = 0xffdc; // define number of lines
    public static final int DRI = 0xffdd; // define restart interval
    public static final int DHP = 0xffde; // define hierarchical progression
    public static final int EXP = 0xffdf; // expand reference components
    public static final int COM = 0xfffe; // comment
}
