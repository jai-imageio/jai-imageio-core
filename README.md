jai-imageio-core (standalone)
=============================

[![Build Status](https://travis-ci.org/jai-imageio/jai-imageio-core.svg)](https://travis-ci.org/jai-imageio/jai-imageio-core)

NOTE: This is a community fork of the original `java.net` project
jai-imageio-core (which is no longer available upstream).


This project adds read/write support for the following file types to 
Java's [ImageIO](https://docs.oracle.com/javase/8/docs/api/javax/imageio/ImageIO.html):
 
* wbmp
* bmp
* pcx
* pnm
* raw
* tiff
* gif (write)


_Note that most of these formats are now [supported by Java 9](https://docs.oracle.com/javase/9/docs/api/javax/imageio/package-summary.html)._

The classes of this projects are not generally used directly, but through
the SPI plugin mechanism of ImageIO. See the 
[ImageIO guide](https://docs.oracle.com/javase/8/docs/technotes/guides/imageio/spec/imageio_guideTOC.fm.html)
for details. 

The implementations provided by this project use the package name
`com.github.jaiimageio`, note that other implementations might be provided by
your Java installation or other dependencies.

This project is called 'standalone' as unlike the original
jai-imageio-core it has removed dependencies to jai-core packages (`javax.media.jai`) and JPEG
2000 (`jj2000`).  This version also does not include the C
implementations from libJIIO, meaning that this version is platform independent
and fully redistributable under the 3-clause BSD license in
[LICENSE.txt](LICENSE.txt) (and thus is Apache and GPL compatible).

JPEG 2000 support is available as an additional module 
[jai-imageio-jpeg2000](https://github.com/jai-imageio/jai-imageio-jpeg2000)
as it has a different (non-GPL compatible) license.


If you are not concerned about GPL compatibility or source code
availability, you may want to check out
https://github.com/geosolutions-it/imageio-ext/ which is actively
maintained and extends the original imageio with many useful features,
but depends on the [binary distribution of jai\_core](
http://download.osgeo.org/webdav/geotools/javax/media/jai_core/1.1.3/).


Contribute
----------

You are encouraged to raise a 
[Github Pull Request](https://github.com/jai-imageio/jai-imageio-core/pulls)
with any suggested improvements.

You can also raise an
[issue](https://github.com/jai-imageio/jai-imageio-core/issues) - your stacktrace
might still be of use to someone else.

jai-imageio GitHub committers and contributors include (in no particular order):

* [Stian Soiland-Reyes](https://orcid.org/0000-0001-9842-9718)
* [John Hewson](http://jahewson.com/)
* [Peter Hull](https://github.com/peterhull90)
* [Mark Carroll](https://github.com/mtbc)
* [Robin Stevens](https://github.com/PissedCapslock)
* [Yannick De Turck](https://github.com/yannickdeturck)
* [Butch Howard](https://github.com/butchhoward)
* [Roger Leigh](https://github.com/rleigh-codelibre)
* [Mykola Pavluchynskyi](https://github.com/mykolap)
* [Glen](https://github.com/glenhein) 
* [Peter Jodeleit](https://github.com/pejobo)
* [Luca Bellonda](https://github.com/lbellonda)
* [Nicolai Parlog](https://github.com/nicolaiparlog)
* [Réda Housni Alaoui](https://github.com/reda-alaoui)
* [Sébastien Besson](https://github.com/sbesson)
* [Curtis Rueden](https://github.com/ctrueden)
* [Ghislain Bonamy](https://www.linkedin.com/in/gbonamy/)
* [Jean-Marie Burel](https://github.com/jburel)




Usage
-----

This project requires Java 6 or newer.  To build this project, use [Apache
Maven](https://maven.apache.org/download.cgi) 
3.0.5 or newer and run:

    mvn clean install

To use jai-imageio-core from a Maven project, add:

    <dependency>
        <groupId>com.github.jai-imageio</groupId>
        <artifactId>jai-imageio-core</artifactId>
        <version>1.3.1</version>
    </dependency>

To find the latest released `<version>` above, see 
[jai-imageio-core at BinTray](https://bintray.com/jai-imageio/maven/jai-imageio-core-standalone)

jai-imageio-core is [mirrored to Maven Central](https://repo1.maven.org/maven2/com/github/jai-imageio/jai-imageio-core/). 

Alternatively (e.g. right after a new release), you can use this
explicit [bintray repository](https://dl.bintray.com/jai-imageio/maven/):

    <repositories>
      <repository>
        <id>bintray-jai-imageio</id>
        <name>jai-imageio at bintray</name>
        <url>https://dl.bintray.com/jai-imageio/maven/</url>
        <snapshots>
          <enabled>false</enabled>
        </snapshots>
      </repository>
    </repositories>

The Maven repository include additional artifact types such as `javadoc` and
`sources` which should be picked up by your IDE's Maven integration.


Download
--------

To download the binary JAR, see the 
[Downloads at BinTray](https://bintray.com/jai-imageio/maven/jai-imageio-core-standalone/view)
or the [GitHub releases](https://github.com/jai-imageio/jai-imageio-core/releases)


Javadoc
-------

Standalone [Javadoc for jai-imageio-core](https://jai-imageio.github.io/jai-imageio-core/javadoc/) is also
provided.



Copyright and licenses
----------------------

* Copyright © 2005 Sun Microsystems
* Copyright © 2010-2014 University of Manchester
* Copyright © 2015-2018 jai-imageio contributors

The source code for the jai-imageio-core project is copyrighted code that
is licensed to individuals or companies who download or otherwise
access the code.

The complete copyright notice for this project is in
[COPYRIGHT.txt](COPYRIGHT.txt)

The source code license for this project is **BSD 3-clause** with an
_additional nuclear disclaimer_, see
[LICENSE.txt](LICENSE.txt). 


Changelog
---------

* 1.4.0 - Community bug fix release
  * Build requires Maven 3, Java 6
  * Java 9 fixes (issues #24, #26)
  * Avoid empty string from ImageIO.getReaderMIMETypes (issue #27)
  * Maven version range bug (issues #23, #25)
  * TIFFImageMetadata.java support legacy `com.sun` attributes (issues #19, #20)
  * TIFF parsing gave ClassCastException in isIFDPointer (issue #43)
  * LSB-encoded TIFF images (issues #37, #39)
  * PackageUtil: Fix NPE (issue #34)
  * Fix for a buffer overflow problem in TIFF CCITT T.6 compressor (issue #22)  
* 1.3.1 - Available as OSGi bundle (issue #13). 
      Fixed memory leak in TIFFImageWriter (issue #14).
* 1.3.0 - Java package changed to com.github.jaiimageio (issue #10).
    MANIFEST.MF metadata corrected.
* 1.2.1 - Version 1.2.1 released. Pushing to Maven Central and BinTray. 
      Workaround for OpenJDK8 libjpeg bug (issue #6).
      groupId changed from net.java.dev.jai-imageio to com.github.jai-imageio.
      Fix for PNM ASCII write (issue #7).
* 1.2-pre-dr-b04-2014-09-13 - Removed last jpeg2000 plugin. Javadoc now includes overview.      
* 1.2-pre-dr-b04-2014-09-12 - Separated out [JPEG 2000](https://github.com/jai-imageio/jai-imageio-core/issues/4)
      support from [jai-imageio-core](http://github.com/jai-imageio/jai-imageio-core)
      for [licensing reasons](https://github.com/jai-imageio/jai-imageio-core/issues/4).
      Re-enabled junit test (issue #5).
* 1.2-pre-dr-b04-2013-04-23 - Updated README and pom, newer maven plugins, removed
  broken links to dev.java.net. Javadocs included and published at
  http://jai-imageio.github.com/jai-imageio-core/
* 1.2-pre-dr-b04-2011-07-04 - Avoid Maven many build warnings. Fixed character set.
* 1.2-pre-dr-b04-2010-04-30 - Initial Maven version, based on CVS checkout from
  dev.java.net, but with Maven pom.xml and only code covered
  by open source license.



More info
---------

* https://github.com/jai-imageio/jai-imageio-core
* https://jai-imageio.github.io/jai-imageio-core/javadoc/
* http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html
* https://docs.oracle.com/javase/8/docs/api/javax/imageio/ImageIO.html
* https://docs.oracle.com/javase/8/docs/technotes/guides/imageio/index.html 
* https://docs.oracle.com/javase/8/docs/technotes/guides/imageio/spec/imageio_guideTOC.fm.html

These links are no longer available as `java.net` has shut down:

* https://java.net/projects/jai-imageio-core/
* http://download.java.net/media/jai/builds/release/1_1_3/
