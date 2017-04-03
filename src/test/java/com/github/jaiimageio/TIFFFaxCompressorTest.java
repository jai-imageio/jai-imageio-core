/**
 * Adapted from https://java.net/jira/browse/JAI_IMAGEIO_CORE-194
 * 
 * Contributed by trejkaz
 * 
 */
package com.github.jaiimageio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.junit.Test;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFT6Compressor;

public class TIFFFaxCompressorTest {

    @Test
    public void testPngToTiff() throws Exception {
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/out7.png"));
        OutputStream out = new ByteArrayOutputStream();
        File f = File.createTempFile("test", ".tiff");
        out = new FileOutputStream(f);
        System.out.println(f);
        ImageOutputStream output = ImageIO.createImageOutputStream(out);
        ImageWriter writer = new TIFFImageWriterSpi().createWriterInstance();
        writer.setOutput(output);
        ImageWriteParam parameters = writer.getDefaultWriteParam();
        parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parameters.setCompressionType("CCITT T.6");
        IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(parameters);
        IIOMetadata imageMetadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image),
                parameters);
        writer.write(streamMetadata, new IIOImage(image, null, imageMetadata), parameters);
        writer.dispose();
    }

    @Test
    public void testTiffT6Compressor_64() throws Exception {
        byte[] uncompressed = { 85,85,85,85,85,85,85,85 };
        TIFFT6Compressor compressor = new TIFFT6Compressor();
        compressor.setStream(ImageIO.createImageOutputStream(new ByteArrayOutputStream())); //TODO verify results
        compressor.encode(uncompressed, 0, 64, 1, new int[] { 1 }, 8);
    }

    // fails:
    @Test
    public void testTiffT6Compressor_72() throws Exception {
        byte[] uncompressed = { 85,85,85,85,85,85,85,85,85 };
        TIFFT6Compressor compressor = new TIFFT6Compressor();
        compressor.setStream(ImageIO.createImageOutputStream(new ByteArrayOutputStream())); //TODO verify results
        compressor.encode(uncompressed, 0, 72, 1, new int[] { 1 }, 9);
    }
}