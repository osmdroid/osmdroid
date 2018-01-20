package org.osmdroid.samplefragments.milstd2525;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.UnitDef;

/**
 * created on 1/16/2018.
 *
 * @author Alex O'Ree
 */

public class SimpleSymbol {

    private int minPoints = 1;
    private int maxPoints = 1;
    private String basicSymbolId = "";
    private String description = "";
    private String hierarchy = "";
    private boolean canDraw=true;

    public String getSymbolCode() {
        return symbolCode;
    }

    public void setSymbolCode(String symbolCode) {
        this.symbolCode = symbolCode;
    }

    private String path = "";
    private String symbolCode="";

    public static SimpleSymbol createFrom(UnitDef def) {
        SimpleSymbol s= new SimpleSymbol();
        s.setBasicSymbolId(def.getBasicSymbolId());
        s.setDescription(def.getDescription());
        s.setHierarchy(def.getHierarchy());
        s.setPath(def.getFullPath());
        s.canDraw=def.getDrawCategory()==UnitDef.DRAW_CATEGORY_POINT;
        return s;
    }

    public static SimpleSymbol createFrom(SymbolDef def) {
        SimpleSymbol s= new SimpleSymbol();
        s.setBasicSymbolId(def.getBasicSymbolId());
        s.setDescription(def.getDescription());
        s.setHierarchy(def.getHierarchy());
        s.setPath(def.getFullPath());
        s.setMaxPoints(def.getMaxPoints());
        s.setMinPoints(def.getMinPoints());
        s.canDraw = def.getDrawCategory()!=SymbolDef.DRAW_CATEGORY_DONOTDRAW;
        return s;
    }


    public int getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(int minPoints) {
        this.minPoints = minPoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public String getBasicSymbolId() {
        return basicSymbolId;
    }

    public void setBasicSymbolId(String basicSymbolId) {
        this.basicSymbolId = basicSymbolId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean canDraw() {
        return canDraw;
    }
}
