package com.python.ide;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WheelInstaller {

    public static File install(
            Context context,
            Uri uri
    ) throws Exception {

        File libsDir =
                new File(
                        context.getFilesDir(),
                        "python_libs"
                );

        if (!libsDir.exists()) {
            libsDir.mkdirs();
        }

        InputStream input =
                context.getContentResolver()
                        .openInputStream(uri);

        ZipInputStream zip =
                new ZipInputStream(input);

        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {

            File outFile =
                    new File(
                            libsDir,
                            entry.getName()
                    );

            if (entry.isDirectory()) {
                outFile.mkdirs();
            } else {

                File parent =
                        outFile.getParentFile();

                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                FileOutputStream fos =
                        new FileOutputStream(outFile);

                byte[] buffer =
                        new byte[8192];

                int len;

                while ((len = zip.read(buffer)) > 0) {
                    fos.write(
                            buffer,
                            0,
                            len
                    );
                }

                fos.close();
            }

            zip.closeEntry();
        }

        zip.close();

        return libsDir;
    }
}