// Created by plusminus on 3:43:56 PM - Mar 6, 2009
package org.osmdroid.mtp.util;

import java.io.File;

public class FolderFileCounter {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @return <code>true</code> on success, <code>false</code> otherwise.
	 */
	public static int getTotalRecursiveFileCount(final File pFolder){
		final File[] children = pFolder.listFiles();

		int tmpCnt = 0;
		for(final File c : children){
			if(c.isDirectory()){
				tmpCnt += getTotalRecursiveFileCount(c);
			}else{
				tmpCnt++;
			}
		}
		return tmpCnt;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
