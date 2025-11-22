package ie.universityofgalway.groupnine.infrastructure.product.adapter;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.product.port.ImageStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.Iterator;

/**
 * Stores product images on the local filesystem under a configurable base directory.
 */
@Component
public class LocalImageStorageAdapter implements ImageStoragePort {

    private final Path baseDir;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif", "application/octet-stream"
    );

    // Max dimension for the longest side; images will be downscaled preserving aspect ratio
    private static final int MAX_DIMENSION = 1400;

    public LocalImageStorageAdapter(@Value("${app.images.dir:./data/product-images}") String dir) {
        this.baseDir = Path.of(dir).toAbsolutePath().normalize();
    }

    @Override
    public void saveVariantImage(ProductId productId, VariantId variantId, String originalFilename, String contentType, InputStream data) throws IOException {
        String normalizedCt = contentType == null ? "application/octet-stream" : contentType.toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(normalizedCt)) {
            // Allow through; we'll try to decode via ImageIO and re-encode
            normalizedCt = "application/octet-stream";
        }

        // Read all bytes so we can retry/fallback safely
        byte[] originalBytes = data.readAllBytes();

        // Animated GIF? keep original to avoid breaking animation
        if (isAnimatedGif(originalBytes, normalizedCt)) {
            rawCopy(productId, variantId, originalFilename, contentType, originalBytes);
            return;
        }

        // Decode image; if decoding fails, fall back to storing as-is
        BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (inputImage == null) {
            rawCopy(productId, variantId, originalFilename, contentType, originalBytes);
            return;
        }

        // Downscale if needed (no additional compression; preserve original format when possible)
        BufferedImage processed = scaleDownIfNeeded(inputImage, MAX_DIMENSION);

        // If no scaling occurred, keep original bytes to avoid any quality loss
        if (processed.getWidth() == inputImage.getWidth() && processed.getHeight() == inputImage.getHeight()) {
            rawCopy(productId, variantId, originalFilename, contentType, originalBytes);
            return;
        }

        boolean hasAlphaChannel = processed.getColorModel().hasAlpha();
        boolean alphaUsed = hasAlphaChannel && isAlphaUsed(processed);

        // Preserve original extension/content-type where possible
        String ext = extFrom(originalFilename, contentType);
        String targetFormat;
        String targetContentType;
        switch (ext) {
            case "png" -> { targetFormat = "png"; targetContentType = "image/png"; }
            case "jpg", "jpeg" -> { targetFormat = "jpg"; targetContentType = "image/jpeg"; }
            case "webp" -> {
                if (hasWriter("webp")) { targetFormat = "webp"; targetContentType = "image/webp"; }
                else if (alphaUsed) { targetFormat = "png"; targetContentType = "image/png"; }
                else { targetFormat = "jpg"; targetContentType = "image/jpeg"; }
            }
            case "gif" -> { targetFormat = "gif"; targetContentType = "image/gif"; }
            default -> {
                if (alphaUsed) { targetFormat = "png"; targetContentType = "image/png"; }
                else { targetFormat = "jpg"; targetContentType = "image/jpeg"; }
            }
        }

        // If writing JPEG but image has alpha, flatten onto white background
        if ("jpg".equals(targetFormat) && hasAlphaChannel) {
            processed = toOpaque(processed, Color.WHITE);
        }

        // Encode with maximum quality, no extra compression loss
        byte[] bytes;
        if ("jpg".equals(targetFormat) || "webp".equals(targetFormat)) {
            bytes = writeCompressed(processed, targetFormat, 1.0f);
        } else {
            // PNG/GIF: use default writer without lossy compression
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processed, targetFormat, baos);
            bytes = baos.toByteArray();
        }

        Path dir = baseDir.resolve(productId.getId().toString()).resolve("variants");
        Files.createDirectories(dir);
        Path target = dir.resolve(variantId.getId().toString() + "." + targetFormat);
        Path tmp = dir.resolve(variantId.getId().toString() + ".tmp");

        Files.write(tmp, bytes);
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        Files.writeString(dir.resolve(variantId.getId().toString() + ".ct"), targetContentType);
    }

    @Override
    public Optional<ImageData> loadVariantImage(ProductId productId, VariantId variantId) throws IOException {
        Path dir = baseDir.resolve(productId.getId().toString()).resolve("variants");
        if (!Files.exists(dir)) return Optional.empty();

        // Try known extensions
        for (String ext : new String[]{"png", "jpg", "jpeg", "webp", "gif"}) {
            Path p = dir.resolve(variantId.getId().toString() + "." + ext);
            if (Files.exists(p) && Files.isRegularFile(p)) {
                byte[] bytes = Files.readAllBytes(p);
                String ct = readContentType(dir, variantId).orElseGet(() -> probeContentType(p));
                return Optional.of(new ImageData(bytes, ct));
            }
        }
        return Optional.empty();
    }

    private Optional<String> readContentType(Path dir, VariantId variantId) {
        try {
            Path ct = dir.resolve(variantId.getId().toString() + ".ct");
            if (Files.exists(ct)) {
                return Optional.ofNullable(Files.readString(ct)).map(String::trim).filter(s -> !s.isBlank());
            }
        } catch (IOException ignored) {}
        return Optional.empty();
    }

    private String probeContentType(Path p) {
        try {
            String t = Files.probeContentType(p);
            if (t != null) return t;
        } catch (IOException ignored) {}
        String fn = p.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fn.endsWith(".png")) return "image/png";
        if (fn.endsWith(".jpg") || fn.endsWith(".jpeg")) return "image/jpeg";
        if (fn.endsWith(".webp")) return "image/webp";
        if (fn.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    private String extFrom(String originalFilename, String contentType) {
        if (originalFilename != null) {
            String name = originalFilename.toLowerCase(Locale.ROOT);
            if (name.endsWith(".png")) return "png";
            if (name.endsWith(".jpg")) return "jpg";
            if (name.endsWith(".jpeg")) return "jpeg";
            if (name.endsWith(".webp")) return "webp";
            if (name.endsWith(".gif")) return "gif";
        }
        if (contentType == null) return "bin";
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "bin";
        };
    }

    private void rawCopy(ProductId productId, VariantId variantId, String originalFilename, String contentType, byte[] data) throws IOException {
        String ext = extFrom(originalFilename, contentType);
        if ("bin".equals(ext)) ext = "img";
        Path dir = baseDir.resolve(productId.getId().toString()).resolve("variants");
        Files.createDirectories(dir);
        Path target = dir.resolve(variantId.getId().toString() + "." + ext);
        Path tmp = dir.resolve(variantId.getId().toString() + ".tmp");
        Files.write(tmp, data);
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        String ct = contentType != null ? contentType : probeContentType(target);
        Files.writeString(dir.resolve(variantId.getId().toString() + ".ct"), ct);
    }

    private boolean hasWriter(String format) {
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(format);
        return it != null && it.hasNext();
    }

    private byte[] writeCompressed(BufferedImage img, String format, Float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (writers != null && writers.hasNext()) {
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed() && quality != null) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(Math.max(0f, Math.min(1f, quality)));
                }
                writer.write(null, new IIOImage(img, null, null), param);
            } finally {
                writer.dispose();
            }
        } else {
            ImageIO.write(img, format, baos);
        }
        return baos.toByteArray();
    }

    private BufferedImage toOpaque(BufferedImage src, Color bg) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(bg);
            g.fillRect(0, 0, out.getWidth(), out.getHeight());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    private BufferedImage scaleDownIfNeeded(BufferedImage src, int maxDim) {
        int w = src.getWidth();
        int h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxDim) return src;
        double scale = (double) maxDim / (double) max;
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage dst = new BufferedImage(nw, nh, type);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, nw, nh, null);
        } finally {
            g.dispose();
        }
        return dst;
    }

    private boolean isAlphaUsed(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) return false;
        java.awt.image.Raster alpha = img.getAlphaRaster();
        if (alpha == null) return false;
        int w = img.getWidth();
        int h = img.getHeight();
        int stepX = Math.max(1, w / 64);
        int stepY = Math.max(1, h / 64);
        int[] px = new int[1];
        for (int y = 0; y < h; y += stepY) {
            for (int x = 0; x < w; x += stepX) {
                alpha.getPixel(x, y, px);
                if (px[0] != 255) return true;
            }
        }
        alpha.getPixel(w - 1, h - 1, px);
        return px[0] != 255;
    }

    private boolean isAnimatedGif(byte[] data, String contentType) {
        try {
            String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
            if (!ct.contains("gif")) return false;
            try (MemoryCacheImageInputStream mcis = new MemoryCacheImageInputStream(new ByteArrayInputStream(data))) {
                Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("gif");
                if (it == null || !it.hasNext()) return false;
                ImageReader r = it.next();
                try {
                    r.setInput(mcis, false, false);
                    int frames = r.getNumImages(true);
                    return frames > 1;
                } finally {
                    r.dispose();
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
