package org.nocrala.tools.gis.data.esri.shapefile.exception;

public class InvalidShapeFileException extends Exception {

    private static final long serialVersionUID = 9052794347808071370L;

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
