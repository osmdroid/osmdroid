package org.osmdroid.debug;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.debug.browser.CacheBrowserActivity;
import org.osmdroid.debug.model.SqlTileWriterExt;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.util.Counters;

import java.util.ArrayList;
import java.util.List;

/**
 * A debug utility to show various cache metrics and management
 * <p>
 * requires api11+ due to the use of sqlite
 * <p>
 * created on 12/21/2016.
 *
 * @author Alex O'Ree
 * @since 5.6.2
 */

public class CacheAnalyzerActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, Runnable {
    SqlTileWriterExt cache = null;
    TextView cacheStats;
    AlertDialog show = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_analyzer);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        cacheStats = findViewById(R.id.cacheStats);

        final ArrayList<String> list = new ArrayList<>();
        list.add("Browse the cache");
        list.add("Purge the cache");
        list.add("Purge a specific tile source");
        list.add("See the debug counters");

        ListView lv = findViewById(R.id.statslist);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onResume() {
        super.onResume();
        cache = new SqlTileWriterExt();
        new Thread(this).start();
    }

    public void onPause() {
        super.onPause();
        cache.onDetach();
        cache = null;
        if (show != null)
            show.dismiss();
        show = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                this.startActivity(new Intent(this, CacheBrowserActivity.class));
                break;
            case 1:
                purgeCache();
                break;
            case 2:
                purgeTileSource();
                break;
            case 3:
                showDebugCounters();
                break;
        }
    }

    private void showDebugCounters() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tile Source");
        StringBuilder sb = new StringBuilder();
        sb.append(Counters.class.getCanonicalName() + "\nPerformance and debug counters\n\n");
        sb.append("Out of memory errors: " + Counters.countOOM + "\n");
        sb.append("File cache hit: " + Counters.fileCacheHit + "\n");
        sb.append("File cache miss: " + Counters.fileCacheMiss + "\n");
        sb.append("File cache oom: " + Counters.fileCacheOOM + "\n");
        sb.append("File cache save errors: " + Counters.fileCacheSaveErrors + "\n");
        sb.append("Tile download errors: " + Counters.tileDownloadErrors + "\n");
        builder.setMessage(sb.toString());

        show = builder.show();
    }

    private void purgeTileSource() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tile Source");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        List<SqlTileWriterExt.SourceCount> sources = cache.getSources();
        for (int i = 0; i < sources.size(); i++) {
            arrayAdapter.add(sources.get(i).source);
        }

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String item = arrayAdapter.getItem(which);
                boolean b = cache.purgeCache(item);
                if (b)
                    Toast.makeText(CacheAnalyzerActivity.this, "SQL Cache purged", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(CacheAnalyzerActivity.this, "SQL Cache purge failed, see logcat for details", Toast.LENGTH_LONG).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void purgeCache() {
        SqlTileWriter sqlTileWriter = new SqlTileWriter();
        boolean b = sqlTileWriter.purgeCache();
        sqlTileWriter.onDetach();
        sqlTileWriter = null;
        if (b)
            Toast.makeText(this, "SQL Cache purged", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "SQL Cache purge failed, see logcat for details", Toast.LENGTH_LONG).show();
    }

    @Override
    public void run() {
        if (cache == null)
            return;
        List<SqlTileWriterExt.SourceCount> sources = cache.getSources();
        final StringBuilder sb = new StringBuilder("Source: tile count\n");
        if (sources.isEmpty())
            sb.append("None");
        for (final SqlTileWriterExt.SourceCount sourceCount : sources) {
            sb.append("Source ").append(sourceCount.source);
            sb.append(": count=").append(sourceCount.rowCount);
            sb.append("; minsize=").append(sourceCount.sizeMin);
            sb.append("; maxsize=").append(sourceCount.sizeMax);
            sb.append("; totalsize=").append(sourceCount.sizeTotal);
            sb.append("; avgsize=").append(sourceCount.sizeAvg);
            sb.append("\n");
        }
        long expired = 0;
        if (cache != null)
            expired = cache.getRowCountExpired();
        sb.append("Expired tiles: " + expired);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView tv = findViewById(R.id.cacheStats);

                    if (tv != null) {
                        tv.setText(sb.toString());
                    }
                } catch (Exception ex) {

                }
            }
        });
    }
}
