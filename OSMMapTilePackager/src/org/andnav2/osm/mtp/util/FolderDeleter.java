// Created by plusminus on 2:27:27 AM - Mar 6, 2009
package org.andnav2.osm.mtp.util;

import java.io.File;


public class FolderDeleter {
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
	public static boolean deleteFolder(final File pFolder){
		final File[] children = pFolder.listFiles();
		
		for(File c : children){
			if(c.isDirectory()){
				if(!deleteFolder(c)){
					System.err.println("Could not delete " + c.getAbsolutePath());
					return false;
				}
			}else{
				if(!c.delete()){
					System.err.println("Could not delete " + c.getAbsolutePath());
					return false;
				}
			}
		}
		return pFolder.delete();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
