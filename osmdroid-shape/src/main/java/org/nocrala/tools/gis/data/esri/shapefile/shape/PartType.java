package org.nocrala.tools.gis.data.esri.shapefile.shape;

public enum PartType {

    TRIANGLE_STRIP(0), //
    TRIANGLE_FAN(1), //
    OUTER_RING(2), //
    INNER_RING(3), //
    FIRST_RING(4), //
    RING(5); //

    private int id;

    private PartType(int id) {
        this.id = id;
    }

    // parse

    public static PartType parse(final int tid) {
        for (PartType st : PartType.values()) {
            if (st.getId() == tid) {
                return st;
            }
        }
        return null;
    }

    // Getters

    /**
     * Returns the part type's numeric ID, as defined by the ESRI specification.
     *
     * @return
     */
    public int getId() {
        return this.id;
    }

}
