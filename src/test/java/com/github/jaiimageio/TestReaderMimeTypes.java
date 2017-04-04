package com.github.jaiimageio;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

public class TestReaderMimeTypes {
    @Test
    public void noEmptyMimeType() throws Exception {
        List<String> types = Arrays.asList(ImageIO.getReaderMIMETypes());
        assertFalse(types.contains(""));
    }
}
