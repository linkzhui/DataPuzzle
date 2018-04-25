package com.example.raymon.datapuzzle;

/**
 * Created by yeizz on 4/10/2018.
 */

public class FileFragment {

    public static final String TABLE_NAME = "FileFragments";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FileFragments_Origin = "Origin_File";
    public static final String COLUMN_FileFragments_First = "File_Fragment_1";
    public static final String COLUMN_FileFragments_First_Uri = "File_Fragment_1_Uri";
    public static final String COLUMN_FileFragments_Second = "File_Fragment_2";
    public static final String COLUMN_FileFragments_Second_Uri = "File_Fragment_2_Uri";
    public static final String COLUMN_FileFragments_Third = "File_Fragment_3";
    public static final String COLUMN_FileFragments_Third_Uri = "File_Fragment_3_Uri";

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
                    + COLUMN_FileFragments_Third_Uri + " TEXT"
                    + ")";


    public FileFragment() {

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

    public void setFileFragmentNameOneUri(String fileFragmentNameOneUri) {
        this.fileFragmentNameOneUri = fileFragmentNameOneUri;
    }

    public void setFileFragmentNameTwoUri(String fileFragmentNameTwoUri) {
        this.fileFragmentNameTwoUri = fileFragmentNameTwoUri;
    }

    public void setFileFragmentNameThreeUri(String fileFragmentNameThreeUri) {
        this.fileFragmentNameThreeUri = fileFragmentNameThreeUri;
    }

    public String getFileFragmentNameOne() {
        return fileFragmentNameOne;
    }

    public String getFileFragmentNameTwo() {
        return fileFragmentNameTwo;
    }

    public String getFileFragmentNameThree() {
        return fileFragmentNameThree;
    }

    public String getFileFragmentNameOneUri() {
        return fileFragmentNameOneUri;
    }

    public String getFileFragmentNameTwoUri() {
        return fileFragmentNameTwoUri;
    }

    public String getFileFragmentNameThreeUri() {
        return fileFragmentNameThreeUri;
    }

    public int getId() { return id; }

    public String getFileOriginName() { return fileOriginName;}




}
