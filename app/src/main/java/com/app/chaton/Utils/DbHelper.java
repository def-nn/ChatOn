package com.app.chaton.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.User;
import com.app.chaton.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper{

    public final static String DATABASE = "dbChatOn";

    public final static String TABLE_AVATARS = "avatars";
    public final static String TABLE_MESSAGES = "messages";

    private final static int DB_VERSION = 1;

    private final static String AVATARS_NAME = "name";
    private final static String AVATARS_FILE = "file";

    private SQLiteDatabase db;
    private Long userId;

    public DbHelper(Context context) {
        super(context, DATABASE, null, DB_VERSION);
        db = getWritableDatabase();
        userId = new PreferenceHelper(context.getSharedPreferences(
                context.getResources().getString(R.string.PREFERENCE_FILE), Context.MODE_PRIVATE)).getId();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_AVATARS + "("
                + AVATARS_NAME + " text unique, "
                + AVATARS_FILE + " blob);");

        db.execSQL("create table " + TABLE_MESSAGES + "("
                + Message.ID + " integer, "
                + Message.TEMP_ID + " text unique, "
                + Message.COMPANION + " integer, "
                + Message.TYPE + " integer, "
                + Message.MESSAGE + " text, "
                + Message.VIEWED + " integer, "
                + Message.CREATED_AT + " text, "
                + Message.STATE + " integer);");
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
        return cursor.getBlob(1);
    }

    public long writeAvatarToCache(String img_name, byte[] img_data) throws IOException{
        ContentValues imgData = new ContentValues();
        imgData.put(AVATARS_NAME, img_name);
        imgData.put(AVATARS_FILE, img_data);

        return db.insert(TABLE_AVATARS, null, imgData);
    }

    public void writeMessagesToDb(List<Message> messageList, Long companionId) {
        for (Message message : messageList) {
            message.setState(Message.STATE_SUCCESS);
            addMessToDb(message, companionId);
        }
    }

    public boolean messagesInDb(Long companionId) {
        Cursor cursor = db.query(TABLE_MESSAGES, null, Message.COMPANION + " = ?",
                new String[] {companionId.toString()}, null, null, null);
        return (cursor.getCount() > 0);
    }

    public void addMessToDb(Message message, Long companionId) {
        ContentValues messData = new ContentValues();

        messData.put(Message.ID, message.getId());
        messData.put(Message.TEMP_ID, message.getTempId());
        messData.put(Message.COMPANION, companionId);
        messData.put(Message.TYPE, message.getType());
        messData.put(Message.MESSAGE, message.getBody());
        messData.put(Message.VIEWED, message.getViewed());
        messData.put(Message.CREATED_AT, message.createdAt());
        messData.put(Message.STATE, message.getState());

        long rows = db.insert(TABLE_MESSAGES, null, messData);
        Log.d("myLogs", "inserted " + companionId + " " + messData.toString());
    }

    public long changeMessageState(String temp_id, int state, long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Message.STATE, state);
        contentValues.put(Message.ID, id);
        long rows = db.update(TABLE_MESSAGES, contentValues, Message.TEMP_ID + " = ?", new String[] {temp_id});
        Log.d("myLogs", "changed state: " + rows + " " + contentValues.toString());
        return rows;
    }

    public void labelMessagesViewed(Long companionId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Message.VIEWED, 1);
        long rows = db.update(
                TABLE_MESSAGES,
                contentValues,
                Message.COMPANION+ " = ? and " + Message.VIEWED + " = ?",
                new String[] {companionId.toString(), String.valueOf(0)}
        );
        Log.d("myLogs", "labeled state: " + rows + " " + rows);
    }

    public Cursor getMessageList(Long companionId) {
        return db.query(TABLE_MESSAGES, null, Message.COMPANION + " = ?",
                new String[] {companionId.toString()}, null, null, Message.CREATED_AT + " ASC");
    }

}
