package com.python.ide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.python.ide.databinding.ActivitySettingsBinding;

import java.io.File;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private ActivityResultLauncher<String> pickWhlLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(
                getLayoutInflater()
        );

        setContentView(
                binding.getRoot()
        );

        setSupportActionBar(
                binding.toolbar
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(
                v -> finish()
        );

        loadInstalledLibraries();

        pickWhlLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri == null) {
                                return;
                            }

                            Executors.newSingleThreadExecutor()
                                    .execute(() -> {

                                        try {

                                            WheelInstaller.install(
                                                    SettingsActivity.this,
                                                    uri
                                            );

                                            runOnUiThread(() -> {

                                                loadInstalledLibraries();

                                                Toast.makeText(
                                                        SettingsActivity.this,
                                                        "Library berhasil diinstall",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                            });

                                        } catch (Exception e) {

                                            runOnUiThread(() ->

                                                    Toast.makeText(
                                                            SettingsActivity.this,
                                                            e.toString(),
                                                            Toast.LENGTH_LONG
                                                    ).show()

                                            );

                                        }

                                    });

                        }
                );

        binding.cardEditor.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                SettingsActivity.this,
                                EditorSettingsActivity.class
                        )
                )
        );

        binding.cardWhl.setOnClickListener(v ->
                pickWhlLauncher.launch("*/*")
        );

        if (binding.btnPickWhl != null) {

            binding.btnPickWhl.setOnClickListener(v ->
                    pickWhlLauncher.launch("*/*")
            );

        }
    }

    private void loadInstalledLibraries() {

        File libsDir =
                new File(
                        getFilesDir(),
                        "python_libs"
                );

        if (!libsDir.exists()) {

            binding.txtInstalledWhl.setText(
                    "Belum ada library terinstall"
            );

            return;
        }

        File[] files = libsDir.listFiles();

        if (files == null || files.length == 0) {

            binding.txtInstalledWhl.setText(
                    "Belum ada library terinstall"
            );

            return;
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "Installed Libraries:\n\n"
        );

        for (File file : files) {

            if (file.isDirectory()) {

                String name = file.getName();

                if (name.endsWith(".dist-info") ||
                        name.endsWith(".data") ||
                        name.equals("__pycache__")) {
                    continue;
                }

                builder.append("• ")
                        .append(name)
                        .append("\n");
            }
        }

        binding.txtInstalledWhl.setText(
                builder.toString()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}