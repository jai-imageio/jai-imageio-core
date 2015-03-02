package com.github.jaiimageio;

import com.github.jaiimageio.plugins.pnm.PNMImageWriteParam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test PPM writing, ASCII and RAW.
 * @author peterhull90@gmail.com
 */
public class PnmWriteTest {

    private final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

    @Before
    public void randomize() {
    	Random r = new Random(1337);
    	for (int x=0; x<16; x++) {
    		for (int y=0; y<16; y++) {
    			image.setRGB(x, y, r.nextInt());
    		}
		}
    }
    
    
    @Test
    public void writeRaw() throws Exception {
        File f = File.createTempFile("test-raw", ".ppm");
        f.deleteOnExit();
        //System.out.println(f);
        write(f, true, image);
    }

    @Test
    public void writeAscii() throws Exception {
        File f = File.createTempFile("test-ascii", ".ppm");
        f.deleteOnExit();
        write(f, false, image);
    }

    private static void write(File f, boolean raw, BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("ppm");
        assertTrue(writers.hasNext());
        ImageWriter writer = writers.next();
        PNMImageWriteParam params = (PNMImageWriteParam) writer.getDefaultWriteParam();
        params.setRaw(raw);
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), params);
        writer.dispose();
        ios.close();
    }
}
