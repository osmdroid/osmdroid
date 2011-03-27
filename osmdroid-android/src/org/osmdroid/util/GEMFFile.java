package org.osmdroid.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

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

	private static final long FILE_SIZE_LIMIT = 1 * 1024 * 1024 * 1024; // 1GB
	private static final int FILE_COPY_BUFFER_SIZE = 1024;
	
	private static final int VERSION = 4;
	private static final int TILE_SIZE = 256;

	private static final int U32_SIZE = 4;
	private static final int U64_SIZE = 8;
	
	
	// ===========================================================
	// Fields
	// ===========================================================

	// Path to first GEMF file (additional files as <basename>-1, <basename>-2, ...
	private String mLocation;

	// All GEMF file parts for this archive
	private List<RandomAccessFile> mFiles = new ArrayList<RandomAccessFile>();
	private List<String> mFileNames = new ArrayList<String>();
	
	// Tile ranges represented within this archive
	private List<GEMFRange> mRangeData = new ArrayList<GEMFRange>();

	// File sizes for offset calculation
	private List<Long> mFileSizes = new ArrayList<Long>();

	// List of tile sources within this archive
	private LinkedHashMap<Integer, String> mSources = new LinkedHashMap<Integer, String>();

	// Fields to restrict to a single source for reading
	private boolean mSourceLimited = false;
	private int mCurrentSource = 0;
	
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	
	/*
	 * Constructor to read existing GEMF archive
	 * 
	 * @param pLocation
	 * 		File object representing first GEMF archive file
	 */
	public GEMFFile (File pLocation) throws FileNotFoundException, IOException {
		this(pLocation.getAbsolutePath());
	}
	
	
	/*
	 * Constructor to read existing GEMF archive
	 * 
	 * @param pLocation
	 * 		String object representing path to first GEMF archive file
	 */
	public GEMFFile (String pLocation) throws FileNotFoundException, IOException {
		mLocation = pLocation;
		openFiles();
		readHeader();
	}
	
	
	/*
	 * Constructor to create new GEMF file from directory of sources/tiles.
	 * 
	 * @param pLocation
	 * 		String object representing path to first GEMF archive file.
	 * 		Additional files (if archive size exceeds FILE_SIZE_LIMIT
	 * 		will be created with numerical suffixes, eg: test.gemf-1, test.gemf-2.
	 * @param pSourceFolders
	 * 		Each specified folder will be imported into the GEMF archive as a seperate
	 * 		source. The name of the folder will be the name of the source in the archive.
	 */
	public GEMFFile (final String pLocation, final List<File> pSourceFolders) 
		throws FileNotFoundException, IOException {
		/*
		 * 1. For each source folder
		 *   1. Create array of zoom levels, X rows, Y rows
		 * 2. Build index data structure index[source][zoom][range]
		 *   1. For each S-Z-X find list of Ys values
		 *   2. For each S-Z-X-Ys set, find complete X ranges
		 *   3. For each S-Z-Xr-Ys set, find complete Y ranges, create Range record  
		 * 3. Write out index
		 *   1. Header
		 *   2. Sources
		 *   3. For each Range
		 *     1. Write Range record
		 * 4. For each Range record
		 *   1. For each Range entry
		 *     1. If over file size limit, start new data file
		 *     2. Write tile data
		 */
		
		this.mLocation = pLocation;
		
		// Create in-memory array of sources, X and Y values.
		LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>>> dirIndex = 
			new LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>>>();
		
		for (File sourceDir: pSourceFolders) {

			LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>> zList = 
				new LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>>();

			for (File zDir: sourceDir.listFiles()) {
				// Make sure the directory name is just a number
				try {
					Integer.parseInt(zDir.getName());
				} catch (NumberFormatException e) {
					continue;
				}
				
				LinkedHashMap<Integer, LinkedHashMap<Integer, File>> xList = 
					new LinkedHashMap<Integer, LinkedHashMap<Integer, File>>();
				
				for (File xDir: zDir.listFiles()) {

					// Make sure the directory name is just a number
					try {
						Integer.parseInt(xDir.getName());
					} catch (NumberFormatException e) {
						continue; 
					}
				
					LinkedHashMap<Integer, File> yList = new LinkedHashMap<Integer, File>();
					for (File yFile: xDir.listFiles()) {

						try {
							Integer.parseInt(yFile.getName().substring(
									0, yFile.getName().indexOf('.')));
						} catch (NumberFormatException e) {
							continue;
						}

						yList.put(Integer.parseInt(yFile.getName().substring(
								0, yFile.getName().indexOf('.'))), yFile);
					}
				
					xList.put(new Integer(xDir.getName()), yList);
				}

				zList.put(Integer.parseInt(zDir.getName()), xList);
			}
				
			dirIndex.put(sourceDir.getName(), zList);
		}
		
		// Create a source index list
		LinkedHashMap<String, Integer> sourceIndex = new LinkedHashMap<String, Integer>();
		LinkedHashMap<Integer, String> indexSource = new LinkedHashMap<Integer, String>();
		int si = 0;
		for (String source: dirIndex.keySet()) {
			sourceIndex.put(source, new Integer(si));
			indexSource.put(new Integer(si), source);
			++si;
		}

		// Create the range objects
		List<GEMFRange> ranges = new ArrayList<GEMFRange>();
	
		for (String source: dirIndex.keySet()) {
			for (Integer zoom: dirIndex.get(source).keySet()) {
				
				// Get non-contiguous Y sets for each Z/X
				LinkedHashMap<List<Integer>, List<Integer>> ySets = 
					new LinkedHashMap<List<Integer>, List<Integer>>();
				
				for (Integer x: new TreeSet<Integer>(dirIndex.get(source).get(zoom).keySet())) {
					
					List<Integer> ySet = new ArrayList<Integer>();
					for (Integer y: dirIndex.get(source).get(zoom).get(x).keySet()) {
						ySet.add(y);
					}
				
					if (ySet.size() == 0) continue;
					
					Collections.sort(ySet);
					
					if (! ySets.containsKey(ySet)) {
						ySets.put(ySet, new ArrayList<Integer>());
					}
					
					ySets.get(ySet).add(x);
				}
			
				// For each Y set find contiguous X sets
				LinkedHashMap<List<Integer>, List<Integer>> xSets = 
					new LinkedHashMap<List<Integer>, List<Integer>>();
				
				for (List<Integer> ySet: ySets.keySet()) {
					
					TreeSet<Integer> xList = new TreeSet<Integer>(ySets.get(ySet));
					
					List<Integer> xSet = new ArrayList<Integer>();
					for(int i = xList.first(); i < xList.last() + 1; ++i) {
						if (xList.contains(new Integer(i))) {
							xSet.add(new Integer(i));
						} else {
							if (xSet.size() > 0) {
								xSets.put(ySet, xSet);
								xSet = new ArrayList<Integer>();
							}
						}
					}
					
					if (xSet.size() > 0) {
						xSets.put(ySet, xSet);
					}
				}

				// For each contiguous X set, find contiguous Y sets and create GEMFRange object
				for (List<Integer> xSet: xSets.keySet()) {
					
					TreeSet<Integer> yList = new TreeSet<Integer>(xSet);
					TreeSet<Integer> xList = new TreeSet<Integer>(ySets.get(xSet));
				
					GEMFRange range = new GEMFFile.GEMFRange();
					range.zoom = zoom;
					range.sourceIndex = sourceIndex.get(source);
					range.xMin = xList.first();
					range.xMax = xList.last();
					
					for(int i = yList.first(); i < yList.last() + 1; ++i) {
						if (yList.contains(new Integer(i))) {
							if (range.yMin == null) range.yMin = i;
							range.yMax = i;
						} else {
							
							if (range.yMin != null) {
								ranges.add(range);
							
								range = new GEMFFile.GEMFRange();
								range.zoom = zoom;
								range.sourceIndex = sourceIndex.get(source);
								range.xMin = xList.first();
								range.xMax = xList.last();
							}
						}
					}

					if (range.yMin != null) {
						ranges.add(range);
					}
				}
			}
		}
		
		
		// Calculate size of header for computation of data offsets
		int source_list_size = 0;
		for (String source: sourceIndex.keySet())
			source_list_size += (U32_SIZE + U32_SIZE + source.length());

		long offset = 
			U32_SIZE + // GEMF Version
			U32_SIZE + // Tile size
			U32_SIZE + // Number of sources
			source_list_size + 
			ranges.size() * ((U32_SIZE * 6) + U64_SIZE) +
			U32_SIZE; // Number of ranges
		
		// Calculate offset for each range in the data set
		for (GEMFRange range: ranges) {
			range.offset = offset;
			
			for (int x = range.xMin; x < range.xMax + 1; ++x) {
				for (int y = range.yMin; y < range.yMax + 1; ++y) {
					offset += (U32_SIZE + U64_SIZE);
				}
			}
		}
		
		long headerSize = offset;

		RandomAccessFile gemfFile = new RandomAccessFile(pLocation, "rw");

		// Write version header
		gemfFile.writeInt(VERSION);
		
		// Write file size header
		gemfFile.writeInt(TILE_SIZE);
		
		// Write number of sources
		gemfFile.writeInt(sourceIndex.size());
		
		// Write source list
		for (String source: sourceIndex.keySet()) {
			gemfFile.writeInt(sourceIndex.get(source));
			gemfFile.writeInt(source.length());
			gemfFile.write(source.getBytes());
		}
		
		// Write number of ranges
		gemfFile.writeInt(ranges.size());
		
		// Write range objects
		for (GEMFRange range: ranges) {
			gemfFile.writeInt(range.zoom);
			gemfFile.writeInt(range.xMin);
			gemfFile.writeInt(range.xMax);
			gemfFile.writeInt(range.yMin);
			gemfFile.writeInt(range.yMax);
			gemfFile.writeInt(range.sourceIndex);
			gemfFile.writeLong(range.offset);
		}

		// Write file offset list
		for (GEMFRange range: ranges) {
			for (int x = range.xMin; x < range.xMax + 1; ++x) {
				for (int y = range.yMin; y < range.yMax + 1; ++y) {
					gemfFile.writeLong(offset);
					long fileSize = dirIndex.get(
							indexSource.get(
									range.sourceIndex)).get(range.zoom).get(x).get(y).length();
					gemfFile.writeInt((int)fileSize);
					offset += fileSize;
				}
			}
		}
		
		//
		// Write tiles
		//
		
		byte[] buf = new byte[FILE_COPY_BUFFER_SIZE];
		
		long currentOffset = headerSize;
		int fileIndex = 0;
		
		for (GEMFRange range: ranges) {
			for (int x = range.xMin; x < range.xMax + 1; ++x) {
				for (int y = range.yMin; y < range.yMax + 1; ++y) {

					long fileSize = dirIndex.get(
							indexSource.get(range.sourceIndex)).get(range.zoom).get(x).get(y).length();
					
					if (currentOffset + fileSize > FILE_SIZE_LIMIT) {
						gemfFile.close();
						++fileIndex;
						gemfFile = new RandomAccessFile(pLocation + "-" + fileIndex, "rw");
						currentOffset = 0;
					} else {
						currentOffset += fileSize;
					}
					
					FileInputStream tile = new FileInputStream(
							dirIndex.get(
									indexSource.get(
											range.sourceIndex)).get(range.zoom).get(x).get(y));

					int read = tile.read(buf, 0, FILE_COPY_BUFFER_SIZE);
					while (read != -1) {
						gemfFile.write(buf, 0, read);
						read = tile.read(buf, 0, FILE_COPY_BUFFER_SIZE);
					}

					tile.close();
				}
			}
		}

		gemfFile.close();
		
		// Complete construction of GEMFFile object
		openFiles();
		readHeader();
	}
	
	
	// ===========================================================
	// Private Methods
	// ===========================================================
	
	
	/*
	 * Close open GEMF file handles.
	 */
	public void close() throws IOException {
		for (RandomAccessFile file: mFiles) {
			file.close();
		}
	}

	
	/*
	 * Find all files composing this GEMF archive, open them as RandomAccessFile
	 * and add to the mFiles list.
	 */
	private void openFiles() throws FileNotFoundException {
		// Populate the mFiles array
		
		File base = new File(mLocation);
		mFiles.add(new RandomAccessFile(base, "r"));
		mFileNames.add(base.getPath());
		
		int i = 0;
		for(;;) {
			i = i + 1;
			File nextFile = new File(mLocation + "-" + i);
			if (nextFile.exists()) {
				mFiles.add(new RandomAccessFile(nextFile, "r"));
				mFileNames.add(nextFile.getPath());
			} else {
				break;
			}
		}
	}
	
	
	/*
	 * Read header of archive, cache Ranges.
	 */
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
		
		// Read Source List
		int sourceCount = baseFile.readInt();

		for (int i=0;i<sourceCount;i++) {
			int sourceIndex = baseFile.readInt();
			int sourceNameLength = baseFile.readInt();
			byte[] nameData = new byte[sourceNameLength];
			baseFile.read(nameData, 0, sourceNameLength);
			
			String sourceName = new String(nameData);
			mSources.put(new Integer(sourceIndex), sourceName);
		}

		// Read Ranges
		int num_ranges = baseFile.readInt();
		for (int i=0;i<num_ranges;i++) {
			GEMFRange rs = new GEMFRange();
			rs.zoom = baseFile.readInt();
			rs.xMin = baseFile.readInt();
			rs.xMax = baseFile.readInt();
			rs.yMin = baseFile.readInt();
			rs.yMax = baseFile.readInt();
			rs.sourceIndex = baseFile.readInt();
			rs.offset = baseFile.readLong();
			mRangeData.add(rs);
		}
	}

	
	// ===========================================================
	// Public Methods
	// ===========================================================

	
	/*
	 * Returns the base name of the first file in the GEMF archive.
	 */
	public String getName() {
		return mLocation;
	}
	
	/*
	 * Returns a LinkedHashMap of the sources in this archive, as names and indexes.
	 */
	public LinkedHashMap<Integer, String> getSources() {
		return mSources;
	}
	
	/*
	 * Set single source for getInputStream() to use. Otherwise, first tile found
	 * with specified Z/X/Y coordinates will be returned.
	 */
	public void selectSource(int pSource) {
		if (mSources.containsKey(new Integer(pSource))) {
			mSourceLimited = true;
			mCurrentSource = pSource;
		}
	}

	/*
	 * Allow getInputStream() to use any source in the archive.
	 */
	public void acceptAnySource() {
		mSourceLimited = false;
	}
	
	/*
	 * Return list of zoom levels contained within this archive.
	 */
	public Set<Integer> getZoomLevels() {
		Set<Integer> zoomLevels = new TreeSet<Integer>();

		for (GEMFRange rs: mRangeData) {
			zoomLevels.add(rs.zoom);
		}

		return zoomLevels;
	}

	/*
	 * Get an InputStream for the tile data specified by the Z/X/Y coordinates.
	 * 
	 * @return InputStream of tile data, or null if not found.
	 */
	public InputStream getInputStream(int pX, int pY, int pZ) {
		GEMFRange range = null;

		for (GEMFRange rs: mRangeData)
		{
			if ((pZ == rs.zoom)
					&& (pX >= rs.xMin)
					&& (pX <= rs.xMax)
					&& (pY >= rs.yMin)
					&& (pY <= rs.yMax)
					&& (( ! mSourceLimited) || (rs.sourceIndex == mCurrentSource))) {
				range = rs;
				break;
			}
		}

		if (range == null)	{
			return null;
		}

		long dataOffset;
		int dataLength;

		try	{

			// Determine offset to requested tile record in the header
			int numY = range.yMax + 1 - range.yMin;
			int xIndex = pX - range.xMin;
			int yIndex = pY - range.yMin;
			long offset = (xIndex * numY) + yIndex;
			offset *= (U32_SIZE + U64_SIZE);
			offset += range.offset;
			

			// Read tile record from header, get offset and size of data record
			RandomAccessFile baseFile = mFiles.get(0);
			baseFile.seek(offset);
			dataOffset = baseFile.readLong();
			dataLength = baseFile.readInt();

			// Seek to correct data file and offset.
			RandomAccessFile pDataFile = mFiles.get(0);
			int index = 0;
			if (dataOffset > mFileSizes.get(0))	{
				int fileListCount = mFileSizes.size();

				while ((index < (fileListCount - 1)) && 
						(dataOffset > mFileSizes.get(index))) {
					
					dataOffset -= mFileSizes.get(index);
					index += 1;
				}

				pDataFile = mFiles.get(index);
			}

			// Read data block into a byte array
			pDataFile.seek(dataOffset);

			return new GEMFInputStream(mFileNames.get(index), dataOffset, dataLength);

		} catch (java.io.IOException e) {
			return null;
		}
	}
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	// Class to represent a range of stored tiles within the archive.
	private class GEMFRange	{
		Integer zoom;
		Integer xMin;
		Integer xMax;
		Integer yMin;
		Integer yMax;
		Integer sourceIndex;
		Long offset;
		
		public String toString() {
			return String.format(
					"GEMF Range: source=%d, zoom=%d, x=%d-%d, y=%d-%d, offset=0x%08X",
					sourceIndex, zoom, xMin, xMax, yMin, yMax, offset);
		}
	};
	
	// InputStream class to hand to the tile loader system. It wants an InputStream, and it is more
	// efficient to create a new open file handle pointed to the right place, than to buffer the file
	// in memory.
	class GEMFInputStream extends InputStream {

		RandomAccessFile raf;
		int remainingBytes;
		
		GEMFInputStream(String filePath, long offset, int length) throws IOException {
			this.raf = new RandomAccessFile(filePath, "r");
			raf.seek(offset);

			this.remainingBytes = length;
		}
		
		@Override
		public int available() {
			return remainingBytes;
		}
		
		@Override
		public void close() throws IOException {
			raf.close();
		}
		
		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException {
			int read = raf.read(buffer, offset, length > remainingBytes ? remainingBytes : length);
			
			remainingBytes -= read;
			return read;			
		}
		
		@Override
		public int read() throws IOException {			
			if (remainingBytes > 0) {
				remainingBytes--;
				return raf.read();
			} else {
				throw new IOException("End of stream");
			}
		}
		
		@Override
		public long skip(long byteCount) {
			return 0;
		}
	}
}
