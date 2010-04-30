/*
 * $RCSfile: ImageWriteCRIF.java,v $
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

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.CRIFImpl;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.WritablePropertySource;
import com.sun.media.jai.operator.ImageWriteDescriptor;

public final class ImageWriteCRIF extends CRIFImpl {
    public static void main(String[] args) throws Throwable {
        java.io.File inFile = new java.io.File(args[0]);
        java.io.File outFile = new java.io.File(args[1]);
        String format = args.length > 2 ? args[2] : "png";
        String mode = args.length > 3 ? args[3] : "rendered";

        int imageIndex = 0;

        javax.imageio.stream.ImageInputStream inStream =
            javax.imageio.ImageIO.createImageInputStream(inFile);

        java.util.Iterator iter =
            javax.imageio.ImageIO.getImageReaders(inStream);
        javax.imageio.ImageReader reader =
            (javax.imageio.ImageReader)iter.next();

        reader.setInput(inStream);

        RenderedImage image = reader.read(imageIndex);

        javax.imageio.metadata.IIOMetadata streamMetadata =
            reader.getStreamMetadata();
        javax.imageio.metadata.IIOMetadata imageMetadata =
            reader.getImageMetadata(imageIndex);

        java.awt.image.BufferedImage[] thumbnails = null;
        if(reader.hasThumbnails(imageIndex)) {
            int numThumbnails = reader.getNumThumbnails(imageIndex);
            thumbnails = new java.awt.image.BufferedImage[numThumbnails];
            for(int i = 0; i < numThumbnails; i++) {
                thumbnails[i] = reader.readThumbnail(imageIndex, i);
            }
        }

        ImageWriteCRIF crif = new ImageWriteCRIF();

        ParameterBlock pb = new ParameterBlock();

        if(mode.equalsIgnoreCase("rendered")) {
            pb.addSource(image);
        } else if(mode.equalsIgnoreCase("renderable")) {
            ParameterBlock renderablePB = new ParameterBlock();
            renderablePB.addSource(image);
            RenderableImage renderable =
                javax.media.jai.JAI.createRenderable("renderable",
                                                     renderablePB);
            pb.addSource(renderable);
        }

        pb.add(outFile); // Output
        pb.add(format); // Format

        pb.add(Boolean.TRUE); // UseProperties
        pb.add(Boolean.TRUE); // Transcode
        pb.add(Boolean.TRUE); // VerifyOutput
        pb.add(Boolean.TRUE); // AllowPixelReplacement

        pb.add(null); // TileSize

        pb.add(streamMetadata);
        pb.add(imageMetadata);
        pb.add(thumbnails);

        pb.add(null); // EventListener[]
        pb.add(null); // Locale

        pb.add(null); // ImageWriteParam
        pb.add(null); // ImageWriter

        if(mode.equalsIgnoreCase("rendered")) {
            crif.create(pb, null);
        } else if(mode.equalsIgnoreCase("renderable")) {
            java.awt.geom.AffineTransform transform =
                new java.awt.geom.AffineTransform(256, 0, 0, 512, 0, 0);
            crif.create(new RenderContext(transform), pb);
        }
    }

    public ImageWriteCRIF() {
        super();
    }

    /**
     * Attempt to create an {@link ImageOutputStream} for the supplied
     * output. The following sequence is effected:
     * <ol>
     * <li><ul>
     * <li>If <code>output</code> is an <code>ImageOutputStream</code> it
     * is cast and returned.</li>
     * <li>If <code>output</code> is a <code>String</code> it is converted
     * to a read-write <code>RandomAccessFile</code>.</li>
     * <li>If <code>output</code> is a <code>Socket</code> it is converted
     * to an <code>OutputStream</code>.</li>
     * </ul></li>
     * <li><code>ImageIO.createImageOutputStream()</code> is invoked
     * with parameter set to the (possibly converted) output and the
     * value it returns (which could be <code>null</code>) is returned
     * to the caller.</li>
     * </ol>
     *
     * @param output An <code>Object</code> to be used as the destination,
     * such as a <code>String</code>, <code>File</code>, writable
     * <code>RandomAccessFile</code>, <code>OutputStream</code>, writable
     * <code>Socket</code>, or writable <code>Channel</code>.
     *
     * @return An <code>ImageOutputStream</code> or <code>null</code>.
     */
    static ImageOutputStream getImageOutputStream(Object output) {
        // The value to be returned.
        ImageOutputStream stream = null;

        // If already an ImageOutputStream just cast.
        if(output instanceof ImageOutputStream) {
            stream = (ImageOutputStream)output;
        } else {
            if(output instanceof String) {
                // If output is a String replace it with a RandomAccessFile.
                try {
                    // 'output' is conditionally checked for writability
                    // in the OperationDescriptor.
                    output = new RandomAccessFile((String)output, "rw");
                } catch(Exception e) {
                    throw new RuntimeException
                        (I18N.getString("ImageWriteCRIF0")+" "+output);
                }
            } else if(output instanceof Socket) {
                // If output is a Socket replace it with an OutputStream.
                try {
                    // XXX check binding, connection, closed, shutdown
                    // as these could have changed.
                    output = ((Socket)output).getOutputStream();
                } catch(Exception e) {
                    throw new RuntimeException
                        (I18N.getString("ImageWriteCRIF1")+" "+output);
                }
            }

            // Create the ImageOutputStream.
            try {
                stream = ImageIO.createImageOutputStream(output);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        return stream;
    }

    /**
     * {@link RenderedImageFactory} implementation.
     */
    public RenderedImage create(ParameterBlock pb,
                                RenderingHints rh) {
        return create(0, false, pb, rh);
    }

    private static ImageWriteParam getWriteParam(ImageWriteParam param,
                                                 ImageWriter writer) {
        // Set default to original ImageWriteParam.
        ImageWriteParam newParam = param;

        if(param == null) {
            newParam = writer.getDefaultWriteParam();
        } else if(param.getClass().getName().equals(
                      "javax.imageio.ImageWriteParam")) {
            // The ImageWriteParam passed in is non-null. As the
            // ImageWriteParam class is not Cloneable, if the param
            // class is simply ImageWriteParam, then create a new
            // ImageWriteParam instance and set all its fields
            // which were set in param. This will eliminate problems
            // with concurrent modification of param for the cases
            // in which there is not a special ImageWriteParam used.

            // Create a new ImageWriteParam instance.
            newParam = writer.getDefaultWriteParam();

            // Set all fields which need to be set.

            // IIOParamController field.
            if(param.hasController()) {
                newParam.setController(param.getController());
            }

            // Destination fields.
            newParam.setDestinationOffset(param.getDestinationOffset());
            newParam.setDestinationType(param.getDestinationType());

            // Source fields.
            newParam.setSourceBands(param.getSourceBands());
            newParam.setSourceRegion(param.getSourceRegion());
            newParam.setSourceSubsampling(param.getSourceXSubsampling(),
                                          param.getSourceYSubsampling(),
                                          param.getSubsamplingXOffset(),
                                          param.getSubsamplingYOffset());

            // Compression.
            if(param.canWriteCompressed()) {
                int compressionMode = param.getCompressionMode();
                newParam.setCompressionMode(compressionMode);
                if(compressionMode == ImageWriteParam.MODE_EXPLICIT) {
                    newParam.setCompressionQuality(param.getCompressionQuality());
                    newParam.setCompressionType(param.getCompressionType());
                }
            }

            // Progressive
            if(param.canWriteProgressive()) {
                newParam.setProgressiveMode(param.getProgressiveMode());
            }

            // Tiling
            if(param.canWriteTiles()) {
                int tilingMode = param.getTilingMode();
                newParam.setTilingMode(tilingMode);
                if(tilingMode == ImageWriteParam.MODE_EXPLICIT) {
                    newParam.setTiling(param.getTileWidth(),
                                       param.getTileHeight(),
                                       param.getTileGridXOffset(),
                                       param.getTileGridYOffset());
                }
            }
        }

        return newParam;
    }

    /**
     * If tiling is supported, determine the appropriate tile size and
     * set it on the returned param if necessary. The returned param
     * will either be a new ImageWriteParam or the one passed in with
     * its tiling settings possibly modified.
     */
    private static ImageWriteParam setTileSize(ImageWriteParam param,
                                               ImageWriter writer,
                                               Dimension tileSize,
                                               RenderedImage source) {

        ImageWriteParam returnParam = getWriteParam(param, writer);

        // If tiling possible set tile size if needed.
        if(returnParam.canWriteTiles()) {
            if(tileSize != null) {
                // Check tile size.
                if(tileSize.width <= 0 || tileSize.height <= 0) {
                    throw new IllegalArgumentException
                        ("tileSize.width <= 0 || tileSize.height <= 0");
                }

                // Use specified tile size.
                returnParam.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
                returnParam.setTiling(tileSize.width,
                                      tileSize.height,
                                      0, 0); // XXX set tile offsets?
            } else if(param == null) {
                if(source.getNumXTiles() > 1 || source.getNumYTiles() > 1) {
                    // Null tile size and param args: use source tile size.
                    returnParam.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
                    returnParam.setTiling(source.getTileWidth(),
                                          source.getTileHeight(),
                                          0, 0); // XXX set tile offsets?
                }
            } else if(returnParam.getTilingMode() ==
                      ImageWriteParam.MODE_EXPLICIT) {
                // Param passed in has explicit mode set but the tile
                // grid might not actually be set.
                boolean setTileSize = false;

                // Save reference to preferred tile size array.
                Dimension[] preferredTileSizes =
                    returnParam.getPreferredTileSizes();

                // Set the tile width.
                int tileWidth = 0;
                try {
                    // Try to get it from the param.
                    tileWidth = returnParam.getTileWidth();
                } catch(IllegalStateException e) {
                    // Not set in the param.
                    setTileSize = true;

                    if(preferredTileSizes != null &&
                       preferredTileSizes.length >= 2 &&
                       preferredTileSizes[0].width > 0 &&
                       preferredTileSizes[1].width > 0) {
                        // Use average of first two preferred tile widths.
                        tileWidth = (preferredTileSizes[0].width +
                                     preferredTileSizes[1].width) / 2;
                    } else {
                        // Use source image tile width.
                        tileWidth = source.getTileWidth();
                    }
                }

                // Set the tile height.
                int tileHeight = 0;
                try {
                    // Try to get it from the param.
                    tileHeight = returnParam.getTileHeight();
                } catch(IllegalStateException e) {
                    // Not set in the param.
                    setTileSize = true;

                    if(preferredTileSizes != null &&
                       preferredTileSizes.length >= 2 &&
                       preferredTileSizes[0].height > 0 &&
                       preferredTileSizes[1].height > 0) {
                        // Use average of first two preferred tile heights.
                        tileHeight = (preferredTileSizes[0].height +
                                      preferredTileSizes[1].height) / 2;
                    } else {
                        // Use source image tile height.
                        tileHeight = source.getTileHeight();
                    }
                }

                // Set the tile size if not previously set in the param.
                if(setTileSize) {
                    returnParam.setTiling(tileWidth,
                                          tileHeight,
                                          0, 0); // XXX set tile offsets?
                }
            }
        }

        return returnParam;
    }

    static RenderedImage create(int imageIndex,
                                boolean writeToSequence,
                                ParameterBlock pb,
                                RenderingHints rh) {

        // Value to be returned.
        RenderedImage image = null;

        // Get the source image.
        RenderedImage source = pb.getRenderedSource(0);

        // Get the writer parameters.
        ImageWriteParam param = (ImageWriteParam)pb.getObjectParameter(12);

        // Set the target image type.
        ImageTypeSpecifier destinationType = null;
        if(param != null) {
            destinationType = param.getDestinationType();
        }
        if(destinationType == null) {
            destinationType = new ImageTypeSpecifier(source);
        }

        // Get the writer.
        ImageWriter writer = (ImageWriter)pb.getObjectParameter(13);

        if(writer == null) {
            // Get the format. Should be non-null from OperationDescriptor.
            String format = (String)pb.getObjectParameter(1);

            // Find a writer.
            Iterator writers = ImageIO.getImageWriters(destinationType,
                                                       format);

            // Get the writer.
            if(writers != null && writers.hasNext()) {
                writer = (ImageWriter)writers.next();
            }
        }

        // XXX What if no writer? Exception?
        if(writer != null) {
            // XXX Replace ImageWriter parameter in ParameterBlock?

            ImageOutputStream streamToClose = null;

            // Set the output if not writing to a sequence (in which
            // case the output should already be set.
            if(!writeToSequence) {
                // Get the output.
                Object output = pb.getObjectParameter(0);

                // Try to get an ImageOutputStream.
                ImageOutputStream stream = getImageOutputStream(output);

                // Set stream to close if not writing to a sequence.
                streamToClose = stream != output ? stream : null;

                // Set the writer's output.
                writer.setOutput(stream != null ? stream : output);
            }

            // Get the property use flag.
            boolean useProperties =
                ((Boolean)pb.getObjectParameter(2)).booleanValue();

            // Get the transcoding flag.
            boolean transcode =
                ((Boolean)pb.getObjectParameter(3)).booleanValue();

            IIOMetadata streamMetadata = null;
            if(!writeToSequence) {
                // Get the stream metadata.
                streamMetadata = (IIOMetadata)pb.getObjectParameter(7);

                // If null, get stream metadata from source properties
                // if allowed.
                if(streamMetadata == null && useProperties) {
                    Object streamMetadataProperty =
                        source.getProperty(
                            ImageWriteDescriptor.PROPERTY_NAME_METADATA_STREAM);
                    if(streamMetadataProperty instanceof IIOMetadata) {
                        streamMetadata = (IIOMetadata)streamMetadataProperty;
                    }
                }

                // Transcode the stream metadata if requested.
                if(streamMetadata != null && transcode) {
                    // Overwrite the stream metadata with transcoded metadata.
                    streamMetadata =
                        writer.convertStreamMetadata(streamMetadata,
                                                     param);
                }
            }

            // Get the image metadata.
            IIOMetadata imageMetadata =
                (IIOMetadata)pb.getObjectParameter(8);

            // If null, get image metadata from source properties if allowed.
            if(imageMetadata == null && useProperties) {
                Object imageMetadataProperty =
                    source.getProperty(
                        ImageWriteDescriptor.PROPERTY_NAME_METADATA_IMAGE);
                if(imageMetadataProperty instanceof IIOMetadata) {
                    imageMetadata = (IIOMetadata)imageMetadataProperty;
                }
            }

            // Transcode the image metadata if requested.
            if(imageMetadata != null && transcode) {
                // Overwrite the image metadata with transcoded metadata.
                imageMetadata = writer.convertImageMetadata(imageMetadata,
                                                            destinationType,
                                                            param);
            }

            // Get the thumbnails if supported by the writer.
            BufferedImage[] thumbnails = null;
            if(writer.getNumThumbnailsSupported(destinationType,
                                                param,
                                                streamMetadata,
                                                imageMetadata) > 0) {
                thumbnails = (BufferedImage[])pb.getObjectParameter(9);

                // If null, get thumbnails from source properties if allowed.
                if(thumbnails == null && useProperties) {
                    Object thumbnailsProperty =
                        source.getProperty(
                            ImageWriteDescriptor.PROPERTY_NAME_METADATA_IMAGE);
                    if(thumbnailsProperty instanceof BufferedImage[]) {
                        thumbnails = (BufferedImage[])thumbnailsProperty;
                    }
                }
            }

            // Get the locale parameter and set on the writer.
            Locale locale = (Locale)pb.getObjectParameter(11);
            if(locale != null) {
                writer.setLocale(locale);
            }

            // Get the listeners parameter and set on the writer.
            EventListener[] listeners =
                (EventListener[])pb.getObjectParameter(10);
            if(listeners != null) {
                for(int i = 0; i < listeners.length; i++) {
                    EventListener listener = listeners[i];
                    if(listener instanceof IIOWriteProgressListener) {
                        writer.addIIOWriteProgressListener(
                            (IIOWriteProgressListener)listener);
                    }
                    if(listener instanceof IIOWriteWarningListener) {
                        writer.addIIOWriteWarningListener(
                            (IIOWriteWarningListener)listener);
                    }
                }
            }

            // Set the tile size.
            // XXX Replace ImageWriteParam parameter in ParameterBlock?
            param = setTileSize(param, writer,
                                (Dimension)pb.getObjectParameter(6),
                                source);

            // Create the IIOImage container.
            IIOImage iioImage = new IIOImage(source,
                                             thumbnails != null ?
                                             Arrays.asList(thumbnails) : null,
                                             imageMetadata);

            try {
                // Write the image.
                if(writeToSequence) {
                    writer.writeToSequence(iioImage, param);
                } else {
                    writer.write(streamMetadata, iioImage, param);
                }

                // Get the pixel replacement parameter.
                boolean allowPixelReplacement =
                    ((Boolean)pb.getObjectParameter(5)).booleanValue();

                // Set the return value.
                if(allowPixelReplacement &&
                   source instanceof PlanarImage &&
                   writer.canReplacePixels(imageIndex)) {

                    // Create an image which is a PropertyChangeListener of
                    // "invalidregion" events including RenderingChangeEvents.
                    image = new PixelReplacementImage(source,
                                                      rh,
                                                      param,
                                                      writer,
                                                      imageIndex,
                                                      streamToClose);

                    // Register the image as a sink of its source so that
                    // it automatically receives events.
                    ((PlanarImage)source).addSink(image);
                } else if(!writeToSequence) {
                    Object writerOutput = writer.getOutput();
                    if(writerOutput != pb.getObjectParameter(0) &&
                       writerOutput instanceof ImageOutputStream) {
                        // This block is executed if and only if pixel
                        // replacement is not occurring, a sequence is
                        // not being written, and an ImageOutputStream
                        // inaccessible to the application is set on the
                        // ImageWriter.
                        ((ImageOutputStream)writerOutput).flush();
                    }

                    // Set the return value to the original image or
                    // a wrapped version thereof.
                    image = source instanceof WritablePropertySource ?
                        source : new RenderedImageAdapter(source);
                }

                // Set required properties.
                WritablePropertySource wps = (WritablePropertySource)image;

                // Set the ImageWriteParam property.
                wps.setProperty(
                    ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITE_PARAM,
                    param);

                // Set the ImageWriter property.
                wps.setProperty(
                    ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER,
                    writer);

                // Set the stream metadata property.
                if(streamMetadata != null) {
                    wps.setProperty(
                        ImageWriteDescriptor.PROPERTY_NAME_METADATA_STREAM,
                        streamMetadata);
                }

                // Set the image metadata property.
                if(imageMetadata != null) {
                    wps.setProperty(
                        ImageWriteDescriptor.PROPERTY_NAME_METADATA_IMAGE,
                        imageMetadata);
                }

                // Set the thumbnail property.
                if(thumbnails != null) {
                    wps.setProperty(
                        ImageWriteDescriptor.PROPERTY_NAME_THUMBNAILS,
                        thumbnails);
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        return image;
    }
}
