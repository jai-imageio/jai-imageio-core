/*
 * $RCSfile: CLibImageReader.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.11 $
 * $Date: 2006/02/28 01:33:31 $
 * $State: Exp $
 */
package com.github.jaiimageio.impl.plugins.clib;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
//import com.sun.medialib.codec.jiio.Constants;
//import com.sun.medialib.codec.jiio.mediaLibImage;

// XXX Need to verify compliance of all methods with ImageReader specificaiton.
public abstract class CLibImageReader extends ImageReader {
    // The current image index.
    private int currIndex = -1;

    // The position of the byte after the last byte read so far.
    private long highWaterMark = Long.MIN_VALUE;

    // An <code>ArrayList</code> of <code>Long</code>s indicating the stream
    // positions of the start of each image. Entries are added as needed.
    private ArrayList imageStartPosition = new ArrayList();

    // The number of images in the stream, if known, otherwise -1.
    private int numImages = -1;

//    // The image returned by the codecLib Decoder.
//    private mediaLibImage mlibImage = null;

    // The index of the cached image.
    private int mlibImageIndex = -1;

    /**
     * Returns true if and only if both arguments are null or
     * both are non-null and have the same length and content.
     */
    private static boolean subBandsMatch(int[] sourceBands,
                                         int[] destinationBands) {
        if(sourceBands == null && destinationBands == null) {
            return true;
        } else if(sourceBands != null && destinationBands != null) {
            if (sourceBands.length != destinationBands.length) {
                // Shouldn't happen ...
                return false;
            }
            for (int i = 0; i < sourceBands.length; i++) {
                if (sourceBands[i] != destinationBands[i]) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private static final void subsample(Raster src, int subX, int subY,
                                        WritableRaster dst) {
        int sx0 = src.getMinX();
        int sy0 = src.getMinY();
        int sw = src.getWidth();
        int syUB = sy0 + src.getHeight();

        int dx0 = dst.getMinX();
        int dy0 = dst.getMinY();
        int dw = dst.getWidth();

        int b = src.getSampleModel().getNumBands();
        int t = src.getSampleModel().getDataType();

        int numSubSamples = (sw + subX - 1)/subX;

        if(t == DataBuffer.TYPE_FLOAT || t == DataBuffer.TYPE_DOUBLE) {
            float[] fsamples = new float[sw];
            float[] fsubsamples = new float[numSubSamples];

            for(int k = 0; k < b; k++) {
                for(int sy = sy0, dy = dy0; sy < syUB; sy += subY, dy++) {
                    src.getSamples(sx0, sy, sw, 1, k, fsamples);
                    for(int i = 0, s = 0; i < sw; s++, i += subX) {
                        fsubsamples[s] = fsamples[i];
                    }
                    dst.setSamples(dx0, dy, dw, 1, k, fsubsamples);
                }
            }
        } else {
            int[] samples = new int[sw];
            int[] subsamples = new int[numSubSamples];

            for(int k = 0; k < b; k++) {
                for(int sy = sy0, dy = dy0; sy < syUB; sy += subY, dy++) {
                    src.getSamples(sx0, sy, sw, 1, k, samples);
                    for(int i = 0, s = 0; i < sw; s++, i += subX) {
                        subsamples[s] = samples[i];
                    }
                    dst.setSamples(dx0, dy, dw, 1, k, subsamples);
                }
            }
        }
    }                                 

    protected CLibImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * An <code>Iterator</code> over a single element.
     */
    private class SoloIterator implements Iterator {
        Object theObject;

        SoloIterator(Object o) {
            if(o == null) {
                new IllegalArgumentException
                    (I18N.getString("CLibImageReader0"));
            }
            theObject = o;
        }

        public boolean hasNext() {
            return theObject != null;
        }

        public Object next() {
            if(theObject == null) {
                throw new NoSuchElementException();
            }
            Object theNextObject = theObject;
            theObject = null;
            return theNextObject;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    


    /**
     * Returns the index of the image cached in the private
     * <code>mlibImage</code> instance variable or -1 if no
     * image is currently cached.
     */
    protected int getImageIndex() {
        return mlibImageIndex;
    }


    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }
}
