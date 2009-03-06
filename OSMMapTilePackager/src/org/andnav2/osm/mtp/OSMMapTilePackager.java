// Created by plusminus on 9:22:20 PM - Mar 5, 2009
package org.andnav2.osm.mtp;

import java.io.File;
import java.util.Scanner;

import org.andnav2.osm.mtp.adt.OSMTileInfo;
import org.andnav2.osm.mtp.download.DownloadManager;
import org.andnav2.osm.mtp.util.FolderDeleter;
import org.andnav2.osm.mtp.util.FolderZipper;
import org.andnav2.osm.mtp.util.Util;


public class OSMMapTilePackager {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public static void main(String[] args) {
		if(args == null || args.length == 0)
			printUsageAndExit();		

		/* PArsing will only start if this variable was set. */
		String serverURL = null;
		String destinationFile = null;
		String tempFolder = null;
		Double north = null;
		Double south = null;
		Double east = null;
		Double west = null;
		Integer maxzoom = null;
		int minzoom = 0;

		try{
			for (int i = 0; i < args.length; i += 2) {
				if(args[i].equals("-u")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						serverURL = args[i+1];
					}
				}else if(args[i].equals("-d")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						destinationFile = args[i+1];
					}
				}else if(args[i].equals("-zmin")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						minzoom = Integer.parseInt(args[i+1]);
					}
				}else if(args[i].equals("-zmax")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						maxzoom = Integer.parseInt(args[i+1]);
					}
				}else if(args[i].equals("-t")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						tempFolder = args[i+1];
					}
				}else if(args[i].equals("-n")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						north = Double.parseDouble(args[i+1]);
					}
				}else if(args[i].equals("-s")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						south = Double.parseDouble(args[i+1]);
					}
				}else if(args[i].equals("-e")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						east = Double.parseDouble(args[i+1]);
					}
				}else if(args[i].equals("-w")){
					if(i >= args.length){
						printUsageAndExit();
					}else{
						west = Double.parseDouble(args[i+1]);
					}
				}
			}
		}catch(NumberFormatException nfe){
			printUsageAndExit();			
		}

		if(serverURL == null || tempFolder == null || destinationFile == null)
			printUsageAndExit();

		if(north == null || south == null || east == null || west == null)
			printUsageAndExit();

		System.out.println("---------------------------");
		runDownloading(serverURL, tempFolder, minzoom, maxzoom, north, south, east, west);
		System.out.println("---------------------------");
		runZipToFile(tempFolder, destinationFile);
		System.out.println("---------------------------");
		runCleanup(tempFolder);
		System.out.println("---------------------------");
	}

	private static void printUsageAndExit() {
		System.out.println("Usage:\n" +
				"-u\t[OSM-style tile URL: http://_URL_/%d/%d/%d.png]\n" +
				"-d\t[Destination-file: C:\\mappack.zip]\n" +
				"-zmin\t[Minimum zoomLevel to download]\n" +
				"-zmax\t[Maximum zoomLevel to download]\n" +
				"-n\t[North Latitude]\n" +
				"-s\t[South Latitude]\n" +
				"-e\t[East Longitude]\n" +
				"-w\t[West Longitude]\n"
		);
		System.exit(0);
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

	private static void runZipToFile(final String pTempFolder, final String pDestinationFile) {
		try {
			System.out.print("Zipping files ...");
			FolderZipper.zipFolderToFile(new File(pDestinationFile), new File(pTempFolder));
			System.out.println(" done.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runCleanup(final String pTempFolder){
		abortIfUserIsNotSure("This will delete the temp folder: " + pTempFolder + " !");
		
		/* deleteDirecto*/
		System.out.print("Deleting temp folder ...");
		FolderDeleter.deleteFolder(new File(pTempFolder));
		System.out.println(" done.");
	}

	private static void runDownloading(final String pBaseURL, final String pTempFolder, final int pMinZoom, final int pMaxzoom, final double pNorth, final double pSouth, final double pEast, final double pWest) {
		final String pTempBaseURL = pTempFolder 
		+ File.separator + "%d"
		+ File.separator + "%d"
		+ File.separator + "%d"
		+ pBaseURL.substring(pBaseURL.lastIndexOf('.'))
		.replace(File.separator + File.separator, File.separator);

		final DownloadManager dm = new DownloadManager(pBaseURL, pTempBaseURL);

		/* Calculate file-count. */
		int fileCnt = 0; 
		for(int z = pMinZoom; z <= pMaxzoom; z++){
			final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(pNorth, pWest, z);
			final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(pSouth, pEast, z);

			final int dx = lowerRight.x - upperLeft.x + 1;
			final int dy = lowerRight.y - upperLeft.y + 1;
			fileCnt += dx * dy;
		}

		abortIfUserIsNotSure("This will download: " + fileCnt + " Maptiles!");	

		/* For each zoomLevel. */
		for(int z = pMinZoom; z <= pMaxzoom; z++){
			final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(pNorth, pWest, z);
			final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(pSouth, pEast, z);

			System.out.print("ZoomLevel: " + z + " ");
			for(int x = upperLeft.x; x <= lowerRight.x; x++){
				for(int y = upperLeft.y; y <= lowerRight.y; y++){
					dm.add(new OSMTileInfo(x,y,z));
				}	
			}
			try{	
				dm.waitEmpty();
				System.out.println(" done.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			System.out.print("Awaiting termination of all threads ...");
			dm.waitFinished();
			System.out.println(" done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void abortIfUserIsNotSure(final String message) {
		System.out.println(message);
		System.out.print("Are you sure? [Y/N] ?: ");
		final String line = new Scanner(System.in).nextLine().toUpperCase().trim();

		if(!line.equals("Y") && !line.equals("YES")){
			System.err.println("User aborted.");
			System.exit(0);
		}	
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
