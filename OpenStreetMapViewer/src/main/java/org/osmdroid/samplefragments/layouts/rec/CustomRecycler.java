package org.osmdroid.samplefragments.layouts.rec;

/**
 * created on 1/13/2017.
 *
 * @author PalilloKun
 * https://github.com/PalilloKun/SampleMapWithRecyclerView/blob/master/app/src/main/java/com/palitokun/mapwrecyclerview/CustomRecycler/CustomRecycler.java
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

/**
 * Custom Adapter for Recycler data
 *
 * @author PalilloKun
 */

public class CustomRecycler extends RecyclerView.Adapter<CustomRecycler.ViewHolder> {

    public ArrayList<Info> data;
    public Context contextActual;
    public ArrayList<String> list;


    public CustomRecycler(ArrayList<Info> a) {
        data = a;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    /*
     *  Class for map layout
     * */

    public class MapViewHolder extends ViewHolder {

        MapView mapaShow;

        public MapViewHolder(View v) {
            super(v);

            this.mapaShow = v.findViewById(R.id.mapShow);
        }
    }

    /*
     * Class for infodata layout
     * */
    public class InfoDataViewHolder extends ViewHolder {

        TextView TitleInfoTxt;
        TextView ContentInfodata;


        public InfoDataViewHolder(View v) {
            super(v);
            this.TitleInfoTxt = v.findViewById(R.id.TitleInfoTxt);
            this.ContentInfodata = v.findViewById(R.id.ContentInfodata);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        /*
         *   viewType = 1, is a Map
         *   viewType = 2, is a Graphic
         *   viewType = 3, is a InfoData
         *
         *   In this example, only put two layouts: Map and Info
         * */

        if (viewType == 1 || viewType == 8) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recyclerviewcard, viewGroup, false);
            return new MapViewHolder(v);
        } else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recyclercard2, viewGroup, false);
            return new InfoDataViewHolder(v);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        //For Info data
        if (viewHolder.getItemViewType() != 1 && viewHolder.getItemViewType() != 8) {

            Info dat = data.get(position);
            InfoDataViewHolder Indicador = (InfoDataViewHolder) viewHolder;

            Indicador.TitleInfoTxt.setText(dat.getTitle());
            Indicador.ContentInfodata.setText(dat.getContent());
        } else {

            Info dat = data.get(position);
            MapViewHolder Indicador = (MapViewHolder) viewHolder;
            Indicador.mapaShow.setMultiTouchControls(true);
            Indicador.mapaShow.setClickable(false);

            //on osmdroid-android v5.6.5 and older AND API16 or newer, uncomment the following
            //Indicador.mapaShow.setHasTransientState(true);


            Indicador.mapaShow.getController().setZoom(14);
            Indicador.mapaShow.getController().setCenter(new GeoPoint(-25.2961407, -57.6309129));
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        //return mDataSetTypes[position];
        return Integer.valueOf(data.get(position).getTypeLayout());

    }
}