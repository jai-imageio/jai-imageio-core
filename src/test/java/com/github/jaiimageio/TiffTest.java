package com.github.jaiimageio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFT6Compressor;
import com.github.jaiimageio.plugins.tiff.TIFFImageReadParam;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class TiffTest {
	@Test
	public void testReadTiff() throws Exception {

		URL g4File = getClass().getResource("/checkerg4.tiff");
		BufferedImage bufferedImage = ImageIO.read(g4File);
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
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("tiff");
        assertTrue(writers.hasNext());
        TIFFImageWriter tiffImageWriter = (TIFFImageWriter)writers.next();
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
