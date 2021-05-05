// Created by plusminus on 11:39:40 AM - Apr 8, 2009
package org.osmdroid.mtp.ui;

import org.osmdroid.mtp.OSMMapTilePackager;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OSMMapTilePackagerUI extends JFrame {
    // ===========================================================
    // Constants
    // ===========================================================

    private static final long serialVersionUID = 749039680990304151L;

    // ===========================================================
    // Fields
    // ===========================================================
    private final JPanel mainPanel;
    private final JLabel lblURL = new JLabel("URL:");

    private final JLabel lblDestination = new JLabel("Destination File (.zip, .gemf, or .sqlite):");
    private final JTextField txtDestination = new JTextField();
    private final JButton cmdDestinationBrowse = new JButton("Browse");
    private final JLabel lblTempFolder = new JLabel("Temp-Folder (and tile source name):");
    private final JTextField txtTempFolder = new JTextField();
    private final JButton cmdTempFolderBrowse = new JButton("Browse");
    private final JTextField txtURL = new JTextField("https://b.tile.openstreetmap.org/%d/%d/%d.png");
    private final JButton cmdURLTest = new JButton("Test");

    private final JButton cmdExecute = new JButton("Execute");

    private final JLabel lblMinZoom = new JLabel("MinZoom:");
    private final JLabel lblMaxZoom = new JLabel("MaxZoom:");
    private final JSlider sliMinZoom = new JSlider();
    private final JSlider sliMaxZoom = new JSlider();

    private final JLabel lblNorth = new JLabel("North:");
    private final JTextField txtNorth = new JTextField();

    private final JLabel lblEast = new JLabel("East:");
    private final JTextField txtEast = new JTextField();

    private final JLabel lblSouth = new JLabel("South:");
    private final JTextField txtSouth = new JTextField();

    private final JLabel lblWest = new JLabel("West:");
    private final JTextField txtWest = new JTextField();

    private final JLabel lblFileAppendix = new JLabel("FileAppendix:");
    private final JTextField txtFileAppendix = new JTextField("");
    private final JCheckBox chkForce = new JCheckBox("Force");
    private final JLabel lblForce = new JLabel("(Will not ask on problems.)");
    private final JLabel lblStatus = new JLabel("Status:");

    // ===========================================================
    // Constructors
    // ===========================================================
    public static void main(final String[] args) {
        final JFrame j = new OSMMapTilePackagerUI();
        j.setPreferredSize(new Dimension(640, 400));
        j.setDefaultCloseOperation(EXIT_ON_CLOSE);
        j.pack();
        j.setVisible(true);
    }

    public OSMMapTilePackagerUI() {
        mainPanel = new JPanel();
        this.add(this.mainPanel);
        final GridBagLayout gbpanel0 = new GridBagLayout();
        final GridBagConstraints gbcpanel0 = new GridBagConstraints();
        mainPanel.setLayout(gbpanel0);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 0;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblURL, gbcpanel0);
        mainPanel.add(lblURL);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 1;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblDestination, gbcpanel0);
        mainPanel.add(lblDestination);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 1;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtDestination, gbcpanel0);
        mainPanel.add(txtDestination);

        gbcpanel0.gridx = 2;
        gbcpanel0.gridy = 1;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(cmdDestinationBrowse, gbcpanel0);
        cmdDestinationBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setAcceptAllFileFilterUsed(true);

                final int result = jfc.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    final String absolutePath = jfc.getSelectedFile().getAbsolutePath();
                    if (absolutePath.endsWith(".zip")) {
                        txtDestination.setText(absolutePath);
                    } else {
                        txtDestination.setText(absolutePath + ".zip");
                    }
                }
            }
        });
        mainPanel.add(cmdDestinationBrowse);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 2;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblTempFolder, gbcpanel0);
        mainPanel.add(lblTempFolder);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 2;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtTempFolder, gbcpanel0);
        mainPanel.add(txtTempFolder);

        gbcpanel0.gridx = 2;
        gbcpanel0.gridy = 2;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(cmdTempFolderBrowse, gbcpanel0);
        cmdTempFolderBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setAcceptAllFileFilterUsed(false);

                final int result = jfc.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    txtTempFolder.setText(jfc.getSelectedFile().getAbsolutePath());
                }
            }
        });
        mainPanel.add(cmdTempFolderBrowse);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 0;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtURL, gbcpanel0);
        mainPanel.add(txtURL);

        gbcpanel0.gridx = 2;
        gbcpanel0.gridy = 0;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(cmdURLTest, gbcpanel0);
        cmdURLTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(String.format(txtURL.getText(), 0, 0, 0)));
                    } catch (final Throwable t) {
                        t.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Could not open browser.");
                }
            }
        });
        mainPanel.add(cmdURLTest);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 3;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblMinZoom, gbcpanel0);
        mainPanel.add(lblMinZoom);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 4;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblMaxZoom, gbcpanel0);
        mainPanel.add(lblMaxZoom);

        sliMinZoom.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                lblMinZoom.setText("MinZoom: (" + sliMinZoom.getValue() + ")");
            }
        });
        sliMinZoom.setMinimum(0);
        sliMinZoom.setMaximum(22);
        sliMinZoom.setMajorTickSpacing(1);
        sliMinZoom.setMinorTickSpacing(1);
        sliMinZoom.setValue(0);
        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 3;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(sliMinZoom, gbcpanel0);
        mainPanel.add(sliMinZoom);

        sliMaxZoom.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                lblMaxZoom.setText("MaxZoom: (" + sliMaxZoom.getValue() + ")");
            }
        });
        sliMaxZoom.setMaximum(22);
        sliMaxZoom.setMinimum(0);
        sliMaxZoom.setValue(10);
        sliMaxZoom.setMajorTickSpacing(1);
        sliMaxZoom.setMinorTickSpacing(1);
        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 4;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(sliMaxZoom, gbcpanel0);
        mainPanel.add(sliMaxZoom);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 5;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblNorth, gbcpanel0);
        mainPanel.add(lblNorth);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 5;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtNorth, gbcpanel0);
        txtNorth.setText("90.0");
        mainPanel.add(txtNorth);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 6;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblEast, gbcpanel0);
        mainPanel.add(lblEast);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 6;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtEast, gbcpanel0);
        txtEast.setText("180.0");
        mainPanel.add(txtEast);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 7;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblSouth, gbcpanel0);
        mainPanel.add(lblSouth);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 7;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtSouth, gbcpanel0);
        txtSouth.setText("-90.0");
        mainPanel.add(txtSouth);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 9;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblFileAppendix, gbcpanel0);

        mainPanel.add(lblFileAppendix);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 8;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtWest, gbcpanel0);
        txtWest.setText("-180.0");
        mainPanel.add(txtWest);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 8;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 0;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblWest, gbcpanel0);
        mainPanel.add(lblWest);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 9;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(txtFileAppendix, gbcpanel0);

        mainPanel.add(txtFileAppendix);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 10;
        gbcpanel0.gridwidth = 1;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(chkForce, gbcpanel0);
        mainPanel.add(chkForce);

        gbcpanel0.gridx = 1;
        gbcpanel0.gridy = 10;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblForce, gbcpanel0);
        mainPanel.add(lblForce);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 11;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(cmdExecute, gbcpanel0);
        mainPanel.add(cmdExecute);

        gbcpanel0.gridx = 0;
        gbcpanel0.gridy = 12;
        gbcpanel0.gridwidth = 2;
        gbcpanel0.gridheight = 1;
        gbcpanel0.fill = GridBagConstraints.BOTH;
        gbcpanel0.weightx = 1;
        gbcpanel0.weighty = 0;
        gbcpanel0.anchor = GridBagConstraints.NORTH;
        gbpanel0.setConstraints(lblStatus, gbcpanel0);
        mainPanel.add(lblStatus);

        cmdExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                //TODO validate user input
                //Start the process
                final double north = Double.parseDouble(txtNorth.getText());
                final double south = Double.parseDouble(txtSouth.getText());
                final double east = Double.parseDouble(txtEast.getText());
                final double west = Double.parseDouble(txtWest.getText());
                final int minzoom = sliMinZoom.getValue();
                final int maxzoom = sliMaxZoom.getValue();
                final String url = txtURL.getText();
                final String destination = txtDestination.getText();
                final String temp = txtTempFolder.getText();
                final String appendix = txtFileAppendix.getText();
                final int threads = Runtime.getRuntime().availableProcessors();

                final int count = OSMMapTilePackager.runFileExpecter(minzoom, maxzoom, north, south, east, west);

                int dialogButton = JOptionPane.showConfirmDialog(null, "This will download " + count + " tiles. OK?", "Warning", JOptionPane.YES_NO_OPTION);

                if (dialogButton == JOptionPane.YES_OPTION) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OSMMapTilePackager.execute(url, destination, temp, threads, appendix, minzoom, maxzoom, north, south, east, west, new OSMMapTilePackager.ProgressNotification() {
                                @Override
                                public void updateProgress(String msg) {
                                    lblStatus.setText(msg);
                                }
                            });

                            int dialogButton2 = JOptionPane.showConfirmDialog(null, "All done, do you want delete the tile cache?", "Warning", JOptionPane.YES_NO_OPTION);

                            if (dialogButton2 == JOptionPane.YES_OPTION) {
                                OSMMapTilePackager.runCleanup(temp, false);

                            }
                            lblStatus.setText("Done");
                        }
                    }).start();

                }
            }
        });

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================
    // ===========================================================
    // Methods
    // ===========================================================
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
