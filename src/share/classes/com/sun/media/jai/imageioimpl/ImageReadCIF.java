/*
 * $RCSfile: ImageReadCIF.java,v $
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
 * $Date: 2005/02/11 05:01:54 $
 * $State: Exp $
 */
package com.sun.media.jai.imageioimpl;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageReader;
import javax.media.jai.CollectionImage;
import javax.media.jai.CollectionImageFactory;
import javax.media.jai.CollectionOp;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import com.sun.media.jai.operator.ImageReadDescriptor;

public class ImageReadCIF implements CollectionImageFactory {

    static CollectionImage createStatic(ParameterBlock args,
                                        RenderingHints hints) {

        // Clone the ParameterBlock as the ImageChoice will be overwritten.
        ParameterBlock renderedPB = (ParameterBlock)args.clone();

        // Get the ImageChoice.
        int[] imageIndices = (int[])args.getObjectParameter(1);

        // Variables to be set in the subsequent "if" block.
        // XXX Could probably collapse the if block into a single code seq.
        int numSources;
        ImageIOCollectionImage imageList = null;

        if(imageIndices == null) {
            // null-valued ImageChoice: load all images.

            // Load the first image.
            renderedPB.set(0, 1);
            PlanarImage image = JAI.create("ImageRead", renderedPB, hints);

            // Get the ImageReader property.
            Object readerProperty =
                image.getProperty(
                    ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);

            // Try to read the number of images.
            if(readerProperty instanceof ImageReader) {
                try {
                    // XXX Really should not allow search here. If search
                    // is disallowed and -1 is returned from getNumImages(),
                    // then "ImageRead" should just be called until an
                    // IndexOutOfBoundsException is caught.
                    numSources =
                        ((ImageReader)readerProperty).getNumImages(true);
                } catch(Exception e) { // IOException
                    // Default to one source.
                    numSources = 1;
                }
            } else {
                numSources = 1;
            }

            // Allocate and fill index array.
            imageIndices = new int[numSources];
            for(int i = 0; i < numSources; i++) {
                imageIndices[i] = i;
            }

            // Allocate list and add first image.
            imageList = new ImageIOCollectionImage(numSources);
            imageList.add(image);
        } else {
            // Set the number of sources and create the list.
            numSources = imageIndices.length;
            imageList = new ImageIOCollectionImage(numSources);

            // Load the first image requested.
            renderedPB.set(imageIndices[0], 1);
            PlanarImage image = JAI.create("ImageRead", renderedPB, hints);

            // Add the first image to the list.
            imageList.add(image);
        }

        // Read subsequent images and add to the list.
        for(int idx = 1; idx < numSources; idx++) {
            renderedPB.set(imageIndices[idx], 1);
            PlanarImage image = JAI.create("ImageRead", renderedPB, hints);
            imageList.add(image);
        }

        // Get the first image in the Collection.
        PlanarImage firstImage = (PlanarImage)imageList.get(0);

        // Transfer properties to the Collection.
        ImageReadCRIF.copyProperty(firstImage,
                                   imageList,
                                   ImageReadDescriptor.PROPERTY_NAME_IMAGE_READ_PARAM);
        ImageReadCRIF.copyProperty(firstImage,
                                   imageList,
                                   ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
        ImageReadCRIF.copyProperty(firstImage,
                                   imageList,
                                   ImageReadDescriptor.PROPERTY_NAME_METADATA_STREAM);

        return imageList;
    }

    /** Constructor. */
    public ImageReadCIF() {}

    public CollectionImage create(ParameterBlock args,
                                  RenderingHints hints) {
        return createStatic(args, hints);
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
