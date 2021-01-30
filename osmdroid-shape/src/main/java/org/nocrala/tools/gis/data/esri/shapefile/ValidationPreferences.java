package org.nocrala.tools.gis.data.esri.shapefile;

import org.nocrala.tools.gis.data.esri.shapefile.shape.PartType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

/**
 * This class allows the client to relax the level of validation when reading a
 * shape file. When instantiated, all validations are turned on by default, so
 * the client can specifically turn off the required ones.
 */
public class ValidationPreferences {

    private static final int DEFAULT_MAX_NUMBER_OF_POINTS_PER_SHAPE = 10000;

    private int expectedRecordNumber = 0;

    private ShapeType forceShapeType = null;
    private boolean allowBadRecordNumbers = false;
    private boolean allowBadContentLength = false;
    private boolean allowMultipleShapeTypes = false;
    private boolean allowUnlimitedNumberOfPointsPerShape = false;
    private int maxNumberOfPointsPerShape = DEFAULT_MAX_NUMBER_OF_POINTS_PER_SHAPE;
    private PartType forcePartType = null;

    // Logic

    void advanceOneRecordNumber() {
        this.expectedRecordNumber++;
    }

    public int getExpectedRecordNumber() {
        return expectedRecordNumber;
    }

    // Accessors

    public PartType getForcePartType() {
        return forcePartType;
    }

    /**
     * Forces the part types to a specific type, disabling its validation. Set
     * this value to null to enable the validation. Defaults to null.
     *
     * @param forcePartType
     */
    public void setForcePartType(PartType forcePartType) {
        this.forcePartType = forcePartType;
    }

    public ShapeType getForceShapeType() {
        return forceShapeType;
    }

    /**
     * Forces the shape type to a specific type, disabling its validation. Set
     * this value to null to enable the validation. Defaults to null.
     *
     * @param forceShapeType
     */
    public void setForceShapeType(ShapeType forceShapeType) {
        this.forceShapeType = forceShapeType;
    }

    public boolean isAllowBadRecordNumbers() {
        return allowBadRecordNumbers;
    }

    /**
     * Inhibits the validation of the record numbers; a correct shape file must
     * have sequential record numbers, starting at 1. Defaults to false.
     *
     * @param allowBadRecordNumbers
     */

    public void setAllowBadRecordNumbers(boolean allowBadRecordNumbers) {
        this.allowBadRecordNumbers = allowBadRecordNumbers;
    }

    public boolean isAllowMultipleShapeTypes() {
        return allowMultipleShapeTypes;
    }

    /**
     * Allows shapes of multiple types in the file; in a correct shape file all
     * shapes must be of a single type, specified on the header of the file.
     * Defaults to false.
     *
     * @param allowMultipleShapeTypes
     */
    public void setAllowMultipleShapeTypes(boolean allowMultipleShapeTypes) {
        this.allowMultipleShapeTypes = allowMultipleShapeTypes;
    }

    public boolean isAllowBadContentLength() {
        return allowBadContentLength;
    }

    /**
     * Inhibits the validation of the content length of each shape. Defaults to
     * false.
     *
     * @param allowBadContentLength
     */
    public void setAllowBadContentLength(boolean allowBadContentLength) {
        this.allowBadContentLength = allowBadContentLength;
    }

    public boolean isAllowUnlimitedNumberOfPointsPerShape() {
        return allowUnlimitedNumberOfPointsPerShape;
    }

    /**
     * Allows any (positive) number of points per shape. It's strongly advised to
     * always limit the number of points per shape; otherwise, a corrupt file with
     * a gigantic (garbage) number of points may crash the reader with an
     * OutOfMemory error. Defaults to false, with a default limit of 10000.
     *
     * @param allowUnlimitedNumberOfPointsPerShape
     */

    public void setAllowUnlimitedNumberOfPointsPerShape(
            boolean allowUnlimitedNumberOfPointsPerShape) {
        this.allowUnlimitedNumberOfPointsPerShape = allowUnlimitedNumberOfPointsPerShape;
    }

    public int getMaxNumberOfPointsPerShape() {
        return maxNumberOfPointsPerShape;
    }

    /**
     * Specifies the maximum number of points a shape can have. If a shape is
     * found with a larger number of points a exception is thrown showing the
     * number of points it has. This parameter can be adjusted for different
     * files, or turned off with the method
     * setAllowUnlimitedNumberOfPointsPerShape(). Defaults to 10000.
     *
     * @param maxItems
     */
    public void setMaxNumberOfPointsPerShape(int maxItems) {
        this.maxNumberOfPointsPerShape = maxItems;
    }

}
