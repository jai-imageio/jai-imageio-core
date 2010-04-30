/*
 * $RCSfile: PixelReplacementImage.java,v $
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
 * $Date: 2005/02/11 05:01:55 $
 * $State: Exp $
 */
package com.sun.media.jai.imageioimpl;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Vector;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.PropertyChangeEventJAI;
import javax.media.jai.RenderingChangeEvent;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

/**
 * Implementation of <code>PlanarImage</code> for the "ImageWrite" operation
 * for the case of <code>ImageWriter</code>s which can replace pixels.  The
 * sole purpose of this class is to respond to "invalidregion" events so
 * as to update the written image.
 */
final class PixelReplacementImage extends PlanarImage
    implements PropertyChangeListener {

    /**
     * The <code>ImageWriteParam</code> used in writing the image.
     */
    private ImageWriteParam param;

    /**
     * The <code>ImageWriter</code> used to write the image.
     */
    private ImageWriter writer;

    /**
     * The index of the image to be write.
     */
    private int imageIndex;

    /**
     * A stream to be closed when the instance is disposed; may be null.
     */
    private ImageOutputStream streamToClose;

    /**
     * Creates a <code>Vector</code> containing a single element.
     */
    private static Vector createVector(Object element) {
        Vector v = new Vector(1);
        v.add(element);
        return v;
    }

    /**
     * XXX
     */
    PixelReplacementImage(RenderedImage source,
                          Map configuration,
                          ImageWriteParam param,
                          ImageWriter writer,
                          int imageIndex,
                          ImageOutputStream streamToClose) throws IOException {
        super(new ImageLayout(source),   // Layout same as source.
              createVector(source),
              configuration);

        // Verify that the writer can replace pixels.
        if(!writer.canReplacePixels(imageIndex)) {
            throw new IllegalArgumentException
                ("!writer.canReplacePixels(imageIndex)");
        }

        // Set the instance variables from the parameters.
        // XXX Should ImageWriteParam original settings be cached for
        // testing later to see whether anything important has changed?
        this.param = param;
        this.writer = writer;
        this.imageIndex = imageIndex;
        this.streamToClose = streamToClose;
    }

    /**
     * Close an <code>ImageOutputStream</code> passed in.
     */
    public void dispose() {
        if(streamToClose != null) {
            try {
                streamToClose.close();
            } catch(IOException e) {
                // Ignore it.
            }
        }

        super.dispose();
    }

    /**
     * Gets a tile.
     *
     * @param tileX The X index of the tile.
     * @param tileY The Y index of the tile.
     */
    public Raster getTile(int tileX, int tileY) {
        return getSourceImage(0).getTile(tileX, tileY);
    }

    // --- PropertyChangeListener implementation ---

    // XXX Doc
    public void propertyChange(PropertyChangeEvent evt) {
        PlanarImage source = getSourceImage(0);
        Object eventSource = evt.getSource();

        //
        // Process the event if the writer can replace pixels,
        // the event source is the source of this OpImage,
        // and the event name is "invalidregion".
        //
        if((evt instanceof PropertyChangeEventJAI &&
            evt.getPropertyName().equalsIgnoreCase("invalidregion") &&
            eventSource.equals(source)) ||
           (evt instanceof RenderingChangeEvent &&
            evt.getOldValue().equals(source) &&
            eventSource instanceof RenderedOp &&
            evt.getNewValue().equals(((RenderedOp)eventSource).getRendering()))) {

            // Get the invalid region information.
            Shape srcInvalidRegion = null;

            if(evt instanceof RenderingChangeEvent) {
                // RenderingChangeEvent presumably from a source RenderedOp.
                RenderingChangeEvent rcEvent = (RenderingChangeEvent)evt;

                // Get the invalidated region of the source.
                srcInvalidRegion = rcEvent.getInvalidRegion();

                // Reset this image's source.
                source = (PlanarImage)evt.getNewValue();
                setSource(source, 0);

                // If entire source is invalid replace with source bounds.
                if(srcInvalidRegion == null) {
                    srcInvalidRegion =
                        ((PlanarImage)rcEvent.getOldValue()).getBounds();
                }
            } else {
                // Get the invalidated region of the source.
                Object evtNewValue = (Shape)evt.getNewValue();

                // Continue if the value class is correct.
                if(evtNewValue instanceof Shape) {
                    srcInvalidRegion = (Shape)evtNewValue;

                    // If entire source is invalid replace with source bounds.
                    if(srcInvalidRegion == null) {
                        srcInvalidRegion = source.getBounds();
                    }
                }
            }

            // Return if the invalid portion could not be determined.
            if(srcInvalidRegion == null) {
                return;
            }

            // Return if the invalid region does not overlap the param region.
            if(param != null) {
                Rectangle sourceRegion = param.getSourceRegion();
                if(sourceRegion != null &&
                   !srcInvalidRegion.intersects(sourceRegion)) {
                    return;
                }
            } else {
                param = writer.getDefaultWriteParam();
            }

            // Get indices of all tiles overlapping the invalid region.
            Point[] tileIndices =
                source.getTileIndices(srcInvalidRegion.getBounds());

            // Should not happen but return if tileIndices is null.
            if(tileIndices == null) return;

            // Get subsampling values.
            int gridX = minX + param.getSubsamplingXOffset();
            int gridY = minY + param.getSubsamplingYOffset();
            int stepX = param.getSourceXSubsampling();
            int stepY = param.getSourceYSubsampling();
            boolean isSubsampling =
                stepX != 1 || stepY != 1 || gridX != minX || gridY != minY;
  
             // Loop over affected tiles.
            int numTiles = tileIndices.length;
            for(int i = 0; i < numTiles; i++) {
                // Save the next tile index.
                Point tileIndex = tileIndices[i];
  
                 // Compute tile bounds.
                Rectangle tileRect =
                    source.getTileRect(tileIndex.x, tileIndex.y);
  
                 // Replace if bounds intersect invalid region.
                if(srcInvalidRegion.intersects(tileRect)) {
                    // Get the source tile.
                    Raster raster = source.getTile(tileIndex.x, tileIndex.y);
 
                    Rectangle destRect;
                    if(isSubsampling) {
                        int destMinX =
                            (tileRect.x - gridX + stepX - 1)/stepX;
                        int destMinY =
                            (tileRect.y - gridY + stepY - 1)/stepY;
                        int destMaxX =
                            (tileRect.x + tileRect.width -
                             gridX + stepX - 1)/stepX;
                        int destMaxY =
                            (tileRect.y + tileRect.height -
                             gridY + stepY - 1)/stepY;
                        destRect = new Rectangle(destMinX, destMinY,
                                                 destMaxX - destMinX,
                                                 destMaxY - destMinY);
                    } else {
                        destRect = tileRect;
                    }

                    // Replace the pixels.
                    try {
                        synchronized(writer) {
                            writer.prepareReplacePixels(imageIndex, destRect);
                            param.setDestinationOffset(destRect.getLocation());
                            writer.replacePixels(raster, param);
                            writer.endReplacePixels();
                        }
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
