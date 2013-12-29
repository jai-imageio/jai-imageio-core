
package com.sun.media.imageioimpl.plugins.jpeglossless;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.imageio.stream.ImageInputStream;


/**
 * Decompresses lossless JPEG images.
 */
public class LosslessJPEGCodec {
  
  /**
   * The CodecOptions parameter should have the following fields set:
   *  {@link CodecOptions#interleaved interleaved}
   *  {@link CodecOptions#littleEndian littleEndian}
   */
  public BufferedImage decompress(ImageInputStream in, CodecOptions options)
    throws IOException
  {
    if (in == null) {
      throw new IllegalArgumentException("No data to decompress.");
    }
    
    if (options == null) { 
        options = CodecOptions.getDefaultOptions();
    }
    
    byte[] buf = new byte[0];

    int width = 0, height = 0;
    int bitsPerSample = 0, nComponents = 0, bytesPerSample = 0;
    int[] horizontalSampling = null, verticalSampling = null;
    int[] quantizationTable = null;
    short[][] huffmanTables = null;

    int startPredictor = 0;

    int[] dcTable = null, acTable = null;

    while (in.getStreamPosition() < in.length() - 1) {
      int code = in.readShort() & 0xffff;
      int length = in.readShort() & 0xffff;
      long fp = in.getStreamPosition();
      if (length > 0xff00) {
        length = 0;
        in.seek(fp - 2);
      }
      else if (code == JPEGConstants.SOS) {
        nComponents = in.read();
        dcTable = new int[nComponents];
        acTable = new int[nComponents];
        for (int i=0; i<nComponents; i++) {
          in.read(); // componentSelector
          int tableSelector = in.read();
          dcTable[i] = (tableSelector & 0xf0) >> 4;
          acTable[i] = tableSelector & 0xf;
        }
        startPredictor = in.read();
        in.read(); // endPredictor
        in.read(); // pointTransform

        // read image data

        byte[] toDecode = new byte[(int) (in.length() - in.getStreamPosition())];
        in.read(toDecode);

        // scrub out byte stuffing

        ByteVector b = new ByteVector();
        for (int i=0; i<toDecode.length; i++) {
          b.add(toDecode[i]);
          if (toDecode[i] == (byte) 0xff && toDecode[i + 1] == 0) i++;
        }
        toDecode = b.toByteArray();

        BitBuffer bb = new BitBuffer(toDecode);
        HuffmanCodec huffman = new HuffmanCodec();
        HuffmanCodecOptions huffmanOptions = new HuffmanCodecOptions();
        huffmanOptions.bitsPerSample = bitsPerSample;
        huffmanOptions.maxBytes = buf.length / nComponents;

        int nextSample = 0;
        while (nextSample < buf.length / nComponents) {
          for (int i=0; i<nComponents; i++) {
            huffmanOptions.table = huffmanTables[dcTable[i]];
            int v = 0;

            if (huffmanTables != null) {
              v = huffman.getSample(bb, huffmanOptions);
              if (nextSample == 0) {
                v += (int) Math.pow(2, bitsPerSample - 1);
              }
            }

            // apply predictor to the sample
            int predictor = startPredictor;
            if (nextSample < width * bytesPerSample) predictor = 1;
            else if ((nextSample % (width * bytesPerSample)) == 0) {
              predictor = 2;
            }

            int componentOffset = i * (buf.length / nComponents);

            int indexA = nextSample - bytesPerSample + componentOffset;
            int indexB = nextSample - width * bytesPerSample + componentOffset;
            int indexC = nextSample - (width + 1) * bytesPerSample +
              componentOffset;

            int sampleA = indexA < 0 ? 0 :
              DataTools.bytesToInt(buf, indexA, bytesPerSample, false);
            int sampleB = indexB < 0 ? 0 :
              DataTools.bytesToInt(buf, indexB, bytesPerSample, false);
            int sampleC = indexC < 0 ? 0 :
              DataTools.bytesToInt(buf, indexC, bytesPerSample, false);

            if (nextSample > 0) {
              int pred = 0;
              switch (predictor) {
                case 1:
                  pred = sampleA;
                  break;
                case 2:
                  pred = sampleB;
                  break;
                case 3:
                  pred = sampleC;
                  break;
                case 4:
                  pred = sampleA + sampleB + sampleC;
                  break;
                case 5:
                  pred = sampleA + ((sampleB - sampleC) / 2);
                  break;
                case 6:
                  pred = sampleB + ((sampleA - sampleC) / 2);
                  break;
                case 7:
                  pred = (sampleA + sampleB) / 2;
                  break;
              }
              v += pred;
            }

            int offset = componentOffset + nextSample;

            DataTools.unpackBytes(v, buf, offset, bytesPerSample, false);
          }
          nextSample += bytesPerSample;
        }
      }
      else {
        length -= 2; // stored length includes length param
        if (length == 0) {
            continue;
        }

        if (code == JPEGConstants.EOI) {

        } else if (code == JPEGConstants.SOF3) {
          // lossless w/Huffman coding
          bitsPerSample = in.read();
          height = in.readShort();
          width = in.readShort();
          nComponents = in.read();
          horizontalSampling = new int[nComponents];
          verticalSampling = new int[nComponents];
          quantizationTable = new int[nComponents];
          for (int i=0; i<nComponents; i++) {
            in.skipBytes(1);
            int s = in.read();
            horizontalSampling[i] = (s & 0xf0) >> 4;
            verticalSampling[i] = s & 0x0f;
            quantizationTable[i] = in.read();
          }

          bytesPerSample = bitsPerSample / 8;
          if ((bitsPerSample % 8) != 0) bytesPerSample++;

          buf = new byte[width * height * nComponents * bytesPerSample];
        }
        else if (code == JPEGConstants.SOF11) {
          throw new RuntimeException(
            "Arithmetic coding is not yet supported");
        }
        else if (code == JPEGConstants.DHT) {
          if (huffmanTables == null) {
            huffmanTables = new short[4][];
          }
          int s = in.read();
          byte destination = (byte) (s & 0xf);
          int[] nCodes = new int[16];
          Vector table = new Vector();
          for (int i=0; i<nCodes.length; i++) {
            nCodes[i] = in.read();
            table.add(new Short((short) nCodes[i]));
          }

          for (int i=0; i<nCodes.length; i++) {
            for (int j=0; j<nCodes[i]; j++) {
              table.add(new Short((short) (in.read() & 0xff)));
            }
          }
          huffmanTables[destination] = new short[table.size()];
          for (int i=0; i<huffmanTables[destination].length; i++) {
            huffmanTables[destination][i] = ((Short) table.get(i)).shortValue();
          }
        }
        in.seek(fp + length);
      }
    }

    if (options.interleaved && nComponents > 1) {
      // data is stored in planar (RRR...GGG...BBB...) order
      byte[] newBuf = new byte[buf.length];
      for (int i=0; i<buf.length; i+=nComponents*bytesPerSample) {
        for (int c=0; c<nComponents; c++) {
          int src = c * (buf.length / nComponents) + (i / nComponents);
          int dst = i + c * bytesPerSample;
          System.arraycopy(buf, src, newBuf, dst, bytesPerSample);
        }
      }
      buf = newBuf;
    }

    if (options.littleEndian && bytesPerSample > 1) {
      // data is stored in big endian order
      // reverse the bytes in each sample
      byte[] newBuf = new byte[buf.length];
      for (int i=0; i<buf.length; i+=bytesPerSample) {
        for (int q=0; q<bytesPerSample; q++) {
          newBuf[i + bytesPerSample - q - 1] = buf[i + q];
        }
      }
      buf = newBuf;
    }

    //return buf;
    
    // START CODY
    
    int dataType = 0;
    DataBuffer dataBuffer = null;
    
    if (bytesPerSample == 1) {
        dataType = DataBuffer.TYPE_BYTE;
        dataBuffer = new DataBufferByte(width * height * nComponents);
        byte[] b = ((DataBufferByte)dataBuffer).getData();
        System.arraycopy(buf, 0, b, 0, buf.length);
        
    } else if (bytesPerSample == 2) {
        if (options.signed) {
            dataType = DataBuffer.TYPE_SHORT;
            dataBuffer = new DataBufferShort(width * height * nComponents);
            short[] s = ((DataBufferShort)dataBuffer).getData();
            for (int i = 0; i < buf.length; i += 2) {
                s[i / 2] = DataTools.bytesToShort(buf, i, 2, true);
            }
            
        } else {
            dataType = DataBuffer.TYPE_USHORT;
            dataBuffer = new DataBufferUShort(width * height * nComponents);
            short[] s = ((DataBufferUShort)dataBuffer).getData();
            for (int i = 0; i < buf.length; i += 2) {
                s[i / 2] = DataTools.bytesToShort(buf, i, 2, true);
            }
        }
        
    } else {
        throw new IllegalStateException("Unsupported BPP (bytesPerSample=" + bytesPerSample + ", bitsPerSample=" + bitsPerSample + ")");
    }   

    // Assumes nComponents = 1 (grayscale)
    SampleModel sampleModel = new ComponentSampleModel(
            dataType,
            width,
            height,
            nComponents,
            width,
            new int[] { 0 });
    
    WritableRaster raster = Raster.createWritableRaster(
            sampleModel,
            dataBuffer,
            new Point(0, 0));

    int[] bits = new int[nComponents];
    Arrays.fill(bits, bitsPerSample);
    
    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    
    ColorModel colorModel = new ComponentColorModel(
            colorSpace,
            bits,
            false,
            false,
            Transparency.OPAQUE,
            dataType);
    
    BufferedImage outputImage = new BufferedImage(colorModel, raster, false, null);
    return outputImage;
  }

}