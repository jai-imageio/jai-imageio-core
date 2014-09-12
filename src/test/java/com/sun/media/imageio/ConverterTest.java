package com.sun.media.imageio;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ConverterTest {

	@Test
	public void testname() throws Exception {
		System.out.println(Arrays.asList(ImageIO.getReaderMIMETypes()));
		System.out.println(Arrays.asList(ImageIO.getWriterFormatNames()));
		System.out.println(Arrays.asList(ImageIO.getReaderFormatNames()));

		URL pngFile = getClass().getResource("/test.png");
		BufferedImage img = ImageIO.read(pngFile);

		for (String type : ImageIO.getWriterFormatNames()) {
			File f = File.createTempFile("imageio-test", "." + type);
			ImageIO.write(img, type, f);
			System.out.println(f);
			try {
				ImageIO.read(f);
			} catch (Exception ex) {
				System.err.println("Failed " + f  + " " + ex.getLocalizedMessage());
			}
		}
	}
	
}
