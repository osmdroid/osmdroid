package org.osmdroid.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GEMF File handler class.
 * 
 * Reference: https://sites.google.com/site/abudden/android-map-store
 * 
 * @author A. S. Budden
 * @author Erik Burrows
 * 
 */
public class GEMFFile {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final Logger logger = LoggerFactory.getLogger(GEMFFile.class);

	private static final int VERSION = 4;
	private static final int TILE_SIZE = 256;

	private static final boolean DEBUGMODE = false;

	// ===========================================================
	// Fields
	// ===========================================================

	// Path to first GEMF file (additional files as <basename>-1, <basename>-2, ...
	private String mLocation;

	// All GEMF file parts for this archive
	private List<RandomAccessFile> mFiles = new ArrayList<RandomAccessFile>();

	// Tile ranges represented within this archive
	private List<GEMFRange> mRangeData = new ArrayList<GEMFRange>();

	// File sizes for offset calculation
	private List<Long> mFileSizes = new ArrayList<Long>();

	// List of tile sources within this archive
	private HashMap<Integer, String> mSources = new HashMap<Integer, String>();

	// Fields to restrict to a single source for reading
	private boolean mSourceLimited = false;
	private int mCurrentSource = 0;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public GEMFFile (File pLocation) throws FileNotFoundException, IOException {
		this(pLocation.getAbsolutePath());
	}
	
	public GEMFFile (String pLocation) throws FileNotFoundException, IOException {
		mLocation = pLocation;
		openFiles();
		readHeader();
		
		if (DEBUGMODE)
			logger.debug("Initialized: " + pLocation);
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	public String getName() {
		return mLocation;
	}
	
	public HashMap<Integer, String> getSources() {
		return mSources;
	}
	
	public void selectSource(int pSource) {
		if (mSources.containsKey(new Integer(pSource))) {
			mSourceLimited = true;
			mCurrentSource = pSource;
		}
	}
	
	public void acceptAnySource() {
		mSourceLimited = false;
	}
	
	public Set<Integer> getZoomLevels() {
		Set<Integer> setZoomLevels = new TreeSet<Integer>();

		for (GEMFRange rs: mRangeData) {
			setZoomLevels.add(rs.zoom);
		}

		return setZoomLevels;
	}
	
	public void close() throws IOException {
		for (RandomAccessFile file: mFiles) {
			file.close();
		}
	}

	private void openFiles() throws FileNotFoundException {
		// Populate the mFiles array
		
		File base = new File(mLocation);
		mFiles.add(new RandomAccessFile(base, "r"));
		
		int i = 0;
		for(;;) {
			i = i + 1;
			File nextFile = new File(mLocation + "-" + i);
			if (nextFile.exists()) {
				mFiles.add(new RandomAccessFile(nextFile, "r"));
			} else {
				break;
			}
		}
	}
	
	private void readHeader() throws IOException {
		final RandomAccessFile baseFile = mFiles.get(0);
		
		// Get file sizes
		for (RandomAccessFile file : mFiles) {
			mFileSizes.add(file.length());
		}
				
		// Version
		int version = baseFile.readInt();
		if (version != VERSION) {
			throw new IOException("Bad file version: " + version);
		}
		
		// Tile Size
		int tile_size = baseFile.readInt();
		if (tile_size != TILE_SIZE) {
			throw new IOException("Bad tile size: " + tile_size);
		}
		
		// Source List
		int source_count = baseFile.readInt();

		for (int i=0;i<source_count;i++) {
			int source_index = baseFile.readInt();
			int source_name_length = baseFile.readInt();
			byte[] name_data = new byte[source_name_length];
			baseFile.read(name_data, 0, source_name_length);
			String name = new String(name_data);
			mSources.put(new Integer(source_index), name);
		}

		// Ranges
		int num_ranges = baseFile.readInt();
		for (int i=0;i<num_ranges;i++) {
			GEMFRange rs = new GEMFRange();
			rs.zoom = baseFile.readInt();
			rs.xmin = baseFile.readInt();
			rs.xmax = baseFile.readInt();
			rs.ymin = baseFile.readInt();
			rs.ymax = baseFile.readInt();
			rs.source_index = baseFile.readInt();
			rs.offset = baseFile.readLong();
			mRangeData.add(rs);
		}
	}
	
	public InputStream getInputStream(int pX, int pY, int pZ) {
		GEMFRange range = null;
		final int U32SIZE = 4;
		final int U64SIZE = 8;

		for (GEMFRange rs: mRangeData)
		{
			if ((pZ == rs.zoom)
					&& (pX >= rs.xmin)
					&& (pX <= rs.xmax)
					&& (pY >= rs.ymin)
					&& (pY <= rs.ymax)
					&& (( ! mSourceLimited) || (rs.source_index == mCurrentSource))) {

				if (DEBUGMODE)
					logger.debug("(" + pX + "," + pY + ") in range "
							+ "((" + rs.xmin + ".." + rs.xmax + "),("
							+ rs.ymin + ".." + rs.ymax + "))");
				range = rs;
				break;

			} else {
				if (DEBUGMODE)
					logger.debug("(" + pX + "," + pY + ") not in range "
						+ "((" + rs.xmin + ".." + rs.xmax + "),("
						+ rs.ymin + ".." + rs.ymax + "))");
			}
		}

		if (range == null)	{
			if (DEBUGMODE)
				logger.debug("Couldn't find relevant range");
			
			return null;
		}

		byte[] data;
		long data_file_offset;
		int data_file_length;

		try	{

			// Determine offset to requested tile record in the header
			int num_y = range.ymax + 1 - range.ymin;
			int x_index = pX - range.xmin;
			int y_index = pY - range.ymin;
			long offset = (x_index * num_y) + y_index;
			offset *= (U32SIZE + U64SIZE);
			offset += range.offset;
			

			// Read tile record from header, get offset and size of data record
			RandomAccessFile baseFile = mFiles.get(0);
			baseFile.seek(offset);
			data_file_offset = baseFile.readLong();
			data_file_length = baseFile.readInt();

			
			// Seek to correct data file and offset.
			RandomAccessFile pDataFile = mFiles.get(0);
			int index = 0;
			if (data_file_offset > mFileSizes.get(0))	{
				int filelist_count = mFileSizes.size();

				while ((index < (filelist_count - 1)) && (data_file_offset > mFileSizes.get(index))) {
					data_file_offset -= mFileSizes.get(index);
					index += 1;
				}

				pDataFile = mFiles.get(index);
			}


			// Read data block into a byte array
			data = new byte[data_file_length];
			pDataFile.seek(data_file_offset);
			pDataFile.read(data, 0, data_file_length);
			
		} catch (java.io.IOException e) {
			return null;
		}

		// Return byte array as InputStream as required by tile provider framework
		return new ByteArrayInputStream(data, 0, data_file_length);
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	// Class to represent a range of stored tiles within the datafile.
	private class GEMFRange	{
		int zoom;
		int xmin;
		int xmax;
		int ymin;
		int ymax;
		int source_index;
		long offset;
	};
}
