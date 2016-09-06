// this file generates javdoc on package level

/**
 * <ul>
 * <li>An {@link org.osmdroid.views.overlay.Overlay} is something shown on top of a
 * {@link org.osmdroid.views.MapView}. </li>
 * <li>The {@link org.osmdroid.views.overlay.OverlayWithIW} is an {@link org.osmdroid.views.overlay.Overlay} that
 *  contain data {@link org.osmdroid.views.overlay.OverlayWithIW#getTitle() title} ,
 * a {@link org.osmdroid.views.overlay.OverlayWithIW#getSnippet() snippet or description},
 * and optionally a {@link org.osmdroid.views.overlay.OverlayWithIW#getSubDescription() "sub-description"} and that
 * can be shown in a popup-{@link org.osmdroid.views.overlay.infowindow.InfoWindow} (a bubble).</li>
 * <li>A {@link org.osmdroid.views.overlay.Polyline} is a list of {@link org.osmdroid.api.IGeoPoint}s, where line segments are
 * drawn between consecutive points that can have a
 * popup-{@link org.osmdroid.views.overlay.infowindow.InfoWindow} (a bubble).</li>
 * <li>{@link org.osmdroid.views.overlay.Polygon} is a line drawing on the earth's surface that
 * can have a popup-{@link org.osmdroid.views.overlay.infowindow.InfoWindow} (a bubble).</li>
 * <li>{@link org.osmdroid.views.overlay.Marker} is an icon placed at a particular point on the
 * map's surface that can have a popup-{@link org.osmdroid.views.overlay.infowindow.InfoWindow} (a bubble).</li>
 * <li>{@link org.osmdroid.views.overlay.infowindow.InfoWindow} is a (pop-up-) View that can
 * be displayed on an {@link org.osmdroid.views.MapView}, associated to a {@link org.osmdroid.api.IGeoPoint}.</li>
 * <li>{@link org.osmdroid.views.overlay.infowindow.BasicInfoWindow} is the default
 * implementation of {@link org.osmdroid.views.overlay.infowindow.InfoWindow} for a
 * {@link org.osmdroid.views.overlay.OverlayWithIW}.</li>
 * <li>{@link org.osmdroid.views.overlay.infowindow.MarkerInfoWindow} is the default
 * implementation of {@link org.osmdroid.views.overlay.infowindow.InfoWindow} for a
 * {@link org.osmdroid.views.overlay.Marker}.</li>
 * <li>{@link org.osmdroid.views.overlay.IconOverlay} is an icon placed at a particular
 * {@link org.osmdroid.api.IGeoPoint} on the {@link org.osmdroid.views.MapView}'s surface.</li>
 * <li>{@link org.osmdroid.views.overlay.ClickableIconOverlay} is a clickable icon item on the
 * {@link org.osmdroid.views.MapView} containing {@link org.osmdroid.api.IGeoPoint},
 * {@link org.osmdroid.views.overlay.ClickableIconOverlay#getID() unique id} and
 * {@link org.osmdroid.views.overlay.ClickableIconOverlay#getData() data}.</li>
 * <li>A {@link org.osmdroid.views.overlay.FolderOverlay} contain other {@link org.osmdroid.views.overlay.Overlay}s.</li>
 * </ul>
 *
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='https://github.com/osmdroid/osmdroid/tree/master/osmdroid-android/src/main/doc/marker-classes.png' />
 *
 */
package org.osmdroid.views.overlay;

/*

# PlantUML sourcecode for the generated uml images

@startuml marker-classes.png
title Marker, Polygon, Icon, ... in a map
MapView *-- Overlay

abstract Overlay <|-- OverlayWithIW
    abstract OverlayWithIW  <|-- Marker
    OverlayWithIW  <|-- Polygon
    OverlayWithIW  <|-- Polyline

Overlay  <|- IconOverlay

IconOverlay  <|-- ClickableIconOverlay
    abstract ClickableIconOverlay

abstract InfoWindow  <|-- MarkerInfoWindow
    OverlayWithIW *-> InfoWindow

    FolderOverlay -|> Overlay
    FolderOverlay *-- Overlay
@enduml

@startuml marker-infowindow-classes.png
title popup-InfoWindow (bubble) to be shown from Marker,...
    MapView *-- OverlayWithIW
    abstract OverlayWithIW  <|-- Marker
    OverlayWithIW  <|-- Polygon
    OverlayWithIW  <|-- Polyline

abstract InfoWindow  <|-- BasicInfoWindow
MapView *--* InfoWindow
BasicInfoWindow  <|-- MarkerInfoWindow

OverlayWithIW *-> InfoWindow : open
@enduml




 */