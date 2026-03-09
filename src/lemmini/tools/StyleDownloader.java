package lemmini.tools;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import lemmini.game.Core;
import lemmini.game.LemmException;

public class StyleDownloader {
	
	private static final String STYLES_ZIP = "https://github.com/Willicious/RetroLemmini/raw/refs/heads/main/styles.zip";
    
	public static void downloadAllStyles(Consumer<String> progressCallback) throws IOException {
	    progressCallback.accept("Downloading styles...");

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
	                progressCallback.accept("Updating: " + entry.getName());
	            }
	        }
	    }

	    Files.deleteIfExists(tempZip);
	    progressCallback.accept("All styles updated!");
	}
    
	public static void startDownload() {
	    final JDialog dialog = new JDialog((Frame) null, "Updating Styles", true);
	    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	    final JLabel label = new JLabel("Updating styles. Please wait...", SwingConstants.CENTER);
	    dialog.add(label);

	    dialog.setSize(350, 120);
	    dialog.setLocationRelativeTo(null);
	    dialog.setResizable(false);

	    SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {

	        private Exception ex = null;

	        @Override
	        protected Void doInBackground() {
	            try {
	                StyleDownloader.downloadAllStyles(message -> publish(message));
	            } catch (Exception e) {
	                ex = e;
	            }
	            return null;
	        }

	        @Override
	        protected void process(java.util.List<String> chunks) {
	            label.setText(chunks.get(chunks.size() - 1));
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
	
	public static void reInitializeCore() {
        try {
			Core.init(Core.getJarDirectory());
		} catch (LemmException e) {
		} catch (IOException e) {
		}
	}
}