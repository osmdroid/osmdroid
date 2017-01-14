package org.osmdroid.samplefragments.layouts.rec;

import java.util.ArrayList;

/**
 * created on 1/13/2017.
 *
 * @author PalilloKun
 */

public class ConstructorInfoData {

    public ArrayList<Info> obtainData() {


        ArrayList<Info> data = new ArrayList<Info>();

        data.add(new Info("1","Map","Hello!"));
        data.add(new Info("3","Graphic","Im Graphic!"));
        data.add(new Info("3","Information","Im Info!"));

        return data;

    }
}
