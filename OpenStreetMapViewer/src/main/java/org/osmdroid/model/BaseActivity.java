package org.osmdroid.model;

import android.app.Activity;

/**
 * Created by alex on 10/21/16.
 */

public abstract class BaseActivity extends Activity implements IBaseActivity {
    public abstract String getActivityTitle();
}
