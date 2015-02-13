package com.github.jaiimageio;

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
			if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("jpeg")) {
				// Avoid issue #6 on OpenJDK8/Debian 
				continue;
			}
			
			File f = File.createTempFile("imageio-test", "." + type);
			ImageIO.write(img, type, f);
			System.out.println(f);
			ImageIO.read(f);
		}
	}
	
}
