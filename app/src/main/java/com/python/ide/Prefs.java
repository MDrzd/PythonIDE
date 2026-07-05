package com.python.ide;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String PREF_NAME = "editor_settings";
    private static final String KEY_FONT_SIZE = "font_size";

    public static int getFontSize(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getInt(KEY_FONT_SIZE, 14);
    }

    public static void setFontSize(Context context, int size) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit().putInt(KEY_FONT_SIZE, size).apply();
    }
    
    private static final String KEY_WORD_WRAP = "word_wrap";

public static boolean getWordWrap(Context context) {
    SharedPreferences prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    return prefs.getBoolean(KEY_WORD_WRAP, true);
}

public static void setWordWrap(Context context, boolean value) {
    SharedPreferences prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    prefs.edit().putBoolean(KEY_WORD_WRAP, value).apply();
}
    
    private static final String KEY_EDITOR_TEXT = "editor_text";

public static void setEditorText(Context context, String text) {
    SharedPreferences prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    prefs.edit().putString(KEY_EDITOR_TEXT, text).apply();
}

public static String getEditorText(Context context) {
    SharedPreferences prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    return prefs.getString(
            KEY_EDITOR_TEXT,
            "print('Hello World')\nprint(10 + 20)"
    );
}

}