package com.python.ide;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.python.ide.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private CodeEditor editor;

    private Uri currentFileUri = null;

    private final ActivityResultLauncher<Intent> openFileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Uri uri = result.getData().getData();

                            currentFileUri = uri;

                            try {

                                InputStream input =
                                        getContentResolver()
                                                .openInputStream(uri);

                                byte[] data = input.readAllBytes();

                                input.close();

                                editor.setText(
                                        new String(
                                                data,
                                                StandardCharsets.UTF_8
                                        )
                                );

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> saveFileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            currentFileUri =
                                    result.getData().getData();

                            saveFile();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding =
                ActivityMainBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (!Python.isStarted()) {
            Python.start(
                    new AndroidPlatform(this)
            );
        }

        editor = binding.editor;

        applyEditorTheme();

        editor.setTextSize(
                Prefs.getFontSize(this)
        );

        editor.setWordwrap(
                Prefs.getWordWrap(this)
        );

        editor.setText(
                Prefs.getEditorText(this)
        );

        binding.fab.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            TerminalActivity.class
                    );

            intent.putExtra(
                    "code",
                    editor.getText().toString()
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {

        super.onResume();

        editor.setTextSize(
                Prefs.getFontSize(this)
        );

        editor.setWordwrap(
                Prefs.getWordWrap(this)
        );
    }

    @Override
    public void onConfigurationChanged(
            Configuration newConfig
    ) {

        super.onConfigurationChanged(newConfig);

        if (editor != null) {
            applyEditorTheme();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        Prefs.setEditorText(
                this,
                editor.getText().toString()
        );
    }

    private void applyEditorTheme() {

        int nightModeFlags =
                getResources()
                        .getConfiguration()
                        .uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        EditorColorScheme scheme =
                new EditorColorScheme();

        if (nightModeFlags ==
                Configuration.UI_MODE_NIGHT_YES) {

            scheme.setColor(
                    EditorColorScheme.WHOLE_BACKGROUND,
                    0xff202124
            );

            scheme.setColor(
                    EditorColorScheme.TEXT_NORMAL,
                    0xffeeeeee
            );

        } else {

            scheme.setColor(
                    EditorColorScheme.WHOLE_BACKGROUND,
                    0xffffffff
            );

            scheme.setColor(
                    EditorColorScheme.TEXT_NORMAL,
                    0xff000000
            );
        }

        editor.setColorScheme(scheme);
    }

    private void openFile() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType(
                "text/*"
        );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        openFileLauncher.launch(intent);
    }

    private void saveFile() {

        if (currentFileUri == null) {
            return;
        }

        try {

            OutputStream output =
                    getContentResolver()
                            .openOutputStream(
                                    currentFileUri
                            );

            output.write(
                    editor.getText()
                            .toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );

            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAsFile() {

        Intent intent =
                new Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                );

        intent.setType(
                "text/plain"
        );

        intent.putExtra(
                Intent.EXTRA_TITLE,
                "main.py"
        );

        saveFileLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        binding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {

        getMenuInflater()
                .inflate(
                        R.menu.main_menu,
                        menu
                );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            MenuItem item
    ) {

        int id = item.getItemId();

        if (id == R.id.action_open) {

            openFile();
            return true;
        }

        if (id == R.id.action_save) {

            saveFile();
            return true;
        }

        if (id == R.id.action_save_as) {

            saveAsFile();
            return true;
        }

        if (id == R.id.action_settings) {

            startActivity(
                    new Intent(
                            this,
                            SettingsActivity.class
                    )
            );

            return true;
        }

        return super.onOptionsItemSelected(
                item
        );
    }
}