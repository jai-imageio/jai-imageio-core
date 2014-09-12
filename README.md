jai-imageio-core-standalone
===========================

[![Build Status](https://travis-ci.org/stain/jai-imageio-core.svg)](https://travis-ci.org/stain/jai-imageio-core)

NOTE: This is a 'standalone' version of
[jai-imageio-core](https://java.net/projects/jai-imageio-core/) where
dependencies to jai-core (javax.media.jai) and JPEG 2000 (jj2000)
has been removed. This version also does not include any of the C
implementations from libJIIO, meaning that this version is platform independent
and fully redistributable under the 3-clause BSD license in LICENSE.txt
(and thus is GPL compatible).

[JPEG 2000 support](https://github.com/stain/jai-imageio-jpeg2000)
is available as an additional module `jai-imageio-jpeg2000`
with a different (non-GPL compatible) license.

There is **NO FURTHER DEVELOPMENT** in this repository; any commits here are
just to keep the build working with recent versions of Maven/Java - the
date in the version number indicates the time of such modifications
and should not have any effect on functionality.

If you are not concerned about GPL compatibility or source code
availability, you might instead want to use
https://github.com/geosolutions-it/imageio-ext/ which is actively
maintained and extends the original imageio with many useful features,
but depends on the [binary distribution of jai_core](
http://download.osgeo.org/webdav/geotools/javax/media/jai_core/1.1.3/).


Usage
-----

To build this project, use Apache Maven 2.0.9 or newer and run:

    mvn clean install

To use jai-imageio-core-standalone from a Maven project, add:

    <dependency>
        <groupId>net.java.dev.jai-imageio</groupId>
        <artifactId>jai-imageio-core-standalone</artifactId>
        <version>1.2-pre-dr-b04-2014-09-12</version>
    </dependency>

and:

    <repositories>
        <repository>
            <releases />
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>mygrid-repository</id>
            <name>myGrid Repository</name>
            <url>http://www.mygrid.org.uk/maven/repository</url>
        </repository>
    </repositories>

This repository includes source JARs and javadoc, which should be picked
up for instance by the Eclipse Maven support.  

Download
--------

To download the binary JARs, browse the 
[Maven repository](http://www.mygrid.org.uk/maven/repository/net/java/dev/jai-imageio/jai-imageio-core-standalone/).


Javadoc
-------

Standalone [Javadoc for jai-imageio-core](http://stain.github.io/jai-imageio-core/javadoc/) is also
provided.



Copyright and licenses
----------------------

* Copyright © 2005 Sun Microsystems
* Copyright © 2010-2014 University of Manchester

The source code for the jai-imageio-core project is copyrighted code that
is licensed to individuals or companies who download or otherwise
access the code.

The complete copyright notice for this project is in
[COPYRIGHT.md](COPYRIGHT.md)

The source code license for this project is BSD 3-clause, see
[LICENSE.txt](LICENSE.txt)




Changelog
---------

* 2010-04-30 - Initial Maven version, based on CVS checkout from
  dev.java.net, but with Maven pom.xml and only code covered
  by open source license.
* 2011-07-04 - Avoid Maven many build warnings. Fixed character set.
* 2013-04-23 - Updated README and pom, newer maven plugins, removed
  broken links to dev.java.net. Javadocs included and published at
  http://stain.github.com/jai-imageio-core/
* 2014-09-12 -  Separated out [JPEG 2000](https://github.com/stain/jai-imageio-core/issues/4)
      support from [jai-imageio-core](http://github.com/stain/jai-imageio-core)
      for [licensing reasons](https://github.com/stain/jai-imageio-core/issues/4).
      Re-enabled junit test (issue #5).



More info
---------

* https://github.com/stain/jai-imageio-core
* http://stain.github.io/jai-imageio-core/javadoc/
* https://java.net/projects/jai-imageio-core/
* http://www.oracle.com/technetwork/java/current-142188.html
* http://download.java.net/media/jai/builds/release/1_1_3/
