package org.osmdroid.mtp;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Created by alex on 9/13/16.
 */

public class TilePackagerTest {


    @Test
    public void runBasicTest()throws Exception {
        runTest("fr_mapnick_12.zip");
    }


    @Test
    public void runBasicTestSql()throws Exception {
        runTest("fr_mapnick_12.sql");
    }


    private void runTest(String outputFile) throws Exception{
        OSMMapTilePackager.main(new String[]{

                "-force",
                "-u", "https://tiles.openstreetmap.org/%d/%d/%d.png",
                "-t", "Mapnik",
                "-d", outputFile,
                "-zmax", "2",
                "-n", "51.091099",
                "-s", "41.366379",
                "-e", "9.543055",
                "-w", "-4.790556"
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(new File(outputFile).exists());
        Assert.assertTrue(new File(outputFile).length() > 0);

        new File(outputFile).delete();

    }
}
