package org.osmdroid.tileprovider.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Based on some of the responses from this question
 * http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location/15612964#15612964
 * Returns the first storage mount point with the most space that is writable for use as the default
 * location for the osmdroid cache. If an external mount point is not available, application private
 * storage will be used
 * Created by alex on 10/19/16.
 */

public class StorageUtils {
    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";

    private static final String TAG = "StorageUtils";

    public static class StorageInfo {

        public final String path;
        public final boolean internal;
        public boolean readonly;
        public final int display_number;
        public long freeSpace = 0;
        String displayName = "";

        public StorageInfo(String path, boolean internal, boolean readonly, int display_number) {
            this.path = path;
            this.internal = internal;

            this.display_number = display_number;
            if (Build.VERSION.SDK_INT >= 9) {
                this.freeSpace = new File(path).getFreeSpace();
            }
            if (!readonly) {
                //confirm it's writable
                File f = new File(path + File.separator + UUID.randomUUID().toString());
                try {
                    f.createNewFile();
                    f.delete();
                    this.readonly = false;
                } catch (Throwable e) {
                    this.readonly = true;
                }
            } else {
                this.readonly = readonly;
            }
            StringBuilder res = new StringBuilder();
            if (internal) {
                res.append("Internal SD card");
            } else if (display_number > 1) {
                res.append("SD card " + display_number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            displayName = res.toString();
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String val) {
            displayName = val;
        }
    }


    /**
     * returns all storage paths, writable or not
     *
     * @return
     */
    public static List<StorageInfo> getStorageList() {

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = "";
        boolean def_path_internal = false;
        String def_path_state = "";
        boolean def_path_readonly = true;
        boolean def_path_available = false;
        try {
            if (Environment.getExternalStorageDirectory() != null) {
                def_path = Environment.getExternalStorageDirectory().getPath();
            }
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            def_path_internal = (Build.VERSION.SDK_INT >= 9) && !Environment.isExternalStorageRemovable();
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            def_path_state = Environment.getExternalStorageState();
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED) || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }

        try {
            def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        BufferedReader buf_reader = null;
        try {
            HashSet<String> paths = new HashSet<String>();
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            int cur_display_number = 1;
            Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (mount_point.equals(def_path)) {
                        paths.add(def_path);
                        list.add(0, new StorageInfo(def_path, def_path_internal, readonly, -1));
                    } else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                            && !line.contains("/mnt/asec")
                            && !line.contains("/mnt/obb")
                            && !line.contains("/dev/mapper")
                            && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            // if (isWritable(new File(mount_point+ File.separator)))
                            list.add(new StorageInfo(mount_point, false, readonly, cur_display_number++));
                        }
                    }
                }
            }

            if (!paths.contains(def_path) && def_path_available && def_path.length() > 0) {
                list.add(0, new StorageInfo(def_path, def_path_internal, def_path_readonly, -1));
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                }
            }
        }
        Set<File> allStorageLocationsRevised = getAllStorageLocationsRevised();
        Iterator<File> iterator = allStorageLocationsRevised.iterator();
        while (iterator.hasNext()) {
            File next = iterator.next();
            boolean found = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).path.equals(next.getAbsolutePath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                list.add(new StorageInfo(next.getAbsolutePath(), false, false, -1));
            }
        }


        return list;
    }


    /**
     * gets the best possible storage location by freespace
     *
     * @return
     */
    public static File getStorage() {
        //Map<String, File> allStorageLocations = getAllStorageLocations();
        //if (allStorageLocations.isEmpty()){
        //use the current app's storage
        //  return Environment.getDataDirectory();
        //}

        StorageInfo ptr = null;
        List<StorageInfo> storageList = getStorageList();
        for (int i = 0; i < storageList.size(); i++) {
            StorageInfo storageInfo = storageList.get(i);
            if (!storageInfo.readonly && isWritable(new File(storageInfo.path))) {
                if (ptr != null) {
                    //compare free space
                    if (ptr.freeSpace < storageInfo.freeSpace) {
                        ptr = storageInfo;
                    }
                } else {
                    ptr = storageInfo;
                }
            }
        }
        if (ptr != null) {
            return new File(ptr.path);
        }
        //http://stackoverflow.com/questions/21230629/getfilesdir-vs-environment-getdatadirectory
        try {
            return Environment.getExternalStorageDirectory();
        } catch (Exception ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            return null;
        }
    }

    /**
     * gets the best possible storage location by freespace
     *
     * @return
     */
    public static File getStorage(final Context ctx) {
        //Map<String, File> allStorageLocations = getAllStorageLocations();
        //if (allStorageLocations.isEmpty()){
        //use the current app's storage
        //  return Environment.getDataDirectory();
        //}

        StorageInfo ptr = null;
        List<StorageInfo> storageList = getStorageList();
        for (int i = 0; i < storageList.size(); i++) {
            StorageInfo storageInfo = storageList.get(i);
            if (!storageInfo.readonly && isWritable(new File(storageInfo.path))) {
                if (ptr != null) {
                    //compare free space
                    if (ptr.freeSpace < storageInfo.freeSpace) {
                        ptr = storageInfo;
                    }
                } else {
                    ptr = storageInfo;
                }
            }
        }
        if (ptr != null) {
            return new File(ptr.path);
        }
        //http://stackoverflow.com/questions/21230629/getfilesdir-vs-environment-getdatadirectory
        return new File(ctx.getDatabasePath("temp.sqlite").getAbsolutePath().replace("temp.sqlite", ""));
    }


    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    /**
     * @return True if the external storage is writable. False otherwise.
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }

    public static boolean isWritable(File path) {
        //if (path.exists())
        {
            //if (path.canWrite())
            {
                //try to create a new file, save it, then delete it
                try {

                    File tmp = new File(path.getAbsolutePath() + File.separator + "osm.tmp");
                    FileOutputStream fos = new FileOutputStream(tmp);
                    fos.write("hi".getBytes());
                    fos.close();
                    Log.i(TAG, path.getAbsolutePath() + " is writable");
                    tmp.delete();
                    return true;
                } catch (Throwable ex) {
                    Log.i(TAG, path.getAbsolutePath() + " is NOT writable");
                    return false;
                }
            }
        }
        //return false;

    }

    /**
     * @return A map of all storage locations available
     */
    public static Map<String, File> getAllStorageLocations() {
        Map<String, File> map = new HashMap<String, File>(10);

        List<String> mMounts = new ArrayList<String>(10);
        List<String> mVold = new ArrayList<String>(10);
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");
        Scanner scanner = null;
        try {
            File mountFile = new File("/proc/mounts");
            if (mountFile.exists()) {
                scanner = new Scanner(mountFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        // don't add the default mount path
                        // it's already in the list.
                        if (!element.equals("/mnt/sdcard"))
                            mMounts.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                try {
                    scanner.close();
                } catch (Exception ex) {
                }
            scanner = null;
        }

        try {
            File voldFile = new File("/system/etc/vold.fstab");
            if (voldFile.exists()) {
                scanner = new Scanner(voldFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":"))
                            element = element.substring(0, element.indexOf(":"));
                        if (!element.equals("/mnt/sdcard"))
                            mVold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                try {
                    scanner.close();
                } catch (Exception ex) {
                }
            scanner = null;
        }


        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            if (!mVold.contains(mount))
                mMounts.remove(i--);
        }
        mVold.clear();

        List<String> mountHash = new ArrayList<String>(10);

        for (String mount : mMounts) {
            File root = new File(mount);
            if (root.exists() && root.isDirectory() && root.canWrite()) {
                File[] list = root.listFiles();
                String hash = "[";
                if (list != null) {
                    for (File f : list) {
                        hash += f.getName().hashCode() + ":" + f.length() + ", ";
                    }
                }
                hash += "]";
                if (!mountHash.contains(hash)) {
                    String key = SD_CARD + "_" + map.size();
                    if (map.size() == 0) {
                        key = SD_CARD;
                    } else if (map.size() == 1) {
                        key = EXTERNAL_SD_CARD;
                    }
                    mountHash.add(hash);
                    map.put(key, root);
                }
            }
        }

        mMounts.clear();

        if (map.isEmpty()) {
            map.put(SD_CARD, Environment.getExternalStorageDirectory());
        }

        //ok now that we've done the dirty linux work, let's pull in the android bits
        if (!map.containsValue(Environment.getExternalStorageDirectory()))
            map.put(SD_CARD, Environment.getExternalStorageDirectory());


        String primary_sd = System.getenv("EXTERNAL_STORAGE");
        if (primary_sd != null) {
            File t = new File(primary_sd);
            if (t.exists() && !map.containsValue(t))
                map.put(SD_CARD, t);
        }

        String secondary_sd = System.getenv("SECONDARY_STORAGE");
        if (secondary_sd != null) {
            String[] split = secondary_sd.split(File.pathSeparator);
            for (int i = 0; i < split.length; i++) {
                File t = new File(split[i]);
                if (t.exists() && !map.containsValue(t))
                    map.put(SD_CARD, t);
            }
        }


        return map;
    }

    /**
     * @return A map of all storage locations available
     */
    private static Set<File> getAllStorageLocationsRevised() {

        Set<File> map = new HashSet<>();
        String primary_sd = System.getenv("EXTERNAL_STORAGE");
        if (primary_sd != null) {
            File t = new File(primary_sd + File.separator);
            if (isWritable(t)) {
                map.add(t);
            }
        }

        String secondary_sd = System.getenv("SECONDARY_STORAGE");
        if (secondary_sd != null) {
            String[] split = secondary_sd.split(File.pathSeparator);
            for (int i = 0; i < split.length; i++) {
                File t = new File(split[i] + File.separator);
                if (isWritable(t)) {
                    map.add(t);
                }
            }
        }


        if (Environment.getExternalStorageDirectory() != null) {
            File t = Environment.getExternalStorageDirectory();
            if (isWritable(t)) {
                map.add(t);
            }
        }


        List<String> mMounts = new ArrayList<String>(10);
        List<String> mVold = new ArrayList<String>(10);
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");

        Scanner scanner = null;
        try {
            File mountFile = new File("/proc/mounts");
            if (mountFile.exists()) {
                scanner = new Scanner(mountFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        // don't add the default mount path
                        // it's already in the list.
                        if (!element.equals("/mnt/sdcard"))
                            mMounts.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                try {
                    scanner.close();
                } catch (Exception ex) {
                }
            scanner = null;
        }

        try {
            File voldFile = new File("/system/etc/vold.fstab");
            if (voldFile.exists()) {
                scanner = new Scanner(voldFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":"))
                            element = element.substring(0, element.indexOf(":"));
                        if (!element.equals("/mnt/sdcard"))
                            mVold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                try {
                    scanner.close();
                } catch (Exception ex) {
                }
            scanner = null;
        }


        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            if (!mVold.contains(mount))
                mMounts.remove(i--);
        }
        mVold.clear();

        List<String> mountHash = new ArrayList<String>(10);

        for (String mount : mMounts) {
            File root = new File(mount);
            if (root.exists() && root.isDirectory() && root.canWrite()) {
                File[] list = root.listFiles();
                String hash = "[";
                if (list != null) {
                    for (File f : list) {
                        hash += f.getName().hashCode() + ":" + f.length() + ", ";
                    }
                }
                hash += "]";
                if (!mountHash.contains(hash)) {
                    String key = SD_CARD + "_" + map.size();
                    if (map.size() == 0) {
                        key = SD_CARD;
                    } else if (map.size() == 1) {
                        key = EXTERNAL_SD_CARD;
                    }
                    mountHash.add(hash);
                    if (isWritable(root)) {
                        map.add(root);
                    }
                }
            }
        }

        mMounts.clear();


        return map;
    }
}
