package org.osmdroid.debug.browser;

import android.app.Activity;
import android.os.Bundle;
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
 * @see org.osmdroid.debug.CacheAnalyzerActivity
 * @author Alex O'Ree
 * @since 5.6.2
 */

public class CacheBrowserActivity extends Activity {
    SqlTileWriterExt cache = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_browser);

    }

    public void onResume() {
        super.onResume();

        cache = new SqlTileWriterExt();
        CacheAdapter adapter = new CacheAdapter(this, cache);

        ListView lv = findViewById(R.id.cacheListView);
        lv.setAdapter(adapter);

        ((TextView) findViewById(R.id.rows)).setText(cache.getRowCount(null) + "");
        ((TextView) findViewById(R.id.size)).setText(StorageAdapter.readableFileSize(MainActivity.updateStoragePrefreneces(this)));
        ((TextView) findViewById(R.id.date)).setText("Now " + FileDateUtil.getModifiedDate(System.currentTimeMillis()));
    }

    public void onPause() {
        super.onPause();
        cache.onDetach();
        cache = null;
    }
}
