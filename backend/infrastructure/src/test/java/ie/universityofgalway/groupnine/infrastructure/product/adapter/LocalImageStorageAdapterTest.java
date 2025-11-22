package ie.universityofgalway.groupnine.infrastructure.product.adapter;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalImageStorageAdapterTest {

    @Test
    void savesCompressedJpegAndLoadsBack() throws Exception {
        Path tmp = Files.createTempDirectory("img-test-");
        LocalImageStorageAdapter adapter = new LocalImageStorageAdapter(tmp.toString());

        // Create an opaque RGB image to force JPEG path
        BufferedImage img = new BufferedImage(1800, 1200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0,0, img.getWidth(), img.getHeight());
            g.setColor(Color.BLACK);
            g.drawString("Hello", 50, 50);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos); // feed as PNG; adapter may convert to JPEG
        byte[] original = baos.toByteArray();

        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());

        adapter.saveVariantImage(pid, vid, "image.png", "image/png", new ByteArrayInputStream(original));

        Optional<ie.universityofgalway.groupnine.service.product.port.ImageStoragePort.ImageData> loaded = adapter.loadVariantImage(pid, vid);
        assertTrue(loaded.isPresent());
        String ct = loaded.get().getContentType();
        assertTrue(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp"));
        assertTrue(loaded.get().getBytes().length > 0);

        // Ensure the .ct file exists
        Path ctPath = tmp.resolve(pid.getId().toString()).resolve("variants").resolve(vid.getId().toString() + ".ct");
        assertTrue(Files.exists(ctPath));
        String savedCt = Files.readString(ctPath).trim();
        assertTrue(savedCt.equals("image/jpeg") || savedCt.equals("image/png") || savedCt.equals("image/webp"));
    }

    @Test
    void savesPngWhenTransparencyPresent() throws Exception {
        Path tmp = Files.createTempDirectory("img-test-alpha-");
        LocalImageStorageAdapter adapter = new LocalImageStorageAdapter(tmp.toString());

        // Create a semi-transparent image to trigger PNG
        BufferedImage img = new BufferedImage(500, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(new Color(255, 0, 0, 128));
            g.fillRect(0,0, img.getWidth(), img.getHeight());
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] original = baos.toByteArray();

        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());

        adapter.saveVariantImage(pid, vid, "alpha.png", "image/png", new ByteArrayInputStream(original));

        var loaded = adapter.loadVariantImage(pid, vid);
        assertTrue(loaded.isPresent());
        assertEquals("image/png", loaded.get().getContentType());
    }

    @Test
    void rawCopyWhenDecodeFails() throws Exception {
        Path tmp = Files.createTempDirectory("img-test-raw-");
        LocalImageStorageAdapter adapter = new LocalImageStorageAdapter(tmp.toString());

        byte[] garbage = new byte[]{10,20,30,40,50,60};
        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());

        adapter.saveVariantImage(pid, vid, "unknown.bin", "image/png", new ByteArrayInputStream(garbage));

        var loaded = adapter.loadVariantImage(pid, vid);
        assertTrue(loaded.isPresent());
        assertEquals("image/png", loaded.get().getContentType());
        assertEquals(garbage.length, loaded.get().getBytes().length);
    }

    @Test
    void jpgExtWithAlphaIsFlattenedToOpaqueJpeg() throws Exception {
        Path tmp = Files.createTempDirectory("img-test-jpg-alpha-");
        LocalImageStorageAdapter adapter = new LocalImageStorageAdapter(tmp.toString());

        // Create ARGB image larger than MAX_DIMENSION to force scaling/encode branch
        BufferedImage img = new BufferedImage(2000, 1600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(new Color(0, 0, 255, 120));
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] original = baos.toByteArray();

        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());

        adapter.saveVariantImage(pid, vid, "photo.jpg", "image/jpeg", new ByteArrayInputStream(original));
        var loaded = adapter.loadVariantImage(pid, vid);
        assertTrue(loaded.isPresent());
        assertEquals("image/jpeg", loaded.get().getContentType());
        // Read back to ensure alpha channel is not present
        BufferedImage back = ImageIO.read(new ByteArrayInputStream(loaded.get().getBytes()));
        assertNotNull(back);
        assertFalse(back.getColorModel().hasAlpha());
    }

    @Test
    void loadReturnsEmptyWhenNotFound() throws Exception {
        Path tmp = Files.createTempDirectory("img-test-missing-");
        LocalImageStorageAdapter adapter = new LocalImageStorageAdapter(tmp.toString());
        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());
        assertTrue(adapter.loadVariantImage(pid, vid).isEmpty());
    }

    @Test
    void loadWithoutCtFallsBackToProbe() throws Exception {
        Path tmp = Files.createTempDirectory("img-test-probe-");
        LocalImageStorageAdapter adapter = new LocalImageStorageAdapter(tmp.toString());

        // Create large opaque image to trigger scaling and encoding; keep PNG ext to save as PNG
        BufferedImage img = new BufferedImage(1600, 1200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] original = baos.toByteArray();

        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());
        adapter.saveVariantImage(pid, vid, "pic.png", "image/png", new ByteArrayInputStream(original));

        // Delete .ct to force probe
        Path dir = tmp.resolve(pid.getId().toString()).resolve("variants");
        Files.deleteIfExists(dir.resolve(vid.getId().toString() + ".ct"));

        var loaded = adapter.loadVariantImage(pid, vid);
        assertTrue(loaded.isPresent());
        assertEquals("image/png", loaded.get().getContentType());
    }
}
