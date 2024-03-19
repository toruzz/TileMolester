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

package tm.filelistener;

/**
*
* Listener for Sega Master System (*.sms) files.
*
**/

public class SegaMasterSystemFileListener extends TMFileListener {

    private static final int[] tmr_Sega = { 0x54,0x4D,0x52,0x20,0x53,0x45,0x47,0x41 };

/**
*
* Detects if this is a Sega Master System file.
* The criteria are that the extension is either sms or gg and that the string
* "TMR SEGA" appears at offset 0x7FF0.
*
**/

    public boolean doFormatDetect(final byte[] data, String extension) {
        // verify extension
        if (!(extension.equals("sms")
            || extension.equals("gg"))) {
            return false;
        }

        // verify "TMR SEGA" string
        for (int i=0; i<tmr_Sega.length; i++) {
            if ((data[0x7FF0+i] & 0xFF) != tmr_Sega[i]) {
                return false;
            }
        }

        return true;
    }

/**
*
* Does nothing on file load.
*
**/

    public void fileLoaded(byte[] data, String extension) {
    }

/**
*
* Recalculates the checksum on file save.
*
**/

    public void fileSaving(byte[] data, String extension) {
        // update checksum
        int checkSum = 0;
        for (int i=0; i<0x7FF0; i++) {
            checkSum += data[i] & 0xFF;
        }
        for (int i=0x8000; i<data.length; i++) {
            checkSum += data[i] & 0xFF;
        }
        data[0x7FFA] = (byte)(checkSum & 0xFF);
        data[0x7FFB] = (byte)((checkSum >> 8) & 0xFF);
    }

}