package com.example.enclosure;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import java.util.ArrayList;


public class UserDBHelper<girth> extends SQLiteOpenHelper {
    private static final String DB_NAME = "user.db";
    private static final int DB_VERSION = 1;
    double area;
    private static UserDBHelper mHelper = null;
   // private final double area;
    private SQLiteDatabase mDB = null;
    public static final String TABLE_NAME = "user_info";

    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    private UserDBHelper(Context context, int version) {
        super(context, DB_NAME, null, version);
    }


    //获取数据库帮助器
    public static UserDBHelper getInstance(Context context, int version) {
        if (version > 0 && mHelper == null) {
            mHelper = new UserDBHelper(context, version);
        } else if (mHelper == null) {
            mHelper = new UserDBHelper(context);
        }
        return mHelper;
    }

    //打开读连接
    public SQLiteDatabase OpenReadLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getReadableDatabase();
        }
        return mDB;
    }

    //打开写连接
    public SQLiteDatabase OpenWriteLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getWritableDatabase();
        }
        return mDB;
    }
//创建数据库
    public void onCreate(SQLiteDatabase db) {
        String drop_sql = "DROP TABLE IF EXISTS" + TABLE_NAME + ";";
        db.execSQL(drop_sql);
        String create_sql = "CREATE TABLE IF NOT EXISTS" + TABLE_NAME + "("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "area VARCHAR NOT NULL," + "girth VARCHAR NOT NULL" + ");";
        db.execSQL(create_sql);
    }
    //修改数据库
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    public int delete(String condition) {
        return mDB.delete(TABLE_NAME, condition, null);
    }

    //往该表添加多条面积
    public long insert(double area) {
        long result = -1;
        //插入新纪录
        ContentValues cv = new ContentValues();
        cv.put("area", area);
        result = mDB.insert(TABLE_NAME, "", cv);
            return result;
    }
//添加周长
    public long inserts(double grith) {
        long result = -1;
        ContentValues cv = new ContentValues();
        cv.put("grith", grith);
        result = mDB.insert(TABLE_NAME, "", cv);
        return result;
    }
}

