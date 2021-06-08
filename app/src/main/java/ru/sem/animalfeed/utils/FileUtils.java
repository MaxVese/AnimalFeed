package ru.sem.animalfeed.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import ru.sem.animalfeed.App;
import ru.sem.animalfeed.R;
import ru.sem.animalfeed.model.Animal;
import ru.sem.animalfeed.model.History;


public class FileUtils {

    private static final String TAG = "FileUtils";

    public static void copyFile(File source, File dest) throws IOException {
        FileInputStream is = new FileInputStream(source);
        if(!dest.exists()) {
            if (!dest.createNewFile()) {
                dest.delete();
                dest.createNewFile();
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    public static String exportCSV(List<History> items, Context context, String fileName)
            throws FileNotFoundException, UnsupportedEncodingException {
        String fn = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                +"/"+fileName;
        Log.d(TAG, "exportCSV: fn="+fn);
        File file = new File(fn);
        //file.mkdirs();
        PrintWriter pw = new PrintWriter(file.getAbsolutePath(), "cp1251");
        for(History item:items) {
            pw.println(item.getInfo()+";"+item.getDate().format(DateUtilsA.formatDT));
        }
        pw.close();


        DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);

        downloadManager.
                addCompletedDownload(file.getName(),
                        context.getString(R.string.db_saved), true,
                        "text/*",//text/plain
                        file.getAbsolutePath(),
                        file.length(),
                        true);
        return file.getAbsolutePath();
    }

    public static String exportAnimalToCSV(List<Animal> items, Context context, String fileName)
            throws FileNotFoundException, UnsupportedEncodingException {
        String fn = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                +"/"+fileName;
        Log.d(TAG, "exportCSV: fn="+fn);
        File file = new File(fn);
        //file.mkdirs();
        PrintWriter pw = new PrintWriter(file.getAbsolutePath(), "cp1251");
        for(Animal item:items) {
            pw.println(item.getId()+";"
                    +item.getName()+";"
                    +item.getKind()+";"
                    +item.getGender()+";"
                    +item.getInterval()+";"
                    +DateUtilsA.getStringDateTime(item.getLastFeed())+";"
                    +DateUtilsA.getStringDateTime(item.getNextFeed())+";"
                    +item.getPhoto()
            );
        }
        pw.close();


        DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);

        downloadManager.
                addCompletedDownload(file.getName(),
                        "БД animals сохранена", true,
                        "text/*",//text/plain
                        file.getAbsolutePath(),
                        file.length(),
                        true);
        return file.getAbsolutePath();
    }
}
