/**
 * An {@link org.osmdroid.views.overlay.Overlay} is something shown on top of a
 * {@link org.osmdroid.views.MapView}.
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