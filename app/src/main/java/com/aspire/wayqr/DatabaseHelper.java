package com.aspire.wayqr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;

import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "plane_values";
    private static final String ID_COL = "ID";
    private static final String LOC_COL = "LOCATION";
    private static final String QR_COL = "QR";
    private static final String X_COL = "X";
    private static final String Y_COL = "Y";
    private static final String Z_COL = "Z";
    private View view;
    private String finalLoc = "DEMO";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String coordinatesTable = "CREATE TABLE " + TABLE_NAME + "(" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LOC_COL + " TEXT," +
                QR_COL + " TEXT, " +
                X_COL + " TEXT," +
                Y_COL + " TEXT," +
                Z_COL + " TEXT" + ")";
        sqLiteDatabase.execSQL(coordinatesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlLiteDatabase, int oldVersion, int newVersion) {
        sqlLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqlLiteDatabase);
    }

    public long addCoordinates(Float coOne, Float coTwo, Float coThree, String tableValue, String qrValue) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOC_COL, tableValue);
        contentValues.put(QR_COL, qrValue);
        contentValues.put(X_COL, coOne.toString());
        contentValues.put(Y_COL, coTwo.toString());
        contentValues.put(Z_COL, coThree.toString());
        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return result;
    }

    public List<AxisModel> getCoordinates(String locationValue, String qrNameValue) {
        List<AxisModel> axisList = new ArrayList<>();
        SQLiteDatabase liteDatabase = this.getWritableDatabase();
        Cursor cursor = liteDatabase.query(TABLE_NAME, new String[]{ID_COL, LOC_COL, QR_COL, X_COL,
                        Y_COL, Z_COL}, LOC_COL + "=? AND " + QR_COL + "=?", new String[]{locationValue, qrNameValue},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                AxisModel model = new AxisModel();
                model.set_id(cursor.getInt(cursor.getColumnIndex(ID_COL)));
                model.set_location(cursor.getString(cursor.getColumnIndex(LOC_COL)));
                model.set_location(cursor.getString(cursor.getColumnIndex(QR_COL)));
                model.set_xAxis(cursor.getString(cursor.getColumnIndex(X_COL)));
                model.set_yAxis(cursor.getString(cursor.getColumnIndex(Y_COL)));
                model.set_zAxis(cursor.getString(cursor.getColumnIndex(Z_COL)));
                //Adding to List
                axisList.add(model);
            } while (cursor.moveToNext());
        }
        return axisList;
    }

    public void deleteDemo() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL(" DELETE FROM " + TABLE_NAME + " WHERE " + LOC_COL + "== '" + finalLoc + "'");
    }

    public void deleteALL() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
//        sqLiteDatabase.execSQL(" DELETE FROM " + TABLE_NAME);
        sqLiteDatabase.delete(TABLE_NAME, null,null);
    }

    public Vector3 finalPoint() {
        int finalPoint = pointsCount() - 1;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, new String[]{X_COL, Y_COL, Z_COL},
                ID_COL + "=?", new String[]{String.valueOf(finalPoint)},
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Vector3 vector3 = new Vector3(Float.parseFloat(cursor.getString(cursor.getColumnIndex(X_COL))),
                Float.parseFloat(cursor.getString(cursor.getColumnIndex(Y_COL))),
                Float.parseFloat(cursor.getString(cursor.getColumnIndex(Z_COL))));
        cursor.close();
        return vector3;
    }

    public int pointsCount() {
        int count = 0;
        String countQuery = " SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(countQuery, null);
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
}
