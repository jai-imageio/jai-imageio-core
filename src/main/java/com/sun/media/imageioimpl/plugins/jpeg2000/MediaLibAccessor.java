/*
 * $RCSfile: MediaLibAccessor.java,v $
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
 * $Revision: 1.1 $
 * $Date: 2005/02/11 05:01:36 $
 * $State: Exp $
 */
package com.sun.media.imageioimpl.plugins.jpeg2000;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.IOException;
import java.lang.NoClassDefFoundError;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.media.imageioimpl.common.ImageUtil;
/**
 *  An adapter class for presenting image data in a mediaLibImage
 *  format, even if the data isn't stored that way.  MediaLibAccessor
 *  is meant to make the common case (ComponentRasters) and allow
 *  them to be accelerated via medialib.  Note that unlike RasterAccessor,
 *  MediaLibAccessor does not work with all cases.  In the event that
 *  MediaLibAccessor can not deal with a give collection of Rasters,
 *  findCompatibleTag will return the value MediaLibAccessor.TAG_INCOMPATIBLE.
 *  OpImages that use MediaLibAccessor should be paired with RIF's
 *  which check that findCompatibleTag returns a valid tag before
 *  actually constructing the Mlib OpImage.
 */

public class MediaLibAccessor {
    /**
     *  Value indicating how far COPY_MASK info is shifted to avoid
     *  interfering with the data type info
     */
    private static final int COPY_MASK_SHIFT = 7;

    /* Value indicating how many bits the COPY_MASK is */
    private static final int COPY_MASK_SIZE = 1;

    /** The bits of a FormatTag associated with how dataArrays are obtained. */
    public static final int COPY_MASK = 0x1 << COPY_MASK_SHIFT;

    /** Flag indicating data is raster's data. */
    public static final int UNCOPIED = 0x0 << COPY_MASK_SHIFT;

    /** Flag indicating data is a copy of the raster's data. */
    public static final int COPIED = 0x01 << COPY_MASK_SHIFT;

    /** The bits of a FormatTag associated with pixel datatype. */
    public static final int DATATYPE_MASK = (0x1 << COPY_MASK_SHIFT) - 1;

    /**
     * Value indicating how far BINARY_MASK info is shifted to avoid
     * interfering with the data type and copying info.
     */
    private static final int BINARY_MASK_SHIFT =
        COPY_MASK_SHIFT+COPY_MASK_SIZE;

    /** Value indicating how many bits the BINARY_MASK is */
    private static final int BINARY_MASK_SIZE = 1;

    /** The bits of a FormatTag associated with binary data. */
    public static final int BINARY_MASK =
        ((1 << BINARY_MASK_SIZE) - 1) << BINARY_MASK_SHIFT;

    /** Flag indicating data are not binary. */
    public static final int NONBINARY = 0x0 << BINARY_MASK_SHIFT;

    /** Flag indicating data are binary. */
    public static final int BINARY = 0x1 << BINARY_MASK_SHIFT;

    /** FormatTag indicating data in byte arrays and uncopied. */
    public static final int
        TAG_BYTE_UNCOPIED = DataBuffer.TYPE_BYTE | UNCOPIED;

    /** FormatTag indicating data in unsigned short arrays and uncopied. */
    public static final int
        TAG_USHORT_UNCOPIED = DataBuffer.TYPE_USHORT | UNCOPIED;

    /** FormatTag indicating data in short arrays and uncopied. */
    public static final int
        TAG_SHORT_UNCOPIED = DataBuffer.TYPE_SHORT | UNCOPIED;

    /** FormatTag indicating data in integer arrays and uncopied. */
    public static final int
        TAG_INT_UNCOPIED = DataBuffer.TYPE_INT | UNCOPIED;

    /** FormatTag indicating data in float arrays and uncopied. */
    public static final int
        TAG_FLOAT_UNCOPIED = DataBuffer.TYPE_FLOAT | UNCOPIED;

    /** FormatTag indicating data in double arrays and uncopied. */
    public static final int
        TAG_DOUBLE_UNCOPIED = DataBuffer.TYPE_DOUBLE | UNCOPIED;

    /** FormatTag indicating data in byte arrays and uncopied. */
    public static final int
        TAG_BYTE_COPIED = DataBuffer.TYPE_BYTE | COPIED;

    /** FormatTag indicating data in unsigned short arrays and copied. */
    public static final int
        TAG_USHORT_COPIED = DataBuffer.TYPE_USHORT | COPIED;

    /** FormatTag indicating data in short arrays and copied. */
    public static final int
        TAG_SHORT_COPIED = DataBuffer.TYPE_SHORT | COPIED;

    /** FormatTag indicating data in short arrays and copied. */
    public static final int
        TAG_INT_COPIED = DataBuffer.TYPE_INT | COPIED;

    /** FormatTag indicating data in float arrays and copied. */
    public static final int
        TAG_FLOAT_COPIED = DataBuffer.TYPE_FLOAT | COPIED;

    /** FormatTag indicating data in double arrays and copied. */
    public static final int
        TAG_DOUBLE_COPIED = DataBuffer.TYPE_DOUBLE | COPIED;

    /** The raster that is the source of pixel data. */
    protected Raster raster;

    /** The rectangle of the raster that MediaLibAccessor addresses. */
    protected Rectangle rect;

    /** The number of bands per pixel in the data array. */
    protected int numBands;

    /** The offsets of each band in the src image */
    protected int bandOffsets[];

    /** Tag indicating the data type of the data and whether its copied */
    protected int formatTag;


    /**
     * Whether packed data are preferred when processing binary images.
     * This tag is ignored if the data are not binary.
     */
    private boolean areBinaryDataPacked = false;

    /**
     *  Returns the most efficient FormatTag that is compatible with
     *  the destination raster and all source rasters.
     *
     *  @param srcs the source <code>Raster</code>; may be <code>null</code>.
     *  @param dst  the destination <code>Raster</code>.
     */
    public static int findCompatibleTag(Raster src) {
        SampleModel dstSM = src.getSampleModel();
        int dstDT = dstSM.getDataType();

        int defaultDataType = dstSM.getDataType();

        boolean allComponentSampleModel =
             dstSM instanceof ComponentSampleModel;
        boolean allBinary = ImageUtil.isBinary(dstSM);

        if(allBinary) {
            // The copy flag is not set until the mediaLibImage is
            // created as knowing this information requires too much
            // processing to determine here.
            return DataBuffer.TYPE_BYTE | BINARY;
        }

        if (!allComponentSampleModel) {
            if ((defaultDataType == DataBuffer.TYPE_BYTE) ||
                (defaultDataType == DataBuffer.TYPE_USHORT) ||
                (defaultDataType == DataBuffer.TYPE_SHORT)) {
                defaultDataType = DataBuffer.TYPE_INT;
            }
        }

        int tag = defaultDataType | COPIED;

        if (!allComponentSampleModel) {
            return tag;
        }

        if (isPixelSequential(dstSM))
            return dstDT | UNCOPIED;
        return tag;
    }

    /**
     *  Determines if the SampleModel stores data in a way that can
     *  be represented by a mediaLibImage without copying
     */
    public static boolean isPixelSequential(SampleModel sm) {
        ComponentSampleModel csm = null;
        if (sm instanceof ComponentSampleModel) {
            csm = (ComponentSampleModel)sm;
        } else {
            return false;
        }
        int pixelStride = csm.getPixelStride();
        int bandOffsets[] = csm.getBandOffsets();
        int bankIndices[] = csm.getBankIndices();
        if (pixelStride != bandOffsets.length) {
            return false;
        }

        //XXX: for band-selection result
        if (pixelStride != sm.getNumBands())
            return false;

        for (int i = 0; i < bandOffsets.length; i++) {
            if (bandOffsets[i] >= pixelStride ||
                bankIndices[i] != bankIndices[0]) {
                return false;
            }
            for (int j = i+1; j < bandOffsets.length; j++) {
               if (bandOffsets[i] == bandOffsets[j]) {
                   return false;
               }

               //XXX: for BGR images
               if (bandOffsets[i] != i)
                  return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the <code>MediaLibAccessor</code>
     * represents binary data.
     */
    public boolean isBinary() {
        return ((formatTag & BINARY_MASK) == BINARY);
    }

    /**
     *  Returns the data type of the RasterAccessor object. Note that
     *  this datatype is not necessarily the same data type as the
     *  underlying raster.
     */
    public int getDataType() {
        return formatTag & DATATYPE_MASK;
    }

    /**
     *  Returns true if the MediaLibAccessors's data is copied from it's
     *  raster.
     */
    public boolean isDataCopy() {
        return ((formatTag & COPY_MASK) == COPIED);
    }

    /** Returns the bandOffsets. */
    public int[] getBandOffsets() {
        return bandOffsets;
    }

    /**
     *  Returns parameters in the appropriate order if MediaLibAccessor
     *  has reordered the bands or is attempting to make a
     *  BandSequential image look like multiple PixelSequentialImages
     */
    public int[] getIntParameters(int band, int params[]) {
        int returnParams[] = new int[numBands];
        for (int i = 0; i < numBands; i++) {
            returnParams[i] = params[bandOffsets[i+band]];
        }
        return returnParams;
    }

    /**
     *  Returns parameters in the appropriate order if MediaLibAccessor
     *  has reordered the bands or is attempting to make a
     *  BandSequential image look like multiple PixelSequentialImages
     */
    public int[][] getIntArrayParameters(int band, int[][] params) {
        int returnParams[][] = new int[numBands][];
        for (int i = 0; i < numBands; i++) {
            returnParams[i] = params[bandOffsets[i+band]];
        }
        return returnParams;
    }

    /**
     *  Returns parameters in the appropriate order if MediaLibAccessor
     *  has reordered the bands or is attempting to make a
     *  BandSequential image look like multiple PixelSequentialImages
     */
    public double[] getDoubleParameters(int band, double params[]) {
        double returnParams[] = new double[numBands];
        for (int i = 0; i < numBands; i++) {
            returnParams[i] = params[bandOffsets[i+band]];
        }
        return returnParams;
    }



    private int[] toIntArray(double vals[]) {
        int returnVals[] = new int[vals.length];
        for (int i = 0; i < vals.length; i++) {
            returnVals[i] = (int)vals[i];
        }
        return returnVals;
    }

    private float[] toFloatArray(double vals[]) {
        float returnVals[] = new float[vals.length];
        for (int i = 0; i < vals.length; i++) {
            returnVals[i] = (float)vals[i];
        }
        return returnVals;
    }

}

