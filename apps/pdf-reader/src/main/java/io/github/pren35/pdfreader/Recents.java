package io.github.pren35.pdfreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** The list of recently opened PDFs, newest first, stored on the phone only. */
final class Recents {
    static final class Entry {
        final Uri uri;
        final String name;

        Entry(Uri uri, String name) {
            this.uri = uri;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final String PREFS = "recents";
    private static final String KEY = "entries";
    private static final int MAX = 30;

    private Recents() {
    }

    static List<Entry> load(Context context) {
        List<Entry> entries = new ArrayList<>();
        String json = prefs(context).getString(KEY, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                entries.add(new Entry(Uri.parse(o.getString("uri")), o.getString("name")));
            }
        } catch (JSONException e) {
            // A damaged list is simply started again.
        }
        return entries;
    }

    static void add(Context context, Uri uri, String name) {
        List<Entry> entries = load(context);
        entries.removeIf(e -> e.uri.equals(uri));
        entries.add(0, new Entry(uri, name));
        while (entries.size() > MAX) {
            entries.remove(entries.size() - 1);
        }
        save(context, entries);
    }

    static void remove(Context context, Uri uri) {
        List<Entry> entries = load(context);
        entries.removeIf(e -> e.uri.equals(uri));
        save(context, entries);
    }

    private static void save(Context context, List<Entry> entries) {
        JSONArray array = new JSONArray();
        try {
            for (Entry e : entries) {
                array.put(new JSONObject().put("uri", e.uri.toString()).put("name", e.name));
            }
        } catch (JSONException e) {
            return;
        }
        prefs(context).edit().putString(KEY, array.toString()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
