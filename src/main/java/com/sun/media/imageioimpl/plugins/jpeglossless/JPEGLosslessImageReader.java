package com.sun.media.imageioimpl.plugins.jpeglossless;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class JPEGLosslessImageReader extends ImageReader {
    private ImageInputStream iis;
    private BufferedImage image;

    public JPEGLosslessImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
    

    /** Overrides the method defined in the superclass. */
    @Override
    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        iis = (ImageInputStream) input;
        if (iis != null) {
            iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            //iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        }
    }


    @Override
    public int getWidth(int imageIndex) throws IOException {
        return read(imageIndex, null).getWidth();
    }

    
    @Override
    public int getHeight(int imageIndex) throws IOException {
        return read(imageIndex, null).getHeight();
    }

    
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    
    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        return null;
    }

    
    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }
    

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }
    

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        
        if (image != null) {
            return image;
        }
        
        LosslessJPEGCodec codec = new LosslessJPEGCodec();
        CodecOptions options = CodecOptions.getDefaultOptions();
        //options.signed = true;
        options.littleEndian = true;
        
        image = codec.decompress(iis, options);
        return image;
    }


    @Override
    public boolean canReadRaster() {
        return true;
    }


    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        return read(imageIndex, param).getRaster();
    }
}
