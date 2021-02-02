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

package tm.fileselection;

/**
*
*
*
**/

public class TMPaletteFileFilter extends TMFileFilter {

    private String codecID; // color codec
    private int size;
    private int offset;
    private int endianness;

/**
*
*
*
**/

    public TMPaletteFileFilter(String extlist, String description, String codecID, int size, int offset, int endianness) {
        super(extlist, description);
        this.codecID = codecID;
        this.size = size;
        this.offset = offset;
        this.endianness = endianness;
    }

/**
*
*
*
**/

    public String getCodecID() {
        return codecID;
    }

/**
*
*
*
**/

    public int getSize() {
        return size;
    }

/**
*
*
*
**/

    public int getOffset() {
        return offset;
    }

/**
*
*
*
**/

    public int getEndianness() {
        return endianness;
    }

}