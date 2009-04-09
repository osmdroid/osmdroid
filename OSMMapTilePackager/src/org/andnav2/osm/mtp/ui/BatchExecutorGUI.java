// Created by plusminus on 2:29:40 PM - Mar 8, 2009
package org.andnav2.osm.mtp.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.andnav2.osm.mtp.OSMMapTilePackager;


public class BatchExecutorGUI extends JFrame {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final long serialVersionUID = 5863710966745357864L;

	// ===========================================================
	// Fields
	// ===========================================================
	
	private JTextArea mTxtBatchItems;
	private JButton mBtnStartBatch;

	private JProgressBar mProgressBar;

	private int mProgress;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static void main(String[] args) {
		new BatchExecutorGUI().setVisible(true);
	}
	
	
	public BatchExecutorGUI() {
		initGUI();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(300, 200));
		this.pack();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());
		
		this.mTxtBatchItems = new JTextArea();
		this.add(this.mTxtBatchItems, BorderLayout.CENTER);
		this.mTxtBatchItems.setFont(new Font("Tahoma", Font.PLAIN, 8));
		
		
		this.mBtnStartBatch = new JButton("Run Batch");
		this.add(this.mBtnStartBatch, BorderLayout.SOUTH);
		this.mBtnStartBatch.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				freezeUI();
				startBatch();
				unFreezeUI();
			}
		});
		
		this.mProgressBar = new JProgressBar();
		this.mProgressBar.setStringPainted(true);
		this.add(this.mProgressBar, BorderLayout.NORTH);
	}

	private void freezeUI() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		this.mBtnStartBatch.setEnabled(false);
		this.mTxtBatchItems.setEnabled(false);
	}

	private void unFreezeUI() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.mBtnStartBatch.setEnabled(true);
		this.mTxtBatchItems.setEnabled(true);
	}

	private void incrementProgress() {
		synchronized (this) {
			this.mProgress++;
			this.mProgressBar.setValue(this.mProgress);
		}
		this.mProgressBar.paint(this.mProgressBar.getGraphics());	
	}

	private void startBatch() {
		final String txtBatchItemsContent = this.mTxtBatchItems.getText();
		final int numLines = txtBatchItemsContent.split("\n").length;
		
		this.mProgressBar.setMaximum(numLines);
		
		final Scanner scan = new Scanner(txtBatchItemsContent);

		this.mProgress = 0;
		this.mProgressBar.setValue(this.mProgress);
		this.mProgressBar.paint(this.mProgressBar.getGraphics());	
		
		while(scan.hasNextLine()){
			final String currentLine = scan.nextLine();
			
			final Thread runner = new Thread(new Runnable(){
				@Override
				public void run() {
					OSMMapTilePackager.main(currentLine.split(" "));
					BatchExecutorGUI.this.incrementProgress();
				}
			});
			runner.start();
			try {
				runner.join();
			} catch (InterruptedException e){ e.printStackTrace(); }
		}
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
