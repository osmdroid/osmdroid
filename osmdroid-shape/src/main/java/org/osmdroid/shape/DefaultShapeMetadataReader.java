package org.osmdroid.shape;

public class DefaultShapeMetadataReader extends ShapeMetadataReader {
    @Override
    public String getSnippet() {
        return metaString;
    }

    @Override
    public String getTitle() {
        if (metaString.length() > 100) {
            return metaString.substring(0, 96) + "...";
        }
        return metaString;
    }

    @Override
    public String getSubDescription() {
        return boxString;
    }
}
