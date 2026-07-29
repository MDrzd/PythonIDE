package com.python.ide;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.python.ide.databinding.ActivityEditorBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class EditorActivity extends AppCompatActivity {

    private ActivityEditorBinding binding;

    private CodeEditor editor;

    private Uri currentFileUri = null;

    private File projectDir;
    private File mainFile;

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
                ActivityEditorBinding.inflate(
                        getLayoutInflater()
                );
                
                applyKeepScreenOn();

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

        String projectPath =
        getIntent().getStringExtra(
                "project_path"
        );

projectDir =
        new File(projectPath);

mainFile =
        new File(
                projectDir,
                "main.py"
        );

loadProject();

        binding.fab.setOnClickListener(v -> {

            if (Prefs.getAutoSave(this)) {
        saveProject();
    }

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

    applyKeepScreenOn();
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

    if (Prefs.getAutoSave(this)) {
        saveProject();
    }
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
    
     private void applyKeepScreenOn() {

    if (Prefs.getKeepScreenOn(this)) {

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

    } else {

        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
    }
}

    private void loadProject() {

        try {

            if (!mainFile.exists()) {

                mainFile.createNewFile();

                return;
            }

            FileInputStream input =
                    new FileInputStream(mainFile);

            byte[] data =
                    new byte[(int) mainFile.length()];

            input.read(data);

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

    private void saveProject() {

        try {

            FileOutputStream output =
                    new FileOutputStream(mainFile);

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

            saveProject();
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