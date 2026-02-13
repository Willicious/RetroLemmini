package lemmini.tools;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import lemmini.game.Core;

public class StyleDownloader {
	
	private static final String STYLES_ZIP = "https://github.com/Willicious/RetroLemmini/raw/refs/heads/main/styles.zip";
    
    public static void downloadAllStyles() throws IOException {
        System.out.println("Downloading ZIP: " + STYLES_ZIP);

        Path tempZip = Files.createTempFile("styles", ".zip");
        try (InputStream in = new URL(STYLES_ZIP).openStream()) {
            Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
        }
        
        Path installDir = Core.resourcePath.resolve(Core.STYLES_PATH);

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(tempZip))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                Path outPath = installDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zipIn, outPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Extracted: " + outPath);
                }
            }
        }

        Files.deleteIfExists(tempZip);
        System.out.println("All styles updated!");
    }
    
    public static void startDownload() {
        final JDialog dialog = new JDialog((Frame) null, "Updating Styles", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.add(new JLabel("Updating styles. Please wait...", SwingConstants.CENTER));
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Exception ex = null;

            @Override
            protected Void doInBackground() {
                try {
                    StyleDownloader.downloadAllStyles();
                } catch (Exception e) {
                	ex = e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                dialog.dispose();

                if (ex == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Styles updated successfully!",
                            "Download Complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to update styles:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
        dialog.setVisible(true);
    }
}