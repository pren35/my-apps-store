package io.github.pren35.pdfreader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

final class Files {
    private Files() {
    }

    /** The file's name as the file manager shows it, or a fallback from the address. */
    static String displayName(Context context, Uri uri) {
        try (Cursor c = context.getContentResolver().query(
                uri, new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst() && !c.isNull(0)) {
                return c.getString(0);
            }
        } catch (RuntimeException e) {
            // Some apps refuse the query; fall back below.
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : "PDF";
    }

    /** True if the file can still be opened (it may have been moved or deleted). */
    static boolean canOpen(Context context, Uri uri) {
        try {
            context.getContentResolver().openFileDescriptor(uri, "r").close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
