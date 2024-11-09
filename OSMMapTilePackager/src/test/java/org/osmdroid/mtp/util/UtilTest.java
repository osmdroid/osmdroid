package org.osmdroid.mtp.util;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.mtp.adt.OSMTileInfo;

import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void level0Test() {

        OSMTileInfo mapTileFromCoordinates = Util.getMapTileFromCoordinates(90, -180, 1);

        Assert.assertEquals(1, mapTileFromCoordinates.zoom);
        Assert.assertEquals(0, mapTileFromCoordinates.x);
        Assert.assertEquals(0, mapTileFromCoordinates.y);
    }
}