package io.github.pren35.pdfreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.pdf.viewer.fragment.PdfViewerFragment;

/** Shows one PDF: smooth scrolling, pinch zoom, double-tap zoom, search and text selection. */
public class ViewerActivity extends AppCompatActivity {
    private static final String TAG_PDF = "pdf";

    private Uri uri;
    private PdfViewerFragment pdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_viewer);
        setTitle(Files.displayName(this, uri));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pdf = (PdfViewerFragment) getSupportFragmentManager().findFragmentByTag(TAG_PDF);
        if (pdf == null) {
            pdf = new PdfViewerFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.pdf_container, pdf, TAG_PDF)
                    .commitNow();
            pdf.setDocumentUri(uri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            pdf.setTextSearchActive(true);
            return true;
        } else if (id == R.id.action_share) {
            Intent send = new Intent(Intent.ACTION_SEND)
                    .setType("application/pdf")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, getString(R.string.share)));
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
