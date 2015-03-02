/*
 * $RCSfile: CLibImageWriter.java,v $
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
 * $Revision: 1.6 $
 * $Date: 2007/02/06 22:14:59 $
 * $State: Exp $
 */
package com.github.jaiimageio.impl.plugins.clib;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;

public abstract class CLibImageWriter extends ImageWriter {
    /**
     * Returns the data array from the <code>DataBuffer</code>.
     */
    private static final Object getDataBufferData(DataBuffer db) {
        Object data;

        int dType = db.getDataType();
        switch (dType) {
        case DataBuffer.TYPE_BYTE:
            data = ((DataBufferByte)db).getData();
            break;
        case DataBuffer.TYPE_USHORT:
            data = ((DataBufferUShort)db).getData();
            break;
        default:
            throw new IllegalArgumentException
                (I18N.getString("Generic0")+" "+dType);
        }

        return data;
    }

    /**
     * Returns a contiguous <code>Raster</code> of data over the specified
     * <code>Rectangle</code>. If the region is a sub-region of a single
     * tile, then a child of that tile will be returned. If the region
     * overlaps more than one tile and has 8 bits per sample, then a
     * pixel interleaved Raster having band offsets 0,1,... will be returned.
     * Otherwise the Raster returned by <code>im.copyData(null)</code> will
     * be returned.
     */
    private static final Raster getContiguousData(RenderedImage im,
                                                  Rectangle region) {
        if(im == null) {
            throw new IllegalArgumentException("im == null");
        } else if(region == null) {
            throw new IllegalArgumentException("region == null");
        }

        Raster raster;
        if(im.getNumXTiles() == 1 && im.getNumYTiles() == 1) {
            // Image is not tiled so just get a reference to the tile.
            raster = im.getTile(im.getMinTileX(), im.getMinTileY());

            // Ensure result has requested coverage.
            Rectangle bounds = raster.getBounds();
            if (!bounds.equals(region)) {
                raster = raster.createChild(region.x, region.y,
                                            region.width, region.height,
                                            region.x, region.y,
                                            null);
            }
        } else {
            // Image is tiled.

            // Create an interleaved raster for copying for 8-bit case.
            // This ensures that for RGB data the band offsets are {0,1,2}.
            SampleModel sampleModel = im.getSampleModel();
            WritableRaster target = sampleModel.getSampleSize(0) == 8 ?
                Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                               im.getWidth(),
                                               im.getHeight(),
                                               sampleModel.getNumBands(),
                                               new Point(im.getMinX(),
                                                         im.getMinY())) :
                null;

            // Copy the data.
            raster = im.copyData(target);
        }

        return raster;
    }

    /**
     * Subsamples and sub-bands the input <code>Raster</code> over a
     * sub-region and stores the result in a <code>WritableRaster</code>.
     *
     * @param src The source <code>Raster</code>
     * @param sourceBands The source bands to use; may be <code>null</code>
     * @param subsampleX The subsampling factor along the horizontal axis.
     * @param subsampleY The subsampling factor along the vertical axis.
     * in which case all bands will be used.
     * @param dst The destination <code>WritableRaster</code>.
     * @throws IllegalArgumentException if <code>source</code> is
     * <code>null</code> or empty, <code>dst</code> is <code>null</code>,
     * <code>sourceBands.length</code> exceeds the number of bands in
     * <code>source</code>, or <code>sourcBands</code> contains an element
     * which is negative or greater than or equal to the number of bands
     * in <code>source</code>.
     */
    private static void reformat(Raster source,
                                 int[] sourceBands,
                                 int subsampleX,
                                 int subsampleY,
                                 WritableRaster dst) {
        // Check for nulls.
        if(source == null) {
            throw new IllegalArgumentException("source == null!");
        } else if(dst == null) {
            throw new IllegalArgumentException("dst == null!");
        }

        // Validate the source bounds. XXX is this needed?
        Rectangle sourceBounds = source.getBounds();
        if(sourceBounds.isEmpty()) {
            throw new IllegalArgumentException
                ("source.getBounds().isEmpty()!");
        }

        // Check sub-banding.
        boolean isSubBanding = false;
        int numSourceBands = source.getSampleModel().getNumBands();
        if(sourceBands != null) {
            if(sourceBands.length > numSourceBands) {
                throw new IllegalArgumentException
                    ("sourceBands.length > numSourceBands!");
            }

            boolean isRamp = sourceBands.length == numSourceBands;
            for(int i = 0; i < sourceBands.length; i++) {
                if(sourceBands[i] < 0 || sourceBands[i] >= numSourceBands) {
                    throw new IllegalArgumentException
                        ("sourceBands[i] < 0 || sourceBands[i] >= numSourceBands!");
                } else if(sourceBands[i] != i) {
                    isRamp = false;
                }
            }

            isSubBanding = !isRamp;
        }

        // Allocate buffer for a single source row.
        int sourceWidth = sourceBounds.width;
        int[] pixels = new int[sourceWidth*numSourceBands];

        // Initialize variables used in loop.
        int sourceX = sourceBounds.x;
        int sourceY = sourceBounds.y;
        int numBands = sourceBands != null ?
            sourceBands.length : numSourceBands;
        int dstWidth = dst.getWidth();
        int dstYMax = dst.getHeight() - 1;
        int copyFromIncrement = numSourceBands*subsampleX;

        // Loop over source rows, subsample each, and store in destination.
        for(int dstY = 0; dstY <= dstYMax; dstY++) {
            // Read one row.
            source.getPixels(sourceX, sourceY, sourceWidth, 1, pixels);

            // Copy within the same buffer by left shifting.
            if(isSubBanding) {
                int copyFrom = 0;
                int copyTo = 0;
                for(int i = 0; i < dstWidth; i++) {
                    for(int j = 0; j < numBands; j++) {
                        pixels[copyTo++] = pixels[copyFrom + sourceBands[j]];
                    }
                    copyFrom += copyFromIncrement;
                }
            } else {
                int copyFrom = copyFromIncrement;
                int copyTo = numSourceBands;
                // Start from index 1 as no need to copy the first pixel.
                for(int i = 1; i < dstWidth; i++) {
                    int k = copyFrom;
                    for(int j = 0; j < numSourceBands; j++) {
                        pixels[copyTo++] = pixels[k++];
                    }
                    copyFrom += copyFromIncrement;
                }
            }

            // Set the destionation row.
            dst.setPixels(0, dstY, dstWidth, 1, pixels);

            // Increment the source row.
            sourceY += subsampleY;
        }
    }

    protected CLibImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    public IIOMetadata convertImageMetadata(IIOMetadata inData,
                                            ImageTypeSpecifier imageType,
                                            ImageWriteParam param) {
        return null;
    }

    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
                                             ImageWriteParam param) {
        return null;
    }

    public IIOMetadata
        getDefaultImageMetadata(ImageTypeSpecifier imageType,
                                ImageWriteParam param) {
        return null;
    }

    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    /* XXX
    protected int getSignificantBits(RenderedImage image) {
        SampleModel sampleModel = image.getSampleModel();
        int numBands = sampleModel.getNumBands();
        int[] sampleSize = sampleModel.getSampleSize();
        int significantBits = sampleSize[0];
        for(int i = 1; i < numBands; i++) {
            significantBits = Math.max(significantBits, sampleSize[i]);
        }

        return significantBits;
    }
    */

    // Code copied from ImageReader.java with ImageReadParam replaced
    // by ImageWriteParam.
    private static final Rectangle getSourceRegion(ImageWriteParam param,
                                                   int sourceMinX,
                                                   int sourceMinY,
                                                   int srcWidth,
                                                   int srcHeight) {
        Rectangle sourceRegion =
            new Rectangle(sourceMinX, sourceMinY, srcWidth, srcHeight);
        if (param != null) {
            Rectangle region = param.getSourceRegion();
            if (region != null) {
                sourceRegion = sourceRegion.intersection(region);
            }

            int subsampleXOffset = param.getSubsamplingXOffset();
            int subsampleYOffset = param.getSubsamplingYOffset();
            sourceRegion.x += subsampleXOffset;
            sourceRegion.y += subsampleYOffset;
            sourceRegion.width -= subsampleXOffset;
            sourceRegion.height -= subsampleYOffset;
        }

        return sourceRegion;
    }
}
