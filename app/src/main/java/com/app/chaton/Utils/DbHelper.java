package com.app.chaton.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;

public class DbHelper extends SQLiteOpenHelper{

    public final static String DATABASE = "dbChatOn";

    public final static String TABLE_AVATARS = "avatars";

    private final static int DB_VERSION = 1;

    private final static String AVATARS_NAME = "name";
    private final static String AVATARS_FILE = "file";

    private SQLiteDatabase db;

    public DbHelper(Context context) {
        super(context, DATABASE, null, DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_AVATARS + "("
                + AVATARS_NAME + " text unique, "
                + AVATARS_FILE + " blob);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    public boolean avatarInCache(String img_name) {
        return (db.query(
                    TABLE_AVATARS,
                    null,
                    AVATARS_NAME + " = ?",
                    new String[] {img_name},
                    null, null, null)
                .getCount() != 0);
    }

    public byte[] getAvatarFromCache(String img_name) {
        Cursor cursor = db.query(TABLE_AVATARS, null, AVATARS_NAME + " = ?", new String[] {img_name}, null, null, null);
        cursor.moveToFirst();
        return cursor.getBlob(cursor.getColumnIndex(AVATARS_FILE));
    }

    public long writeAvatarToCache(String img_name, byte[] img_data) throws IOException{
        ContentValues imgData = new ContentValues();
        imgData.put(AVATARS_NAME, img_name);
        imgData.put(AVATARS_FILE, img_data);

        return db.insert(TABLE_AVATARS, null, imgData);
    }
}
