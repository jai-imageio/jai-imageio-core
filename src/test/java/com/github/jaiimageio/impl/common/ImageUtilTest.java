package com.github.jaiimageio.impl.common;


import org.junit.Test;

import static com.github.jaiimageio.impl.common.ImageUtil.getJvmVersion;
import static junit.framework.TestCase.assertEquals;

public class ImageUtilTest {

    /**
     * See <a href="http://www.oracle.com/technetwork/java/javase/9-relnote-issues-3704069.html#JDK-8085822">Java 9 release notes</a>.
     */
    @Test
    public void test_parsing_of_java_8_specification_version() {
        assertEquals("java.specification.version=1.8", 8, getJvmVersion("1.8"));
    }


    /**
     * See <a href="http://www.oracle.com/technetwork/java/javase/9-relnote-issues-3704069.html#JDK-8085822">Java 9 release notes</a>.
     */
    @Test
    public void test_parsing_of_java_9_specification_version() {
        assertEquals("java.specification.version=9", 9, getJvmVersion("9"));
    }

}