package com.github.jaiimageio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFLZWDecompressor;
import com.github.jaiimageio.impl.plugins.tiff.TIFFT6Compressor;
import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

public class TiffTest {
	
	// issue37
	@Test
	public void testLSB() throws Exception {
		checkAndCompare("misto_gray");
		checkAndCompare("misto_rgb");
		checkAndCompare("gradiente_rgb");
		checkAndCompare("gradiente_gray");
		checkAndCompare("checker");
		//--
		URL file = getClass().getResource("/lsbtifflzw/export_gimp.tiff");
		BufferedImage bufferedImage = ImageIO.read(file);
		File fx = File.createTempFile("imageio-test", "." + "tiff");
		bufferedImage = ImageIO.read(file);
		ImageIO.write(bufferedImage, "tiff", fx);
		//System.out.println(fx);

	}
	
    private int reverseBits(int inp)
    {
		int iz = 0 ;
		int po2 = 1;
		int rev = 0x80;
		for( int i = 0 ; i < 8 ; i++) {
			if( (inp & po2 ) != 0 ) {
				iz += rev;
			}
			po2 <<= 1 ;
			rev >>= 1;
			
		}
		return iz;
	}

	@Test
	public void testFillOrderReverseTable() throws Exception {
		TIFFLZWDecompressor compressor = new TIFFLZWDecompressor(BaselineTIFFTagSet.PREDICTOR_NONE);
		for( int i = 0 ; i < 256 ; i ++ ) {
			int reversed = reverseBits(i);
			int calculated = compressor.reverseBits(i);
			assertEquals("Index:"+i, reversed, calculated);
		}
	}

	private void checkAndCompare(final String fileName) throws Exception {

		final String fileLsb="/lsbtifflzw/lsb_"+fileName+".tiff";
		final String fileMsb="/lsbtifflzw/msb_"+fileName+".tiff";
		
		URL lsbFile = getClass().getResource(fileLsb);
		BufferedImage bufferedImageLsb = ImageIO.read(lsbFile);
		URL msbFile = getClass().getResource(fileMsb);
		BufferedImage bufferedImageMsb = ImageIO.read(msbFile);
		
		File lsbImageFile = writeImage(bufferedImageLsb) ;
		File msbImageFile = writeImage(bufferedImageMsb) ;
		
		compareFiles(lsbImageFile, msbImageFile);
	}
	
	private void compareFiles(File f1, File f2) throws Exception {
		assertEquals("File length differs:"+f1.getName()+" / "+f2.getName(), f1.length(), f2.length());
		InputStream is1 = null ;
		InputStream is2 = null ;
		try {
			is1 = new FileInputStream(f1);
			is2 = new FileInputStream(f2);
			final int counter = (int)f1.length();
			byte [] b1 = new byte [counter];
			byte [] b2 = new byte [counter];
			final int read1 = is1.read(b1);
			final int read2 = is2.read(b2);
			assertEquals("Read values :"+f1.getName(), read1, f1.length());
			assertEquals("Read values :"+f2.getName(), read2, f2.length());
			for( int l = 0 ; l < counter ; l ++ ) {
				if(b1[l] != b2[l]) {
					assertTrue("File:"+f1.getName()+" offset:"+l, (b1[l] == b2[l]));
				}
			}
		} finally {
			if( null != is1 ) {
				is1.close();
			}
			if( null != is2 ) {
				is2.close();
			}
		}
	}

	private File writeImage(BufferedImage image) throws Exception {
		TIFFImageWriter tiffImageWriter = getImageWriter();
        //----
		TIFFImageWriteParam writeParams = (TIFFImageWriteParam)tiffImageWriter.getDefaultWriteParam();
        /*TIFFNullCompressor compressor = new TIFFNullCompressor();
        writeParams.setCompressionMode(TIFFImageWriteParam.MODE_EXPLICIT);
        writeParams.setCompressionType(compressor.getCompressionType());
        writeParams.setTIFFCompressor(compressor);*/
        //--
        File f = File.createTempFile("imageio-test", ".tiff");
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(new FileOutputStream(f));
        tiffImageWriter.setOutput(imageOutputStream);
        //--
        tiffImageWriter.write(null, new IIOImage(image, null, null), writeParams);
        tiffImageWriter.dispose();
        imageOutputStream.close();
        return f;
	}

	private TIFFImageWriter getImageWriter() {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("tiff");
        while (writers.hasNext()) {
			ImageWriter writer = writers.next();
        	if (writer instanceof TIFFImageWriter) {
        		// Don't return java.desktop/com.sun.imageio.plugins.tiff.TIFFImageWriter
        		// on Java 9
        		return (TIFFImageWriter) writer;
        	}
        }
        fail("Can't find TIFFImageWriter instance");
        return null; // won't happen
	}
	
	@Test
	public void testReadTiff() throws Exception {

		URL g4File = getClass().getResource("/checkerg4.tiff");
		BufferedImage bufferedImage = ImageIO.read(g4File);
		assertEquals(640, bufferedImage.getWidth());
		assertEquals(400, bufferedImage.getHeight());
	}

	@Test
	public void testWriteG4Tiff() throws Exception
	{
                BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_BINARY);

                final int black = 0;
                final int white = 0xFFFFFF;

                for( int row = 0 ; row < 256 ; row ++ ) {
                        for( int col= 0 ; col< 256 ; col++ ) {
                                image.setRGB(col, row, (((row+col)&1)==0) ?white:black);
                        }
                }
                // With Java9, TIFFImageWriter is also provided by the JRE, but
                // it only makes sense for us to test our own implementation
                TIFFImageWriter tiffImageWriter = getImageWriter();
//                if (tiffImageWriter == null) {
//                	return;
//                }
                //----
                TIFFImageWriteParam writeParams = (TIFFImageWriteParam)tiffImageWriter.getDefaultWriteParam();
                TIFFT6Compressor compressor = new TIFFT6Compressor();
                writeParams.setCompressionMode(TIFFImageWriteParam.MODE_EXPLICIT);
                writeParams.setCompressionType(compressor.getCompressionType());
                writeParams.setTIFFCompressor(compressor);
                //--
                File f = File.createTempFile("imageio-test", ".tiff");
                ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(new FileOutputStream(f));
                tiffImageWriter.setOutput(imageOutputStream);
                //--
                tiffImageWriter.write(null, new IIOImage(image, null, null), writeParams);
                tiffImageWriter.dispose();
                imageOutputStream.close();
                ImageIO.read(f);
	}

	@Test
	public void testG4Compressor() throws Exception
	{
		TIFFT6Compressor compressor = new TIFFT6Compressor();
		byte [] input = { 
			0x55, 0x55, 0x55, 0x55, 
			0x55, 0x55, 0x55, 0x55, 0x55
		};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(baos);
		compressor.setStream(imageOutputStream);
		compressor.encode(input, 0, 72, 1, new int [] {1}, 9) ;
	}
	
	@Test
	public void testG4Compressor1() throws Exception
	{
		TIFFT6Compressor compressor = new TIFFT6Compressor();
		byte [] input = { 
			0x55
		};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(baos);
		compressor.setStream(imageOutputStream);
		compressor.encode(input, 0, 1, 1, new int [] {1}, 1) ;
	}
	@Test
	public void testG4Compressor2() throws Exception
	{
		byte [] input = { 
				(byte)0x55, (byte)0x55, (byte)0x55,
				(byte)0xAA, (byte)0xAA, (byte)0xAA,
				(byte)0x55, (byte)0x55, (byte)0x55,
		};
		for( int row = 0 ; row < 3 ; row ++ ) {
			for( int col = 0 ; col < 24 ; col ++ ) {
				TIFFT6Compressor compressor = new TIFFT6Compressor();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(baos);
				compressor.setStream(imageOutputStream);
				compressor.encode(input, 0, col, row, new int [] {1}, 3) ;
			}
		}
	}
	
	@Test
	public void testG4Compressor3() throws Exception
	{
		byte [] input = { 
				(byte)0xFF,
				(byte)0x00,
				(byte)0x55,
		};
		for( int row = 0 ; row < 3 ; row ++ ) {
			for( int col = 0 ; col < 8 ; col ++ ) {
				TIFFT6Compressor compressor = new TIFFT6Compressor();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(baos);
				compressor.setStream(imageOutputStream);
				compressor.encode(input, 0, col, row, new int [] {1}, 1) ;
			}
		}
	}
}
