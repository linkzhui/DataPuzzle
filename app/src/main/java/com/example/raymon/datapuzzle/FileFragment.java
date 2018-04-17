package com.example.raymon.datapuzzle;

/**
 * Created by yeizz on 4/10/2018.
 */

public class FileFragment {

    public static final String TABLE_NAME = "FileFragments";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FileFragments_Origin = "Origin File";
    public static final String COLUMN_FileFragments_First = "File Fragment 1";
    public static final String COLUMN_FileFragments_First_Uri = "File Fragment 1 Uri";
    public static final String COLUMN_FileFragments_Second = "File Fragment 2";
    public static final String COLUMN_FileFragments_Second_Uri = "File Fragment 2 Uri";
    public static final String COLUMN_FileFragments_Third = "File Fragment 3";
    public static final String COLUMN_FileFragments_Third_Uri = "File Fragment 3 Uri";

    private int id;
    private String fileOriginName;
    private String fileFragmentNameOne;
    private String fileFragmentNameTwo;
    private String fileFragmentNameThree;
    private String fileFragmentNameOneUri;
    private String fileFragmentNameTwoUri;
    private String fileFragmentNameThreeUri;





    // Create table SQL query
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FileFragments_Origin + " TEXT,"
                    + COLUMN_FileFragments_First + " TEXT,"
                    + COLUMN_FileFragments_First_Uri + " TEXT,"
                    + COLUMN_FileFragments_Second + " TEXT,"
                    + COLUMN_FileFragments_Second_Uri + " TEXT,"
                    + COLUMN_FileFragments_Third + " TEXT,"
                    + COLUMN_FileFragments_Third_Uri + " TEXT,"
                    + ")";


    public FileFragment() {

    }

    public void setFileFragmentNameOneUri(String fileFragmentNameOneUri) {
        this.fileFragmentNameOneUri = fileFragmentNameOneUri;
    }

    public void setFileFragmentNameTwoUri(String fileFragmentNameTwoUri) {
        this.fileFragmentNameTwoUri = fileFragmentNameTwoUri;
    }

    public void setFileFragmentNameThreeUri(String fileFragmentNameThreeUri) {
        this.fileFragmentNameThreeUri = fileFragmentNameThreeUri;
    }

    public FileFragment(int id, String fileFragmentsOrigin, String fileFragmentsFirst, String fileFragmentsFirstUri, String fileFragmentsSecond, String fileFragmentsSecondUri, String fileFragmentsThird, String fileFragmentsThirdUri) {
        this.id = id;
        this.fileOriginName = fileFragmentsOrigin;

        this.fileFragmentNameOne = fileFragmentsFirst;
        this.fileFragmentNameOneUri = fileFragmentsFirstUri;
        this.fileFragmentNameTwo = fileFragmentsSecond;
        this.fileFragmentNameTwoUri = fileFragmentsSecondUri;
        this.fileFragmentNameThree = fileFragmentsThird;
        this.fileFragmentNameThreeUri = fileFragmentsThirdUri;
    }

    public int getId() {
        return id;
    }

    public String FileOriginName() {
        return fileOriginName;
    }

    public void setFileOriginName(String fileOriginName){
        this.fileOriginName = fileOriginName;
    }

    public void setFileFragmentNameOne(String fileFragmentNameOne){
        this.fileFragmentNameOne = fileFragmentNameOne;
    }

    public void setFileFragmentNameTwo(String fileFragmentNameTwo){
        this.fileFragmentNameTwo = fileFragmentNameTwo;
    }

    public void setFileFragmentNameThree(String fileFragmentNameThree){
        this.fileFragmentNameThree = fileFragmentNameThree;
    }



}
