package org.nocrala.tools.gis.data.esri.shapefile.exception;

public class InvalidShapeFileException extends Exception {

    public InvalidShapeFileException() {
        super();
    }

    public InvalidShapeFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidShapeFileException(String message) {
        super(message);
    }

    public InvalidShapeFileException(Throwable cause) {
        super(cause);
    }

}
