package com.osirus.installer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class OsirusInstaller extends JFrame {
    
    private JTextArea logArea;
    private JButton installButton;
    private JTextField minecraftDirField;
    private JLabel statusLabel;
    
    public OsirusInstaller() {
        setTitle("Osirus Loader Installer v1.0.0");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Top panel - Minecraft directory selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel dirLabel = new JLabel("Minecraft Directory:");
        minecraftDirField = new JTextField(getDefaultMinecraftDir());
        JButton browseButton = new JButton("Browse");
        
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(new File(minecraftDirField.getText()));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                minecraftDirField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        JPanel dirPanel = new JPanel(new BorderLayout(5, 5));
        dirPanel.add(dirLabel, BorderLayout.NORTH);
        dirPanel.add(minecraftDirField, BorderLayout.CENTER);
        dirPanel.add(browseButton, BorderLayout.EAST);
        
        topPanel.add(dirPanel, BorderLayout.CENTER);
        
        // Center panel - Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Installation Log"));
        
        // Bottom panel - Install button and status
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        installButton = new JButton("Install Osirus Loader");
        installButton.setFont(new Font("Arial", Font.BOLD, 14));
        installButton.addActionListener(e -> performInstall());
        
        statusLabel = new JLabel("Ready to install");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        bottomPanel.add(installButton, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null); // Center on screen
    }
    
    private String getDefaultMinecraftDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        
        if (os.contains("win")) {
            return home + "\\AppData\\Roaming\\.minecraft";
        } else if (os.contains("mac")) {
            return home + "/Library/Application Support/minecraft";
        } else {
            return home + "/.minecraft";
        }
    }
    
    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void performInstall() {
        installButton.setEnabled(false);
        statusLabel.setText("Installing...");
        
        new Thread(() -> {
            try {
                Path minecraftDir = Paths.get(minecraftDirField.getText());
                
                if (!Files.exists(minecraftDir)) {
                    throw new IOException("Minecraft directory does not exist: " + minecraftDir);
                }
                
                log("Starting Osirus Loader installation...");
                log("Minecraft directory: " + minecraftDir);
                
                // Extract the bundled osirus-files.zip from the installer JAR
                log("\nExtracting Osirus files...");
                extractBundledZip(minecraftDir);
                
                log("\n✅ Installation complete!");
                log("\nNext steps:");
                log("1. Open the Minecraft Launcher");
                log("2. Select the '1.21.11-pre5-osirus' profile");
                log("3. Click Play!");
                log("4. Place mods in: " + minecraftDir.resolve("mods"));
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Installation successful!");
                    installButton.setText("Close");
                    installButton.setEnabled(true);
                    installButton.removeActionListener(installButton.getActionListeners()[0]);
                    installButton.addActionListener(e -> System.exit(0));
                    
                    JOptionPane.showMessageDialog(this,
                        "Osirus Loader installed successfully!\n\n" +
                        "Launch Minecraft and select the\n" +
                        "'1.21.11-pre5-osirus' profile to get started.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception ex) {
                log("\n❌ Installation failed: " + ex.getMessage());
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Installation failed!");
                    installButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                        "Installation failed:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void extractBundledZip(Path minecraftDir) throws IOException {
        // Read osirus-files.zip from inside this JAR
        InputStream zipStream = getClass().getResourceAsStream("/osirus-files.zip");
        
        if (zipStream == null) {
            throw new IOException("Could not find osirus-files.zip in installer JAR!");
        }
        
        // Create temp file for the zip
        Path tempZip = Files.createTempFile("osirus", ".zip");
        Files.copy(zipStream, tempZip, StandardCopyOption.REPLACE_EXISTING);
        
        // Extract zip contents
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(tempZip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outputPath = minecraftDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                    log("Created directory: " + entry.getName());
                } else {
                    Files.createDirectories(outputPath.getParent());
                    Files.copy(zis, outputPath, StandardCopyOption.REPLACE_EXISTING);
                    log("Extracted: " + entry.getName());
                }
                
                zis.closeEntry();
            }
        }
        
        Files.delete(tempZip);
    }
    
    public static void main(String[] args) {
        try {
            // Use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to default if system LAF fails
        }
        
        SwingUtilities.invokeLater(() -> {
            OsirusInstaller installer = new OsirusInstaller();
            installer.setVisible(true);
        });
    }
}