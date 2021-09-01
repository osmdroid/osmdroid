package org.osmdroid.debug.browser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;

import org.osmdroid.MainActivity;
import org.osmdroid.R;
import org.osmdroid.debug.model.SqlTileWriterExt;
import org.osmdroid.debug.util.FileDateUtil;
import org.osmdroid.intro.StorageAdapter;

/**
 * A simple view for browsing the osmdroid tile cache database
 * created on 12/20/2016.
 *
 * @author Alex O'Ree
 * @see org.osmdroid.debug.CacheAnalyzerActivity
 * @since 5.6.2
 */

public class CacheBrowserActivity extends AppCompatActivity {
    SqlTileWriterExt cache = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_browser);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onResume() {
        super.onResume();

        cache = new SqlTileWriterExt();
        CacheAdapter adapter = new CacheAdapter(this, cache);

        ListView lv = findViewById(R.id.cacheListView);
        lv.setAdapter(adapter);

        ((TextView) findViewById(R.id.rows)).setText(cache.getRowCount(null) + "");
        ((TextView) findViewById(R.id.size)).setText(StorageAdapter.readableFileSize(MainActivity.updateStoragePreferences(this)));
        ((TextView) findViewById(R.id.date)).setText("Now " + FileDateUtil.getModifiedDate(System.currentTimeMillis()));
    }

    public void onPause() {
        super.onPause();
        cache.onDetach();
        cache = null;
    }
}
