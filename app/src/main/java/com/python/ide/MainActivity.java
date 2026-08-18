package com.python.ide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.python.ide.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final ArrayList<Project> projectList =
            new ArrayList<>();

    private ProjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding =
                ActivityMainBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
        new ProjectAdapter(
                projectList,
                new ProjectAdapter.OnProjectClickListener() {

                    @Override
                    public void onClick(Project project) {

                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        EditorActivity.class
                                );

                        intent.putExtra(
                                "project_path",
                                project.getPath()
                        );

                        startActivity(intent);
                    }

                    @Override
                    public void onLongClick(Project project) {

                        showProjectMenu(project);
                    }
                }
        );

        binding.recyclerView.setAdapter(
                adapter
        );

        binding.fab.setOnClickListener(
                v -> showNewProjectDialog()
        );

        loadProjects();
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadProjects();
    }

    private void loadProjects() {

        projectList.clear();

        File projectsDir =
                new File(
                        getFilesDir(),
                        "Projects"
                );

        if (!projectsDir.exists()) {
            projectsDir.mkdirs();
        }

        File[] folders =
                projectsDir.listFiles();

        if (folders != null) {

            for (File folder : folders) {

                if (folder.isDirectory()) {

                    projectList.add(
                            new Project(
                                    folder.getName(),
                                    folder.getAbsolutePath()
                            )
                    );
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showNewProjectDialog() {

        TextInputLayout layout =
                new TextInputLayout(this);

        TextInputEditText editText =
                new TextInputEditText(this);

        editText.setHint(
                "Project name"
        );

        layout.addView(editText);

        int padding =
                (int) (
                        20 *
                        getResources()
                                .getDisplayMetrics()
                                .density
                );

        layout.setPadding(
                padding,
                padding,
                padding,
                0
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "New Project"
                )
                .setView(layout)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Create",
                        (dialog, which) -> {

                            String name =
                                    editText
                                            .getText()
                                            .toString()
                                            .trim();

                            if (name.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Project name cannot be empty",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            createProject(name);
                        }
                )
                .show();
    }

    private void createProject(
            String name
    ) {

        File projectsDir =
                new File(
                        getFilesDir(),
                        "Projects"
                );

        if (!projectsDir.exists()) {
            projectsDir.mkdirs();
        }

        File projectDir =
                new File(
                        projectsDir,
                        name
                );

        if (projectDir.exists()) {

            Toast.makeText(
                    this,
                    "Project already exists",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        projectDir.mkdirs();

        try {

            File mainPy =
                    new File(
                            projectDir,
                            "main.py"
                    );

            FileWriter writer =
        new FileWriter(mainPy);

writer.write(
        """
print("Hello, World!")

"""
);

writer.close();
        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Failed to create project",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        loadProjects();

        Intent intent =
                new Intent(
                        this,
                        EditorActivity.class
                );

        intent.putExtra(
        "project_path",
        projectDir.getAbsolutePath()
);

        startActivity(intent);
    }
    
     private void showProjectMenu(
        Project project
) {

    String[] options = {
            "Rename",
            "Clone",
            "Delete"
    };

    new MaterialAlertDialogBuilder(this)
            .setTitle(project.getName())
            .setItems(options, (dialog, which) -> {

                switch (which) {

                    case 0:
                        showRenameDialog(project);
                        break;

                    case 1:
                        cloneProject(project);
                        break;

                    case 2:
                        showDeleteDialog(project);
                        break;
                }

            })
            .show();
}

       private void showRenameDialog(Project project) {

    TextInputLayout layout = new TextInputLayout(this);
    TextInputEditText editText = new TextInputEditText(this);

    editText.setText(project.getName());
    layout.addView(editText);

    new MaterialAlertDialogBuilder(this)
            .setTitle("Rename Project")
            .setView(layout)
            .setPositiveButton("Rename", (dialog, which) -> {

                String newName = editText.getText().toString().trim();

                if (newName.isEmpty()) {
                    return;
                }

                File oldDir = new File(project.getPath());
                File newDir = new File(oldDir.getParent(), newName);

                if (newDir.exists()) {
                    Toast.makeText(this,
                            "Project already exists",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (oldDir.renameTo(newDir)) {
                    loadProjects();
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
}

      private void showDeleteDialog(Project project) {

    new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Project")
            .setMessage("Delete \"" + project.getName() + "\" ?")
            .setPositiveButton("Delete", (dialog, which) -> {

                deleteRecursive(
                        new File(project.getPath())
                );

                loadProjects();

            })
            .setNegativeButton("Cancel", null)
            .show();
}

      private void deleteRecursive(File file) {

    if (file.isDirectory()) {

        File[] files = file.listFiles();

        if (files != null) {

            for (File child : files) {
                deleteRecursive(child);
            }
        }
    }

    file.delete();
}

         private void cloneProject(Project project) {

    File source = new File(project.getPath());

    File parent = source.getParentFile();

    String baseName = project.getName() + "_copy";

    File dest = new File(parent, baseName);

    int index = 2;

    while (dest.exists()) {
        dest = new File(parent, baseName + index);
        index++;
    }

    try {

        copyDirectory(source, dest);

        Toast.makeText(
                this,
                "Project cloned",
                Toast.LENGTH_SHORT
        ).show();

        loadProjects();

    } catch (Exception e) {

        e.printStackTrace();

        Toast.makeText(
                this,
                "Clone failed",
                Toast.LENGTH_SHORT
        ).show();
    }
}

      private void copyDirectory(
        File source,
        File dest
) throws Exception {

    if (source.isDirectory()) {

        if (!dest.exists()) {
            dest.mkdirs();
        }

        File[] files = source.listFiles();

        if (files != null) {

            for (File file : files) {

                copyDirectory(
                        file,
                        new File(dest, file.getName())
                );
            }
        }

    } else {

        java.io.FileInputStream in =
                new java.io.FileInputStream(source);

        java.io.FileOutputStream out =
                new java.io.FileOutputStream(dest);

        byte[] buffer = new byte[4096];

        int length;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }
}

      

    @Override
    protected void onDestroy() {

        super.onDestroy();

        binding = null;
    }

     @Override
public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate(
            R.menu.home_menu,
            menu
    );

    return true;
}
     @Override
public boolean onOptionsItemSelected(
        MenuItem item
) {

    if (item.getItemId() ==
            R.id.action_settings) {

        startActivity(
                new Intent(
                        this,
                        SettingsActivity.class
                )
        );

        return true;
    }

    return super.onOptionsItemSelected(item);
}
            }
