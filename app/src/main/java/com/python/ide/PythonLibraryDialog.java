package com.python.ide;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class PythonLibraryDialog {

    private static Context context;
    private static File rootDir;
    private static File currentDir;

    public static void show(Context ctx) {
        context = ctx;
        rootDir = new File(context.getFilesDir(), "python_libs");

        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        currentDir = rootDir;
        showDirectory();
    }

    private static void showDirectory() {
        File[] files = currentDir.listFiles();

        ArrayList<File> visibleFiles = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        if (files != null) {
            Arrays.sort(
                    files,
                    Comparator.comparing(
                            File::getName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            for (File file : files) {
                visibleFiles.add(file);
                names.add(
                        (file.isDirectory() ? "📁 " : "📄 ") +
                        file.getName()
                );
            }
        }

        if (names.isEmpty()) {
            names.add("Folder kosong");
        }

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(context);

        builder.setTitle(getRelativePath(currentDir));

        ListView listView = new ListView(context);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_list_item_1,
                        names
                );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= visibleFiles.size()) {
                        return;
                    }

                    File selected = visibleFiles.get(position);

                    if (selected.isDirectory()) {
                        currentDir = selected;
                        showDirectory();
                    }
                }
        );

        listView.setOnItemLongClickListener(
                (parent, view, position, id) -> {

                    if (position >= visibleFiles.size()) {
                        return true;
                    }

                    File selected = visibleFiles.get(position);

                    if (selected.isDirectory()) {
                        showLibraryDeleteMenu(selected);
                    }

                    return true;
                }
        );

        builder.setView(listView);

        if (!currentDir.equals(rootDir)) {

            builder.setNegativeButton(
                    "Back",
                    (dialog, which) -> {

                        File parent =
                                currentDir.getParentFile();

                        if (parent != null &&
                                isInsideRoot(parent)) {

                            currentDir = parent;
                            showDirectory();

                        } else {
                            dialog.dismiss();
                        }
                    }
            );

        } else {

            builder.setNegativeButton(
                    "Close",
                    null
            );
        }

        builder.setNeutralButton(
                "Refresh",
                (dialog, which) ->
                        showDirectory()
        );

        builder.show();
    }

    private static void showLibraryDeleteMenu(
            File library
    ) {

        if (library.equals(rootDir)) {
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(library.getName())
                .setItems(
                        new String[]{"Delete"},
                        (dialog, which) -> {

                            if (which == 0) {
                                confirmDeleteLibrary(library);
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private static void confirmDeleteLibrary(
            File library
    ) {

        if (!library.isDirectory() ||
                library.equals(rootDir) ||
                !isInsideRoot(library)) {

            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Library")
                .setMessage(
                        "Hapus library \"" +
                        library.getName() +
                        "\"?\n\n" +
                        "Semua file di dalam library ini " +
                        "juga akan dihapus."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            if (deleteRecursive(library)) {

                                Toast.makeText(
                                        context,
                                        "Library deleted",
                                        Toast.LENGTH_SHORT
                                ).show();

                                showDirectory();

                            } else {

                                Toast.makeText(
                                        context,
                                        "Failed to delete library",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .show();
    }

    private static boolean deleteRecursive(File file) {

        if (file.isDirectory()) {

            File[] files = file.listFiles();

            if (files != null) {

                for (File child : files) {

                    if (!deleteRecursive(child)) {
                        return false;
                    }
                }
            }
        }

        return file.delete();
    }

    private static boolean isInsideRoot(File file) {

        try {

            String root =
                    rootDir.getCanonicalPath();

            String target =
                    file.getCanonicalPath();

            return target.equals(root) ||
                    target.startsWith(
                            root + File.separator
                    );

        } catch (Exception e) {

            return false;
        }
    }

    private static String getRelativePath(File file) {

        try {

            String root =
                    rootDir.getCanonicalPath();

            String current =
                    file.getCanonicalPath();

            if (current.equals(root)) {
                return "Python Libraries";
            }

            if (current.startsWith(
                    root + File.separator
            )) {

                return "Python Libraries/" +
                        current.substring(
                                root.length() + 1
                        );
            }

        } catch (Exception ignored) {
        }

        return "Python Libraries";
    }
}