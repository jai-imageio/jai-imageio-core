/*
 * $RCSfile: WBMPImageReaderSpi.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2006/04/21 00:02:53 $
 * $State: Exp $
 */
package com.github.jaiimageio.impl.plugins.wbmp;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

import com.github.jaiimageio.impl.common.ClassLoaderUtils;
import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.impl.common.PackageUtil;

public class WBMPImageReaderSpi extends ImageReaderSpi {

    private final ClassLoaderUtils classLoaderUtils = ClassLoaderUtils.getInstance();

    private static String [] writerSpiNames =
        {"com.github.jaiimageio.impl.plugins.wbmp.WBMPImageWriterSpi"};
    private static String[] formatNames = {"wbmp", "WBMP"};
    private static String[] entensions = {"wbmp"};
    private static String[] mimeType = {"image/vnd.wap.wbmp"};

    private boolean registered = false;

    public WBMPImageReaderSpi() {
        super(PackageUtil.getVendor(),
              PackageUtil.getVersion(),
              formatNames,
              entensions,
              mimeType,
              "com.github.jaiimageio.impl.plugins.wbmp.WBMPImageReader",
              STANDARD_INPUT_TYPE,
              writerSpiNames,
              true,
              null, null, null, null,
              true,
              WBMPMetadata.nativeMetadataFormatName,
	      "com.github.jaiimageio.impl.plugins.wbmp.WBMPMetadataFormat",
	      null, null);
    }

    public void onRegistration(ServiceRegistry registry,
                               Class category) {
        if (registered) {
            return;
        }
        registered = true;
	
	// By JDK 1.7, the WBMPImageReader will have been in JDK core for 
	// atleast two FCS releases, so we can set JIIO's to lower priority
	// With JDK 1.8, we can entirely de-register the JIIO one
	ImageUtil.processOnRegistration(registry, category, "WBMP", this,
					8, 7); // JDK version 1.8, 1.7
    }

    public String getDescription(Locale locale) {
	String desc = PackageUtil.getSpecificationTitle() + 
	    " WBMP Image Reader";  
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
        int type = stream.readByte();   // TypeField
        byte fixHeaderField = stream.readByte();

        int width = ImageUtil.readMultiByteInteger(stream);
        int height = ImageUtil.readMultiByteInteger(stream);

        long remainingBytes = stream.length() - stream.getStreamPosition();
        stream.reset();

        // check WBMP "header"
        if (type != 0 || fixHeaderField != 0) {
            // while WBMP reader does not support ext WBMP headers
            return false;
        }
        
        // check image dimension
        if (width <= 0 || height <= 0) {
            return false;
        }

        long scanSize = (width / 8) + ((width % 8) == 0 ? 0 : 1);
        
        return (remainingBytes == scanSize * height);
    }

    public ImageReader createReaderInstance(Object extension)
        throws IIOException {
        return new WBMPImageReader(this);
    }
}

