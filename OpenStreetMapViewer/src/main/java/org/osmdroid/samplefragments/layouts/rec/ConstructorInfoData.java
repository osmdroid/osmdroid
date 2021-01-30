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

        data.add(new Info("1", "Map", "Hello!"));
        data.add(new Info("2", "Graphic", "Im Graphic!"));
        data.add(new Info("3", "Information", "Im Info!"));
        data.add(new Info("4", "Graphic", "Im Graphic!"));
        data.add(new Info("5", "Information", "Im Info!"));
        data.add(new Info("6", "Graphic", "Im Graphic!"));
        data.add(new Info("7", "Information", "Im Info!"));
        data.add(new Info("8", "Map", "Hello!"));

        data.add(new Info("9", "Information", "Im Info!"));
        data.add(new Info("10", "Graphic", "Im Graphic!"));
        data.add(new Info("11", "Information", "Im Info!"));
        data.add(new Info("12", "Graphic", "Im Graphic!"));
        data.add(new Info("13", "Information", "Im Info!"));

        return data;

    }
}
