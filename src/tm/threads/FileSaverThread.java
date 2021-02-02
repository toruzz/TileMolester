/*
*
*    Copyright (C) 2003 Kent Hansen.
*
*    This file is part of Tile Molester.
*
*    Tile Molester is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    Tile Molester is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*/

package tm.threads;

import java.io.*;

/**
*
* Thread for writing a buffer to a file.
*
**/

public class FileSaverThread extends ProgressThread {

    private static final int CHUNK_SIZE = 16384;
    private RandomAccessFile raf=null;
    private int bytesLeft;
    private byte[] contents;

    public FileSaverThread(byte[] contents, File file)
        throws FileNotFoundException, IOException {
        super();
        this.contents = contents;
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(0);
        }
        catch (FileNotFoundException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }
        bytesLeft = contents.length;
        this.setPriority(NORM_PRIORITY);
    }

    public int getPercentageCompleted() {
        int result = (int)(((long)contents.length - (long)bytesLeft) * 100 / (long)contents.length);
        return result;
    }

    public void run() {
        while (bytesLeft > 0) {
            if (bytesLeft > CHUNK_SIZE) {
                try {
                    raf.write(contents, contents.length - bytesLeft, CHUNK_SIZE);
                }
                catch (Exception e) { }
                bytesLeft -= CHUNK_SIZE;
            }
            else {
                try {
                    raf.write(contents, contents.length - bytesLeft, bytesLeft);
                }
                catch (Exception e) { }
                bytesLeft = 0;
            }
            yield();
        }
        try {
            raf.close();
        } catch (Exception e) { }
        // done saving data
    }

}