/*
 * $RCSfile: ImageReadWriteSpi.java,v $
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

import java.awt.image.renderable.ContextualRenderedImageFactory;
import javax.media.jai.CollectionImageFactory;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.OperationRegistry;
import javax.media.jai.OperationRegistrySpi;
import com.sun.media.jai.operator.ImageReadDescriptor;
import com.sun.media.jai.operator.ImageWriteDescriptor;
import javax.media.jai.registry.CollectionRegistryMode;
import javax.media.jai.registry.RenderableRegistryMode;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * {@link OperationRegistrySpi} implementation to register the "ImageRead"
 * and "ImageWrite" operations and their associated image factories.
 */
public class ImageReadWriteSpi implements OperationRegistrySpi {

    /** The name of the product to which these operations belong. */
    private String productName = "com.sun.media.jai";
 
    /** Default constructor. */
    public ImageReadWriteSpi() {}

    /**
     * Registers the "ImageRead" and "ImageWrite" operations and their
     * associated image factories across all supported operation modes.
     * An {@link OperationDescriptor} is created for each operation and
     * registered with the supplied {@link OperationRegistry}. An image
     * factory is then created for each supported mode of each operation
     * registered for that operation with the registry.
     *
     * @param registry The registry with which to register the operations
     * and their factories.
     */
    public void updateRegistry(OperationRegistry registry) {
        // Create the "ImageRead" descriptor instance.
        OperationDescriptor readDescriptor = new ImageReadDescriptor();

        // Register the "ImageRead" descriptor.
        registry.registerDescriptor(readDescriptor);

        // Create the "ImageRead" CRIF.
        ContextualRenderedImageFactory readCRIF = new ImageReadCRIF();

        // Get the "ImageRead" operation name.
        String imageReadName = readDescriptor.getName();

        // Register the "ImageRead" factory for rendered mode.
        registry.registerFactory(RenderedRegistryMode.MODE_NAME,
                                 imageReadName,
                                 productName,
                                 readCRIF);

        // Register the "ImageRead" factory for renderable mode.
        registry.registerFactory(RenderableRegistryMode.MODE_NAME,
                                 imageReadName,
                                 productName,
                                 readCRIF);

        // Create and register the "ImageRead" factory for collection mode.
        registry.registerFactory(CollectionRegistryMode.MODE_NAME,
                                 imageReadName,
                                 productName,
                                 new ImageReadCIF());

        // Create the "ImageWrite" descriptor instance.
        OperationDescriptor writeDescriptor = new ImageWriteDescriptor();

        // Register the "ImageWrite" descriptor.
        registry.registerDescriptor(writeDescriptor);

        // Create the "ImageWrite" CRIF.
        ContextualRenderedImageFactory writeCRIF = new ImageWriteCRIF();

        // Get the "ImageWrite" operation name.
        String imageWriteName = writeDescriptor.getName();

        // Register the "ImageWrite" factory for rendered mode.
        registry.registerFactory(RenderedRegistryMode.MODE_NAME,
                                 imageWriteName,
                                 productName,
                                 writeCRIF);

        // Register the "ImageWrite" factory for renderable mode.
        registry.registerFactory(RenderableRegistryMode.MODE_NAME,
                                 imageWriteName,
                                 productName,
                                 writeCRIF);

        // Create and register the "ImageWrite" factory for collection mode.
        registry.registerFactory(CollectionRegistryMode.MODE_NAME,
                                 imageWriteName,
                                 productName,
                                 new ImageWriteCIF());
    }
}
