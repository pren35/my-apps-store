package io.github.pren35.pdfreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Start screen: recently opened PDFs and a button to open another one. */
public class MainActivity extends AppCompatActivity {
    private ListView list;
    private View empty;

    private final ActivityResultLauncher<String[]> picker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                // Keep access after a restart so the file can be reopened from "Recent".
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Recents.add(this, uri, Files.displayName(this, uri));
                } catch (SecurityException e) {
                    // Opens fine now, just won't be listed.
                }
                open(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.recent_list);
        empty = findViewById(R.id.empty);

        findViewById(R.id.open_button).setOnClickListener(
                v -> picker.launch(new String[] {"application/pdf"}));

        list.setOnItemClickListener((parent, view, position, id) -> {
            Recents.Entry entry = (Recents.Entry) parent.getItemAtPosition(position);
            if (Files.canOpen(this, entry.uri)) {
                Recents.add(this, entry.uri, entry.name);
                open(entry.uri);
            } else {
                Recents.remove(this, entry.uri);
                Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_LONG).show();
                refresh();
            }
        });
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            Recents.Entry entry = (Recents.Entry) parent.getItemAtPosition(position);
            Recents.remove(this, entry.uri);
            Toast.makeText(this, R.string.removed_from_recent, Toast.LENGTH_SHORT).show();
            refresh();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Recents.Entry> entries = Recents.load(this);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries));
        list.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
        empty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void open(Uri uri) {
        Intent intent = new Intent(this, ViewerActivity.class)
                .setData(uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
