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
*
**/

public class INESFileListener extends TMFileListener {

    private static int[] iNES_ID = { 0x4E,0x45,0x53,0x1A };

    public boolean doFormatDetect(final byte[] data, String extension) {
        // verify extension
        if (!extension.equals("nes")) {
            return false;
        }
        // verify NES^Z
        for (int i=0; i<iNES_ID.length; i++) {
            if ((data[i] & 0xFF) != iNES_ID[i]) {
                return false;
            }
        }
        return true;
    }

    public void fileLoaded(byte[] data, String extension) {
    }

    public void fileSaving(byte[] data, String extension) {
    }

}