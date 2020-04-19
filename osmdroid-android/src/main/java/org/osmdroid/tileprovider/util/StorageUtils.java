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
        String displayName;

        public StorageInfo(String path, boolean internal, boolean readonly, int display_number) {
            this.path = path;
            this.internal = internal;

            this.display_number = display_number;
            if (Build.VERSION.SDK_INT >= 9) {
                this.freeSpace = new File(path).getFreeSpace();
            }

            if (!readonly) {
                //confirm it's writable
                this.readonly = !isWritable(new File(path));
            }

            StringBuilder res = new StringBuilder();
            if (internal) {
                res.append("Internal SD card");
            } else if (display_number > 1) {
                res.append("SD card ").append(display_number);
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
     * Returns a {@link List} of {@link StorageInfo} of all storage paths, writable or not.
     *
     * @return
     */
    public static List<StorageInfo> getStorageList() {
        List<StorageInfo> storageInfos = new ArrayList<>();

        StorageInfo primarySharedStorageInfo = getPrimarySharedStorage();

        if (primarySharedStorageInfo != null) {
            storageInfos.add(primarySharedStorageInfo);
        }

        storageInfos.addAll(tryToFindOtherVoIdManagedStorages(primarySharedStorageInfo != null ? primarySharedStorageInfo.path : ""));

        Set<File> allStorageLocationsRevised = getAllStorageLocationsRevised();
        for (File next : allStorageLocationsRevised) {
            boolean found = false;
            for (int i = 0; i < storageInfos.size(); i++) {
                if (storageInfos.get(i).path.equals(next.getAbsolutePath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                storageInfos.add(new StorageInfo(next.getAbsolutePath(), false, false, -1));
            }
        }

        return storageInfos;
    }

    /**
     * gets the best possible storage location by freespace
     *
     * @deprecated As of 6.1.7, use {@link #getBestStorage()} instead.
     */
    @Deprecated
    public static File getStorage() {
        return getBestStorage();
    }

    /**
     * gets the best possible storage location by freespace
     *
     * @return
     */
    public static File getBestStorage() {
       return getBestStorage(null);
    }

    /**
     * gets the best possible storage location by freespace
     *
     * @deprecated As of 6.1.7, use {@link #getBestStorage(Context)} instead.
     */
    @Deprecated
    public static File getStorage(final Context context) {
        return getBestStorage(context);
    }

    /**
     * gets the best possible storage location by freespace
     *
     * @return
     */
    public static File getBestStorage(final Context context) {
        StorageInfo bestStorage = null;
        List<StorageInfo> storageList = getStorageList();
        for (int i = 0; i < storageList.size(); i++) {
            StorageInfo currentStorage = storageList.get(i);
            if (!currentStorage.readonly && isWritable(new File(currentStorage.path))) {
                if (bestStorage != null) {
                    //compare free space
                    if (bestStorage.freeSpace < currentStorage.freeSpace) {
                        bestStorage = currentStorage;
                    }
                } else {
                    bestStorage = currentStorage;
                }
            }
        }
        if (bestStorage != null) {
            return new File(bestStorage.path);
        }
        //http://stackoverflow.com/questions/21230629/getfilesdir-vs-environment-getdatadirectory
        if (context != null) {
            return new File(context.getDatabasePath("temp.sqlite").getAbsolutePath().replace("temp.sqlite", ""));
        } else {
            try {
                return Environment.getExternalStorageDirectory();
            } catch (Exception ex) {
                //trap for android studio layout editor and some for certain devices
                //see https://github.com/osmdroid/osmdroid/issues/508
                return null;
            }
        }
    }

    /**
     * @deprecated As of 6.1.7, use {@link #isPrimarySharedStorageAvailable()} instead.
     * @return True if the external storage is available. False otherwise.
     */
    @Deprecated
    public static boolean isAvailable() {
        return isPrimarySharedStorageAvailable();
    }

    /**
     * @return True if the primary shared storage is available. False otherwise.
     */
    public static boolean isPrimarySharedStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    /**
     * @return True if the primary shared storage is writable. False otherwise.
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * @return True if the path is writable. False otherwise.
     */
    public static boolean isWritable(File path) {
        try {
            File tmp = new File(path.getAbsolutePath() + File.separator + UUID.randomUUID().toString());
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write("hi".getBytes());
            fos.close();
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            Log.i(TAG, path.getAbsolutePath() + " is writable");
            return true;
        } catch (Throwable ex) {
            Log.i(TAG, path.getAbsolutePath() + " is NOT writable");
            return false;
        }
    }

    /**
     * @return A {@link Map} of all storage locations available
     */
    public static Map<String, File> getAllStorageLocations() {
        Map<String, File> map = new HashMap<>(10);

        map.putAll(tryToGetMountedStoragesFromFilesystem());

        //ok now that we've done the dirty linux work, let's pull in the android bits
        if (!map.containsValue(Environment.getExternalStorageDirectory()))
            map.put(SD_CARD, Environment.getExternalStorageDirectory());

        Set<File> fromSystemEnv = tryToGetStorageFromSystemEnv();
        for (File file : fromSystemEnv) {
            if (file.exists() && !map.containsValue(file)) {
                map.put(SD_CARD, file);
            }
        }

        return map;
    }

    /**
     * @return A {@link Set} of all storage locations available
     */
    private static Set<File> getAllStorageLocationsRevised() {
        Set<File> map = new HashSet<>();

        Set<File> fromSystemEnv = tryToGetStorageFromSystemEnv();
        for (File file : fromSystemEnv) {
            if (isWritable(file)) {
                map.add(file);
            }
        }

        if (Environment.getExternalStorageDirectory() != null) {
            File t = Environment.getExternalStorageDirectory();
            if (isWritable(t)) {
                map.add(t);
            }
        }

        Map<String, File> mounts = tryToGetMountedStoragesFromFilesystem();
        for (File file : mounts.values()) {
            if (isWritable(file)) {
                map.add(file);
            }
        }

        return map;
    }

    private static StorageInfo getPrimarySharedStorage() {
        String primarySharedStoragePath = "";
        boolean isPrimarySharedStorageNotRemovable = false;
        String primarySharedStorageState = "";
        boolean isPrimarySharedStorageReadonly = true;
        boolean isPrimarySharedStorageAvailable = false;

        try {
            if (Environment.getExternalStorageDirectory() != null) {
                primarySharedStoragePath = Environment.getExternalStorageDirectory().getPath();
            }
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                isPrimarySharedStorageNotRemovable = !Environment.isExternalStorageRemovable();
            }
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            primarySharedStorageState = Environment.getExternalStorageState();
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            isPrimarySharedStorageAvailable = primarySharedStorageState.equals(Environment.MEDIA_MOUNTED) || primarySharedStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }
        try {
            isPrimarySharedStorageReadonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        } catch (Throwable ex) {
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
            ex.printStackTrace();
        }

        StorageInfo primarySharedStorageInfo = null;
        if (isPrimarySharedStorageAvailable) {
            primarySharedStorageInfo = new StorageInfo(primarySharedStoragePath, isPrimarySharedStorageNotRemovable, isPrimarySharedStorageReadonly, -1);
        }
        return primarySharedStorageInfo;
    }

    private static List<StorageInfo> tryToFindOtherVoIdManagedStorages(String storagePathToIgnore) {
        List<StorageInfo> storageInfos = new ArrayList<>();
        BufferedReader bufferedReader = null;

        try {
            HashSet<String> paths = new HashSet<>();
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            int currentDisplayNumber = 1;
            Log.d(TAG, "/proc/mounts");
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mountPoint = tokens.nextToken(); //mount point
                    if (paths.contains(mountPoint)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    // if mountPoint is the primary shared storage, skip it
                    if (mountPoint.equals(storagePathToIgnore)) {
                        paths.add(storagePathToIgnore);
                    } else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mountPoint);
                            if (new File(mountPoint + File.separator).exists()) {
                                storageInfos.add(new StorageInfo(mountPoint, false, readonly, currentDisplayNumber++));
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return storageInfos;
    }

    private static Map<String, File> tryToGetMountedStoragesFromFilesystem() {
        Map<String, File> map = new HashMap<>();

        List<String> mounts = new ArrayList<>(10);
        List<String> vold = new ArrayList<>(10);
        mounts.add("/mnt/sdcard");
        vold.add("/mnt/sdcard");

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
                        if (!element.equals("/mnt/sdcard")) {
                            mounts.add(element);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception ignored) {
                }
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

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }
                        if (!element.equals("/mnt/sdcard")) {
                            vold.add(element);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception ignored) {
                }
            }
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (!vold.contains(mount)) {
                mounts.remove(i--);
            }
        }
        vold.clear();

        List<String> mountHash = new ArrayList<>(10);
        for (String mount : mounts) {
            File root = new File(mount);
            if (root.exists() && root.isDirectory() && root.canWrite()) {
                File[] list = root.listFiles();
                StringBuilder hash = new StringBuilder("[");
                if (list != null) {
                    for (File f : list) {
                        hash.append(f.getName().hashCode()).append(":").append(f.length()).append(", ");
                    }
                }
                hash.append("]");
                if (!mountHash.contains(hash.toString())) {
                    String key = SD_CARD + "_" + map.size();
                    if (map.size() == 0) {
                        key = SD_CARD;
                    } else if (map.size() == 1) {
                        key = EXTERNAL_SD_CARD;
                    }
                    mountHash.add(hash.toString());
                    map.put(key, root);
                }
            }
        }

        return map;
    }

    private static Set<File> tryToGetStorageFromSystemEnv() {
        Set<File> storages = new HashSet<>();
        String primarySd = System.getenv("EXTERNAL_STORAGE");
        if (primarySd != null) {
            File t = new File(primarySd + File.separator);
            storages.add(t);
        }

        String secondarySd = System.getenv("SECONDARY_STORAGE");
        if (secondarySd != null) {
            String[] split = secondarySd.split(File.pathSeparator);
            for (String s : split) {
                File t = new File(s + File.separator);
                storages.add(t);
            }
        }

        return storages;
    }
}
