
package com.sun.media.imageioimpl.plugins.jpegls;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.imageioimpl.common.PackageUtil;

public class JPEGLSImageReaderSpi extends ImageReaderSpi {

    private static String [] writerSpiNames = {"com.sun.media.imageioimpl.plugins.bmp.JPEGLSImageWriterSpi"};
    private static String[] formatNames = {"jls", "JLS"};
    private static String[] extensions = {"jls"};
    private static String[] mimeTypes = {"image/jpeg-ls", "image/jls"};
    private boolean registered = false;

    public JPEGLSImageReaderSpi() {
        super("Vendor Name",
              "Version",
              formatNames,
              extensions,
              mimeTypes,
              "com.sun.media.imageioimpl.plugins.jpegls.JPEGLSImageReader",
              STANDARD_INPUT_TYPE,
              writerSpiNames,
              false,
              null, null, null, null,
              true,
              "com_sun_media_imageio_plugins_jpegls_image_1.0",
              "com.sun.media.imageioimpl.plugins.jpegls.JPEGLSMetadataFormat",
              null, null);
    }

    public void onRegistration(ServiceRegistry registry,
                               Class category) {
        if (registered) {
            return;
        }
        registered = true;

	// By JDK 1.7, the BMPImageReader will have been in JDK core for 
	// atleast two FCS releases, so we can set JIIO's to lower priority
	// With JDK 1.8, we can entirely de-register the JIIO one
	ImageUtil.processOnRegistration(registry, category, "BMP", this,
					8, 7); // JDK version 1.8, 1.7
    }

    public String getDescription(Locale locale) {
	String desc = PackageUtil.getSpecificationTitle() + 
	    " JPEG-LS Image Reader";  
	return desc;
    }

    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }

        ImageInputStream stream = (ImageInputStream)source;
        byte[] b = new byte[2];
        stream.mark();
        stream.readFully(b);
        stream.reset();

        return (b[0] == 0x42) && (b[1] == 0x4d);
    }

    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new JPEGLSImageReader(this);
    }
}

