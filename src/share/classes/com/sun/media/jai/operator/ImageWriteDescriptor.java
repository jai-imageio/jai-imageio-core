/*
 * $RCSfile: ImageWriteDescriptor.java,v $
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
 * $Date: 2005/02/11 05:01:56 $
 * $State: Exp $
 */
package com.sun.media.jai.operator;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.CollectionOp;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.CollectionRegistryMode;
import javax.media.jai.registry.RenderableRegistryMode;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * An <code>OperationDescriptor</code> describing the "ImageWrite" operation.
 *
 * <p>The "ImageWrite" operation uses the
 * <a href="http://java.sun.com/j2se/1.4/docs/guide/imageio/index.html">Java
 * Image I/O Framework</a> to write images to an output destination. Which
 * formats may be written depends on which {@link javax.imageio.ImageWriter}
 * plug-ins are registered with the Image I/O Framework when the operation is
 * invoked.</p>
 *
 * <p>The output destination will usually be an
 * {@link javax.imageio.stream.ImageOutputStream}, but may be a
 * {@link java.io.File}, {@link java.io.RandomAccessFile},
 * {@link java.io.OutputStream}, {@link java.net.Socket},
 * {@link java.nio.channels.WritableByteChannel}, file path represented as a
 * <code>String</code> or some other type compatible with a writer plug-in. The
 * {@link javax.imageio.ImageIO} class should be used to specify the location
 * and enable the use of cache files via its <code>setCacheDirectory()</code>
 * and <code>setUseCache()</code> methods, respectively. Note that this cache
 * pertains to image stream caching and is unrelated to the JAI
 * <code>TileCache</code>. If an {@link javax.imageio.stream.ImageOutputStream}
 * is created internally by the operation, for example from a
 * {@link java.io.File}-valued <a href="#ParamOutput">Output</a> parameter,
 * then it will be flushed automatically if and only if the operation is not
 * in <a href="#CollectionMode">collection mode</a> and pixel replacement is
 * not occurring.</p>
 *
 * <p>The "ImageWrite" operation supports <a href="#RenderedMode">rendered</a>,
 * <a href="#RenderableMode">renderable</a>, and
 * <a href="#CollectionMode">collection</a> modes and requires a single
 * source. The operation is "immediate" for all modes as specified by
 * <code>OperationDescriptor.isImmediate()</code> so that
 * {@link #isImmediate()} returns <code>true</code>. The operation will
 * therefore be rendered when created via either <code>JAI.create[NS]()</code>
 * or <code>JAI.createCollection[NS]()</code>.
 * A {@link java.awt.RenderingHints} object supplied when the
 * operation is created will have no effect except with respect to the
 * mapping of <code>JAI.KEY_INTERPOLATION</code> and then only in renderable
 * mode.</p>
 *
 * <p>Image properties are used to pass metadata and other information to the
 * writer plug-in and to make available metadata as actually written to the
 * output destination. Property handling is mode-specific.</p>
 *
 * <p><table border=1>
 * <caption><b>Resource List</b></caption>
 * <tr><th>Name</th>        <th>Value</th></tr>
 * <tr><td>GlobalName</td>  <td>ImageWrite</td></tr>
 * <tr><td>LocalName</td>   <td>ImageWrite</td></tr>
 * <tr><td>Vendor</td>      <td>com.sun.media.jai</td></tr>
 * <tr><td>Description</td> <td>Writes an image using the Java Image I/O Framework.</td></tr>
 * <tr><td>DocURL</td>      <td>http://java.sun.com/products/java-media/jai/forDevelopers/jai-imageio-1_0-rc-docs/com/sun/media/jai/operator/ImageWriteDescriptor.html</td></tr>
 * <tr><td>Version</td>     <td>1.0</td></tr>
 * <tr><td>arg0Desc</td>    <td>The output destination.</td></tr>
 * <tr><td>arg1Desc</td>    <td>The format name of the output.</td></tr>
 * <tr><td>arg2Desc</td>    <td>Whether to use image metadata properties as fallbacks.</td></tr>
 * <tr><td>arg3Desc</td>    <td>Whether to transcode metadata before writing.</td></tr>
 * <tr><td>arg4Desc</td>    <td>Whether to verify the validity of the output destination.</td></tr>
 * <tr><td>arg5Desc</td>    <td>Whether to allow pixel replacement in the output image.</td></tr>
 * <tr><td>arg6Desc</td>    <td>The tile size of the output image.</td></tr>
 * <tr><td>arg7Desc</td>    <td>Stream metadata to write to the output.</td></tr>
 * <tr><td>arg8Desc</td>    <td>Image metadata to write to the output.</td></tr>
 * <tr><td>arg9Desc</td>    <td>Thumbnails to write to the output.</td></tr>
 * <tr><td>arg10Desc</td>    <td>EventListeners to be registered with the ImageWriter.</td></tr>
 * <tr><td>arg11Desc</td>    <td>The Locale for the ImageWriter to use.</td></tr>
 * <tr><td>arg12Desc</td>    <td>Java Image I/O write parameter instance.</td></tr>
 * <tr><td>arg13Desc</td>    <td>Java Image I/O writer instance.</td></tr>
 * </table></p>
 *
 * <h2><a name="RenderedMode"</a>Rendered Mode</h2>
 *
 * In rendered mode the "ImageWrite" operation writes a
 * {@link java.awt.image.RenderedImage} to the specified output destination.
 *
 * <h3><a name="RenderedModeParameters"</a>Rendered Mode Parameters</h3>
 *
 * The parameter list of the "ImageWrite" operation in rendered mode is
 * as in the following table.
 *
 * <p><table border=1>
 * <caption><b>Rendered Mode Parameter List</b></caption>
 * <tr><th>Name</th>           <th>Class Type</th>
 *                             <th>Default Value</th></tr>
 * <tr><td><a href="#ParamOutput">
 *     Output</a></td>          <td>java.lang.Object.class</td>
 *                             <td>NO_PARAMETER_DEFAULT</td>
 * <tr><td><a href="#ParamFormat">
 *     Format</a></td>    <td>java.lang.String</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamUseProperties">
 *     UseProperties</a></td>   <td>java.lang.Boolean</td>
 *                             <td>TRUE</td>
 * <tr><td><a href="#ParamTranscode">
 *     Transcode</a></td> <td>java.lang.Boolean</td>
 *                             <td>TRUE</td>
 * <tr><td><a href="#ParamVerifyOutput">
 *     VerifyOutput</a></td>    <td>java.lang.Boolean</td>
 *                             <td>TRUE</td>
 * <tr><td><a href="#ParamAllowPixelReplacement">
 *     AllowPixelReplacement</a></td> <td>java.lang.Boolean</td>
 *                             <td>FALSE</td>
 * <tr><td><a href="#ParamTileSize">
 *     TileSize</a></td>    <td>java.awt.Dimension</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamStreamMetadata">
 *     StreamMetadata</a></td>    <td>javax.imageio.metadata.IIOMetadata</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamImageMetadata">
 *     ImageMetadata</a></td>    <td>javax.imageio.metadata.IIOMetadata</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamThumbnails">
 *     Thumbnails</a></td>    <td>java.awt.BufferedImage[]</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamListeners">
 *     Listeners</a></td>      <td>java.util.EventListener[]</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamLocale">
 *     Locale</a></td>         <td>java.util.Locale</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamWriteParam">
 *     WriteParam</a></td>      <td>javax.imageio.ImageWriteParam</td>
 *                             <td>null</td>
 * <tr><td><a href="#ParamWriter">
 *     Writer</a></td>         <td>javax.imageio.ImageWriter</td>
 *                             <td>null</td>
 * </table></p>
 *
 * <p>The rendered mode parameters are handled as follows:
 *
 * <ul>
 * <p><li><a name="ParamOutput"</a>
 * If Output is a String it is assumed to represent a file path.
 * </li></p>
 * <p><li><a name="ParamFormat"</a>
 * Format will be used to obtain an ImageWriter if one is not supplied. If
 * this parameter is null and Writer is non-null and has an originating
 * ImageWriterSpi, then the first format name listed by that provider will be
 * used. If Writer is null and Output is a File or a String, an attempt will
 * be made to derive the format name from the suffix of the file path. If
 * this fails, then the format will default to "PNG" as this is the most
 * versatile writer plug-in in the Java 2 core.
 * </li></p>
 * <p><li><a name="ParamUseProperties"</a>
 * If UseProperties is TRUE, then if stream or image metadata or thumbnails
 * are not provided as parameters, an attempt will be made to derive them
 * from the source image using the respective image properties previously
 * described for the "ImageRead" operation.
 * </li></p>
 * <p><li><a name="ParamTranscode"</a>
 * If Transcode is TRUE, then any stream or metadata derived either from
 * operation parameters or source image properties will be converted using
 * the ImageWriter's implementation of ImageTranscoder.
 * </li></p>
 * <p><li><a name="ParamVerifyOutput"</a>
 * If VerifyOutput is TRUE, then if the Output is a File or a String it will
 * be verified that a file at the specified location may either be overwritten
 * or created. If Output is a Socket, it will be verified that it is bound,
 * connected, not closed, and its write-half is not shut down. If any of
 * these checks fails, an exception will be thrown when the operation is
 * created. This parameter is ignored for other output types.
 * </li></p>
 * <p><li><a name="ParamAllowPixelReplacement"</a>
 * If AllowPixelReplacement is TRUE, and the ImageWriter supports pixel
 * replacement, then a construct will be enabled to allow "live" updating
 * of the output in response to RenderingChangeEvents or "InvalidRegion"
 * events.
 * </li></p>
 * <p><li><a name="ParamTileSize"</a>
 * TileSize specifies the desired tile size; it is used as defined by the
 * <a href="#RenderedModeTiling">tiling algorithm</a>. This parameter is
 * ignored if the ImageWriter does not support tiling.
 * Regardless of the capabilities of the writer, an exception will be thrown
 * when the operation is created if this parameter is non-null and either
 * its width or height is not positive.
 * </li></p>
 * <p><li><a name="ParamStreamMetadata"</a>
 * If StreamMetadata is non-null, then the parameter will take priority over
 * the corresponding image property as the source of stream metadata to
 * be written.
 * </li></p>
 * <p><li><a name="ParamImageMetadata"</a>
 * If ImageMetadata is non-null, then the parameter will take priority over
 * the corresponding image property as the source of image metadata to
 * be written.
 * </li></p>
 * <p><li><a name="ParamThumbnails"</a>
 * If Thumbnails is non-null, then the parameter will take priority over
 * the corresponding image property as the source of image thumbnails to
 * be written.
 * </li></p>
 * <p><li><a name="ParamListeners"</a>
 * Listeners will be used to set any progress or warning listeners of the
 * ImageWriter. Each element in the java.util.EventListener array will be
 * added for all types of listener it implements. For example if a listener
 * object implements all of the javax.imageio.event.IIOWrite*Listeners
 * interfaces then it will be added as a listener of each of the three types.
 * Any elements in the array which do not implement any of the
 * IIOWrite*Listeners will be ignored.
 * </li></p>
 * <p><li><a name="ParamLocale"</a>
 * Locale will be used to set the Locale of the ImageWriter. This parameter
 * overrides the equivalent setting of the Writer parameter if the latter is
 * also supplied.
 * </li></p>
 * <p><li><a name="ParamWriteParam"</a>
 * If WriteParam is null, an ImageWriteParam will be derived internally using
 * ImageWriter.getDefaultWriteParam().
 * </li></p>
 * <p><li><a name="ParamWriter"</a>
 * If Writer is null, an attempt will be made to find an ImageWriter capable
 * of writing the image. If this attempt to obtain an ImageWriter fails, an
 * exception will be thrown.
 * </li></p>
 * </ul>
 * </p>
 *
 * <h4><a name="SyncPolicy"</a>Parameters and Synchronization Policy</h4>
 *
 * Similarly to the case of any ImageReadParam or ImageReader supplied to the
 * "ImageRead" operation, any ImageWriteParam or ImageWriter supplied to the
 * "ImageWrite" operation is subject to modification within the operation
 * classes. A policy similar to the
 * <a href="./ImageReadDescriptor.html#SyncPolicy">"ImageRead"
 * synchronization policy</a> therefore applies as well for "ImageWrite".
 * 
 * <p>In the Sun Microsystems implementation of this operation these potential
 * conflicts have been mitigated to a certain extent:
 * 
 * <ul>
 * <p><li>
 * If the param is cloneable then it is cloned and the clone used internally.
 *  Otherwise if the param is an instance of ImageWriteParam itself rather than
 *   of a subclass thereof, i.e., getClass().getName() invoked on the param
 *   returns "javax.imageio.ImageWriteParam", then a new ImageWriteParam is
 *   constructed and the settings of the original param copied to it. If the
 *   param is not cloneable and is an instance of a proper subclass of
 *   ImageWriteParam then it is used directly.</li></p>
 * 
 * <p><li>
 * The only ImageWriter methods invoked after rendering are
 *   prepareReplacePixels(int,Rectangle), replacePixels(Raster,ImageWriteParam),
 *   and endReplacePixels() and these are invoked within a method synchronized
 *   on the ImageWriter object.</li></p>
 * </ul>
 * </p>
 *
 * <h3><a name="RenderedModeTiling"</a>Tiling</h3>
 *
 * The following algorithm is used to determine the tile size of the
 * image written to the output destination:
 * 
 * <pre>
 * if ImageWriter cannot write tiles
 *    output is untiled
 * else
 *    if TileSize parameter is non-null
 *       set tile size to TileSize
 *    else
 *       if WriteParam is null
 *          set tile size to source tile size
 *       else
 *          if tilingMode is ImageWriteParam.MODE_EXPLICIT
 *             if tile dimension is set in WriteParam
 *                set tile size to tile dimension from WriteParam
 *             else
 *                if preferred tile dimension is set in WriteParam
 * 		  set tile size to average of first two preferred dimensions
 *                else
 *                   set tile size to source tile size
 *          else // tilingMode is not ImageWriteParam.MODE_EXPLICIT
 *             the plug-in decides the tile size
 * </pre>
 * 
 * There is no mechanism to set the tile grid offsets of the output.
 *
 * <h3><a name="RenderedModePixelReplacement"</a>Pixel Replacement</h3>
 *
 * If AllowPixelReplacement is TRUE, the ImageWriter can replace pixels, and
 * the source is a PlanarImage, then the rendering of the operation
 * will respond to RenderingChangeEvents and Shape-valued PropertyChangeEvents
 * named "InvalidRegion". The rendering will be automatically registered as
 * a sink of the rendering of the operation node's source. As the source
 * rendering does not usually generate events, the calling code must also
 * explicitly register the "ImageWrite" rendering as a sink of the source
 * node. By whatever means the event is generated, when the rendering
 * receives such an event, it will determine the indices of all tiles which
 * overlap the invalid region and will replace the pixels of all these tiles
 * in the output.
 * 
 * <p>Note that this behavior differs from what would happen if the RenderedOp
 * created by the operation received a RenderingChangeEvent: in this case a
 * new rendering of the node would be created using the ParameterBlock and
 * RenderingHints currently in effect. This would cause the entire image to be
 * rewritten at the current position of the output. This will also happen
 * when AllowPixelReplacement is FALSE. In effect in both of these cases the
 * behavior in response to a RenderingChangeEvent is unspecified and the result
 * will likely be unexpected.</p>
 * 
 * <p>To avoid any inadvertent overwriting of the destination as a result of
 * events received by the RenderedOp, the following usage is recommended when
 * the objective is automatic pixel replacement:
 * 
 * <pre>
 *        // Sources, parameters, and hints.
 *        ParameterBlock args;
 *        RenderingHints hints;
 * 
 *        // Create the OperationNode.
 *        RenderedOp imageWriteNode = JAI.create("ImageWrite", args, hints);
 * 
 *        // Get the rendering which already exists due to "immediate" status.
 *        RenderedImage imageWriteRendering = imageWriteNode.getRendering();
 * 
 *        // Unhook the OperationNode as a sink of its source OperationNode.
 *        imageWriteNode.getSourceImage(0).removeSink(imageWriteNode);
 *
 *        // Add the rendering as a sink of the source OperationNode.
 *        imageWriteNode.getSourceImage(0).addSink(imageWriteRendering);
 * 
 *        // Free the OperationNode for garbage collection.
 *        imageWriteNode = null;
 * </pre>
 * 
 * At this point a reference to imageWriteRendering must be held as long as the
 * data of the source of the operation may change. Then provided the events are
 * correctly propagated to imageWriteRendering, the data in the output file
 * will be automatically updated to match the source data.</p>
 * 
 * <p>If pixel replacement is not the objective and inadvertent overwriting is
 * to be avoided then the safest approach would be the following:
 * 
 * <pre>
 *        // Create the OperationNode.
 *        RenderedOp imageWriteNode = JAI.create("ImageWrite", args, hints);
 * 
 *        // Unhook the OperationNode as a sink of its source
 *        imageWriteNode.getSourceImage(0).removeSink(imageWriteNode);
 * </pre>
 * 
 * The image is written by the first statement and no reference to the
 * rendering need be retained as before.
 *
 * <h3><a name="RenderedModeProperties"</a>Image Properties in Rendered Mode</h3>
 *
 * Image properties are used for metadata, thumbnails, and writer-related
 * information. The following properties may be set on the RenderedOp created
 * for the "ImageWrite" operation in rendered mode:
 * 
 * <p><table border=1>
 * <caption><b>Rendered Mode Image Properties</b></caption>
 * <tr>
 * <th>Property Name</th>
 * <th>Type</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>JAI.ImageWriteParam</td>
 * <td>ImageWriteParam</td>
 * <td>Set to ImageWriteParam actually used which may differ from the one passed in.</td>
 * </tr>
 * <tr>
 * <td>JAI.ImageWriter</td>
 * <td>ImageWriter</td>
 * <td>Set to ImageWriter actually used.</td>
 * </tr>
 * <tr>
 * <td>JAI.ImageMetadata</td>
 * <td>IIOMetadata</td>
 * <td>Set if and only if image metadata are available; may be transcoded.</td>
 * </tr>
 * <tr>
 * <td>JAI.StreamMetadata</td>
 * <td>IIOMetadata</td>
 * <td>Set if and only if stream metadata are available; may be transcoded.</td>
 * </tr>
 * <tr>
 * <td>JAI.Thumbnails</td>
 * <td>BufferedImage[]</td>
 * <td>Set if and only thumbnails are provided and the writer supportes writing them.</td>
 * </tr>
 * </table></p>
 * 
 * <p>If a given property is not set, this implies of course that the names of
 * absent properties will not appear in the array returned by getPropertyNames()
 * and getProperty() invoked to obtain absent properties will return
 * java.awt.Image.UndefinedProperty as usual.</p>
 * 
 * <p>The ImageWriter and ImageWriteParam may be used for subsequent invocations
 * of the operation or for informational purposes. Care should be taken in using
 * these property values with respect to the synchronization issues previously
 * discussed.</p>
 * 
 * <p>Metadata properties will be set to those actually written to the output. They
 * may be derived either from input parameters or source properties depending on
 * the values of the StreamMetadata, ImageMetadata, and UseProperties parameters.
 * They will be transcoded data if Transcode is TRUE and the ImageWriter supports
 * transcoding.</p>
 * 
 * <p>All properties will be set when the node is rendered.</p>
 *
 * <h2><a name="RenderableMode"</a>Renderable Mode</h2>
 *
 * In renderable mode the "ImageWrite" operation requires a
 * {@link java.awt.image.renderable.RenderableImage} source and writes a
 * {@link java.awt.image.RenderedImage} to the specified output destination.
 * As the "immediate" designation specified by {@link #isImmediate()}
 * has no effect in renderable mode, no image will be written without further
 * action by the calling code. To write an image, createRendering(),
 * createScaledRendering(), or createDefaultRendering()
 * must be invoked. Each of these will create a RenderedImage by forwarding the
 * createRendering() or equivalent call to the source image. The resulting
 * RenderedImage will be written to the output according to the
 * <a href="#RenderedMode">rendered mode</a> operation of "ImageWrite".
 * If a mapping of <code>JAI.KEY_INTERPOLATION</code> is supplied via a
 * <code>RenderingHints</code> passed to the operation, then the interpolation
 * type it specifies will be used to create the rendering if interpolation is
 * required.
 *
 * <h3><a name="RenderableModeParameters"</a>Renderable Mode Parameters</h3>
 *
 * The parameter list of the "ImageRead" operation in renderable mode is
 * identical to the <a href="#RenderedModeParameters">rendered mode
 * parameter list</a>.
 *
 * <h3>Pixel Replacement in Renderable Mode</h3>
 *
 * Pixel replacement pertains only to RenderedImages generated by rendering the
 * RenderableOp. It may occur if the same conditions apply as described for
 * pixel replacement in rendered mode. Due to the unspecified nature of the
 * underlying rendered sources of any rendering, this is not a recommended
 * procedure.
 *
 * <h3>Image Properties in Renderable Mode</h3>
 *
 * The RenderableOp node itself does not have any ImageWrite-related
 * properties. Any RenderedImages created by rendering the RenderableOp
 * (thereby writing an image to the output as described), may have
 * <a href="#RenderedModeProperties">rendered mode properties</a> set.
 *
 * <h2><a name="CollectionMode"</a>Collection Mode</h2>
 *
 * In collection mode the "ImageWrite" operation requires a
 * {@link java.util.Collection} source and writes its contents to the
 * specified output destination.
 *
 * <p>The Collection is treated as a sequence of images which will be
 * extracted from the Collection in the order returned by a new Iterator.
 * Elements in the Collection which are not RenderedImages will be ignored.
 * The derived sequence of images will then be written to the output.</p>
 * 
 * <p>If there is only one RenderedImage in the source Collection, this image
 * will be written as done in rendered mode operation. If there is more than
 * one RenderedImage, the sequence of RenderedImages will be written as an
 * image sequence. In the latter case the ImageWriter must be able to write
 * sequences.</p>
 *
 * <h3><a name="CollectionModeParameters"</a>Collection Mode Parameters</h3>
 *
 * Identical parameter list to rendered mode except:
 * 
 * <p><table border=1>
 * <caption><b>Collection Mode Parameter Differences</b></caption>
 * <tr><th>Name</th>           <th>Class Type</th>
 *                             <th>Default Value</th></tr>
 * <tr><td>ImageMetadata</td>  <td>javax.imageio.metadataIIOMetadata[]</td>
 *                             <td>null</td>
 * <tr><td>Thumbnails</td>     <td>java.awt.image.BufferedImage[][]</td>
 *                             <td>null</td>
 * </table></p>
 * 
 * <ul>
 * <p><li>
 * If the source is not a CollectionOp then the number of RenderedImages in
 * the source is counted. If it is not at least one then an exception is
 * thrown when the operation is created. If it is greater than one, then
 * the ImageWriter is checked to determine whether it can write sequences.
 * If it cannot then an exception is thrown when the operation is created.
 * </li></p>
 * <p><li>
 * The first index of the thumbnails array corresponds to the ordinal position
 * of the image in the collection and the second index to the thumbnails of
 * that image.
 * </li></p>
 * </ul>
 * 
 * <p>
 * The change to the ImageMetadata and Thumbnails parameters is that there can
 * now be a distinct image metadata object and thumbnail array for each image
 * in the Collection. The components of these respective arrays will be indexed
 * using the sequence of RenderedImages extracted from the source Collection by
 * the Iterator. It is the responsibility of the caller to ensure that this
 * sequencing is correct. In this context it is advisable to use a source
 * Collection which maintains the order of its elements such as a List.
 * </p>
 *
 * <h3>Pixel Replacement in Collection Mode</h3>
 *
 * If the value of the AllowPixelReplacement parameter is TRUE, then the
 * rendered Collection will contain RenderedImages which are registered as
 * listeners of their respective sources. Each image in the rendered Collection
 * will however be a rendering as opposed to a RenderedOp. This obviates the
 * need to unhook such a RenderedOp from its source as suggested. Two actions
 * on the part of the application are however necessary in this case:  1) the
 * sequence must be manually ended, and 2) the Collection node must be removed
 * as a sink of its source Collection. The first action is necessary as
 * pixels may be replaced at various times in various images in the sequence
 * and it is not possible to terminate the sequence at rendering time, and there
 * is no reliable mechanism to detect programmatically when this may later be
 * effected. The second action is necessary because a CollectionChangeEvent
 * received by the Collection node would cause the node to be re-rendered, i.e.,
 * the collection data to be rewritten using the current state of all parameters.
 * This will in fact also happen when AllowPixelReplacement is FALSE. In effect
 * in both of these cases the behavior in response to a CollectionChangeEvent
 * is unspecified and the result will likely be unexpected.
 * 
 * <p>
 * To ensure proper termination of the image sequence and avoid any inadvertent
 * overwriting of the destination as a result of events received by the
 * CollectionOp, the following usage is recommended when the objective is
 * automatic pixel replacement:
 * 
 * <pre>
 *        // Sources, parameters, and hints.
 *        ParameterBlock args;
 *        RenderingHints hints;
 * 
 *        // Create the Collection.
 *        CollectionImage imageWriteCollection =
 *            (CollectionImage)JAI.createCollection("ImageWrite", args, hints);
 * 
 *        // Unhook the Collection node from the source to avoid
 *        // re-renderings caused by CollectionChangeEvents.
 *        if(args.getSource(0) instanceof CollectionImage) {
 *            CollectionImage sourceCollection =
 * 	       (CollectionImage)args.getSource(0);
 *            sourceCollection.removeSink(imageWriteCollection);
 *        }
 * 
 *        // !!! Pixel replacement activity happens here ... !!!
 * 
 *        // Get the ImageWriter.
 *        ImageWriter writer =
 *            (ImageWriter)imageWriteCollection.getProperty("JAI.ImageWriter");
 * 
 *        // End the sequence if necessary.
 *        if(writer.canWriteSequence()) {
 *            writer.endWriteSequence();
 *        }
 * </pre>
 * </p>
 * 
 * <p>
 * Using the foregoing construct, all pixels in all images written to the output
 * sequence will remain current with the in-memory data of their respective
 * source provided all events are propagated as expected. Note that it is not
 * necessary to end the sequence manually if pixel replacement is not allowed or
 * is not supported. Also the sequence must be manually ended if and only if the
 * writer is capable of writing sequences. This permits pixel replacement to
 * work in the case where the source collection contains only a single image
 * and the writer supports pixel replacement but cannot write sequences.
 * </p>
 * 
 * <p>
 * If pixel replacement is not the objective, i.e., AllowPixelReplacement is
 * FALSE, and inadvertent overwriting is to be avoided then the safest approach
 * would be the following:
 * 
 * <pre>
 *        // Create the Collection.
 *        Collection imageWriteCollection =
 *            JAI.create("ImageWrite", args, hints);
 * 
 *        // Unhook the Collection node from the source to avoid
 *        // re-renderings caused by CollectionChangeEvents.
 *        if(args.getSource(0) instanceof CollectionImage) {
 *            CollectionImage sourceCollection =
 * 	       (CollectionImage)args.getSource(0);
 *            sourceCollection.removeSink(imageWriteCollection);
 *        }
 * </pre>
 * 
 * The image is written by the first statement and no reference to the
 * rendering need be retained.</p>
 *
 * <h3>Image Properties in Collection Mode</h3>
 *
 * Contingent on parameter settings and the presence of the appropriate
 * metadata, the rendered Collection may have the "JAI.StreamMetadata",
 * "JAI.ImageReadParam", and "JAI.ImageReader" properties set. Each
 * RenderedImage in the Collection may contain
 * <a href="#RenderedModeProperties">rendered mode properties</a>
 * contingent on parameter settings and data availability. Metadata
 * properties may be transcoded.
 *
 * @see javax.media.jai.OperationDescriptor
 * @see javax.imageio.ImageWriter
 * @see javax.imageio.ImageWriteParam
 * @see javax.imageio.metadata.IIOMetadata
 * @see javax.imageio.stream.ImageOutputStream
 */
public class ImageWriteDescriptor extends OperationDescriptorImpl {

    // Property name constants have package access for image factory use.

    /** ImageWriteParam property name "JAI.ImageWriteParam". */
    public static final String PROPERTY_NAME_IMAGE_WRITE_PARAM =
        "JAI.ImageWriteParam";

    /** ImageWriter property name "JAI.ImageWriter". */
    public static final String PROPERTY_NAME_IMAGE_WRITER =
        "JAI.ImageWriter";

    /**
     * Image metadata property name. Set to same value as
     * {@link ImageReadDescriptor#PROPERTY_NAME_METADATA_IMAGE}.
     */
    public static final String PROPERTY_NAME_METADATA_IMAGE =
        ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE;

    /**
     * Stream metadata property name. Set to same value as
     * {@link ImageReadDescriptor#PROPERTY_NAME_METADATA_STREAM}.
     */
    public static final String PROPERTY_NAME_METADATA_STREAM =
        ImageReadDescriptor.PROPERTY_NAME_METADATA_STREAM;

    /**
     * Thumbnail property name. Set to same value as
     * {@link ImageReadDescriptor#PROPERTY_NAME_THUMBNAILS}.
     */
    public static final String PROPERTY_NAME_THUMBNAILS =
        ImageReadDescriptor.PROPERTY_NAME_THUMBNAILS;

    /**
     * Test method.
     *
     * @param args {inputFile, outputFile [, mode]}
     * @throws Throwable any error.
     */
    /* XXX
    public static void main(String[] args) throws Throwable {
        String inputFile = args[0];
        String outputFile = args[1];
        String modeName = args.length > 2 ?
            args[2] : RenderedRegistryMode.MODE_NAME;
        String formatName = args.length > 3 ?
            args[3] : null;

        ParameterBlock pb = new ParameterBlock();
        pb.set(new java.io.File(outputFile), 0);
        if(formatName != null) {
            pb.set(formatName, 1);
        }

        java.awt.image.RenderedImage[] images = null;
        if(modeName.equalsIgnoreCase(RenderedRegistryMode.MODE_NAME)) {
            pb.addSource(ImageIO.read(new java.io.File(inputFile)));
            images = new java.awt.image.RenderedImage[1];
            pb.set(new Dimension(128, 128), 6);
            images[0] = JAI.create("ImageWrite", pb, null);
            PrintProps.print((javax.media.jai.PropertySource)images[0]);
        } else if(modeName.equalsIgnoreCase(RenderableRegistryMode.MODE_NAME)) {
            ParameterBlock renderablePB = new ParameterBlock();
            renderablePB.addSource(ImageIO.read(new java.io.File(inputFile)));
            pb.addSource(javax.media.jai.JAI.createRenderable("renderable", renderablePB));
            java.awt.image.renderable.RenderableImage ri =
                JAI.createRenderable("ImageWrite", pb, null);
            PrintProps.print((javax.media.jai.PropertySource)ri);
            images = new java.awt.image.RenderedImage[1];
            //java.awt.image.renderable.RenderContext rc =
            //    new java.awt.image.renderable.RenderContext(
            //        new java.awt.geom.AffineTransform(42, 0, 0, 42, 0, 0));
            //images[0] = ri.createRendering(rc);
            images[0] = ri.createDefaultRendering();
            PrintProps.print((javax.media.jai.PropertySource)images[0]);
        } else if(modeName.equalsIgnoreCase(CollectionRegistryMode.MODE_NAME)) {
            java.util.ArrayList sourceCollection = new java.util.ArrayList();
            Object input =
                ImageIO.createImageInputStream(new java.io.File(inputFile));
            javax.imageio.ImageReader reader =
                (javax.imageio.ImageReader)ImageIO.getImageReaders(input).next();
            reader.setInput(input);
            int imageIndex = 0;
            do {
                try {
                    RenderedImage nextImage = reader.read(imageIndex);
                    sourceCollection.add(nextImage);
                } catch(IndexOutOfBoundsException e) {
                    break;
                }
                imageIndex++;
            } while(true);
            pb.addSource(sourceCollection);
            java.util.Collection imageCollection =
                JAI.createCollection("ImageWrite", pb, null);
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
    private static final String OPERATION_NAME = "ImageWrite";

    /**
     * The resource strings that provide the general documentation and
     * specify the parameter list for the "ImageWrite" operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  "ImageWrite"},
        {"LocalName",   "ImageWrite"},
        {"Vendor",      "com.sun.media.jai"},
        {"Description", I18N.getString("ImageWriteDescriptor0")},
        {"DocURL",      "http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/ImageWriteDescriptor.html"},
        {"Version",     I18N.getString("DescriptorVersion")},
        {"arg0Desc",    I18N.getString("ImageWriteDescriptor1")},
        {"arg1Desc",    I18N.getString("ImageWriteDescriptor2")},
        {"arg2Desc",    I18N.getString("ImageWriteDescriptor3")},
        {"arg3Desc",    I18N.getString("ImageWriteDescriptor4")},
        {"arg4Desc",    I18N.getString("ImageWriteDescriptor5")},
        {"arg5Desc",    I18N.getString("ImageWriteDescriptor6")},
        {"arg6Desc",    I18N.getString("ImageWriteDescriptor7")},
        {"arg7Desc",    I18N.getString("ImageWriteDescriptor8")},
	{"arg8Desc",    I18N.getString("ImageWriteDescriptor9")},
	{"arg9Desc",    I18N.getString("ImageWriteDescriptor10")},
	{"arg10Desc",    I18N.getString("ImageWriteDescriptor11")},
	{"arg11Desc",    I18N.getString("ImageWriteDescriptor12")},
	{"arg12Desc",    I18N.getString("ImageWriteDescriptor13")},
	{"arg13Desc",    I18N.getString("ImageWriteDescriptor14")}
    };

    /** The parameter names for the "ImageWrite" operation. */
    private static final String[] paramNames = {
        "Output", "Format", "UseProperties", "Transcode",
        "VerifyOutput", "AllowPixelReplacement", "TileSize",
        "StreamMetadata", "ImageMetadata", "Thumbnails",
        "Listeners", "Locale", "WriteParam", "Writer"
    };

    /** The parameter class types for rendered mode of "ImageWrite". */
    private static final Class[] renderedParamClasses = {
        java.lang.Object.class,                   // Output
        java.lang.String.class,                   // Format
	java.lang.Boolean.class,                  // UseProperties
	java.lang.Boolean.class,                  // Transcode
	java.lang.Boolean.class,                  // VerifyOutput
	java.lang.Boolean.class,                  // AllowPixelReplacement
	java.awt.Dimension.class,                 // TileSize
	javax.imageio.metadata.IIOMetadata.class, // StreamMetadata
	javax.imageio.metadata.IIOMetadata.class, // ImageMetadata
        java.awt.image.BufferedImage[].class,     // Thumbnails
        java.util.EventListener[].class,          // Listeners
        java.util.Locale.class,                   // Locale
	javax.imageio.ImageWriteParam.class,      // WriteParam
	javax.imageio.ImageWriter.class           // Writer
    };

    /** The parameter default values for rendered mode of "ImageWrite". */
    private static final Object[] renderedParamDefaults = {
        NO_PARAMETER_DEFAULT, // Output
        null,                 // Format
        Boolean.TRUE,         // UseProperties
        Boolean.TRUE,         // Transcode
        Boolean.TRUE,         // VerifyOutput
        Boolean.FALSE,        // AllowPixelReplacement
        null,                 // TileSize
        null,                 // StreamMetadata
        null,                 // ImageMetadata
        null,                 // Thumbnails
        null,                 // Listeners
        null,                 // Locale
        null,                 // WriteParam
        null                  // Writer
    };

    /** The parameter class types for renderable mode of "ImageWrite". */
    private static final Class[] renderableParamClasses =
        renderedParamClasses;

    /** The parameter default values for renderable mode of "ImageWrite". */
    private static final Object[] renderableParamDefaults =
        renderedParamDefaults;

    /** The parameter class types for collection mode of "ImageWrite". */
    private static final Class[] collectionParamClasses = {
        java.lang.Object.class,                     // Output
        java.lang.String.class,                     // Format
	java.lang.Boolean.class,                    // UseProperties
	java.lang.Boolean.class,                    // Transcode
	java.lang.Boolean.class,                    // VerifyOutput
	java.lang.Boolean.class,                    // AllowPixelReplacement
	java.awt.Dimension.class,                   // TileSize
	javax.imageio.metadata.IIOMetadata.class,   // StreamMetadata
	javax.imageio.metadata.IIOMetadata[].class, // ImageMetadata
        java.awt.image.BufferedImage[][].class,     // Thumbnails
        java.util.EventListener[].class,            // Listeners
        java.util.Locale.class,                     // Locale
	javax.imageio.ImageWriteParam.class,        // WriteParam
	javax.imageio.ImageWriter.class             // Writer
    };

    /** The parameter default values for collection mode of "ImageWrite". */
    private static final Object[] collectionParamDefaults =
        renderedParamDefaults;

    /** Constructor. */
    public ImageWriteDescriptor() {
        super(resources,
              new String[] {RenderedRegistryMode.MODE_NAME,
                            RenderableRegistryMode.MODE_NAME,
                            CollectionRegistryMode.MODE_NAME},
              null, // sourceNames
              new Class[][] {{RenderedImage.class},
                             {RenderableImage.class},
                             {Collection.class}}, // sourceClasses
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
     * representing the "ImageWrite" operation in rendered mode. The
     * method packs the source and parameters into a new
     * <code>ParameterBlock</code> and invokes
     * {@link JAI#create(String,ParameterBlock,RenderingHints)}.
     *
     * @param source The image to be written.
     * @param output The output destination.
     * @param format The format name of the output.
     * @param useProperties Whether to use image metadata properties as
     * fallbacks.
     * @param transcode Whether to transcode metadata before writing.
     * @param verifyOutput Whether to verify the validity of the output
     * destination.
     * @param allowPixelReplacement Whether to allow pixel replacement
     * in the output image.
     * @param tileSize The tile size of the output image.
     * @param streamMetadata Stream metadata to write to the output.
     * @param imageMetadata Image metadata to write to the output.
     * @param thumbnails Thumbnails to write to the output.
     * @param listeners EventListeners to be registered with the ImageWriter.
     * @param locale The Locale for the ImageWriter to use.
     * @param writeParam Java Image I/O write parameter instance.
     * @param writer Java Image I/O writer instance.
     * @param hints Operation hints.
     * @return a reference to the operation source.
     */
    public static RenderedOp create(RenderedImage source,
                                    ImageOutputStream output,
                                    String format,
                                    Boolean useProperties,
                                    Boolean transcode,
                                    Boolean verifyOutput,
                                    Boolean allowPixelReplacement,
                                    Dimension tileSize,
                                    IIOMetadata streamMetadata,
                                    IIOMetadata imageMetadata,
                                    BufferedImage[] thumbnails,
                                    EventListener[] listeners,
                                    Locale locale,
                                    ImageWriteParam writeParam,
                                    ImageWriter writer,
                                    RenderingHints hints) {

        ParameterBlock args = new ParameterBlock();

        args.addSource(source);

        args.add(output);
        args.add(format);
        args.add(useProperties);
        args.add(transcode);
        args.add(verifyOutput);
        args.add(allowPixelReplacement);
        args.add(tileSize);
        args.add(streamMetadata);
        args.add(imageMetadata);
        args.add(thumbnails);
        args.add(listeners);
        args.add(locale);
        args.add(writeParam);
        args.add(writer);

        return JAI.create(OPERATION_NAME, args, hints);
    }

    /**
     * Type-safe convenience method for creating a {@link Collection}
     * representing the "ImageWrite" operation in collection mode. The
     * method packs the source and parameters into a new
     * <code>ParameterBlock</code> and invokes
     * {@link JAI#createCollection(String,ParameterBlock,RenderingHints)}.
     *
     * @param source The collection to be written.
     * @param output The output destination.
     * @param format The format name of the output.
     * @param useProperties Whether to use image metadata properties as
     * fallbacks.
     * @param transcode Whether to transcode metadata before writing.
     * @param verifyOutput Whether to verify the validity of the output
     * destination.
     * @param allowPixelReplacement Whether to allow pixel replacement
     * in the output image.
     * @param tileSize The tile size of the output image.
     * @param streamMetadata Stream metadata to write to the output.
     * @param imageMetadata Image metadata to write to the output.
     * @param thumbnails Thumbnails to write to the output.
     * @param listeners EventListeners to be registered with the ImageWriter.
     * @param locale The Locale for the ImageWriter to use.
     * @param writeParam Java Image I/O write parameter instance.
     * @param writer Java Image I/O writer instance.
     * @param hints Operation hints.
     * @return a reference to the operation source.
     */
    public static Collection createCollection(Collection source,
                                              ImageOutputStream output,
                                              String format,
                                              Boolean useProperties,
                                              Boolean transcode,
                                              Boolean verifyOutput,
                                              Boolean allowPixelReplacement,
                                              Dimension tileSize,
                                              IIOMetadata streamMetadata,
                                              IIOMetadata[] imageMetadata,
                                              BufferedImage[][] thumbnails,
                                              EventListener[] listeners,
                                              Locale locale,
                                              ImageWriteParam writeParam,
                                              ImageWriter writer,
                                              RenderingHints hints) {

        ParameterBlock args = new ParameterBlock();

        args.addSource(source);

        args.add(output);
        args.add(format);
        args.add(useProperties);
        args.add(transcode);
        args.add(verifyOutput);
        args.add(allowPixelReplacement);
        args.add(tileSize);
        args.add(streamMetadata);
        args.add(imageMetadata);
        args.add(thumbnails);
        args.add(listeners);
        args.add(locale);
        args.add(writeParam);
        args.add(writer);

        return JAI.createCollection(OPERATION_NAME, args, hints);
    }

    /**
     * Type-safe convenience method for creating a {@link RenderableOp}
     * representing the "ImageWrite" operation in renderable mode. The
     * method packs the source and parameters into a new
     * <code>ParameterBlock</code> and invokes
     * {@link JAI#createRenderable(String,ParameterBlock,RenderingHints)}.
     *
     * @param source The renderable source to be written.
     * @param output The output destination.
     * @param format The format name of the output.
     * @param useProperties Whether to use image metadata properties as
     * fallbacks.
     * @param transcode Whether to transcode metadata before writing.
     * @param verifyOutput Whether to verify the validity of the output
     * destination.
     * @param allowPixelReplacement Whether to allow pixel replacement
     * in the output image.
     * @param tileSize The tile size of the output image.
     * @param streamMetadata Stream metadata to write to the output.
     * @param imageMetadata Image metadata to write to the output.
     * @param thumbnails Thumbnails to write to the output.
     * @param listeners EventListeners to be registered with the ImageWriter.
     * @param locale The Locale for the ImageWriter to use.
     * @param writeParam Java Image I/O write parameter instance.
     * @param writer Java Image I/O writer instance.
     * @param hints Operation hints.
     * @return a reference to the operation source.
     */
    public static RenderableOp createRenderable(RenderableImage source,
                                                ImageOutputStream output,
                                                String format,
                                                Boolean useProperties,
                                                Boolean transcode,
                                                Boolean verifyOutput,
                                                Boolean allowPixelReplacement,
                                                Dimension tileSize,
                                                IIOMetadata streamMetadata,
                                                IIOMetadata imageMetadata,
                                                BufferedImage[] thumbnails,
                                                EventListener[] listeners,
                                                Locale locale,
                                                ImageWriteParam writeParam,
                                                ImageWriter writer,
                                                RenderingHints hints) {
        ParameterBlock args = new ParameterBlock();

        args.addSource(source);

        args.add(output);
        args.add(format);
        args.add(useProperties);
        args.add(transcode);
        args.add(verifyOutput);
        args.add(allowPixelReplacement);
        args.add(tileSize);
        args.add(streamMetadata);
        args.add(imageMetadata);
        args.add(thumbnails);
        args.add(listeners);
        args.add(locale);
        args.add(writeParam);
        args.add(writer);

        return JAI.createRenderable(OPERATION_NAME, args, hints);
    }

    /**
     * Returns true indicating that the operation should be rendered
     * immediately during a call to <code>JAI.create[]()</code> or
     * <code>JAI.createCollection[NS]()</code>.
     *
     * @see javax.media.jai.OperationDescriptor
     */
    public boolean isImmediate() {
        return true;
    }

    /**
     * Validates the parameters in the supplied <code>ParameterBlock</code>.
     *
     * <p>In addition to the standard validation performed by the
     * corresponding superclass method, this method verifies the following:
     * <ul>
     * <li>if <i>VerifyOutput</i> is <code>TRUE</code> and <i>Output</i>
     * is a <code>File</code> or <code>String</code>, whether the
     * corresponding physical file is writable, i.e., exists and may
     * be overwritten or does not exist and may be created; and</li>
     * <li>if <i>VerifyOutput</i> is <code>TRUE</code> and <i>Output</i>
     * is a <code>Socket</code>, whether it is bound, connected, open,
     * and the write-half is not shut down; and</li>
     * <li>if in collection mode (<code>modeName</code> equals
     * {@link CollectionRegistryMode#MODE_NAME}), the source is not a
     * {@link CollectionOp}, and the size of the source
     * {@link Collection} is greater than unity, whether the
     * {@link ImageWriter} <i>cannot</i> write sequences.</li>
     * </ul>
     *
     * If the superclass method finds that the arguments are invalid, or if
     * this method determines that any of the foregoing conditions is true,
     * an error message will be appended to <code>msg</code> and
     * <code>false</code> will be returned; otherwise <code>true</code> will
     * be returned.</p>
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

        // Get the Output parameter.
        Object output = args.getObjectParameter(0);

        // Check the output if so requested by "VerifyOutput".
	Boolean verifyOutput = (Boolean)args.getObjectParameter(4);
	if (verifyOutput.booleanValue()){
            if(output instanceof File || output instanceof String) {
                // Set file and path variables.
                File file = null;
                String path = null;
                if(output instanceof File) {
                    file = (File)output;
                    path = file.getPath();
                } else if(output instanceof String) {
                    path = (String)output;
                    file = new File(path);
                }

                // Perform non-destructive test that the file
                // may be created and written.
                try {
                    if (file.exists()) {
                        if (!file.canWrite()) {
                            // Cannot write to existing file.
                            msg.append(file.getPath() + " " +
                                       I18N.getString("ImageWriteDescriptor15"));
                            return false;
                        }
                    } else {
                        if (!file.createNewFile()) {
                            // Cannot create file.
                            msg.append(file.getPath() + " " +
                                       I18N.getString("ImageWriteDescriptor16"));
                            return false;
                        }
                        file.delete();
                    }
                } catch (IOException ioe) {
                    // I/O exception during createNewFile().
                    msg.append(file.getPath() + " " +
                               I18N.getString("ImageWriteDescriptor17") + " " +
                               ioe.getMessage());
                    return false;
                } catch (SecurityException se) {
                    // Security exception during exists(), canWrite(),
                    // createNewFile(), or delete().
                    msg.append(file.getPath() + " " +
                               I18N.getString("ImageWriteDescriptor18") + " " +
                               se.getMessage());
                    return false;
                }
            } else if(output instanceof Socket) {
                Socket socket = (Socket)output;

                if(socket.isOutputShutdown()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageWriteDescriptor19"));
                    return false;
                } else if(socket.isClosed()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageWriteDescriptor20"));
                    return false;
                } else if(!socket.isBound()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageWriteDescriptor21"));
                    return false;
                } else if(!socket.isConnected()) {
                    msg.append("\"" + socket + "\": " + 
                               I18N.getString("ImageWriteDescriptor22"));
                    return false;
                }
            }
        }

        // Get the Format parameter.
        String format = (String)args.getObjectParameter(1);

        // Get the ImageWriter parameter.
        ImageWriter writer = (ImageWriter)args.getObjectParameter(13);

        if(format == null) {
            // Attempt to get the format from the ImageWriter provider.
            if(writer != null) {

                // Get the SPI.
                ImageWriterSpi spi = writer.getOriginatingProvider();

                // Set from the SPI.
                if(spi != null) {
                    format = spi.getFormatNames()[0];
                }
            }

            // Attempt to deduce the format from the file suffix.
            if(format == null &&
               (output instanceof File || output instanceof String)) {

                // Set the file name string.
                String name = output instanceof File ?
                    ((File)output).getName() : (String)output;

                // Extract the suffix.
                String suffix = name.substring(name.lastIndexOf(".") + 1);

                // Get the writers of that suffix.
                Iterator writers = ImageIO.getImageWritersBySuffix(suffix);

                if(writers != null) {
                    // Get the first writer.
                    writer = (ImageWriter)writers.next();

                    if(writer != null) {
                        // Get the SPI.
                        ImageWriterSpi spi = writer.getOriginatingProvider();

                        // Set from the SPI.
                        if(spi != null) {
                            format = spi.getFormatNames()[0];
                        }
                    }
                }
            }

            // Default to the most versatile core Java Image I/O writer.
            if(format == null) {
                format = "PNG";
            }

            // Replace the format setting.
            if(format != null) {
                args.set(format, 1);
            }
        }

        // Check the tile size parameter if present.
        Dimension tileSize = (Dimension)args.getObjectParameter(6);
        if(tileSize != null && (tileSize.width <= 0 || tileSize.height <= 0)) {
            msg.append(I18N.getString("ImageWriteDescriptor23"));
            return false;
        }

        // For collection mode, verify that the source collection contains
        // at least one RenderedImage and that the writer can handle sequences
        // if there is more than one RenderedImage in the source collection.
        if(modeName.equalsIgnoreCase(CollectionRegistryMode.MODE_NAME)) {
            // Get the source collection.
            Collection source = (Collection)args.getSource(0);

            // If the source collection is a CollectionOp do not perform this
            // check as invoking source.size() will render the node.
            if(!(source instanceof CollectionOp)) {

                // Determine the number of RenderedImages in the Collection.
                int numRenderedImages = 0;
                Iterator iter = source.iterator();
                while(iter.hasNext()) {
                    if(iter.next() instanceof RenderedImage) {
                        numRenderedImages++;
                    }
                }

                if(numRenderedImages == 0) {
                    msg.append(I18N.getString("ImageWriteDescriptor24"));
                    return false;
                } else if(numRenderedImages > 1) {
                    // Get the writer parameter.
                    writer = (ImageWriter)args.getObjectParameter(13);

                    // If the parameter writer is null, get one based on the
                    // format.
                    if(writer == null && format != null) {
                        // Get the writers of that format.
                        Iterator writers =
                            ImageIO.getImageWritersByFormatName(format);

                        if(writers != null) {
                            // Get the first writer.
                            writer = (ImageWriter)writers.next();
                        }
                    }

                    if(writer != null) {
                        // Check that the writer can write sequences.
                        if(!writer.canWriteSequence()) {
                            msg.append(I18N.getString("ImageWriteDescriptor25"));
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
