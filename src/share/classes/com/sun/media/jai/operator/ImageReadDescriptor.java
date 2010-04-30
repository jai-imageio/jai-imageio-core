/*
 * $RCSfile: ImageReadDescriptor.java,v $
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
 * $Date: 2005/12/01 00:40:32 $
 * $State: Exp $
 */
package com.sun.media.jai.operator;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ContextualRenderedImageFactory;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.EventListener;
import java.util.Locale;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.PropertyGenerator;
import javax.media.jai.RenderedOp;
import javax.media.jai.RenderableOp;
import javax.media.jai.registry.CollectionRegistryMode;
import javax.media.jai.registry.RenderableRegistryMode;
import javax.media.jai.registry.RenderedRegistryMode;
import com.sun.media.jai.util.PropertyGeneratorImpl;

/**
 * An <code>OperationDescriptor</code> describing the "ImageRead" operation.
 *
 * <p>The "ImageRead" operation uses the
 * <a href="http://java.sun.com/j2se/1.4/docs/guide/imageio/index.html">Java
 * Image I/O Framework</a> to read images from an input source. Which formats
 * may be read depends on which {@link javax.imageio.ImageReader} plug-ins are
 * registered with the Image I/O Framework when the operation is invoked.</p>
 *
 * <p>The input source will usually be an
 * {@link javax.imageio.stream.ImageInputStream}, but may be a
 * {@link java.io.File}, {@link java.io.RandomAccessFile},
 * {@link java.io.InputStream}, {@link java.net.URL},
 * {@link java.net.Socket}, {@link java.nio.channels.ReadableByteChannel},
 * file path represented as a <code>String</code> or some other type
 * compatible with a reader plug-in. The
 * {@link javax.imageio.ImageIO} class should be used to specify the location
 * and enable the use of cache files via its <code>setCacheDirectory()</code>
 * and <code>setUseCache()</code> methods, respectively. Note that this cache
 * pertains to image stream caching and is unrelated to the JAI
 * <code>TileCache</code>.</p>
 *
 * <p>The "ImageRead" operation supports <a href="#RenderedMode">rendered</a>,
 * <a href="#RenderableMode">renderable</a>, and
 * <a href="#CollectionMode">collection</a> modes and requires no source image.
 * A {@link java.awt.RenderingHints} object may be supplied when the operation
 * is created. In addition to the {@link java.awt.RenderingHints.Key} hints
 * recognized by the eventual <code>OpImage</code> constructor, an
 * <a href="#ImageLayout"><code>ImageLayout</code> hint</a> may also be
 * supplied. The settings of this <code>ImageLayout</code> override any other
 * possible derivation of its components. In particular, it is possible that
 * the generated image(s) have a different tile layout than that present in
 * the image read from the input source.</p>
 *
 * <p>Image properties are used to make available metadata and other
 * information. Property provision is mode-specific.</p>
 *
 * <p><table border=1>
 * <caption><b>Resource List</b></caption>
 * <tr><th>Name</th>        <th>Value</th></tr>
 * <tr><td>GlobalName</td>  <td>ImageRead</td></tr>
 * <tr><td>LocalName</td>   <td>ImageRead</td></tr>
 * <tr><td>Vendor</td>      <td>com.sun.media.jai</td></tr>
 * <tr><td>Description</td> <td>Reads an image using the Java Image I/O Framework.</td></tr>
 * <tr><td>DocURL</td>      <td>http://java.sun.com/products/java-media/jai/forDevelopers/jai-imageio-1_0-rc-docs/com/sun/media/jai/operator/ImageReadDescriptor.html</td></tr>
 * <tr><td>Version</td>     <td>1.0</td></tr>
 * <tr><td>arg0Desc</td>    <td>The input source.</td></tr>
 * <tr><td>arg1Desc</td>    <td>The index or indices of the image(s) to read.</td></tr>
 * <tr><td>arg2Desc</td>    <td>Whether metadata should be read if available.</td></tr>
 * <tr><td>arg3Desc</td>    <td>Whether thumbnails should be read if available.</td></tr>
 * <tr><td>arg4Desc</td>    <td>Whether to verify the validity of the input source.</td></tr>
 * <tr><td>arg5Desc</td>    <td>EventListeners to be registered with the ImageReader.</td></tr>
 * <tr><td>arg6Desc</td>    <td>The Locale for the ImageReader to use.</td></tr>
 * <tr><td>arg7Desc</td>    <td>Java Image I/O read parameter instance.</td></tr>
 * <tr><td>arg8Desc</td>    <td>Java Image I/O reader instance.</td></tr>
 * </table></p>
 *
 * <h2><a name="RenderedMode"</a>Rendered Mode</h2>
 *
 * In rendered mode the "ImageRead" operation creates a
 * {@link java.awt.image.RenderedImage} from the specified input source.
 *
 * <h3><a name="RenderedModeParameters"</a>Rendered Mode Parameters</h3>
 *
 * The parameter list of the "ImageRead" operation in rendered mode is
 * as in the following table.
 *
 * <p><table border=1>
 * <caption><b>Rendered Mode Parameter List</b></caption>
 * <tr><th>Name</th>           <th>Class Type</th>
 *                             <th>Default Value</th></tr>
 * <tr><td><a href="#ParamInput">
 *     Input</a></td>          <td>java.lang.Object.class</td>
 *                             <td>NO_PARAMETER_DEFAULT</td>
 * <tr><td><a href="#ParamImageChoice">
 *     ImageChoice</a></td>    <td>java.lang.Integer</td>
 *                             <td>0</td>
 * <tr><td><a href="#ParamReadMetadata">
 *     ReadMetadata</a></td>   <td>java.lang.Boolean</td>
 *                             <td>TRUE</td>
 * <tr><td><a href="#ParamReadThumbnails">
 *     ReadThumbnails</a></td> <td>java.lang.Boolean</td>
 *                             <td>TRUE</td>
 * <tr><td><a href="#ParamVerifyInput">
 *     VerifyInput</a></td>    <td>java.lang.Boolean</td>
 *                             <td>TRUE</td>
 * <tr><td><a href="#ParamListeners">
 *     Listeners</a></td>      <td>java.util.EventListener[]</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamLocale">
 *     Locale</a></td>         <td>java.util.Locale</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamReadParam">
 *     ReadParam</a></td>      <td>javax.imageio.ImageReadParam</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamReader">
 *     Reader</a></td>         <td>javax.imageio.ImageReader</td>
 *                             <td>null</td>
 * </table></p>
 *
 * <p>The rendered mode parameters are handled as follows:
 *
 * <ul>
 * <p><li><a name="ParamInput"</a>
 * If Input is a String it is assumed to represent a file path. When
 *   the node is rendered, if Input is not already an ImageInputStream, then
 *   ImageIO.createImageInputStream() will be used to attempt to derive an
 *   ImageInputStream. If such an ImageInputStream is available, it will be
 *   set as the input of the ImageReader; otherwise the original value of
 *   Input will be used. Before attempting to apply createImageInputStream(),
 *   if Input is a String it will be converted to a RandomAccessFile, or if it
 *   is a Socket or a URL to an InputStream. If the conversion of the Input
 *   String to RandomAccessFile fails, it will be converted to an
 *   InputStream by accessing it as a resource bundled in a JAR file.</li></p>
 * 
 * <p><li><a name="ParamImageChoice"</a>
 * If ImageChoice is negative an exception will be thrown when the
 * operation is created.</li></p>
 * 
 * <p><li><a name="ParamReadMetadata"</a>
 * Image properties associated with metadata will be set if and only if
 *   ReadMetadata is TRUE and the respective metadata are defined. This
 *   parameter overrides the setting of the ignoreMetadata field of the
 *   Reader parameter if the latter is also supplied. Metadata property
 *   values will not be set until requested, i.e., their computation is
 *   deferred.</li></p>
 * 
 * <p><li><a name="ParamReadThumbnails"</a>
 * The image property associated with thumbnails will be set if and only if
 *   ReadThumbnails is TRUE and thumbnails are defined for the image at the
 *   specified index. Thumbnail property values are not set until requested,
 *   i.e., their computation is deferred.</li></p>
 * 
 * <p><li><a name="ParamVerifyInput"</a>
 * If VerifyInput is TRUE and Input is either a File or a String which
 *   specifies a file path, then canRead() will be invoked on Input or a
 *   derived File object as appropriate. If canRead() returns 'false' an
 *   exception will be thrown. Note that the canRead() method can not be
 *   invoked in the case of the Input being a String specifying a file path
 *   which can be accessed only as an InputStream resource from a JAR file.
 *   This option is useful to suppress verifying
 *   the existence of a file on the local file system when this operation is
 *   created as the local portion of a remote operation which will be
 *   rendered on a remote peer. This verification will occur when the
 *   operation is created. </li></p>
 * 
 * <p><li>If VerifyInput is TRUE and Input is a Socket, then an exception will
 *   be thrown if the socket is not bound, not connected, is closed, or its
 *   read-half is shut down. This verification will occur when the
 *   operation is created.</li></p>
 * 
 * <p><li><a name="ParamListeners"</a>
 * Listeners will be used to set any progress, update, or warning listeners
 * of the ImageReader. Each element in the java.util.EventListener array will
 * be added for all types of listener it implements. For example if a listener
 *   object implements all of the javax.imageio.event.IIORead*Listeners
 *   interfaces then it will be added as a listener of each of the three types.
 *   Any elements in the array which do not implement any of the
 * IIORead*Listeners will be ignored.</li></p>
 * 
 * <p><li><a name="ParamLocale"</a>
 * Locale will be used to set the Locale of the ImageReader. This parameter
 *   overrides the equivalent setting of the Reader parameter if the latter is
 *   also supplied.</li></p>
 * 
 * <p><li><a name="ParamReadParam"</a>
 * If ReadParam is null, an ImageReadParam will be derived internally using
 * ImageReader.getDefaultReadParam(). Supplying an ImageReadParam can be
 * useful for special operations such as setting the bands to read via
 * setSourceBands() or the subsampling factors via
 * setSourceSubsampling(). If the ImageReadParam parameter is not an instance
 * of a cloneable subclass of javax.imageio.ImageReadParam, then it would be
 * best to avoid using the setDestinationType(), setDestinationOffset(), and
 * setSourceRegion() methods on the supplied object as these methods will be
 * invoked internally by the reader.</li></p>
 * 
 * <p><li><a name="ParamReader"</a>
 * If Reader is null, an attempt will be made to derive an ImageReader
 *   using ImageIO.getImageReaders().</li></p>
 * </ul></p>
 *
 * <h4><a name="SyncPolicy"</a>Parameters and Synchronization Policy</h4>
 *
 * Note that any supplied ImageReadParam parameter may be modified within this
 * operator. Also, any of the various reading methods of the ImageReader may be
 * invoked at an arbitrary subsequent time to populate the image data. Correct
 * handling of these parameters at the application level is left to the user.
 * Specifically no guarantee as to the correct behavior of this operation is
 * made in the cases wherein a user passes in an ImageReadParam or ImageReader
 * parameter and modifies its state while this operation is still using these
 * objects. This applies especially in the case of multi-threaded applications.
 * In such instances it is recommended that the user either not pass in either
 * of these parameters or simply use the Java Image I/O API directly rather
 * than the JAI operation. (Threads managed internally by JAI, e.g., in the
 * TileScheduler, interact with the image object created by this operation only
 * via synchronized methods thereby preventing potential race conditions.)
 * These caveats also apply to the use of ImageReaders and ImageReadParams
 * obtained from image properties.
 * 
 * <p>The foregoing policy regarding modification of any supplied ImageReadParam
 * or ImageReader is necessary as neither of these classes is cloneable. Given
 * that as a starting point there are in effect three possibilities: (A) do not
 * accept them as parameters, (B) accept them via alternate parameters which do
 * not pose these problems (for example an ImageReaderSpi and a long list of
 * settings represented by the ImageReadParam), or (C) accept them explicitly.
 * Option C has been deemed preferable despite the potential race condition
 * issues.</p>
 * 
 * <p>In the Sun Microsystems implementation of this operation these potential
 * conflicts have been mitigated to a certain extent:
 *
 * <ul>
 * <p><li>If the param is cloneable then it is cloned and the clone used internally.
 *   Otherwise if the param is an instance of ImageReadParam itself rather than
 *   of a subclass thereof, i.e., getClass().getName() invoked on the param
 *   returns "javax.imageio.ImageReadParam", then a new ImageReadParam is
 *   constructed and the settings of the original param copied to it. If the
 *   param is not cloneable and is an instance of a proper subclass of
 *   ImageWriteParam then it is used directly.</li></p>
 * 
 * <p><li>The only ImageReader methods invoked after OpImage construction are
 *   read(int,ImageReadParam), getNumThumbnails(int), readThumbnail(int,int),
 *   isIgnoringMetadata(), getStreamMetadata(), and getImageMetadata(int).
 *   These methods are invoked within getProperty() and with the exception
 *   of isIgnoringMetadata() are synchronized on the ImageReader.</li></p>
 * </ul>
 * </p>
 *
 * <h3><a name="ImageLayout"</a>ImageLayout Hint Handling</h3>
 *
 * If an ImageLayout hint is provided via the operation's RenderingHints, its
 * values will be used. In particular a SampleModel or ColorModel supplied via
 * an ImageLayout hint will override any values set via the ImageTypeSpecifier
 * of the ImageReadParam parameter if the latter is non-null. This signifies
 * that the ImageTypeSpecifier of the OpImage rendering associated with the
 * operation node will be forced to match that of the layout even if this type
 * is different from or incompatible with the image types available from the
 * ImageReader. Note that in such an eventuality an extra amount of memory
 * equal to one image tile might be needed for copying purposes. This copying
 * is handled by the JAI operation itself.
 * 
 * <p>Any fields of the supplied ImageLayout which are not set will be set to
 * default values as follows. The ImageLayout will be cloned before it is
 * modified.</p>
 *
 * <h4>ImageLayout Defaults</h4>
 *
 * <p><ul>
 * <p><li>Image Bounds {minX, minY, width, height}
 * 
 * <p>Each value defaults to the corresponding value of the destination
 * which would be derived on the basis of the source image dimensions
 * and the settings of the ImageReadParam, i.e., source region,
 * subsampling offsets and factors, and destination offset.</p>
 * 
 * <p>It should be noted that unlike in the Java Image I/O API itself,
 * negative coordinates are permitted and the image origin is not
 * required to be at (0,0) as for BufferedImages. Therefore it is
 * possible that a given image be loaded using the same ImageReadParam
 * by an ImageReader and by the "ImageRead" operation with different
 * results. Possible differences would be that the portion of the
 * image with negative coordinates is not clipped as it would be with
 * direct Image I/O access, and no empty extent between (0,0) and the
 * start of the data will be present.</p>
 * 
 * <p>For example, if the ImageReadParam had sourceRegion [0,0,w,h],
 * destinationOffset [-w/2,-h/2], and no subsampling, then the Java
 * Image I/O API would compute the effective source and destination
 * regions to be [w/2,h/2,w/2,h/2] and [0,0,w/2,h/2], respectively.
 * The JAI ImageRead operation would compute the effective source and
 * destination regions to be [0,0,w,h] and [-w/2,-h/2,w,h], respectively.
 * The Image I/O result would therefore be equal to the bottom right
 * quadrant of the JAI result.</p></li></p>
 * 
 * <p><li>Tile Grid {tileGridXOffset, tileGridYOffset, tileWidth, tileHeight}
 *
 * <pre>
 * tileGridXOffset = ImageReader.getTileGridXOffset(imageIndex);
 * tileGridYOffset = ImageReader.getTileGridYOffset(imageIndex);
 * tileWidth = ImageReader.getTileWidth(imageIndex);
 * tileHeight = ImageReader.getTileHeight(imageIndex);
 * </pre></li></p>
 * 
 * <p><li>ColorModel
 * 
 * <pre>
 * ImageReader.getRawImageType(imageIndex).getColorModel();
 * </pre></li></p>
 * 
 * <p><li>SampleModel
 * 
 * <pre>
 * ImageReader.getRawImageType(imageIndex).getSampleModel().createCompatibleSampleModel(tileWidth, tileHeight);
 * </pre></li></p>
 * </ul></p>
 *
 * <h3><a name="RenderedModeProperties"</a>Image Properties in Rendered Mode</h3>
 *
 * Image properties are used to provide metadata, thumbnails, and reader-related
 * information. The following properties may be obtained from the RenderedOp
 * created for the "ImageRead" operation in rendered mode:
 *
 * <p><table border=1>
 * <caption><b>Rendered Mode Image Properties</b></caption>
 * <tr>
 * <th>Property Name</th>
 * <th>Type</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>JAI.ImageReadParam</td>
 * <td>ImageReadParam</td>
 * <td>Set to ImageReadParam actually used which may differ from the one passed in.</td>
 * </tr>
 * <tr>
 * <td>JAI.ImageReader</td>
 * <td>ImageReader</td>
 * <td>Set to ImageReader actually used.</td>
 * </tr>
 * <tr>
 * <td>JAI.ImageMetadata</td>
 * <td>IIOMetadata</td>
 * <td>Set if and only if ReadMetadata parameter is TRUE and image metadata are available.</td>
 * </tr>
 * <tr>
 * <td>JAI.StreamMetadata</td>
 * <td>IIOMetadata</td>
 * <td>Set if and only if ReadMetadata parameter is TRUE and stream metadata are available.</td>
 * </tr>
 * <tr>
 * <td>JAI.Thumbnails</td>
 * <td>BufferedImage[]</td>
 * <td>Set if and only if ReadThumbnails parameter is TRUE and thumbnails are available.</td>
 * </tr>
 * </table></p>
 * 
 * <p>If a given property is not set, this implies of course that the names of
 * absent properties will not appear in the array returned by getPropertyNames()
 * and getProperty() invoked to obtain absent properties will return
 * java.awt.Image.UndefinedProperty as usual.</p>
 * 
 * <p>The ImageReader and ImageReadParam may be used for subsequent invocations
 * of the operation (for example to obtain different images in a multi-page file)
 * or for informational purposes. Care should be taken in using these property
 * values with respect to the synchronization issues previously discussed.</p>
 * 
 * <p>In all cases image metadata properties will be set when the node is rendered,
 * i.e., metadata reading is not subject to the same deferred execution as is
 * image data reading. The thumbnail property value will not be set however until
 * its value is actually requested.</p>
 *
 * <h2><a name="RenderableMode"</a>Renderable Mode</h2>
 *
 * In renderable mode the "ImageRead" operation creates a
 * {@link java.awt.image.renderable.RenderableImage} from the specified
 * input source.
 *
 * <p>It should be noted that although they are discussed in the context of
 * rendered mode, the <a href="#SyncPolicy">parameter synchronization
 * policy</a> and <a href="#ImageLayout">ImageLayout handling methodology</a>
 * apply to renderable mode as well.</p>
 *
 * <h3><a name="RenderableModeParameters"</a>Renderable Mode Parameters</h3>
 *
 * The parameter list of the "ImageRead" operation in renderable mode is
 * identical to the <a href="#RenderedModeParameters">rendered mode parameter
 * list</a> mode except as indicated in the following table.
 *
 * <p><table border=1>
 * <caption><b>Renderable Mode Parameter Differences</b></caption>
 * <tr><th>Name</th>           <th>Class Type</th>
 *                             <th>Default Value</th></tr>
 * <tr><td>ImageChoice</td>    <td>int[]</td>
 *                             <td>int[] {0,...,NumImages-1}</td>
 * </table></p>
 *
 * <p>In the Sun Microsystems renderable mode implementation of the "ImageRead"
 * operation, when createRendering() is invoked on the RenderableImage created
 * by the operation, a MultiResolutionRenderableImage is constructed from a
 * Vector of RenderedImages consisting of the images at the specified indices.
 * These images will be sorted into order of decreasing resolution (as
 * determined by the product of width and height for each image) and inserted
 * in this order in the Vector of images used to construct the
 * MultiResolutionRenderableImage. Metadata will be set on the component
 * RenderedImages as usual for rendered mode. Finally the
 * createRendering() invocation will be forwarded to the underlying
 * MultiResolutionRenderableImage and the resulting RenderedImage returned.</p>
 *
 * <p>Note that using this approach the entire MultiResolutionRenderableImage
 * must be regenerated for each invocation of createRendering(). If multiple
 * renderings are to be created from the RenderableImage without changing
 * the operation parameters, then a more efficient approach would be to use the
 * "JAI.RenderableInput" property to be described.</p>
 *
 * <h3>Image Properties in Renderable Mode</h3>
 *
 * The following property will be set on the RenderableOp created for the
 * "ImageRead" operation in renderable mode:
 *
 * <p><table border=1>
 * <caption><b>Renderable Mode Image Properties</b></caption>
 * <tr>
 * <th>Property Name</th>
 * <th>Type</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>JAI.RenderableInput</td>
 * <td>RenderableImage</td>
 * <td>A RenderableImage derived from the input source according to the supplied set of parameters.</td>
 * </tr>
 * </table></p>
 * 
 * <p>The RenderableImage which is the value of the foregoing property may have
 * set on it any of the properties previously described for rendered mode
 * contingent on parameter settings and data availability. The image metadata
 * and thumbnail properties would be copied from the highest resolution image
 * among those specified by the ImageChoice parameter.</p>
 * 
 * <p>If multiple renderings are to be created from the RenderableImage
 * without changing the operation parameters, then an efficient alternative
 * approach to multiple invocations of createRendering() on the RenderableImage
 * is to obtain the RenderableImage value of the "JAI.RenderableInput" property
 * and invoke createRendering() on this value.</p>
 *
 * <h2><a name="CollectionMode"</a>Collection Mode</h2>
 *
 * In collection mode the "ImageRead" operation creates a
 * <code>Collection</code> of <code>RenderedImage</code>s from the specified
 * input source. This could be used for example to load an animated GIF
 * image or a multi-page TIFF image.
 *
 * <p>It should be noted that although they are discussed in the context of
 * rendered mode, the <a href="#SyncPolicy">parameter synchronization
 * policy</a> and <a href="#ImageLayout">ImageLayout handling methodology</a>
 * apply to collection mode as well.</p>
 *
 * <h3>Collection Mode Parameters</h3>
 *
 * The parameter list of the "ImageRead" operation in collection mode is
 * identical to the <a href="#RenderableModeParameters">renderable mode
 * parameter list</a>. In this case
 * the RenderedImages loaded for the specified indices will be used to create
 * a Collection of RenderedImages. The images will be loaded in the order of
 * the indices in the supplied array and appended to a List. The rendering of
 * the operation will be a CollectionImage the 'imageCollection' instance
 * variable of which will be set to this List.
 *
 * <h3>Image Properties in Collection Mode</h3>
 *
 * Contingent on parameter settings and the presence of the appropriate
 * metadata, the rendered Collection may have the "JAI.StreamMetadata",
 * "JAI.ImageReadParam", and "JAI.ImageReader" properties set. Each
 * RenderedImage in the Collection may contain the
 * <a href="#RenderedModeProperties">rendered mode properties</a> contingent
 * on parameter settings and data availability.
 *
 * @see javax.media.jai.OperationDescriptor
 * @see javax.imageio.ImageReader
 * @see javax.imageio.ImageReadParam
 * @see javax.imageio.metadata.IIOMetadata
 * @see javax.imageio.stream.ImageInputStream
 */
public class ImageReadDescriptor extends OperationDescriptorImpl {

    // Property name constants have package access for image factory use.

    /** ImageReadParam property name "JAI.ImageReadParam". */
    public static final String PROPERTY_NAME_IMAGE_READ_PARAM =
        "JAI.ImageReadParam";

    /** ImageReader property name "JAI.ImageReader". */
    public static final String PROPERTY_NAME_IMAGE_READER =
        "JAI.ImageReader";

    /** Image metadata property name "JAI.ImageMetadata". */
    public static final String PROPERTY_NAME_METADATA_IMAGE =
        "JAI.ImageMetadata";

    /** Stream metadata property name "JAI.StreamMetadata". */
    public static final String PROPERTY_NAME_METADATA_STREAM =
        "JAI.StreamMetadata";

    /** Thumbnail property name "JAI.Thumbnails". */
    public static final String PROPERTY_NAME_THUMBNAILS =
        "JAI.Thumbnails";

    /**
     * Renderable input property name "JAI.RenderableInput".
     */
    public static final String PROPERTY_NAME_RENDERABLE_INPUT =
        "JAI.RenderableInput";

    /**
     * Test method.
     *
     * @param args {inputFile[, mode]}
     * @throws Throwable any error.
     */
    /* XXX
    public static void main(String[] args) {
        String fileName = args[0];
        String modeName = args.length > 1 ?
            args[1] : RenderedRegistryMode.MODE_NAME;

        ParameterBlock pb =
            (new ParameterBlock()).add(new java.io.File(fileName));

        java.awt.image.RenderedImage[] images = null;
        if(modeName.equalsIgnoreCase(RenderedRegistryMode.MODE_NAME)) {
            images = new java.awt.image.RenderedImage[1];
            images[0] = JAI.create("ImageRead", pb, null);
            PrintProps.print((javax.media.jai.PropertySource)images[0]);
        } else if(modeName.equalsIgnoreCase(RenderableRegistryMode.MODE_NAME)) {
            //pb.add(new int[] {0});
            java.awt.image.renderable.RenderableImage ri =
                JAI.createRenderable("ImageRead", pb, null);
            PrintProps.print((javax.media.jai.PropertySource)ri);
            images = new java.awt.image.RenderedImage[1];
            //java.awt.image.renderable.RenderContext rc =
            //    new java.awt.image.renderable.RenderContext(
            //        new java.awt.geom.AffineTransform(42, 0, 0, 42, 0, 0));
            //images[0] = ri.createRendering(rc);
            images[0] = ri.createDefaultRendering();
            PrintProps.print((javax.media.jai.PropertySource)images[0]);
        } else if(modeName.equalsIgnoreCase(CollectionRegistryMode.MODE_NAME)) {
            //pb.add(new int[] {0});
            java.util.Collection imageCollection =
                JAI.createCollection("ImageRead", pb, null);
            PrintProps.print((javax.media.jai.PropertySource)imageCollection);
            images = new java.awt.image.RenderedImage[imageCollection.size()];
            imageCollection.toArray(images);
        } else {
            throw new UnsupportedOperationException(modeName+" mode not supported");
        }

        final java.awt.Frame frame = new java.awt.Frame();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    frame.setEnabled(false);
                    frame.dispose();
                }
            });

        int gridSide = (int)(Math.sqrt(images.length) + 0.5);
        frame.setLayout(new java.awt.GridLayout(gridSide, gridSide));
        java.awt.Dimension screenSize =
            java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width =
            Math.min(screenSize.width/gridSide, images[0].getWidth());
        int height =
            Math.min(screenSize.height/gridSide, images[0].getHeight());
        for(int i = 0; i < images.length; i++) {
            javax.media.jai.widget.ScrollingImagePanel panel =
                new javax.media.jai.widget.ScrollingImagePanel(images[i],
                                                               width, //image.getWidth(),
                                                               height);//image.getHeight());
            frame.add(panel);
        }
        frame.pack();
        frame.show();
    }
    */

    /**
     * The name of the operation.
     */
    private static final String OPERATION_NAME = "ImageRead";

    /**
     * The resource strings that provide the general documentation and
     * specify the parameter list for the "ImageRead" operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  OPERATION_NAME},
        {"LocalName",   OPERATION_NAME},
        {"Vendor",      "com.sun.media.jai"},
        {"Description", I18N.getString("ImageReadDescriptor0")},
        {"DocURL",      "http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/ImageReadDescriptor.html"},
        {"Version",     I18N.getString("DescriptorVersion")},
        {"arg0Desc",    I18N.getString("ImageReadDescriptor1")},
        {"arg1Desc",    I18N.getString("ImageReadDescriptor2")},
        {"arg2Desc",    I18N.getString("ImageReadDescriptor3")},
        {"arg3Desc",    I18N.getString("ImageReadDescriptor4")},
        {"arg4Desc",    I18N.getString("ImageReadDescriptor5")},
        {"arg5Desc",    I18N.getString("ImageReadDescriptor6")},
        {"arg6Desc",    I18N.getString("ImageReadDescriptor7")},
        {"arg7Desc",    I18N.getString("ImageReadDescriptor8")},
	{"arg8Desc",    I18N.getString("ImageReadDescriptor9")}
    };

    /** The parameter names for the "ImageRead" operation. */
    private static final String[] paramNames = {
        "Input", "ImageChoice", "ReadMetadata", "ReadThumbnails",
        "VerifyInput", "Listeners", "Locale", "ReadParam", "Reader"
    };

    /** The parameter class types for rendered mode of "ImageRead". */
    private static final Class[] renderedParamClasses = {
        java.lang.Object.class,             // Input
        java.lang.Integer.class,            // ImageChoice
	java.lang.Boolean.class,            // ReadMetadata
	java.lang.Boolean.class,            // ReadThumbnails
	java.lang.Boolean.class,            // VerifyInput
        java.util.EventListener[].class,    // Listeners
        java.util.Locale.class,             // Locale
	javax.imageio.ImageReadParam.class, // ReadParam
	javax.imageio.ImageReader.class     // Reader
    };

    /** The parameter default values for rendered mode of "ImageRead". */
    private static final Object[] renderedParamDefaults = {
        NO_PARAMETER_DEFAULT, // Input
        new Integer(0),       // ImageChoice
        Boolean.TRUE,         // ReadMetadata
        Boolean.TRUE,         // ReadThumbnails
        Boolean.TRUE,         // VerifyInput
        null,                 // Listeners
        null,                 // Locale
        null,                 // ReadParam
        null                  // Reader
    };

    /** The parameter class types for renderable mode of "ImageRead". */
    private static final Class[] renderableParamClasses = {
        java.lang.Object.class,             // Input
        int[].class,                        // ImageChoice
	java.lang.Boolean.class,            // ReadMetadata
	java.lang.Boolean.class,            // ReadThumbnails
	java.lang.Boolean.class,            // VerifyInput
        java.util.EventListener[].class,    // Listeners
        java.util.Locale.class,             // Locale
	javax.imageio.ImageReadParam.class, // ReadParam
	javax.imageio.ImageReader.class     // Reader
    };

    /** The parameter default values for renderable mode of "ImageRead". */
    private static final Object[] renderableParamDefaults = {
        NO_PARAMETER_DEFAULT, // Input
        null,                 // ImageChoice
        Boolean.TRUE,         // ReadMetadata
        Boolean.TRUE,         // ReadThumbnails
        Boolean.TRUE,         // VerifyInput
        null,                 // Listeners
        null,                 // Locale
        null,                 // ReadParam
        null                  // Reader
    };

    /** The parameter class types for collection mode of "ImageRead". */
    private static final Class[] collectionParamClasses =
        renderableParamClasses;

    /** The parameter default values for collection mode of "ImageRead". */
    private static final Object[] collectionParamDefaults =
        renderableParamDefaults;

    /** Constructor. */
    public ImageReadDescriptor() {
        super(resources,
              new String[] {RenderedRegistryMode.MODE_NAME,
                            RenderableRegistryMode.MODE_NAME,
                            CollectionRegistryMode.MODE_NAME},
              null, // sourceNames
              new Class[][] {null, null, null}, // sourceClasses
              paramNames,
              new Class[][] {renderedParamClasses,
                             renderableParamClasses,
                             collectionParamClasses},
              new Object[][] {renderedParamDefaults,
                              renderableParamDefaults,
                              collectionParamDefaults},
              new Object[][] {null, null, null}); // validParamValues
    }

    /**
     * Type-safe convenience method for creating a {@link RenderedOp}
     * representing the "ImageRead" operation in rendered mode. The
     * method packs the parameters into a new <code>ParameterBlock</code>
     * and invokes {@link JAI#create(String,ParameterBlock,RenderingHints)}.
     *
     * @param input The input source.
     * @param imageChoice The index of the image to read.
     * @param readMetadata Whether metadata should be read if available.
     * @param readThumbnails Whether thumbnails should be read if available.
     * @param verifyInput Whether to verify the validity of the input source.
     * @param listeners EventListeners to be registered with the ImageReader.
     * @param locale The Locale for the ImageReader to use.
     * @param readParam Java Image I/O read parameter instance.
     * @param reader Java Image I/O reader instance.
     * @param hints Hints possibly including an <code>ImageLayout</code>.
     * @return an image derived from the input source.
     */
    public static RenderedOp create(ImageInputStream input,
                                    Integer imageChoice,
                                    Boolean readMetadata,
                                    Boolean readThumbnails,
                                    Boolean verifyInput,
                                    EventListener[] listeners,
                                    Locale locale,
                                    ImageReadParam readParam,
                                    ImageReader reader,
                                    RenderingHints hints) {

        ParameterBlock args = new ParameterBlock();

        args.add(input);
        args.add(imageChoice);
        args.add(readMetadata);
        args.add(readThumbnails);
        args.add(verifyInput);
        args.add(listeners);
        args.add(locale);
        args.add(readParam);
        args.add(reader);

        return JAI.create(OPERATION_NAME, args, hints);
    }

    /**
     * Type-safe convenience method for creating a {@link Collection}
     * representing the "ImageRead" operation in collection mode. The
     * method packs the parameters into a new <code>ParameterBlock</code>
     * and invokes
     * {@link JAI#createCollection(String,ParameterBlock, RenderingHints)}.
     *
     * @param input The input source.
     * @param imageChoice The indices of the images to read.
     * @param readMetadata Whether metadata should be read if available.
     * @param readThumbnails Whether thumbnails should be read if available.
     * @param verifyInput Whether to verify the validity of the input source.
     * @param listeners EventListeners to be registered with the ImageReader.
     * @param locale The Locale for the ImageReader to use.
     * @param readParam Java Image I/O read parameter instance.
     * @param reader Java Image I/O reader instance.
     * @param hints Hints possibly including an <code>ImageLayout</code>.
     * @return a collection of images derived from the input source.
     */
    public static Collection createCollection(ImageInputStream input,
                                              int[] imageChoice,
                                              Boolean readMetadata,
                                              Boolean readThumbnails,
                                              Boolean verifyInput,
                                              EventListener[] listeners,
                                              Locale locale,
                                              ImageReadParam readParam,
                                              ImageReader reader,
                                              RenderingHints hints) {

        ParameterBlock args = new ParameterBlock();

        args.add(input);
        args.add(imageChoice);
        args.add(readMetadata);
        args.add(readThumbnails);
        args.add(verifyInput);
        args.add(listeners);
        args.add(locale);
        args.add(readParam);
        args.add(reader);

        return JAI.createCollection(OPERATION_NAME, args, hints);
    }

    /**
     * Type-safe convenience method for creating a {@link RenderableOp}
     * representing the "ImageRead" operation in renderable mode. The
     * method packs the parameters into a new <code>ParameterBlock</code>
     * and invokes
     * {@link JAI#createRenderable(String,ParameterBlock,RenderingHints)}.
     *
     * @param input The input source.
     * @param imageChoice The indices of the images to read.
     * @param readMetadata Whether metadata should be read if available.
     * @param readThumbnails Whether thumbnails should be read if available.
     * @param verifyInput Whether to verify the validity of the input source.
     * @param listeners EventListeners to be registered with the ImageReader.
     * @param locale The Locale for the ImageReader to use.
     * @param readParam Java Image I/O read parameter instance.
     * @param reader Java Image I/O reader instance.
     * @param hints Hints possibly including an <code>ImageLayout</code>.
     * @return an image capable of rendering an image from those in the
     * input source.
     */
    public static RenderableOp createRenderable(ImageInputStream input,
                                                int[] imageChoice,
                                                Boolean readMetadata,
                                                Boolean readThumbnails,
                                                Boolean verifyInput,
                                                EventListener[] listeners,
                                                Locale locale,
                                                ImageReadParam readParam,
                                                ImageReader reader,
                                                RenderingHints hints) {

        ParameterBlock args = new ParameterBlock();

        args.add(input);
        args.add(imageChoice);
        args.add(readMetadata);
        args.add(readThumbnails);
        args.add(verifyInput);
        args.add(listeners);
        args.add(locale);
        args.add(readParam);
        args.add(reader);

        return JAI.createRenderable(OPERATION_NAME, args, hints);
    }

    /**
     * Returns the array of {@link PropertyGenerator}s for the specified
     * mode of this operation.
     *
     * <p>For renderable mode returns an array containing a single
     * <code>PropertyGenerator</code> which defines a
     * {@link RenderableImage}-valued property named "JAI.RenderableInput".
     * For all other modes <code>null</code> is returned.</p>
     *
     * @param modeName The name of the mode.
     * @return An array containing a single <code>PropertyGenerator</code>
     * if <code>modeName</code> is "renderable" (case-insensitive) or
     * <code>null</code> otherwise.
     */
    public PropertyGenerator[] getPropertyGenerators(String modeName) {
        return modeName.equalsIgnoreCase(RenderableRegistryMode.MODE_NAME) ?
            new PropertyGenerator[] { new ImageReadPropertyGenerator() } :
            null;
    }

    /**
     * Validates the parameters in the supplied <code>ParameterBlock</code>.
     *
     * <p>In addition to the standard validation performed by the
     * corresponding superclass method, this method verifies the following:
     * <ul>
     * <li>whether <i>ImageChoice</i> is negative (rendered mode)
     * or contains any negative indices (other modes); and</li>
     * <li>if <i>VerifyInput</i> is <code>TRUE</code> and <i>Input</i>
     * is a <code>File</code> or <code>String</code>, whether the
     * corresponding physical file exists and is readable; and</li>
     * <li>if <i>VerifyInput</i> is <code>TRUE</code> and <i>Input</i>
     * is a <code>String</code>, converting which to a 
     * corresponding physical file failed, whether it can be converted
     * to an InputStream accessed as a resource from a JAR file; and</li>
     * <li>if <i>VerifyInput</i> is <code>TRUE</code> and <i>Input</i>
     * is a <code>Socket</code>, whether it is bound, connected, open,
     * and the read-half is not shut down.</li>
     * </ul>
     *
     * If the superclass method finds that the arguments are invalid, or if
     * this method determines that any of the foregoing conditions is true,
     * an error message will be appended to <code>msg</code> and
     * <code>false</code> will be returned; otherwise <code>true</code> will
     * be returned.</p>
     *
     * <p>The file existence and readability verification may be suppressed
     * by setting the <i>VerifyInput</i> parameter to <code>FALSE</code>.
     * This might be desirable for example if the operation is being
     * created for remote rendering and <i>Input</i> is a file which is at
     * a location visible on the remote peer but not on the host on which
     * the operation is created.</p>
     *
     * @param modeName The operation mode.
     * @param args The source and parameters of the operation.
     * @param msg A container for any error messages.
     *
     * @return Whether the supplied parameters are valid.
     */
    protected boolean validateParameters(String modeName,
                                         ParameterBlock args,
                                         StringBuffer msg) {
        if (!super.validateParameters(modeName, args, msg)) {
            return false;
        }

        // Check "ImageChoice" for negative value(s).
        if(modeName.equalsIgnoreCase(RenderedRegistryMode.MODE_NAME)) {
            if(args.getIntParameter(1) < 0) {
                msg.append(I18N.getString("ImageReadDescriptor10"));
                return false;
            }
        } else { // Non-rendered modes.
            int[] imageIndices = (int[])args.getObjectParameter(1);
            if(imageIndices != null) {
                for(int i = 0; i < imageIndices.length; i++) {
                    if(imageIndices[i] < 0) {
                        msg.append(I18N.getString("ImageReadDescriptor10"));
                        return false;
                    }
                }
            }
        }

        // Check the input if so requested by "VerifyInput".
	Boolean verifyInput = (Boolean)args.getObjectParameter(4);
	if (verifyInput.booleanValue()){
            // Get the Input parameter.
            Object input = args.getObjectParameter(0);

            if(input instanceof File || input instanceof String) {
                // Set file and path variables.
                File file = null;
                String path = null;
                if(input instanceof File) {
                    file = (File)input;
                    path = file.getPath();
                } else if(input instanceof String) {
                    path = (String)input;
                    file = new File(path);
                }

                // If input is a verify that it exists and is readable.
                if(file != null) {
                    if (!file.exists()) {
			// Check if the file is accessible as an InputStream
			// resource. This would be the case if the application
			// and the image file are packaged in a JAR file
			InputStream is = 
			    getClass().getClassLoader().getResourceAsStream((String)input);
			if(is == null) {
			    msg.append("\"" + path + "\": " + 
				       I18N.getString("ImageReadDescriptor11"));
			    return false;
			}
                    } else if (!file.canRead()) {
                        msg.append("\"" + path + "\": " + 
                                   I18N.getString("ImageReadDescriptor12"));
                        return false;
                    }
                }
            } else if(input instanceof Socket) {
                Socket socket = (Socket)input;

                if(socket.isInputShutdown()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageReadDescriptor13"));
                    return false;
                } else if(socket.isClosed()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageReadDescriptor14"));
                    return false;
                } else if(!socket.isBound()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageReadDescriptor15"));
                    return false;
                } else if(!socket.isConnected()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageReadDescriptor16"));
                    return false;
                }
            }
        }
        
        return true;
    }
}

// XXX Does this need to return ImageReader and ReadParam props also?
// XXX This same property needs to be set also on each rendering so that
// copying does not occur and require more computation.
final class ImageReadPropertyGenerator extends PropertyGeneratorImpl {

    /** Constructor. */
    ImageReadPropertyGenerator() {
        super(new String[] {
                  ImageReadDescriptor.PROPERTY_NAME_RENDERABLE_INPUT},
              new Class[] {RenderableImage.class},
              new Class[] {RenderableOp.class});
    }

    /**
     * Returns the specified property in the renderable layer.
     *
     * @param name   Property name.
     * @param opNode Operation node.
     */
    public Object getProperty(String name,
                              Object opNode) {
        // Check arguments.
        validate(name, opNode);

        // Default to undefined value.
        Object value = java.awt.Image.UndefinedProperty;

        if(opNode instanceof RenderableOp &&
           name.equalsIgnoreCase(
               ImageReadDescriptor.PROPERTY_NAME_RENDERABLE_INPUT)) {

            // Save the node reference and get the hints.
            RenderableOp node = (RenderableOp)opNode;
            RenderingHints hints = node.getRenderingHints();

            // Get the CRIF for "ImageRead".
            OperationRegistry registry = null;
            ContextualRenderedImageFactory crif = null;

            // Try to get the CRIF from a registry specified in the hints.
            if(hints != null &&
               hints.containsKey(JAI.KEY_OPERATION_REGISTRY)) {
                registry =
                    (OperationRegistry)hints.get(JAI.KEY_OPERATION_REGISTRY);
                crif =
                    (ContextualRenderedImageFactory)registry.getFactory(
                        RenderableRegistryMode.MODE_NAME, "ImageRead");
            }

            // If no registry in the hints or that registry does not contain
            // a CRIF for "ImageRead", try to get it from the default registry.
            if(crif == null) {
                registry =
                    JAI.getDefaultInstance().getOperationRegistry();
                crif =
                    (ContextualRenderedImageFactory)registry.getFactory(
                        RenderableRegistryMode.MODE_NAME, "ImageRead");
            }

            // Create the RenderableImage and set the property value to it.
            if(crif != null &&
               crif instanceof com.sun.media.jai.imageioimpl.ImageReadCRIF) {
                value = ((com.sun.media.jai.imageioimpl.ImageReadCRIF)crif).createRenderable(
                            node.getParameterBlock(),
                            hints);
            }
        }

        return value;
    }
}
