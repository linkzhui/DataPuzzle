package com.example.raymon.datapuzzle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yeizz on 4/10/2018.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "FileFragments_db";
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    //create Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(FileFragment.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + FileFragment.TABLE_NAME);
        onCreate(db);
    }

    public long insertFileFragments (String fileFragmentsOrigin, String fileFragmentsFirst, String fileFragmentsFirstUri, String fileFragmentsSecond,String fileFragmentsSecondUri ,String fileFragmentsThird, String fileFragmentsThirdUri) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FileFragment.COLUMN_FileFragments_Origin, fileFragmentsOrigin);
        contentValues.put(FileFragment.COLUMN_FileFragments_First, fileFragmentsFirst);
        contentValues.put(FileFragment.COLUMN_FileFragments_First_Uri,fileFragmentsFirstUri);
        contentValues.put(FileFragment.COLUMN_FileFragments_Second, fileFragmentsSecond);
        contentValues.put(FileFragment.COLUMN_FileFragments_Second_Uri,fileFragmentsSecondUri);
        contentValues.put(FileFragment.COLUMN_FileFragments_Third, fileFragmentsThird);
        contentValues.put(FileFragment.COLUMN_FileFragments_Third_Uri,fileFragmentsThirdUri);
        long id = db.insert(FileFragment.TABLE_NAME, null, contentValues);
        db.close();
        return id;
    }


    public FileFragment getFile(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Cursor cursor =  db.rawQuery( "select * from contacts where id="+id+"", null );
        Cursor cursor = db.query(FileFragment.TABLE_NAME, new String[]{FileFragment.COLUMN_ID, FileFragment.COLUMN_FileFragments_Origin,
                        FileFragment.COLUMN_FileFragments_First, FileFragment.COLUMN_FileFragments_First_Uri,FileFragment.COLUMN_FileFragments_Second,
                        FileFragment.COLUMN_FileFragments_Second_Uri,FileFragment.COLUMN_FileFragments_Third, FileFragment.COLUMN_FileFragments_Third_Uri},
                FileFragment.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null){
            cursor.moveToFirst();
        }

        //prepare FileFragment

        FileFragment fileFragment = new FileFragment( cursor.getInt(cursor.getColumnIndex(FileFragment.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Origin)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_First)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_First_Uri)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Second)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Second_Uri)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Third)),
                cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Third_Uri)));

        cursor.close();
        return fileFragment;
    }

    public List<FileFragment> getAllFiles() {
        List<FileFragment> files = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + FileFragment.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FileFragment fileFragment = new FileFragment();
                fileFragment.setFileOriginName(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Origin)));
                fileFragment.setFileFragmentNameOne(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_First)));
                fileFragment.setFileFragmentNameOneUri(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_First_Uri)));
                fileFragment.setFileFragmentNameTwo(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Second)));
                fileFragment.setFileFragmentNameTwoUri(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Second_Uri)));
                fileFragment.setFileFragmentNameThree(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Third)));
                fileFragment.setFileFragmentNameThreeUri(cursor.getString(cursor.getColumnIndex(FileFragment.COLUMN_FileFragments_Third_Uri)));
                files.add(fileFragment);
            } while (cursor.moveToNext());
        }

        db.close();
        return files;
    }


    public void deleteFileFragment (FileFragment fileFragment) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[]{ fileFragment.getFileOriginName() };
        String updateQuery = FileFragment.COLUMN_FileFragments_Origin + " = ?";

        db.delete(FileFragment.TABLE_NAME, updateQuery, args);
        db.close();
    }

    public void deleteFileFragmentbyName (String originFileName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[]{ originFileName };
        String updateQuery = FileFragment.COLUMN_FileFragments_Origin + " = ?";

        db.delete(FileFragment.TABLE_NAME, updateQuery, args);
        db.close();
    }

    public void updateFileFragment(FileFragment fileFragment) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (fileFragment.getFileFragmentNameOne().equals("null")){
            values.put(FileFragment.COLUMN_FileFragments_First, fileFragment.getFileFragmentNameOne());
            values.put(FileFragment.COLUMN_FileFragments_First_Uri, fileFragment.getFileFragmentNameOneUri());
        }

        else if (fileFragment.getFileFragmentNameTwo().equals("null")){
            values.put(FileFragment.COLUMN_FileFragments_Second, fileFragment.getFileFragmentNameTwo());
            values.put(FileFragment.COLUMN_FileFragments_Second_Uri, fileFragment.getFileFragmentNameTwoUri());
        }

        else if (fileFragment.getFileFragmentNameThree().equals("null")){
            values.put(FileFragment.COLUMN_FileFragments_Third, fileFragment.getFileFragmentNameThree());
            values.put(FileFragment.COLUMN_FileFragments_Third_Uri, fileFragment.getFileFragmentNameThreeUri());
        }

        String updateQuery = FileFragment.COLUMN_FileFragments_Origin + " = ?";
        String[] args = new String[]{fileFragment.getFileOriginName()};

        db.update(FileFragment.TABLE_NAME, values, updateQuery, args);

        
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FileFragment.TABLE_NAME);
        return numRows;
    }
}
