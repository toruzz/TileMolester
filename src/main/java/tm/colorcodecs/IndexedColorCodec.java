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

package tm.colorcodecs;

/**
*
* An IndexedColorCodec translates pixels according to a
* pre-defined array of 32-bit ARGB values.
*
**/

public class IndexedColorCodec extends ColorCodec {

    private int[] colorTable;   // table of pre-defined ARGB values

    public IndexedColorCodec(String id, int bitsPerPixel, int[] colorTable, String description) {
        super(id, bitsPerPixel, description);
        this.colorTable = colorTable;
    }

/**
*
* To decode, simply look up the table.
*
**/

    public int decode(int value) {
        if (value < colorTable.length) {
            return colorTable[value];
        }
        return 0;   // undefined
    }

/**
*
* To encode, find the closest match in the table.
*
**/

    public int encode(int argb) {
        int targetR = (argb & 0x00FF0000) >> 16;
        int targetG = (argb & 0x0000FF00) >> 8;
        int targetB = (argb & 0x000000FF);
        int bestEntry=0, bestDiff=1000000;
        for (int i=0; i<colorTable.length; i++) {
            int val = colorTable[i];
            int r = (val & 0x00FF0000) >> 16;
            int g = (val & 0x0000FF00) >> 8;
            int b = (val & 0x000000FF);
            int diff = Math.abs(targetR - r) + Math.abs(targetG - g) + Math.abs(targetB - b);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestEntry = i;
            }
        }
        return bestEntry;
    }

}