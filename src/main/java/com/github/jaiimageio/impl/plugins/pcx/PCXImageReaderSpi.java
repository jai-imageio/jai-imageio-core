/*
 * $RCSfile: PCXImageReaderSpi.java,v $
 *
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc. All  Rights Reserved.
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
 * $Date: 2007/09/05 00:21:08 $
 * $State: Exp $
 */
package com.github.jaiimageio.impl.plugins.pcx;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

import com.github.jaiimageio.impl.common.ClassLoaderUtils;
import com.github.jaiimageio.impl.common.PackageUtil;

public class PCXImageReaderSpi extends ImageReaderSpi {

    private final ClassLoaderUtils classLoaderUtils = ClassLoaderUtils.getInstance();

    private static String [] writerSpiNames =
        {"com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi"};
    private static String[] formatNames = {"pcx", "PCX"};
    private static String[] extensions = {"pcx"};
    private static String[] mimeTypes = {
        "image/pcx", "image/x-pcx", "image/x-windows-pcx", "image/x-pc-paintbrush"
    };
    private boolean registered = false;

    public PCXImageReaderSpi() {
        super(PackageUtil.getVendor(),
              PackageUtil.getVersion(),
              formatNames,
              extensions,
              mimeTypes,
              "com.github.jaiimageio.impl.plugins.pcx.PCXImageReader",
              STANDARD_INPUT_TYPE,
              writerSpiNames,
              false,
              null, null, null, null,
              true,
              null,
              null,
              null, null);
    }

    public void onRegistration(ServiceRegistry registry, Class category) {
        if (registered) {
            return;
        }
        registered = true;
    }

    public String getDescription(Locale locale) {
	String desc = PackageUtil.getSpecificationTitle() + 
	    " PCX Image Reader";  
	return desc;
    }

    public boolean canDecodeInput(Object source) throws IOException {
        if (!classLoaderUtils.wasLoadedByCurrentClassLoaderAncestor(this)) {
            return false;
        }
        if (!(source instanceof ImageInputStream)) {
            return false;
        }

        ImageInputStream stream = (ImageInputStream)source;
        stream.mark();
        byte b = stream.readByte();
        stream.reset();

        return (b==0x0A);
    }

    public ImageReader createReaderInstance(Object extension)
        throws IIOException {
        return new PCXImageReader(this);
    }
}

