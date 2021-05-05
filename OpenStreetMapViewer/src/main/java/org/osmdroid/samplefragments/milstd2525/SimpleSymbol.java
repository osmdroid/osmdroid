package org.osmdroid.samplefragments.milstd2525;

import android.util.SparseArray;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.UnitDef;

/**
 * This class was created as a work around for
 * <a href="https://github.com/missioncommand/mil-sym-android/issues/82">this</a>.
 * Basically, we want to fill a list adapter with all available symbols. The renderer
 * unfortunately uses different but similar data structures. This class merges the
 * relevant parts of the two symbol defs
 * created on 1/16/2018.
 *
 * @author Alex O'Ree
 * @see SymbolDef
 * @see UnitDef
 * @since 6.0.0
 */

public class SimpleSymbol {

    public enum Echelon2 {
        Null('-'),
        Team_Crew('A'),
        Squad('B'),
        Section('C'),
        Platoon_Detachment('D'),
        Company_Battery_Troop('E'),
        Battalion_Squadron('F'),
        Regiment_Group('G'),
        Bridage('H'),
        Divison('I'),
        Corps('J'),
        Army('K'),
        Army_Group_Front('L'),
        Region('M'),
        Command('N'),
        Wheeled('O'),
        Cross_Country('P'),
        Tracked('Q'),
        Wheeled_and_tracked('R'),
        Towed('S'),
        Rail('T'),
        Over_Snow('U'),
        Sled('V'),
        Pack_Animals('W'),
        Barge('X'),
        Amphibious('Y'),


        ;
        private final char character;

        Echelon2(char c) {
            this.character = c;
        }

        public char getValue() {
            return character;
        }
    }

    public enum Echelon1 {
        Null('-'),
        Headquarters('A'),
        TaskForce_HQ('B'),
        Feint_Dummy_Hq('C'),
        Feint_Dummy_TaskForce_Hq('D'),
        Task_Force('E'),
        Feint_Dummy('F'),
        Feint_Dummy_TaskForce('G'),
        Installation('H'),
        Mobility('M'),
        Towed('N'),

        ;
        private final char character;

        Echelon1(char c) {
            this.character = c;
        }

        public char getValue() {
            return character;
        }
    }

    public enum OrderOfBattle {
        Null('-'),
        Air('A'),
        Electronic('B'),
        Civilian('C'),
        Ground('D'),
        Maritime('N'),
        Strategic_Force('S'),
        Control_Markings('X'),

        ;
        private final char character;

        OrderOfBattle(char c) {
            this.character = c;
        }

        public char getValue() {
            return character;
        }
    }

    private OrderOfBattle orderOfBattle = OrderOfBattle.Null;
    private String countryCode = "--";
    private Echelon2 echelon2 = Echelon2.Null;
    private Echelon1 echelon1 = Echelon1.Null;

    public OrderOfBattle getOrderOfBattle() {
        return orderOfBattle;
    }

    public void setOrderOfBattle(OrderOfBattle orderOfBattle) {
        this.orderOfBattle = orderOfBattle;
    }

    private SparseArray<String> modifiers = new SparseArray<String>();
    private int minPoints = 1;
    private int maxPoints = 1;
    private String basicSymbolId = "";
    private String description = "";
    private String hierarchy = "";
    private boolean canDraw = true;
    private String path = "";
    private String symbolCode = "";

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Echelon2 getEchelon2() {
        return echelon2;
    }

    public void setEchelon2(Echelon2 echelon2) {
        this.echelon2 = echelon2;
    }

    public Echelon1 getEchelon1() {
        return echelon1;
    }

    public void setEchelon1(Echelon1 echelon1) {
        this.echelon1 = echelon1;
    }

    public static SimpleSymbol createFrom(UnitDef def) {
        SimpleSymbol s = new SimpleSymbol();
        s.setBasicSymbolId(def.getBasicSymbolId());
        s.setDescription(def.getDescription());
        s.setHierarchy(def.getHierarchy());
        s.setPath(def.getFullPath());
        s.canDraw = def.getDrawCategory() == UnitDef.DRAW_CATEGORY_POINT;
        return s;
    }

    public static SimpleSymbol createFrom(SymbolDef def) {
        SimpleSymbol s = new SimpleSymbol();
        s.setBasicSymbolId(def.getBasicSymbolId());
        s.setDescription(def.getDescription());
        s.setHierarchy(def.getHierarchy());
        s.setPath(def.getFullPath());
        s.setMaxPoints(def.getMaxPoints());
        s.setMinPoints(def.getMinPoints());
        s.canDraw = def.getDrawCategory() != SymbolDef.DRAW_CATEGORY_DONOTDRAW;
        return s;
    }

    public SparseArray<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(SparseArray<String> modifiers) {
        this.modifiers = modifiers;
    }

    public String getSymbolCode() {
        return symbolCode;
    }

    public void setSymbolCode(String symbolCode) {
        this.symbolCode = symbolCode;
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
