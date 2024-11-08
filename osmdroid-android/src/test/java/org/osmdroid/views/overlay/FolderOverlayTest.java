package org.osmdroid.views.overlay;

import org.junit.Test;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.mocks.TestMarker;
import org.osmdroid.views.overlay.mocks.TestOverlayWithIW;
import org.osmdroid.views.overlay.mocks.TestPolygon;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link FolderOverlay}.
 *
 * @author Daniil Timofeev (hestonic)
 */
public class FolderOverlayTest {

    /**
     * Tests adding a marker to {@link FolderOverlay}.
     * Ensures that an object of type {@link TestMarker} is correctly added to the list of overlays.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testAddMarkerToFolderOverlay() {
        FolderOverlay folderOverlay = new FolderOverlay();
        TestMarker marker = new TestMarker(new BoundingBox(50, 50, 40, 40));
        boolean added = folderOverlay.add(marker);
        assertTrue(added);  // Verify that the marker was successfully added
        List<Overlay> items = folderOverlay.getItems();
        assertEquals(1, items.size());  // Verify that the list contains exactly one item
        assertTrue(items.get(0) instanceof TestMarker);  // Verify that the added object is a TestMarker
    }

    /**
     * Tests adding a polygon to {@link FolderOverlay}.
     * Ensures that an object of type {@link TestPolygon} is correctly added to the list of overlays.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testAddPolygonToFolderOverlay() {
        FolderOverlay folderOverlay = new FolderOverlay();
        TestPolygon polygon = new TestPolygon(new BoundingBox(60, 60, 30, 30));
        boolean added = folderOverlay.add(polygon);
        assertTrue(added);  // Verify that the polygon was successfully added
        List<Overlay> items = folderOverlay.getItems();
        assertEquals(1, items.size());  // Verify that the list contains exactly one item
        assertTrue(items.get(0) instanceof TestPolygon);  // Verify that the added object is a TestPolygon
    }

    /**
     * Tests recalculating the bounds for {@link FolderOverlay}.
     * Ensures that the geographical bounds are correctly recalculated based on the bounding boxes
     * of the added overlays.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testRecalculateBoundsForFolderOverlay() {
        FolderOverlay folderOverlay = new FolderOverlay();
        TestMarker marker = new TestMarker(new BoundingBox(50, 50, 40, 40));
        TestPolygon polygon = new TestPolygon(new BoundingBox(60, 60, 30, 30));

        folderOverlay.add(marker);
        folderOverlay.add(polygon);

        BoundingBox calculatedBounds = folderOverlay.getBounds();

        // Verify that the recalculated bounds are correct
        assertEquals(60, calculatedBounds.getLatNorth(), 0);
        assertEquals(60, calculatedBounds.getLonEast(), 0);
        assertEquals(30, calculatedBounds.getLatSouth(), 0);
        assertEquals(30, calculatedBounds.getLonWest(), 0);
    }

    /**
     * Tests closing all information windows in {@link FolderOverlay}.
     * Ensures that all information windows in the contained overlays are properly closed.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testCloseAllInfoWindowsInFolderOverlay() {
        FolderOverlay folderOverlay = new FolderOverlay();
        TestOverlayWithIW overlayWithIW = new TestOverlayWithIW();
        folderOverlay.add(overlayWithIW);
        folderOverlay.closeAllInfoWindows();
        assertTrue(overlayWithIW.isClosed());  // Verify that the info window was closed
    }

    /**
     * Tests removing an overlay from {@link FolderOverlay}.
     * Ensures that an object of type {@link TestMarker} is correctly removed from the list of overlays.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testRemoveOverlayFromFolderOverlay() {
        FolderOverlay folderOverlay = new FolderOverlay();
        TestMarker marker = new TestMarker(new BoundingBox(50, 50, 40, 40));

        folderOverlay.add(marker);
        assertEquals(1, folderOverlay.getItems().size());  // Verify that the list contains exactly one item

        boolean removed = folderOverlay.remove(marker);
        assertTrue(removed);  // Verify that the marker was successfully removed
        assertEquals(0, folderOverlay.getItems().size());  // Verify that the list is now empty
    }
}
