package ru.sem.animalfeed.db.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class Migration2to3 extends Migration {

    private static final String TAG = "Migration2to3";

    public Migration2to3(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    private class AnimHelper {
        long id, lf, nf;
        String name, photo, kind;
        int gender, interval;
    }

    private static final String sqlAnimalDb =
            "CREATE TABLE \"animals\" (\n" +
                    "\"id\"  INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\"name\"  TEXT,\n" +
                    "\"kind\"  TEXT,\n" +
                    "\"gender\"  INTEGER,\n" +
                    "\"lastFeed\"  INTEGER,\n" +
                    "\"nextFeed\"  INTEGER,\n" +
                    "\"photo\"  TEXT,\n" +
                    "\"interval\"  INTEGER NOT NULL\n" +
                    ");";

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        Cursor cursor = database.query("select * from animals");
        List<AnimHelper> helpers = new ArrayList<>();
        while (cursor.moveToNext()) {
            LocalDateTime dtL = LocalDateTime.parse(cursor.getString(4), formatter);
            LocalDateTime dtN = LocalDateTime.parse(cursor.getString(5), formatter);
            AnimHelper animHelper =new AnimHelper();
            animHelper.id = cursor.getLong(0);
            animHelper.lf = dtL.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            animHelper.nf = dtN.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            animHelper.name = cursor.getString(1);
            animHelper.photo = cursor.getString(6);
            animHelper.kind = cursor.getString(2);
            animHelper.gender = cursor.getInt(3);
            animHelper.interval = cursor.getInt(7);
            helpers.add(animHelper);
        }
        cursor.close();
        database.execSQL("DROP TABLE animals");
        database.execSQL(sqlAnimalDb);

        database.beginTransaction();
        try {
            for (int i = 0; i < helpers.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("lastFeed", helpers.get(i).lf);
                values.put("nextFeed", helpers.get(i).nf);
                values.put("name", helpers.get(i).name);
                values.put("photo", helpers.get(i).photo);
                values.put("kind", helpers.get(i).kind);
                values.put("gender", helpers.get(i).gender);
                values.put("interval", helpers.get(i).interval);
                values.put("id", helpers.get(i).id);
                database.insert("animals", SQLiteDatabase.CONFLICT_REPLACE, values);
            }
            database.setTransactionSuccessful();
            Log.d(TAG, "migrate: success");
        }catch (Exception e){
            Log.d(TAG, "migrate: failure migrate animals data", e);
        }finally {
            database.endTransaction();
        }

        database.execSQL("ALTER TABLE animals ADD COLUMN is_hand INTEGER DEFAULT 0 NOT NULL");
    }
}
