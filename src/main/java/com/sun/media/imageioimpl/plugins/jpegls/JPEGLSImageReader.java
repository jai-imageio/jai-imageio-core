package com.sun.media.imageioimpl.plugins.jpegls;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPEGLSImageReader extends ImageReader {
    private static final Logger LOG = LoggerFactory.getLogger(JPEGLSImageReader.class);

    static double Log(double x) {
        return Math.log(x) / Math.log(2);
    }

    private static int CLAMP_1(int i, int MAXVAL, int NEAR) {
        return ((i > MAXVAL || i < NEAR + 1) ? NEAR + 1 : i);
    }

    private static int CLAMP_2(int i, int MAXVAL, int NEAR, int T1) {
        return ((i > MAXVAL || i < T1) ? T1 : i);
    }

    private static int CLAMP_3(int i, int MAXVAL, int NEAR, int T2) {
        return ((i > MAXVAL || i < T2) ? T2 : i);
    }

    private ImageInputStream iis;
    private boolean gotHeader;
    private boolean gotPixels;
    private int ROWS = 0;
    private int COLUMNS = 0;
    private int ncomponents;
    private int P = 0;       // Sample precision
    private int NEAR = 0;
    private int MAXVAL;
    private int T1;
    private int T2;
    private int T3;
    private int RESET;
    private int RANGE;
    // Number of bits needed to represent MAXVAL with a minumum of 2
    private int bpp;
    // Number of bits needed to represent a mapped error value
    private int qbpp;
    // the value of glimit for a sample encoded in regular mode
    private int LIMIT;
    private int readBitCount;
    private int readBitByte;
    private int readForwardByte;
    private boolean readHaveForwardByte;

    private SampleModel sampleModel;
    private DataBuffer dataBuffer;
    private WritableRaster raster;
    private ColorSpace colorSpace;
    private ColorModel colorModel;
    private BufferedImage outputImage;
    
    
    /**
     * Creates a new JPEG-LS image reader.
     * 
     * @param originator
     */
    public JPEGLSImageReader(ImageReaderSpi originator) {
        super(originator);
    }
    

    /** Overrides the method defined in the superclass. */
    @Override
    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        iis = (ImageInputStream) input;
        if (iis != null) {
            //iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        }
    }

    
    @Override
    public int getWidth(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        readHeader();
        return COLUMNS;
    }
    

    @Override
    public int getHeight(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        readHeader();
        return ROWS;
    }
    

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumImages(boolean arg0) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IOException {

        if (iis == null) {
            throw new IllegalStateException(I18N.getString("JPEGLSImageReader5"));
        }

        checkIndex(imageIndex);
        readHeader();
        readPixels();
        
        return outputImage;
    }

    
    @Override
    public boolean canReadRaster() {
        return true;
    }
    

    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {

        if (iis == null) {
            throw new IllegalStateException(I18N.getString("JPEGLSImageReader5"));
        }

        checkIndex(imageIndex);
        readHeader();
        readPixels();
        
        return raster;
    }
    

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(I18N.getString("JPEGLSImageReader0"));
        }
    }

    
    /**
     * Returns the next unsigned byte.
     * 
     * @return
     * @throws IOException
     */
    private int read8() throws IOException {
        int b = iis.readUnsignedByte();
        if (b < 0 || b > 0xff) {
            System.out.println("Bad byte!");
            System.out.println("b = " + Integer.toHexString(b));
        }
        return b;
    }
    

    /**
     * Returns the next unsigned short using big endian.
     * JPEG headers are big endian by spec (?).
     * 
     * @return
     * @throws IOException
     */
    private int read16BE() throws IOException {
        int a = read8();
        int b = read8();
        return (a << 8) | b;
    }
    
    
    /**
     * Returns the next single bit.
     * 
     * Cannot simply use iis.readBit(), because JPEG has weird marker segment.
     * 
     * @return
     * @throws IOException
     */
    private int readBit() throws IOException {
        // first bits are read from msb of byte
        Assert(readBitCount >= 0);
        if (readBitCount < 1) {
            if (readHaveForwardByte) {
                // skip the stuffed zero bit (otherwise would have been marker)(hence never 0xff)
                readHaveForwardByte = false;
                readBitByte = readForwardByte;
                readBitCount = 7;
            } else {
                readBitByte = read8();
                
                // could be marker segment or data 0xff with following stuffed zero bit
                if (readBitByte == 0xff) { 
                    Assert(readHaveForwardByte == false);
                    readForwardByte = read8();
                    
                    // stuffed zero bit after valid 0xff
                    if ((readForwardByte & 0x80) == 0) {
                        readHaveForwardByte = true;
                        // the valid 0xff is already in readBitByte
                        readBitCount = 8;
                    } else { 
                        // marker segment
                        // marker identifier is 0xff00+readForwardByte
                        // cerr << "readBitByte=" << hex <<
                        // unsigned(readBitByte) << dec << endl;
                        // cerr << "readForwardByte=" << hex <<
                        // unsigned(readForwardByte) << dec << endl;
                        System.out.println("Bad forward byte!");
                        System.out.println("readBitByte = " + Integer.toHexString(readBitByte));
                        System.out.println("readForwardByte = " + Integer.toHexString(readForwardByte));
                        Assert(0); // for now
                    }
                } else {
                    readBitCount = 8;
                }
            }
        }
        
        return (readBitByte >> (--readBitCount)) & 1;
    }
    

    private int readJPEGMarker() throws IOException {
        return read16BE();
    }

    
    private boolean readSOF55(int marker) throws IOException {
        if (marker != JPEGLSConstants.JPEG_MARKER_SOF55) {
            return false;
        }
        
        DEBUG("Reading SOF-55");

        int length = read16BE();
        if (length != 11) {
            DEBUG("Expected length 11 (got " + length + ")");
            return false;
        }

        P = read8();
        if (P == 0) {
            DEBUG("Precision must be greater than zero (got " + P + ")");
            return false;
        }

        ROWS = read16BE();
        if (ROWS == 0) {
            DEBUG("Rows must be greater than zero (got " + ROWS + ")");
            return false;
        }

        COLUMNS = read16BE();
        if (COLUMNS == 0) {
            DEBUG("Columns must be greater than zero (got " + COLUMNS + ")");
            return false;
        }

        ncomponents = read8();
        if (ncomponents != 1) {
            DEBUG("Number of components must be 1 (got " + ncomponents + ")");
            return false;
        }

        int componentid = read8();
        DEBUG("componentid = " + componentid);

        int hvsampling = read8();
        DEBUG("hvsampling = " + hvsampling);

        int quanttable = read8();
        DEBUG("quanttable = " + quanttable);

        return true;
    }

    
    private boolean readSOS(int marker) throws IOException {
        if (marker != JPEGLSConstants.JPEG_MARKER_SOS) {
            return false;
        }
        
        DEBUG("Reading SOS");

        int length = read16BE();
        if (length != 8) {
            DEBUG("Length must be 8 (got " + length + ")");
            return false;
        }

        int ncomponents = read8();
        if (ncomponents != 1) {
            DEBUG("Number of components must be 1 (got " + ncomponents + ")");
            return false;
        }

        int componentid = read8();
        DEBUG("componentid = " + componentid);

        int mappingtable = read8();
        DEBUG("mappingtable = " + mappingtable);

        NEAR = read8();
        DEBUG("NEAR = " + NEAR);

        int ilv = read8();
        DEBUG("ilv = " + ilv);

        int dummy = read8();
        DEBUG("dummy = " + dummy);

        return true;

    }

    private boolean readLSE1(int marker) throws IOException {
        if (marker != JPEGLSConstants.JPEG_MARKER_LSE) {
            return false;
        }
        
        DEBUG("Reading LSE");

        int length = read16BE();
        if (length != 13) {
            DEBUG("Length must be 13 (got " + length + ")");
            return false;
        }

        int id = read8();
        if (id != JPEGLSConstants.JPEG_LSE_ID_L1) {
            DEBUG("Invalid LSE_ID_L1 marker");
            return false;
        }

        MAXVAL = read16BE();
        if (MAXVAL == 0) {
            DEBUG("MAXVAL must be greater than zero (got " + MAXVAL + ")");
            return false;
        }

        T1 = read16BE();
        DEBUG("T1 = " + T1);

        T2 = read16BE();
        DEBUG("T2 = " + T2);

        T3 = read16BE();
        DEBUG("T3 = " + T3);

        RESET = read16BE();
        DEBUG("RESET = " + RESET);

        return true;
    }

    
    private static int determineGolombParameter(long n, long a) {
        Assert(n > 0);
        Assert(a >= 0);
        long k = 0;
        while ((n << k) < a) {
            k++;
            Assert(k < 31);
        }
        return (int) k;
    }

    
    private int decodeMappedErrvalWithGolomb(int k, int glimit, int qbpp)
            throws IOException {
        
        // Read unary representation of remaining most significant bits

        int bit = 0;
        int unarycode = 0;
        int value = 0;
        while ((bit = readBit()) == 0) {
            // stops after bit is 1 (having read and discared trailing 1 bit)
            ++unarycode;
        }

        int offset;
        int bitstoread;
        int limit = glimit - qbpp - 1;
        
        DEBUG("\t\tdecodeMappedErrvalWithGolomb: unarycode = " + unarycode);
        DEBUG("\t\tdecodeMappedErrvalWithGolomb: limit = " + limit);
        
        if (unarycode < limit) {
            // use it to form most significant bits
            DEBUG("\t\tdecodeMappedErrvalWithGolomb: not limited, read " + unarycode + " zero bits (as value) followed by 1 then will read remaining " + k + " bits");
            value = unarycode; // will later get shifted into ms bits
            bitstoread = k;
            offset = 0;
        } else {
            DEBUG("\t\tdecodeMappedErrvalWithGolomb: limited, read " + unarycode + " zero bits followed by 1 then will read remaining " + qbpp + " bits of value-1");
            // no contribution from unary code ... whole value is next
            value = 0;
            bitstoread = qbpp;
            offset = 1;
        }

        // Read least significant k bits

        while (bitstoread-- > 0) {
            bit = readBit();
            value = (value << 1) | bit; // msb bit is read first
        }
        value += offset; // correct for limited case

        DEBUG("\t\tdecodeMappedErrvalWithGolomb: value = " + value);
        return value;
    }

    private int deQuantizeErrval(int NEAR, int Errval) {
        if (NEAR != 0) {
            Errval = Errval * (2 * NEAR + 1);
        }
        
        return Errval;
    }

    private long clampPredictedValue(long X) {
        DEBUG("\t\tclampPredictedValue: before value = " + X);

        if (X > MAXVAL) {
            X = MAXVAL;
        } else if (X < 0) {
            X = 0;
        }

        DEBUG("\t\tclampPredictedValue: after value = " + X);
        return X;
    }

    
    private int codecRunEndSample(int Ra, int Rb, int RANGE, int NEAR,
            int MAXVAL, int RESET, int LIMIT, int qbpp, int rk, long[] A,
            int[] N, int[] Nn) throws IOException {
 DEBUG("\t\tcodecRunEndSample: " + "decoding");
 
     int Ix = 0;

     boolean RItype = (Ra == Rb || Math.abs(Ra-Rb) <= NEAR);

     int SIGN = (!RItype && Ra > Rb) ? -1 : 1;

     int Px = RItype ? Ra : Rb;

 DEBUG("\t\tcodecRunEndSample: Ra = " + Ra);
 DEBUG("\t\tcodecRunEndSample: Rb = " + Rb);
 DEBUG("\t\tcodecRunEndSample: RItype = " + (RItype ? "1":"0"));
 DEBUG("\t\tcodecRunEndSample: SIGN = " + SIGN);
 DEBUG("\t\tcodecRunEndSample: Px = " + Px);

     long TEMP = RItype ? A[366]+(N[366]>>1) : A[365];

     int Q = 365 + (RItype ? 1 : 0);

 DEBUG("\t\tcodecRunEndSample: TEMP = " + TEMP);
 DEBUG("\t\tcodecRunEndSample: Q = " + Q);

     int k = determineGolombParameter(N[Q],TEMP);

 DEBUG("\t\tcodecRunEndSample: k = " + k);

     int  Errval = 0;
     int  updateErrval = 0;
     long EMErrval = 0;

     if (true) {
         EMErrval = decodeMappedErrvalWithGolomb(k,LIMIT-rk-1,qbpp);    // needs work :(

 DEBUG("\t\tcodecRunEndSample: EMErrval = " + EMErrval);

            // use local copy to leave original for parameter update later
            long tEMErrval = EMErrval + (RItype ? 1 : 0);

 DEBUG("\t\tcodecRunEndSample: tEMErrval = " + tEMErrval);

            if (tEMErrval == 0) {
                Errval = 0;
            } else if (k == 0) {
                if (2 * Nn[Q - 365] < N[Q]) {
                    if (tEMErrval % 2 == 0) {
                        // "map = 0" 2 becomes -1, 4 becomes -2, 6 becomes -3
                        Errval = -(int) (tEMErrval >> 1);
                    } else {
                        // "map = 1" 1 becomes 1, 3 becomes 2, 5 becomes 3
                        Errval = (int) ((tEMErrval + 1) >> 1);
                    }
                } else { 
                    // 2*Nn[Q-365] >= N[Q]
                    if (tEMErrval % 2 == 0) {
                        // "map = 0" 2 becomes 1, 4 becomes 2, 6 becomes 3
                        Errval = (int) (tEMErrval >> 1);
                    } else {
                        // "map = 1" 1 becomes -1, 3 becomes -2, 5 becomes -3
                        Errval = -(int) ((tEMErrval + 1) >> 1); 
                    }
                }
            } else {
                if (tEMErrval % 2 == 0) {
                    // "map = 0" 2 becomes 1, 4 becomes 2, 6 becomes 3
                    Errval = (int) (tEMErrval >> 1);
                } else {
                    // "map = 1" 1 becomes -1, 3 becomes -2, 5 becomes -3
                    Errval = -(int) ((tEMErrval + 1) >> 1);
                }
            }


 DEBUG("\t\tcodecRunEndSample: Errval after sign unmapping = " + Errval);

         updateErrval=Errval;

         if (NEAR > 0) {
             Errval = deQuantizeErrval(NEAR,Errval);
         }

 DEBUG("\t\tcodecRunEndSample: Errval SIGN uncorrected = " + Errval);

         if (SIGN < 0) Errval=-Errval;       // if "context type" was negative

 DEBUG("\t\tcodecRunEndSample: Errval result = " + Errval);

         long Rx = Px+Errval;

         // modulo(RANGE*(2*NEAR+1)) as per F.1 Item 14

         // (NB. Is this really the reverse of the encoding procedure ???)

         if (Rx < -NEAR) {
             Rx+=RANGE*(2*NEAR+1);
         } else if (Rx > MAXVAL+NEAR) {
             Rx-=RANGE*(2*NEAR+1);
         }

         Rx = clampPredictedValue(Rx);

         // Apply inverse point transform and mapping table when implemented

         Ix=(int)Rx;
     }

     // Update parameters ...

 DEBUG("\t\tcodecRunEndSample: Update parameters ... updateErrval used = " + updateErrval);
 DEBUG("\t\tcodecRunEndSample: Update parameters ... EMErrval used = " + EMErrval);

 DEBUG("\t\tcodecRunEndSample: A[" + Q + "]  before = " + A[Q] );
 DEBUG("\t\tcodecRunEndSample: N[" + Q + "]  before = " + N[Q] );
 DEBUG("\t\tcodecRunEndSample: Nn[" + (Q-365) + "] before = " + Nn[Q-365]);

     if (updateErrval < 0) {
         ++Nn[Q-365];
     }
     
     A[Q]+=(EMErrval+1-(RItype ? 1 : 0))>>1;
     if (N[Q] == RESET) {
         A[Q]=A[Q]>>1;
         N[Q]=N[Q]>>1;
         Nn[Q-365]=Nn[Q-365]>>1;
     }
     ++N[Q];
     Assert(A[Q] >= 0);

 DEBUG("\t\tcodecRunEndSample: A[" + Q + "]  updated = " + A[Q] );
 DEBUG("\t\tcodecRunEndSample: N[" + Q + "]  updated = " + N[Q] );
 DEBUG("\t\tcodecRunEndSample: Nn[" + (Q-365) + "] updated = " + Nn[Q-365]);

 //if (decompressing) DEBUG("\t\tcodecRunEndSample: value = " + Ix);
 return Ix;
 }

    
    private void readHeader() throws IOException {
        if (gotHeader) {
            return;
        }

        boolean haveLSE1 = false;

        int marker = readJPEGMarker();
        if (marker != JPEGLSConstants.JPEG_MARKER_SOI) {
            throw new IllegalStateException("Expected JPEG SOI (got " + Integer.toHexString(marker) + ")");
        }

        marker = readJPEGMarker();
        if (!readSOF55(marker)) {
            haveLSE1 = readLSE1(marker);
            DEBUG("haveLSE1 = " + haveLSE1);
            if (!haveLSE1) {
                throw new IllegalStateException("Expected LSE1 (marker=" + Integer.toHexString(marker) + ")");
            }

            marker = readJPEGMarker();
            if (!readSOF55(marker)) {
                throw new IllegalStateException("Expected SOF55 (marker=" + Integer.toHexString(marker) + ")");
            }
        }

        marker = readJPEGMarker();
        if (!readSOS(marker)) {
            haveLSE1 = readLSE1(marker);
            DEBUG("haveLSE1 = " + haveLSE1);
            if (!haveLSE1) {
                throw new IllegalStateException("Expected LSE1 (marker=" + Integer.toHexString(marker) + ")");
            }

            marker = readJPEGMarker();
            if (!readSOS(marker)) {
                throw new IllegalStateException("Invalid SOS (marker=" + Integer.toHexString(marker) + ")");
            }
        }

        DEBUG("Markers complete");
        DEBUG("haveLSE1 = " + haveLSE1);
        DEBUG("ROWS = " + ROWS);
        DEBUG("COLUMNS = " + COLUMNS);

        if (!haveLSE1) { // Note that the LSE ID 1 marker is optional
            MAXVAL = (1 << P) - 1;

            if (RESET == 0) {
                RESET = 64; // May have been set on command line
            }

            // Initialization of default parameters as per A.1 reference to
            // C.2.4.1.1.1

            // Thresholds for context gradients ...

            // Only replace T1, T2, T3 if not set on command line ...

            if (MAXVAL >= 128) {
                //int FACTOR = FloorDivision(Math.min(MAXVAL, 4095) + 128, 256);
                int FACTOR = (Math.min(MAXVAL, 4095) + 128) / 256;

                if (T1 != 0) {
                    T1 = CLAMP_1(FACTOR * (JPEGLSConstants.BASIC_T1 - 2) + 2
                            + 3 * NEAR, MAXVAL, NEAR);
                }

                if (T2 != 0) {
                    T2 = CLAMP_2(FACTOR * (JPEGLSConstants.BASIC_T2 - 3) + 3
                            + 5 * NEAR, MAXVAL, NEAR, T1);
                }

                if (T3 != 0) {
                    T3 = CLAMP_3(FACTOR * (JPEGLSConstants.BASIC_T3 - 4) + 4
                            + 7 * NEAR, MAXVAL, NEAR, T2);
                }
            } else {
                //int FACTOR = FloorDivision(256, MAXVAL + 1);
                int FACTOR = 256 / (MAXVAL + 1);
                
                if (T1 != 0) {
                    // ? should these calculations be
                    // float since we are dividing ? :(
                    T1 = CLAMP_1(
                            Math.max(2, JPEGLSConstants.BASIC_T1 / FACTOR + 3
                                    * NEAR), MAXVAL, NEAR);
                }

                if (T2 != 0) {
                    T2 = CLAMP_2(
                            Math.max(3, JPEGLSConstants.BASIC_T2 / FACTOR + 5
                                    * NEAR), MAXVAL, NEAR, T1);
                }

                if (T3 != 0) {
                    T3 = CLAMP_3(
                            Math.max(4, JPEGLSConstants.BASIC_T3 / FACTOR + 7
                                    * NEAR), MAXVAL, NEAR, T2);
                }
            }
        }

        // Initialization as per Annex A.2.1

        //RANGE = FloorDivision(MAXVAL + 2 * NEAR, 2 * NEAR + 1) + 1;
        RANGE = ((MAXVAL + 2 * NEAR) / (2 * NEAR + 1)) + 1;
        DEBUG("RANGE = " + RANGE);

        Assert(MAXVAL == 0 || (1 <= MAXVAL && MAXVAL < 1 << P));
        if (NEAR == 0) {
            Assert(RANGE == MAXVAL + 1);
        }

        // Number of bits needed to represent MAXVAL with a minumum of 2
        bpp = Math.max(2, (int)Math.ceil((Log(MAXVAL + 1))));

        // Number of bits needed to represent a mapped error value
        qbpp = (int)Math.ceil(Log(RANGE));

        // the value of glimit for a sample encoded in regular mode
        LIMIT = 2 * (bpp + Math.max(8, bpp));

        DEBUG("bpp = " + bpp);
        DEBUG("qbpp = " + qbpp);
        DEBUG("LIMIT = " + LIMIT);

        Assert(bpp >= 2);
        Assert(LIMIT > qbpp); // Else LIMIT-qbpp-1 will fail (see A.5.3)
        
        gotHeader = true;
    }
    
    
    private void createImage() {
        if (COLUMNS <= 0) {
            throw new IllegalStateException("Unsupported COLUMNS (" + COLUMNS + ")");
        }
        
        if (ROWS <= 0) {
            throw new IllegalStateException("Unsupported ROWS (" + ROWS + ")");
        }
        
        if (ncomponents != 1) {
            throw new IllegalStateException("Unsupported ncomponents (" + ncomponents + ")");
        }
        
        int dataType = 0;
        
        if (bpp <= 8) {
            dataType = DataBuffer.TYPE_BYTE;
            dataBuffer = new DataBufferByte(COLUMNS * ROWS * ncomponents);
        } else if (bpp <= 16) {
            dataType = DataBuffer.TYPE_USHORT;
            dataBuffer = new DataBufferUShort(COLUMNS * ROWS * ncomponents);
        } else {
            throw new IllegalStateException("Unsupported BPP");
        }

        // Assumes nComponents = 1 (grayscale)
        sampleModel = new ComponentSampleModel(
                dataType,
                COLUMNS,
                ROWS,
                ncomponents,
                COLUMNS,
                new int[] { 0 });
        
        raster = Raster.createWritableRaster(
                sampleModel,
                dataBuffer,
                new Point(0, 0));

        int[] bits = new int[ncomponents];
        Arrays.fill(bits, bpp);
        
        colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        
        colorModel = new ComponentColorModel(
                colorSpace,
                bits,
                false,
                false,
                Transparency.OPAQUE,
                dataType);
        
        outputImage = new BufferedImage(colorModel, raster, false, null);
    }
    
    
    private void readPixels() throws IOException {
        if (gotPixels) {
            return;
        }

        createImage();

        // Initialization of variables ...

        // counters for context type occurence
        // [0..nContexts+2-1]
        // [nContexts],[nContexts+1] for run mode interruption
        int[] N = new int[JPEGLSConstants.nContexts + 2]; 
        
        // accumulated prediction error
        // magnitude [0..nContexts-1]
        // [nContexts],[nContexts+1] for run mode interruption
        long[] A = new long[JPEGLSConstants.nContexts + 2];

        // auxilliary counters for bias
        // cancellation [0..nContexts-1]
        int[] B = new int[JPEGLSConstants.nContexts];

        // counters indicating bias correction
        // value [0..nContexts-1]
        // (never -ve but often used as -N[Q] so int not saves cast)
        int[] C = new int[JPEGLSConstants.nContexts];

        // negative prediction error for run interruption
        // [365..366]
        int[] Nn = new int[2];

        //long A_Init_Value = Math.max(2, FloorDivision(RANGE + (1 << 5), (1 << 6)));
        long A_Init_Value = Math.max(2, (RANGE + (1 << 5)) / ((1 << 6)));
        DEBUG("A_Init_Value = " + A_Init_Value);
        Assert(A_Init_Value > 0);
        
        for (int i = 0; i < JPEGLSConstants.nContexts + 2; ++i) {
            N[i] = 1;
            A[i] = A_Init_Value;
        }

        Nn[0] = 0;
        Nn[1] = 0;

        // The run variables seem to need to live beyond a single run or row !!!
        int RUNIndex = 0;
        int prevRa0 = 0;

        int[] thisRow = new int[COLUMNS];
        int[] prevRow = new int[COLUMNS];
        
        for (int row = 0; row < ROWS; ++row) {
            DEBUG("Row " + row);
            
            for (int col = 0; col < COLUMNS; ++col) {
                DEBUG("\tcol = " + col);

                // c b d .
                // a x . .
                // . . . .

             // Reconstructed value
                long Rx = 0;
                
                // value at edges (first row and first col is zero) ...
                int Ra = 0;
                int Rb = 0;
                int Rc = 0;
                int Rd = 0;

                if (row > 0) {
                    Rb = prevRow[col];
                    Rc = (col > 0) ? prevRow[col - 1] : prevRa0;
                    Ra = (col > 0) ? thisRow[col - 1] : (prevRa0 = Rb);
                    Rd = (col + 1 < COLUMNS) ? prevRow[col + 1] : Rb;
                } else {
                    Ra = (col > 0) ? thisRow[col - 1] : (prevRa0 = 0);
                }

                DEBUG("\t\tRa = " + Ra);
                DEBUG("\t\tRb = " + Rb);
                DEBUG("\t\tRc = " + Rc);
                DEBUG("\t\tRd = " + Rd);

                // NB. We want the Reconstructed values, which are the same
                // in lossless mode, but if NEAR != 0 take care to write back
                // reconstructed values into the row buffers in previous
                // positions

                // Compute local gradient ...

                int D1 = Rd - Rb;
                int D2 = Rb - Rc;
                int D3 = Rc - Ra;

                DEBUG("\t\tD1 = " + D1);
                DEBUG("\t\tD2 = " + D2);
                DEBUG("\t\tD3 = " + D3);

                // Check for run mode ... (should check Abs() works ok for int)

                if (Math.abs(D1) <= NEAR && Math.abs(D2) <= NEAR && Math.abs(D3) <= NEAR) {
                    // Run mode

                    DEBUG("Row at run start " + row);
                    DEBUG("\tcol at run start " + col);
                    if (true) {
                        // dumpReadBitPosition();
                        // Why is RUNIndex not reset to 0 here ?
                        while (true) {
                            int R = readBit();
                            DEBUG("\tcol " + col);
                            DEBUG("\tR " + R);
                            if (R == 1) {
                                // Fill image with 2^J[RUNIndex] samples of Ra
                                // or till EOL
                                int rm = JPEGLSConstants.J_rm[RUNIndex];
                                DEBUG("\tRUNIndex " + RUNIndex);
                                DEBUG("\tFilling with " + rm + " samples of Ra " + Ra);
                                while (rm-- != 0 && col < COLUMNS) {
                                    thisRow[col] = Ra;
                                    PIXEL("pixel[" + row + "," + col + "] = " + thisRow[col]);
                                    ++col;
                                }
                                // This will match when exact count coincides
                                // with end of row ...
                                if (rm == -1 && RUNIndex < 31) {
                                    ++RUNIndex;
                                    DEBUG("\tRUNIndex incremented to " + RUNIndex);
                                }
                                if (col >= COLUMNS) {
                                    DEBUG("\tFilled to end of row");
                                    DEBUG("\tAfter having found end of row ");
                                    // dumpReadBitPosition();
                                    break;
                                }
                            } else {
                                // Read J[RUNIndex] bits and fill image with
                                // that number of samples of Ra
                                int bits = JPEGLSConstants.J[RUNIndex];
                                DEBUG("\tRUNIndex " + RUNIndex);
                                DEBUG("\tReading bits " + bits);
                                int nfill = 0;
                                int bit = 0;
                                // msb bit is read first
                                while (bits-- != 0) {
                                    bit = readBit();
                                    nfill = (nfill << 1) | bit;
                                }
                                DEBUG("\tFill with " + nfill + " samples of Ra " + Ra);
                                // Fill with nfill values of Ra
                                while (nfill-- != 0) {
                                    // if (!(col<(COLUMNS-1))) {
                                    // DEBUG("Fail at line 367 ... !(col<(COLUMNS-1))");
                                    // DEBUG("\tstill to fill " + nfill+1);
                                    // DEBUG("\trow is " + row);
                                    // DEBUG("\tcol is " + col);
                                    // }
                                    Assert(col < COLUMNS);
                                    thisRow[col] = Ra;
                                    PIXEL("pixel[" + row + "," + col + "] = " + thisRow[col]);
                                    ++col;
                                }
                                // Decode the run interruption sample ...
                                DEBUG("\tcol at end of run " + col);
                                DEBUG("\tBefore decoding value that ends run ");
                                // dumpReadBitPosition();

                                // First update local context for interrupting
                                // sample, since weren't kept updated during run

                                if (row > 0) {
                                    Rb = prevRow[col];
                                    Ra = (col > 0) ? thisRow[col - 1] : Rb;
                                } else {
                                    Rb = 0;
                                    Ra = (col > 0) ? thisRow[col - 1] : 0;
                                }
                                DEBUG("\t\tRa = " + Ra);
                                DEBUG("\t\tRb = " + Rb);
                                thisRow[col] = codecRunEndSample(Ra, Rb, RANGE, NEAR, MAXVAL, RESET, LIMIT, qbpp, JPEGLSConstants.J[RUNIndex], A, N, Nn);
                                PIXEL("pixel[" + row + "," + col + "] = " + thisRow[col]);
                                DEBUG("\tValue that ends run " + thisRow[col]);
                                // dumpReadBitPosition();
                                if (RUNIndex > 0) {
                                    --RUNIndex; // NB. Do this AFTER J[RUNIndex]
                                                // used in the limited length
                                                // Golomb coding
                                    DEBUG("\tRUNIndex decremented to " + RUNIndex);
                                }

                                break;
                            }
                        }
                    }

                } else {

                    // Regular mode

                    // Gradient quantization ... (A.3.3)

                    int Q1, Q2, Q3;

                    if (D1 <= -T3)
                        Q1 = -4;
                    else if (D1 <= -T2)
                        Q1 = -3;
                    else if (D1 <= -T1)
                        Q1 = -2;
                    else if (D1 < -NEAR)
                        Q1 = -1;
                    else if (D1 <= NEAR)
                        Q1 = 0;
                    else if (D1 < T1)
                        Q1 = 1;
                    else if (D1 < T2)
                        Q1 = 2;
                    else if (D1 < T3)
                        Q1 = 3;
                    else
                        Q1 = 4;

                    if (D2 <= -T3)
                        Q2 = -4;
                    else if (D2 <= -T2)
                        Q2 = -3;
                    else if (D2 <= -T1)
                        Q2 = -2;
                    else if (D2 < -NEAR)
                        Q2 = -1;
                    else if (D2 <= NEAR)
                        Q2 = 0;
                    else if (D2 < T1)
                        Q2 = 1;
                    else if (D2 < T2)
                        Q2 = 2;
                    else if (D2 < T3)
                        Q2 = 3;
                    else
                        Q2 = 4;

                    if (D3 <= -T3)
                        Q3 = -4;
                    else if (D3 <= -T2)
                        Q3 = -3;
                    else if (D3 <= -T1)
                        Q3 = -2;
                    else if (D3 < -NEAR)
                        Q3 = -1;
                    else if (D3 <= NEAR)
                        Q3 = 0;
                    else if (D3 < T1)
                        Q3 = 1;
                    else if (D3 < T2)
                        Q3 = 2;
                    else if (D3 < T3)
                        Q3 = 3;
                    else
                        Q3 = 4;

                    DEBUG("\t\tQ1 = " + Q1);
                    DEBUG("\t\tQ2 = " + Q2);
                    DEBUG("\t\tQ3 = " + Q3);

                    // Context merging and determination of SIGN ... (A.3.4)

                    int SIGN;

                    // "If the 1st non-zero component of vector (Q1,Q2,Q3) is negative"
                    // ...

                    if (Q1 < 0 || (Q1 == 0 && Q2 < 0) || (Q1 == 0 && Q2 == 0 && Q3 < 0)) {
                        Q1 = -Q1;
                        Q2 = -Q2;
                        Q3 = -Q3;
                        SIGN = -1; // signifies -ve
                    } else {
                        SIGN = 1; // signifies +ve
                    }

                    DEBUG("\t\tSIGN= " + SIGN);

                    DEBUG("\t\tQ1 after SIGN = " + Q1);
                    DEBUG("\t\tQ2 after SIGN = " + Q2);
                    DEBUG("\t\tQ3 after SIGN = " + Q3);

                    // The derivation of Q is not specified in the standard :(

                    // Let's try this approach ....

                    // Q1 can be 0 to 4 only
                    // Q1 1 to 4 and Q2 -4 to 4 and Q3 -4 to 4 = 4*9*9 = 324
                    // Q1 0 and Q2 1 to 4 only and Q3 -4 to 4 = 1*4*9 = 36
                    // Q1 0 and Q2 0 and Q3 0 to 4 = 1*1*5 = 5
                    // total of 365
                    // and 0,0,0 (Q == 360) only occurs for run mode or regular
                    // mode with sample interleaved

                    int Q = 0;

                    if (Q1 == 0) {
                        if (Q2 == 0) {
                            // fills 360..364
                            Q = 360 + Q3;
                        } else {
                            // Q2 is 1 to 4
                            // fills 324..359
                            Q = 324 + (Q2 - 1) * 9 + (Q3 + 4);
                        }
                    } else {
                        // Q1 is 1 to 4
                        // fills 0..323
                        Q = (Q1 - 1) * 81 + (Q2 + 4) * 9 + (Q3 + 4);
                    }

                    DEBUG("\t\tQ = " + Q);

                    // if (Q >= nContexts) {
                    // DEBUG("\t\tQ1 after SIGN = " + Q1);
                    // DEBUG("\t\tQ2 after SIGN = " + Q2);
                    // DEBUG("\t\tQ3 after SIGN = " + Q3);
                    // DEBUG("\t\tQ itself = " + Q);
                    // }
                    // Assert(Q<nContexts); // Just in case

                    // Figure A.5 Edge detecting predictor ...

                    // Predicted value
                    long Px = 0;

                    if (Rc >= Math.max(Ra, Rb)) {
                        Px = Math.min(Ra, Rb);
                    } else if (Rc <= Math.min(Ra, Rb)) {
                        Px = Math.max(Ra, Rb);
                    } else {
                        Px = (int) Ra + Rb - Rc;
                    }

                    DEBUG("\t\tPx = " + Px);

                    // Figure A.6 Prediction correction and clamping ...

                    Px = Px + ((SIGN > 0) ? C[Q] : -C[Q]);

                    DEBUG("\t\tC[Q] = " + C[Q]);
                    DEBUG("\t\tPx corrected = " + Px);

                    Px = clampPredictedValue(Px);

                    DEBUG("\t\tPx clamped = " + Px);

                    // Figure A.10 Prediction error Golomb encoding and
                    // decoding...

                    int k = determineGolombParameter(N[Q], A[Q]);

                    int MErrval = 0;
                    int Errval;
                    int updateErrval = 0;

                    if (true) {
                        // Decode Golomb mapped error from input...
                        MErrval = decodeMappedErrvalWithGolomb(k, LIMIT, qbpp);

                        DEBUG("\t\tMErrval = " + MErrval);

                        // Unmap error from non-negative (inverse of A.5.2
                        // Figure A.11) ...

                        if (NEAR == 0 && k == 0 && 2 * B[Q] <= -N[Q]) {
                            if (MErrval % 2 != 0)
                                Errval = ((int) MErrval - 1) / 2; // 1 becomes
                                                                  // 0, 3
                                                                  // becomes 1,
                                                                  // 5 becomes 2
                            else
                                Errval = -(int) MErrval / 2 - 1; // 0 becomes
                                                                 // -1, 2
                                                                 // becomes -2,
                                                                 // 4 becomes -3
                        } else {
                            if (MErrval % 2 == 0)
                                Errval = (int) MErrval / 2; // 0 becomes 0, 2
                                                            // becomes 1, 4
                                                            // becomes 2
                            else
                                Errval = -((int) MErrval + 1) / 2; // 1 becomes
                                                                   // -1, 3
                                                                   // becomes -2
                        }

                        updateErrval = Errval; // NB. Before dequantization and
                                               // sign correction

                        Errval = deQuantizeErrval(NEAR, Errval);

                        DEBUG("\t\tErrval SIGN uncorrected = " + Errval);

                        if (SIGN < 0)
                            Errval = -Errval; // if "context type" was negative

                        DEBUG("\t\tErrval result = " + Errval);

                        Rx = Px + Errval;

                        // modulo(RANGE*(2*NEAR+1)) as per F.1 Item 14

                        // (NB. Is this really the reverse of the encoding
                        // procedure ???)

                        if (Rx < -NEAR)
                            Rx += RANGE * (2 * NEAR + 1);
                        else if (Rx > MAXVAL + NEAR)
                            Rx -= RANGE * (2 * NEAR + 1);

                        Rx = clampPredictedValue(Rx);

                        // Apply inverse point transform and mapping table when
                        // implemented

                        thisRow[col] = (int) Rx;
                        PIXEL("pixel[" + row + "," + col + "] = " + thisRow[col]);

                    }

                    // Update variables (A.6) ...

                    DEBUG("\t\tUpdate variables with error updateErrval = " + updateErrval);
                    DEBUG("\t\tA[Q] old = " + A[Q]);
                    DEBUG("\t\tB[Q] old = " + B[Q]);
                    DEBUG("\t\tC[Q] old = " + C[Q]);
                    DEBUG("\t\tN[Q] old = " + N[Q]);

                    // A.6.1 Use the signed error after modulo reduction (figure
                    // A.12 note). which is updateErrval

                    B[Q] = B[Q] + updateErrval * (2 * NEAR + 1);
                    A[Q] = A[Q] + Math.abs(updateErrval);
                    if (N[Q] == RESET) {
                        A[Q] = A[Q] >> 1;
                        B[Q] = B[Q] >> 1;
                        N[Q] = N[Q] >> 1;
                    }
                    ++N[Q];
                    A[Q] = A[Q] & JPEGLSConstants.MAX_A;
                    Assert(A[Q] >= JPEGLSConstants.MIN_A && A[Q] < JPEGLSConstants.MAX_A);

                    DEBUG("\t\tA[Q] updated = " + A[Q]);
                    DEBUG("\t\tB[Q] updated = " + B[Q]);
                    DEBUG("\t\tC[Q] updated = " + C[Q]);
                    DEBUG("\t\tN[Q] updated = " + N[Q]);

                    // A.6.2 Context dependent bias cancellation ...

                    if (B[Q] <= -N[Q]) {
                        B[Q] += N[Q];
                        if (C[Q] > JPEGLSConstants.MIN_C) {
                            --C[Q];
                        }
                        if (B[Q] <= -N[Q]) {
                            B[Q] = -N[Q] + 1;
                        }
                    } else if (B[Q] > 0) {
                        B[Q] -= N[Q];
                        if (C[Q] < JPEGLSConstants.MAX_C) {
                            ++C[Q];
                        }
                        if (B[Q] > 0) {
                            B[Q] = 0;
                        }
                    }

                    DEBUG("\t\tA[Q] bias cancelled = " + A[Q]);
                    DEBUG("\t\tB[Q] bias cancelled = " + B[Q]);
                    DEBUG("\t\tC[Q] bias cancelled = " + C[Q]);
                    DEBUG("\t\tN[Q] bias cancelled = " + N[Q]);

                }
            }
            if (true) {
                DEBUG("row=" + row + ", length=" + thisRow.length);
                // if (!writeRow(out,thisRow,COLUMNS,bpp)) Assert(0);
            }
            
            int rowStart = row * COLUMNS;
            for (int i = 0; i < COLUMNS; i++) {
                dataBuffer.setElem(rowStart + i, (thisRow[i]));
            }
            
            int[] tmpRow = thisRow;
            thisRow = prevRow;
            prevRow = tmpRow;
        }
        
        gotPixels = true;
    }

    public static void Assert(boolean x) {
        if (!x) {
            throw new RuntimeException("Assert Fail");
        }
    }
    
    public static void Assert(int x) {
        Assert(x != 0);
    }
    
    public static void Assert(Object obj) {
        Assert(obj != null);
    }

    
    public static void DEBUG(String str) {
        //System.out.println(str);
        LOG.info(str);
    }
    
    
    public static void PIXEL(String str) {
        //System.out.println(str);
        LOG.info(str);
    }

    public static void main(String[] args) throws IOException {
        //File file = new File("/home/cody/Downloads/jpegls_0.06.20120206/data/njltest.jls");
        //File file = new File("/home/cody/Downloads/jpegls_0.06.20120206/data/njltest.noruns.jls");

        //File file = new File("/home/cody/Downloads/jpegls_0.06.20120206/data/hploco.near.jls");
        
        File file = new File("/home/cody/Downloads/jpegls_0.06.20120206/data/CT2.jls");
        //File file = new File("/home/cody/Downloads/jpegls_0.06.20120206/data/CT2.noruns.jls");
        
        ImageInputStream input = new FileImageInputStream(file);
        
        JPEGLSImageReaderSpi spi = new JPEGLSImageReaderSpi();
        JPEGLSImageReader reader = new JPEGLSImageReader(spi);
        reader.setInput(input);
        
        BufferedImage outputImage = reader.read(0, null);

        ImageIO.write(
                outputImage,
                "png",
                new File("/home/cody/" + file.getName() + "-" + System.currentTimeMillis() + ".png"));
    }
}
