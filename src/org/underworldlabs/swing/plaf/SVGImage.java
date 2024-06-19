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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class SVGImage extends BufferedImage {

    private final static GVTBuilder builder;
    private final static UserAgent userAgent;
    private final static DocumentLoader loader;
    private final static BridgeContext bridgeContext;
    private final static SAXSVGDocumentFactory factory;
    private static final Map<Integer, String> scaleQuality;
    private final static BufferedImageTranscoder transcoder;

    static {
        builder = new GVTBuilder();
        scaleQuality = new HashMap<>();
        userAgent = new UserAgentAdapter();
        loader = new DocumentLoader(userAgent);
        transcoder = new BufferedImageTranscoder();
        factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

        bridgeContext = new BridgeContext(userAgent, loader);
        bridgeContext.setDynamicState(BridgeContext.STATIC);

        String css = "svg {" +
                "shape-rendering: %s;" +
                "text-rendering:  %s;" +
                "color-rendering: %s;" +
                "image-rendering: %s;" +
                "}";

        String auto = "auto";
        String crisp = "crispEdges";
        String speed = "optimizeSpeed";
        String quality = "optimizeQuality";
        String precise = "geometricPrecision";
        String legible = "optimizeLegibility";

        scaleQuality.put(SCALE_DEFAULT, String.format(css, auto, auto, auto, auto));
        scaleQuality.put(SCALE_FAST, String.format(css, speed, speed, speed, speed));
        scaleQuality.put(SCALE_REPLICATE, String.format(css, speed, speed, speed, speed));
        scaleQuality.put(SCALE_SMOOTH, String.format(css, precise, precise, quality, quality));
        scaleQuality.put(SCALE_AREA_AVERAGING, String.format(css, crisp, legible, auto, auto));
    }

    private boolean hasRendered;

    private final URL svgUrl;
    private final int scalingHint;
    private final SVGDocument svgDocument;

    private SVGImage(URL svgUrl, SVGDocument svgDocument, int width, int height, int scalingHint) {
        super(width, height, TYPE_INT_ARGB);

        this.svgUrl = svgUrl;
        this.hasRendered = false;
        this.scalingHint = scalingHint;
        this.svgDocument = svgDocument;

        render();
    }

    public static SVGImage fromSvg(URL resource, int width, int height) throws IOException {
        try (InputStream inputStream = resource.openStream()) {
            SVGDocument svg = factory.createSVGDocument(resource.toString(), inputStream);

            return (width > 0 && height > 0) ?
                    fromSvgDocument(resource, svg, width, height) :
                    fromSvgDocument(resource, svg);
        }
    }

    public static SVGImage fromSvgDocument(URL resource, SVGDocument svgDocument) {
        GraphicsNode graphicsNode = builder.build(bridgeContext, svgDocument);
        return new SVGImage(
                resource,
                svgDocument,
                (int) graphicsNode.getBounds().getWidth(),
                (int) graphicsNode.getBounds().getHeight(),
                SCALE_DEFAULT
        );
    }

    public static SVGImage fromSvgDocument(URL resource, SVGDocument doc, int width, int height) {
        return new SVGImage(resource, doc, width, height, SCALE_DEFAULT);
    }

    private File createCSSFile(String css) {
        FileWriter writer = null;
        File file = null;

        try {
            file = File.createTempFile("batik-default-override-", ".css");
            file.deleteOnExit();

            writer = new FileWriter(file);
            writer.write(css);

        } catch (IOException ioe) {
            Log.warning("Couldn't write stylesheet; SVG rendered with Batik defaults");

        } finally {
            closeWriter(writer);
        }

        return file;
    }

    private void render() {

        if (hasRendered)
            return;

        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, (float) getWidth());
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) getHeight());
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, svgDocument.getImplementation());
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");

        File cssFile = null;
        String cssString = scaleQuality.get(scalingHint);

        if (cssString != null) {
            cssFile = createCSSFile(cssString);
            if (cssFile != null)
                hints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());
        }

        transcoder.setTranscodingHints(hints);
        transcoder.setImage(this);

        Graphics2D graphics = (Graphics2D) super.getGraphics();
        Composite savedComposite = graphics.getComposite();
        graphics.setComposite(AlphaComposite.Clear);

        try {
            transcoder.transcode(new TranscoderInput(svgDocument), null);
            hasRendered = true;

        } catch (TranscoderException te) {
            Log.warning("Could not transcode " + svgUrl.getPath() + " to raster image; you're going to get a blank BufferedImage of the correct size.");

        } finally {
            graphics.setComposite(savedComposite);
            deleteFile(cssFile);
        }
    }

    private void deleteFile(File file) {
        try {
            if (file != null)
                Files.deleteIfExists(file.toPath());

        } catch (IOException ignored) {
        }
    }

    private void closeWriter(FileWriter writer) {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }

        } catch (IOException ignored) {
        }
    }

    // --- BufferedImage impl ---

    @Override
    public void coerceData(boolean isAlphaPremultiplied) {
        render();
        super.coerceData(isAlphaPremultiplied);
    }

    @Override
    public WritableRaster copyData(WritableRaster outRaster) {
        render();
        return super.copyData(outRaster);
    }

    @Override
    public WritableRaster getAlphaRaster() {
        render();
        return super.getAlphaRaster();
    }

    @Override
    public Raster getData() {
        render();
        return super.getData();
    }

    @Override
    public Graphics getGraphics() {
        render();
        return super.getGraphics();
    }

    // --- Image impl ---

    @Override
    public Image getScaledInstance(int width, int height, int hints) {
        return new SVGImage(svgUrl, svgDocument, width, height, hints);
    }

    // ---

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        private void setImage(BufferedImage image) {
            this.image = image;
        }

        @Override
        public BufferedImage createImage(int arg0, int arg1) {
            return image;
        }

        @Override
        public void writeImage(BufferedImage arg0, TranscoderOutput arg1) {
        }

    } // BufferedImageTranscoder class

}
