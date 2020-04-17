package org.osmdroid.shape;

public interface GetBubbleText {
    String getSnippet(String metadata, String boundingBox);
    String getTitle(String metadata, String boundingBox);
    String getSubDescription(String metadata, String boundingBox);
}
