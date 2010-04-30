/*
 * $RCSfile: ImageWriteCIF.java,v $
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

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.CollectionImage;
import javax.media.jai.CollectionImageFactory;
import javax.media.jai.CollectionOp;
import javax.media.jai.PropertySource;
import com.sun.media.jai.operator.ImageWriteDescriptor;

public final class ImageWriteCIF implements CollectionImageFactory {
    /** Constructor. */
    public ImageWriteCIF() {}

    public CollectionImage create(ParameterBlock args,
                                  RenderingHints hints) {

        // Get the writer.
        ImageWriter writer = (ImageWriter)args.getObjectParameter(13);

        // Find a writer if null.
        if(writer == null) {
            // Get the format. Should be non-null from OperationDescriptor.
            String format = (String)args.getObjectParameter(1);

            // Find a writer.
            Iterator writers = ImageIO.getImageWritersByFormatName(format);

            // Get the writer.
            if(writers != null) {
                writer = (ImageWriter)writers.next();
            } else {
                throw new RuntimeException
                    (I18N.getString("ImageWriteCIF0")+" "+format);
            }
        }

        // Get the source Collection.
        Collection collection = (Collection)args.getSource(0);

        // Determine the number of RenderedImages in the Collection.
        int numRenderedImages = 0;
        Iterator iter = collection.iterator();
        while(iter.hasNext()) {
            if(iter.next() instanceof RenderedImage) {
                numRenderedImages++;
            }
        }

        // Set the sequence flag.
        boolean writeToSequence = writer.canWriteSequence();

        // Check that the writer can write sequences.
        if(numRenderedImages > 1 && !writeToSequence) {
            throw new RuntimeException
                (I18N.getString("ImageWriteCIF1"));
        }

        // Get the stream metadata.
        IIOMetadata streamMetadata =
            (IIOMetadata)args.getObjectParameter(7);

        // Get the property use flag.
        boolean useProperties =
            ((Boolean)args.getObjectParameter(2)).booleanValue();

        // If null, get stream metadata from source properties if allowed.
        if(streamMetadata == null &&
           useProperties &&
           collection instanceof PropertySource) {
            Object streamMetadataProperty =
                ((PropertySource)collection).getProperty(
                    ImageWriteDescriptor.PROPERTY_NAME_METADATA_STREAM);
            if(streamMetadataProperty instanceof IIOMetadata) {
                streamMetadata = (IIOMetadata)streamMetadataProperty;
            }
        }

        // Get the writer parameters.
        ImageWriteParam param = (ImageWriteParam)args.getObjectParameter(12);

        // Transcode the stream metadata if requested.
        if(streamMetadata != null) {
            // Get the transcoding flag.
            boolean transcode =
                ((Boolean)args.getObjectParameter(3)).booleanValue();

            if(transcode) {
                // Overwrite the stream metadata with transcoded metadata.
                streamMetadata =
                    writer.convertStreamMetadata(streamMetadata,
                                                 param);
            }
        }

        if(writeToSequence) {
            // Write the stream metadata to the sequence.
            try {
                // Get the output.
                Object output = args.getObjectParameter(0);

                // Try to get an ImageOutputStream.
                ImageOutputStream stream =
                    ImageWriteCRIF.getImageOutputStream(output);

                // Set the writer's output.
                writer.setOutput(stream != null ? stream : output);

                // Prepare the sequence.
                writer.prepareWriteSequence(streamMetadata);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Clone the ParameterBlock as the writer, image metadata, and
        // thumbnail parameters will be replaced.
        ParameterBlock imagePB = (ParameterBlock)args.clone();

        // Clear the stream metadata.
        imagePB.set(null, 7);

        // Set the ImageWriter.
        imagePB.set(writer, 13);

        // Get the image metadata array.
        IIOMetadata[] imageMetadata =
            (IIOMetadata[])args.getObjectParameter(8);

        // Get the thumbnail array.
        BufferedImage[] thumbnails =
            (BufferedImage[])args.getObjectParameter(9);

        // Create a new Iterator.
        iter = collection.iterator();

        // Create an ImageIOCollectionImage to contain the result:
        ImageIOCollectionImage imageList =
            new ImageIOCollectionImage(collection.size());

        // Iterate over the collection.
        int imageIndex = 0;
        while(iter.hasNext()) {
            // Get the next element.
            Object nextElement = iter.next();

            // Process if a RenderedImage.
            if(nextElement instanceof RenderedImage) {
                // Replace source with current RenderedImage.
                imagePB.setSource((RenderedImage)nextElement, 0);

                // Replace image metadata.
                if(imageMetadata != null) {
                    imagePB.set(imageMetadata[imageIndex], 8);
                }

                // Replace thumbnail array.
                if(thumbnails != null) {
                    imagePB.set(thumbnails[imageIndex], 9);
                }

                // Write the image to the sequence
                RenderedImage nextImage =
                    ImageWriteCRIF.create(imageIndex,
                                          writeToSequence,
                                          imagePB, hints);

                // If the ImageWriteParam passed in was null, replace it
                // with the first non-null ImageWriteParam property value
                // and set the value in the local ParameterBlock.
                if(param == null) {
                    Object paramPropertyValue =
                        nextImage.getProperty(
                        ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITE_PARAM);

                    if(paramPropertyValue instanceof ImageWriteParam) {
                        param = (ImageWriteParam)paramPropertyValue;

                        // Replace the ImageWriteParam so the CRIF doesn't
                        // have to re-do the tile size initialization.
                        imagePB.set(param, 12);
                    }
                }

                // Add the image to the collection to be returned.
                imageList.add(nextImage);

                // Increment the index.
                imageIndex++;
            }
        }

        // Get the pixel replacement parameter.
        boolean allowPixelReplacement =
            ((Boolean)args.getObjectParameter(5)).booleanValue();

        if(writeToSequence && !allowPixelReplacement) {
            // Complete writing the sequence.
            try {
                // XXX What about pixel replacement? If this is invoked here
                // it will not be possible. How can this be invoked such that
                // pixel replacement can occur but the user is not obliged to
                // call this method manually?
                // Answer: document that the user must obtain the writer from
                // the collection-level ImageWriter property and invoke
                // endWriteSequence() on it.
                writer.endWriteSequence();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Set collection-level properties.
        if(param != null) {
            imageList.setProperty(
                ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITE_PARAM,
                param);
        }
        imageList.setProperty(
            ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER,
            writer);
        if(streamMetadata != null) {
            imageList.setProperty(
                ImageWriteDescriptor.PROPERTY_NAME_METADATA_STREAM,
                streamMetadata);
        }

        // Return CollectionImage.
        return imageList;
    }

    // Forget it.
    public CollectionImage update(ParameterBlock oldParamBlock,
                                  RenderingHints oldHints,
                                  ParameterBlock newParamBlock,
                                  RenderingHints newHints,
                                  CollectionImage oldRendering,
                                  CollectionOp op) {
        return null;
    }
}
