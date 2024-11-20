package org.osmdroid.views.overlay;

import org.junit.Test;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.mocks.TestMarker;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link TypedFolderOverlay}.
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TypedFolderOverlayTest {

    /**
     * Tests adding a marker to {@link TypedFolderOverlay}.
     * Ensures that an object of type {@link TestMarker} is correctly added to the list of overlays.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testTypedOverlayAdd() {
        TypedFolderOverlay<TestMarker> markerFolder = new TypedFolderOverlay<>(TestMarker.class);

        TestMarker marker = new TestMarker(new BoundingBox(50, 50, 40, 40));
        boolean added = markerFolder.add(marker);
        assertTrue(added);  // Verify that the marker was successfully added

        List<TestMarker> items = markerFolder.getItems();
        assertEquals(1, items.size());  // Verify that the list contains exactly one item
        assertNotNull(items.get(0));  // Ensure that the item is not null
    }

    /**
     * Tests adding an overlay of the correct type to {@link TypedFolderOverlay}.
     * Ensures that the correct type is accepted and added successfully.
     *
     * @author Daniil Timofeev (hestonic)
     */
    @Test
    public void testTypedOverlayAddCorrectType() {
        TypedFolderOverlay<TestMarker> markerFolder = new TypedFolderOverlay<>(TestMarker.class);

        TestMarker marker = new TestMarker(new BoundingBox(50, 50, 40, 40));
        boolean added = markerFolder.add(marker);
        assertTrue(added);  // Verify that the marker was successfully added

        List<TestMarker> items = markerFolder.getItems();
        assertEquals(1, items.size());  // Verify that the list contains exactly one item
        assertNotNull(items.get(0));  // Ensure that the item is not null
    }
}