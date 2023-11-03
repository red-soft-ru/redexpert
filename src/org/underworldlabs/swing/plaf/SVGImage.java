package org.underworldlabs.swing.plaf;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.executequery.log.Log;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SVGImage extends BufferedImage {
    final static GVTBuilder builder = new GVTBuilder();
    final static SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
    final static UserAgent userAgent = new UserAgentAdapter();
    final static DocumentLoader loader = new DocumentLoader(userAgent);
    final static BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
    final static BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
    //final static private Log log = LogFactory.getLog(SVGImage.class);
    private static final Map<Integer, String> scaleQuality = new HashMap<Integer, String>();

    static {
        bridgeContext.setDynamicState(BridgeContext.STATIC);
    }

    static {
        String css = "svg {" +
                "shape-rendering: %s;" +
                "text-rendering:  %s;" +
                "color-rendering: %s;" +
                "image-rendering: %s;" +
                "}";
        String precise = "geometricPrecision";
        String quality = "optimizeQuality";
        String speed = "optimizeSpeed";
        String crisp = "crispEdges";
        String legible = "optimizeLegibility";
        String auto = "auto";

        scaleQuality.put(SCALE_DEFAULT, String.format(css, auto, auto, auto, auto));
        scaleQuality.put(SCALE_SMOOTH, String.format(css, precise, precise, quality, quality));
        scaleQuality.put(SCALE_REPLICATE, String.format(css, speed, speed, speed, speed));
        scaleQuality.put(SCALE_AREA_AVERAGING, String.format(css, crisp, legible, auto, auto));
        scaleQuality.put(SCALE_FAST, String.format(css, speed, speed, speed, speed));
    }

    final SVGDocument svg;
    final URL svgUrl;
    boolean hasRendered = false;
    private int scalingHint = SCALE_DEFAULT;

    private SVGImage(URL resource, SVGDocument doc, int width, int height, int hints) {
        super(width, height, TYPE_INT_ARGB);
        scalingHint = hints;
        svgUrl = resource;
        svg = doc;
        render();
    }


    public static SVGImage fromSvg(URL resource, int width, int height) throws IOException {
        InputStream rs = null;
        try {
            rs = resource.openStream();
            SVGDocument svg = factory.createSVGDocument(resource.toString(), rs);
            if (width > 0 && height > 0)
                return fromSvgDocument(resource, svg, width, height);
            else return fromSvgDocument(resource, svg);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    public static SVGImage fromSvgDocument(URL resource, SVGDocument doc) {
        GraphicsNode graphicsNode = builder.build(bridgeContext, doc);
        Double width = graphicsNode.getBounds().getWidth();
        Double height = graphicsNode.getBounds().getHeight();
        return new SVGImage(resource, doc, width.intValue(), height.intValue(), SCALE_DEFAULT);
    }

    public static SVGImage fromSvgDocument(URL resource, SVGDocument doc, int width, int height) {
        return new SVGImage(resource, doc, width, height, SCALE_DEFAULT);
    }

    @Override
    public void coerceData(boolean isAlphaPremultiplied) {
        if (!hasRendered) {
            render();
        }
        super.coerceData(isAlphaPremultiplied);
    }

    @Override
    public WritableRaster copyData(WritableRaster outRaster) {
        if (!hasRendered) {
            render();
        }
        return super.copyData(outRaster);
    }

    private File createCSS(String css) {
        FileWriter cssWriter = null;
        File cssFile = null;
        try {
            cssFile = File.createTempFile("batik-default-override-", ".css");
            cssFile.deleteOnExit();
            cssWriter = new FileWriter(cssFile);
            cssWriter.write(css);
        } catch (IOException ioe) {
            org.executequery.log.Log.warning("Couldn't write stylesheet; SVG rendered with Batik defaults");
        } finally {

            if (cssWriter != null) {
                try {
                    cssWriter.flush();
                    cssWriter.close();
                } catch (IOException ioe) {
                }
            }
        }
        return cssFile;
    }

    @Override
    public WritableRaster getAlphaRaster() {
        if (!hasRendered) {
            render();
        }
        return super.getAlphaRaster();
    }

    @Override
    public Raster getData() {
        if (!hasRendered) {
            render();
        }
        return super.getData();
    }

    @Override
    public Graphics getGraphics() {
        if (!hasRendered) {
            render();
        }
        return super.getGraphics();
    }

    public Image getScaledInstance(int width, int height, int hints) {
        SVGImage newImage = new SVGImage(svgUrl, svg, width, height, hints);
        return newImage;
    }

    private void render() {
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, (float) getWidth());
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) getHeight());
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, svg.getImplementation());
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        String css = scaleQuality.get(scalingHint);
        File cssFile = null;
        if (css != null) {
            cssFile = createCSS(css);
            if (cssFile != null) {
                hints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());
            }
        }
        transcoder.setTranscodingHints(hints);
        transcoder.setImage(this);
        // This may be a re-render, if the scaling quality hint has changed.
        // As such, we force the image into overwrite mode, and kick it back when we're done / fail
        Graphics2D gfx = (Graphics2D) super.getGraphics();
        Composite savedComposite = gfx.getComposite();
        gfx.setComposite(AlphaComposite.Clear);
        try {
            transcoder.transcode(new TranscoderInput(svg), null);
            hasRendered = true;
        } catch (TranscoderException te) {
            Log.warning("Could not transcode " + svgUrl.getPath() + " to raster image; you're going to get a blank BufferedImage of the correct size.");
        } finally {
            gfx.setComposite(savedComposite);
            if (cssFile != null) {
                cssFile.delete();
            }
        }
    }

    public void setScalingHint(int hint) {
        this.scalingHint = hint;
        // Forces a re-render
        this.hasRendered = false;
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image = null;

        @Override
        public BufferedImage createImage(int arg0, int arg1) {

            return image;
        }

        private void setImage(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void writeImage(BufferedImage arg0, TranscoderOutput arg1) throws TranscoderException {
        }
    }
}

