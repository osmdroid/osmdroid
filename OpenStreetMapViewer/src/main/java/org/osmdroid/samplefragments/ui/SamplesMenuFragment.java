package org.osmdroid.samplefragments.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.osmdroid.ExtraSamplesActivity;
import org.osmdroid.ISampleFactory;
import org.osmdroid.R;
import org.osmdroid.model.IBaseActivity;
import org.osmdroid.samplefragments.BaseSampleFragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 * <p>
 * created on 1/1/2017.
 *
 * @author Alex O'Ree
 */

public class SamplesMenuFragment extends Fragment {

    public final static String TAG = "osmfragsample";

    private Bundle savedState = null;
    private ISampleFactory sampleFactory = null;
    private List<IBaseActivity> additionActivitybasedSamples;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    Map<String, Object> titleSampleMap = new HashMap<>();

    public static SamplesMenuFragment newInstance(ISampleFactory fac, List<IBaseActivity> additionActivitybasedSamples) {
        SamplesMenuFragment x = new SamplesMenuFragment();
        x.sampleFactory = fac;
        x.additionActivitybasedSamples = additionActivitybasedSamples;
        return x;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_menu_layout, container, false);

        //http://stackoverflow.com/a/15314508/1203182


        /* (...) */

        /* If the Fragment was destroyed inbetween (screen rotation), we need to recover the savedState first */
        /* However, if it was not, it stays in the instance from the last onDestroyView() and we don't want to overwrite it */
        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(TAG);
        }
        if (savedState != null) {
            if (sampleFactory != null) {
                //do nothing
            } else {
                String factory = savedState.getString("factory");
                ArrayList<String> acts = null;
                if (savedState.containsKey("acts"))
                    acts = savedState.getStringArrayList("acts");
                try {
                    Class<?> aClass = Class.forName(factory);
                    Method method = aClass.getMethod("getInstance");
                    sampleFactory = (ISampleFactory) method.invoke(null);
                    if (acts == null) {
                        additionActivitybasedSamples = Collections.EMPTY_LIST;
                    } else {
                        //restore the list
                        additionActivitybasedSamples = new ArrayList<>();
                        for (int i = 0; i < acts.size(); i++) {
                            additionActivitybasedSamples.add((IBaseActivity) Class.forName(acts.get(i)).newInstance());
                        }
                    }
                } catch (Throwable t) {
                    //can resume for some reason
                    t.printStackTrace();
                    getActivity().finish();
                }
            }

        }
        savedState = null;


        // get the listview
        expListView = root.findViewById(R.id.lvExp);


        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                String title = listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);
                Object o = titleSampleMap.get(title);
                if (o != null && o instanceof BaseSampleFragment) {
                    // Replace Fragment with selected sample
                    BaseSampleFragment frag = (BaseSampleFragment) o;
                    Log.i(TAG, "loading fragment " + frag.getSampleTitle() + ", " + frag.getClass().getCanonicalName());
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().replace(org.osmdroid.R.id.samples_container, frag, ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG)
                            .addToBackStack(null).commit();
                } else if (o != null && o instanceof IBaseActivity && o instanceof Activity) {
                    IBaseActivity activity = (IBaseActivity) o;
                    Intent i = new Intent(getContext(), activity.getClass());
                    Log.i(TAG, "loading activity " + activity.getActivityTitle() + ", " + activity.getClass().getCanonicalName());
                    getActivity().startActivity(i);
                } else if (o == null) {
                    //NOOP
                } else {
                    Toast.makeText(getActivity(), "Example is of an unexpected type, please report this", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        return root;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //the following block of code took me an entire weekend to track down the root cause.
        //if the code block is in onCreate, it will leak. onActivityCreated = no leak.
        //makes no sense, but that's Android for you.

        // preparing list data
        boolean success = prepareListData();
        if (!success) {
            Activity act = getActivity();
            act.finish();
            return;
        }

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /*
     * Preparing the list data
     */
    private boolean prepareListData() {
        Set<String> headers = new HashSet<>();
        listDataHeader = new ArrayList<String>();

        //category, content
        listDataChild = new HashMap<String, List<String>>();
        if (sampleFactory == null || additionActivitybasedSamples == null) {
            //getActivity().getSupportFragmentManager().popBackStack();
            return false;
        }
        //had this throw an NPE once after device rotation and a back button press.
        for (int a = 0; a < sampleFactory.count(); a++) {
            final BaseSampleFragment f = sampleFactory.getSample(a);
            titleSampleMap.put(f.getSampleTitle(), f);
            String clz = f.getClass().getCanonicalName();
            String[] bits = clz.split("\\.");
            String group = bits[bits.length - 2];
            group = capitialize(group);

            headers.add(group);

            if (!listDataChild.containsKey(group)) {
                listDataChild.put(group, new ArrayList<String>());
            }
            listDataChild.get(group).add(f.getSampleTitle());
        }


        if (!additionActivitybasedSamples.isEmpty()) {
            listDataHeader.add("Activities");
            listDataChild.put("Activities", new ArrayList<String>());
            for (int a = 0; a < additionActivitybasedSamples.size(); a++) {
                listDataChild.get("Activities").add(additionActivitybasedSamples.get(a).getActivityTitle());
                titleSampleMap.put(additionActivitybasedSamples.get(a).getActivityTitle(), additionActivitybasedSamples.get(a));
            }
        }


        listDataHeader.addAll(headers);

        return true;
    }

    private String capitialize(String group) {
        if (group.charAt(0) >= 'a' && group.charAt(0) <= 'z') {
            String first = group.substring(0, 1).toUpperCase();
            group = first + group.substring(1);
        }
        return group;
    }


    @Override
    public void onResume() {
        super.onResume();

        //FragmentManager fm = getFragmentManager();
        //fm.popBackStack();
        //System.gc();
    }

    @Override
    public void onDestroyView() {
        expListView = null;
        savedState = saveState();
        super.onDestroyView();
    }

    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Bundle state = new Bundle();
        if (sampleFactory != null)    //yup, hate android
            state.putString("factory", sampleFactory.getClass().getCanonicalName());
        if (additionActivitybasedSamples != null) {
            ArrayList<String> actClasses = new ArrayList<>();
            for (int i = 0; i < additionActivitybasedSamples.size(); i++)
                actClasses.add(additionActivitybasedSamples.get(i).getClass().getCanonicalName());
            state.putStringArrayList("acts", actClasses);
        }
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /* If onDestroyView() is called first, we can use the previously savedState but we can't call saveState() anymore */
        /* If onSaveInstanceState() is called first, we don't have savedState, so we need to call saveState() */
        /* => (?:) operator inevitable! */
        outState.putBundle(TAG, (savedState != null) ? savedState : saveState());
    }
}
