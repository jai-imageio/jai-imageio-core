/*
 * $RCSfile: TIFFImageReaderSpi.java,v $
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
 * $Revision: 1.2 $
 * $Date: 2006/03/31 19:43:41 $
 * $State: Exp $
 */
package com.github.jaiimageio.impl.plugins.tiff;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

import com.github.jaiimageio.impl.common.ClassLoaderUtils;
import com.github.jaiimageio.impl.common.PackageUtil;

public class TIFFImageReaderSpi extends ImageReaderSpi {

    private final ClassLoaderUtils classLoaderUtils = ClassLoaderUtils.getInstance();

    private static final String[] names = { "tif", "TIF", "tiff", "TIFF" };

    private static final String[] suffixes = { "tif", "tiff" };

    private static final String[] MIMETypes = { "image/tiff" };

    private static final String readerClassName =
        "com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader";

    private static final String[] writerSpiNames = {
        "com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi"
    };

    private boolean registered = false;
 
    public TIFFImageReaderSpi() {
        super(PackageUtil.getVendor(),
              PackageUtil.getVersion(),
              names,
              suffixes,
              MIMETypes,
              readerClassName,
              STANDARD_INPUT_TYPE,
              writerSpiNames,
              false,
              TIFFStreamMetadata.nativeMetadataFormatName,
              "com.github.jaiimageio.impl.plugins.tiff.TIFFStreamMetadataFormat",
              null, null,
              true,
              TIFFImageMetadata.nativeMetadataFormatName,
              "com.github.jaiimageio.impl.plugins.tiff.TIFFImageMetadataFormat",
              null, null
              );
    }

    public String getDescription(Locale locale) {
	String desc = PackageUtil.getSpecificationTitle() + 
	    " TIFF Image Reader";
	return desc;
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (!classLoaderUtils.wasLoadedByCurrentClassLoaderAncestor(this)) {
            return false;
        }

        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        
        ImageInputStream stream = (ImageInputStream)input;
        byte[] b = new byte[4];
        stream.mark();
        stream.readFully(b);
        stream.reset();

        return ((b[0] == (byte)0x49 && b[1] == (byte)0x49 &&
                 b[2] == (byte)0x2a && b[3] == (byte)0x00) ||
                (b[0] == (byte)0x4d && b[1] == (byte)0x4d &&
                 b[2] == (byte)0x00 && b[3] == (byte)0x2a));
    }

    public ImageReader createReaderInstance(Object extension) {
        return new TIFFImageReader(this);
    }

    public void onRegistration(ServiceRegistry registry,
                               Class category) {
        if (registered) {
            return;
        }
	
        registered = true;
    }
}
