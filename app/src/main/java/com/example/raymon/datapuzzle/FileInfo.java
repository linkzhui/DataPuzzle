package com.example.raymon.datapuzzle;

import java.io.BufferedOutputStream;

/**
 * Created by raymon on 3/21/18.
 */

public class FileInfo {
    BufferedOutputStream bufferedOutputStream;
    String file_title;
    public FileInfo(BufferedOutputStream bufferedOutputStream, String file_title)
    {
        this.bufferedOutputStream = bufferedOutputStream;
        this.file_title = file_title;
    }
}
