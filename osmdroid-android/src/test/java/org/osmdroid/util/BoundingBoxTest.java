package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class BoundingBoxTest {

    private static final double mDelta = 1E-10;

    @Test
    public void testGetCenterLongitude() {
        Assert.assertEquals(1.5, BoundingBox.getCenterLongitude(1, 2), mDelta);
        Assert.assertEquals(-178.5, BoundingBox.getCenterLongitude(2, 1), mDelta);
    }

    @Test
    public void testOverlaps() {

        //  ________________
        //  |      |       |
        //  |      |       |
        //  |------+-------|
        //  |      |       |
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as &



        //  ________________
        //  |      |       |
        //  |     ***   &  |
        //  |-----*+*------|
        //  |     ***      |
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as &

        BoundingBox box = new BoundingBox(1, 1, -1, -1);
        Assert.assertTrue(box.overlaps(box));

        BoundingBox farAway = new BoundingBox(45, 45, 44, 44);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertFalse(box.overlaps(farAway));

        farAway = new BoundingBox(1.1, 45, 1, 44);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertFalse(box.overlaps(farAway));

        farAway = new BoundingBox(2, 2, -2, -2);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertTrue(box.overlaps(farAway));

        //this is completely within the test box
        farAway = new BoundingBox(0.5, 0.5, -0.5, -0.5);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertTrue(box.overlaps(farAway));
    }

    @Test
    public void testOverlapsWorld() {

        //  ________________
        //  |      |       |
        //  |      |       |
        //  |------+-------|
        //  |      |       |
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as &


        BoundingBox box = new BoundingBox(90, 180, -90, -180);
        Assert.assertTrue(box.overlaps(box));

        BoundingBox farAway = new BoundingBox(45, 44, 44, 45);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertTrue(box.overlaps(farAway));

        farAway = new BoundingBox(1, 44, 1, 45);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertTrue(box.overlaps(farAway));

        farAway = new BoundingBox(2, 2, -2, -2);
        Assert.assertTrue(farAway.overlaps(farAway));
        Assert.assertTrue(box.overlaps(farAway));

        //this is completely within the test box
        farAway = new BoundingBox(0.5, 0.5, -0.5, -0.5);
        Assert.assertTrue(box.overlaps(farAway));
    }

    @Test
    public void testOverlapsDateLine() {


        //  ________________
        //  |      |       |
        //  |**    |     **|
        //  |-*----+-----*-|
        //  |**    |     **|
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as ?
        BoundingBox box = new BoundingBox(45, -178, -45, 178);
        Assert.assertTrue(box.overlaps(box));

        //  ________________
        //  |      |       |
        //  |**    | ??  **|
        //  |-*----+-----*-|
        //  |**    |     **|
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as ?
        BoundingBox farAway = new BoundingBox(45, 44, 44, 45);
        Assert.assertFalse(box.overlaps(farAway));


        //  ________________
        //  |      |       |
        //  |**    |   ? **|
        //  |-*----+-----*-|
        //  |**    |     **|
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as ?
        farAway = new BoundingBox(1, 44, 1, 45);
        Assert.assertFalse(box.overlaps(farAway));


        //  ________________
        //  |      |       |
        //  |**   ?|?    **|
        //  |-*---?+?----*-|
        //  |**   ?|?    **|
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as ?
        farAway = new BoundingBox(2, 2, -2, -2);
        Assert.assertFalse(box.overlaps(farAway));

        farAway = new BoundingBox(0.5, 0.5, -0.5, -0.5);
        Assert.assertFalse(box.overlaps(farAway));

        farAway = new BoundingBox(1, -179, -1, 179);
        Assert.assertTrue(box.overlaps(farAway));
    }


    @Test
    public void testOverlapsDateLine2() {

        //  ________________
        //  |      |       |
        //  |** ?? |     **|
        //  |-*----+-----*-|
        //  |**    |     **|
        //  |      |       |
        //  ----------------
        //box is notated as *
        //test area is notated as ?


        BoundingBox box = new BoundingBox(45, -178, -45, -1);
        Assert.assertTrue(box.overlaps(box));

        BoundingBox farAway = new BoundingBox(45, -74, 44, -72);
        Assert.assertFalse(box.overlaps(farAway));


        //  ________________
        //  |******|     **|
        //  |  ?? *|     * |
        //  |-----*+-----*-|
        //  |     *|     * |
        //  |******|     **|
        //  ----------------
        //box is notated as *
        //test area is notated as ?


        box = new BoundingBox(45, 0, -45, 170);
        Assert.assertTrue(box.overlaps(box));

        farAway = new BoundingBox(40, -74, 38, -72);
        Assert.assertTrue(box.overlaps(farAway));

        farAway = new BoundingBox(40, 5, 38, 4);
        Assert.assertFalse(box.overlaps(farAway));

        farAway = new BoundingBox(-40, 5, -42, 4);
        Assert.assertFalse(box.overlaps(farAway));


    }

    @Test
    public void testOverlap2() {

        //  ________________
        //  |      |       |
        //  |    *****     |
        //  |----*-+-*-----|
        //  |    *****     |
        //  |      |       |
        //  ----------------
        //box is notated as * not too scale
        //test area is notated as ?


        BoundingBox box = new BoundingBox(1, 1, -1, -1);
        Assert.assertTrue(box.overlaps(box));


        //  ________________
        //  |    ?????     |
        //  |    *****     |
        //  |----*-+-*-----|
        //  |    *****     |
        //  |      |       |
        //  ----------------
        //box is notated as * not too scale
        //test area is notated as ?
        //overlap on the norther edge
        BoundingBox item = new BoundingBox(2, 1, 1, -1);
        Assert.assertTrue(box.overlaps(item));
        Assert.assertTrue(item.overlaps(box));


        //  ________________
        //  |              |
        //  |   ?*****     |
        //  |---?*-+-*-----|
        //  |   ?*****     |
        //  |      |       |
        //  ----------------
        //box is notated as * not too scale
        //test area is notated as ?
        //overlap on the western edge of box
        item = new BoundingBox(1, -1, -1, -2);
        Assert.assertTrue(box.overlaps(item));


        //  ________________
        //  |              |
        //  |    *****?    |
        //  |--- *-+-*?----|
        //  |    *****?    |
        //  |      |       |
        //  ----------------
        //box is notated as * not too scale
        //test area is notated as ?

        //overlap on the east edge of box
        item = new BoundingBox(1, 2, -1, 1.0);
        Assert.assertTrue(box.overlaps(item));


        //  ________________
        //  |              |
        //  |    *****     |
        //  |--- *-+-*-----|
        //  |    *****     |
        //  |    ?????     |
        //  ----------------
        //box is notated as * not too scale
        //test area is notated as ?

        //overlap on the southern edge of box
        item = new BoundingBox(-1, 1, -2, -1);
        Assert.assertTrue(box.overlaps(item));


        //  ________________
        //  |              |
        //  |    *****     |
        //  |--- *-+-*-----|
        //  |    *****     |
        //  |      |       |
        //  |    ?????     |
        //  ----------------
        //box is notated as * not too scale
        //test area is notated as ?


        //non overlap on the southern edge of box
        item = new BoundingBox(-2, 1, -4, -1);
        Assert.assertTrue(box.overlaps(item));


    }

    @Test
    public void testSouthernBounds1() {
        //item's southern bounds is just out of view
        BoundingBox view = new BoundingBox(33.29456881383961, -105.6820678709375, 31.99535790385963, -106.67083740234375);
        BoundingBox item = new BoundingBox(31.9277, -106.441352, 31.686508, -106.49126);
        // Assert.assertTrue(view.overlaps(item));
    }

    @Test
    public void testSouthernBoundsSimple() {
        //item's southern bounds is just out of view
        BoundingBox view = new BoundingBox(2, 2, -2, -2);
        BoundingBox item = new BoundingBox(1, 1, 2.1, -1);
        Assert.assertTrue(view.overlaps(item));
    }

    @Test
    public void testNorthernBoundsSimple() {
        //item's southern bounds of itemis just out of view
        BoundingBox view = new BoundingBox(2, 2, -2, -2);

        BoundingBox item = new BoundingBox(2.1, 2, 0, -2);
        Assert.assertTrue(view.overlaps(item));


        item = new BoundingBox(2.1, 2, 1.9, -2);
        Assert.assertTrue(view.overlaps(item));

        item = new BoundingBox(3.1, 2, 1.999999999, -2);
        Assert.assertTrue(view.overlaps(item));

        item = new BoundingBox(3.1, 2, 2.0, -2);
        Assert.assertTrue(view.overlaps(item));

        item = new BoundingBox(3.1, 2, 2.1, -2);
        Assert.assertFalse(view.overlaps(item));
    }


    @Test
    public void testCorpusChristi() {
        BoundingBox item = new BoundingBox(27.696581, -97.243682999999, 27.688781, -97.253063);


        BoundingBox shouldWork = new BoundingBox(27.72243591897344, -97.24737167358398,
            27.63730702015522, -97.30916976928711);
        Assert.assertTrue(shouldWork.overlaps(item));

    }

    @Test
    public void testCorpusChristiViewIsNorth() {
        BoundingBox item = new BoundingBox(27.696581, -97.243682999999, 27.688781, -97.253063);

        BoundingBox viewTop = new BoundingBox(
            27.782999124172314, -97.24748611450195,
            27.697917493482727, -97.30928421020508);
        Assert.assertTrue(viewTop.overlaps(item));
    }


}
