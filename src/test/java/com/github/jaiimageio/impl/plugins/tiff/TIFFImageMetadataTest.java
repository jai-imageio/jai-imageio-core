package com.github.jaiimageio.impl.plugins.tiff;


import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.EXIFParentTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.FaxTIFFTagSet;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import org.junit.Test;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageMetadata;
import org.w3c.dom.*;

import javax.imageio.metadata.IIOInvalidTreeException;

import static junit.framework.TestCase.assertEquals;

public class TIFFImageMetadataTest {

    @Test
    public void test_parseIFD_SUN_EXIFParentTIFFTagSetClassName() throws IIOInvalidTreeException {
        Document xmlDoc = new DocumentImpl();
        Element ifd = xmlDoc.createElement("TIFFIFD");
        ifd.setAttribute("tagSets", TIFFImageMetadata.SUN_EXIFParentTIFFTagSetClassName);

        TIFFIFD tiffIfd = TIFFImageMetadata.parseIFD(ifd);

        assertEquals(1, tiffIfd.getTagSetList().size());
        assertEquals(EXIFParentTIFFTagSet.class.getName(), tiffIfd.getTagSetList().get(0).getClass().getName());
    }

    @Test
    public void test_parseIFD_SUN_FaxTIFFTagSetClassName() throws IIOInvalidTreeException {
        Document xmlDoc = new DocumentImpl();
        Element ifd = xmlDoc.createElement("TIFFIFD");
        ifd.setAttribute("tagSets", TIFFImageMetadata.SUN_FaxTIFFTagSetClassName);
        
	TIFFIFD tiffIfd = TIFFImageMetadata.parseIFD(ifd);
	
	assertEquals(1, tiffIfd.getTagSetList().size());
	assertEquals(FaxTIFFTagSet.class.getName(), tiffIfd.getTagSetList().get(0).getClass().getName());
    }
    
    @Test
    public void test_parseIFD_SUN_BaselineTIFFTagSetClassName() throws IIOInvalidTreeException {
	Document xmlDoc = new DocumentImpl();
	Element ifd = xmlDoc.createElement("TIFFIFD");
	ifd.setAttribute("tagSets", TIFFImageMetadata.SUN_BaselineTIFFTagSetClassName);
	
	TIFFIFD tiffIfd = TIFFImageMetadata.parseIFD(ifd);
	
	assertEquals(1, tiffIfd.getTagSetList().size());
	assertEquals(BaselineTIFFTagSet.class.getName(), tiffIfd.getTagSetList().get(0).getClass().getName());
    }
    
}
