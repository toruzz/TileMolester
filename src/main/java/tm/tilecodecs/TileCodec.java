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

package tm.tilecodecs;

/**
*
* Abstract class for 8x8 ("atomic") tile codecs.
* To add a new tile format, simply extend this class and implement decode() and encode().
*
**/

public abstract class TileCodec {

    public static final int MODE_1D=1;
    public static final int MODE_2D=2;

    private String id;
    private String description;
    protected int[] pixels;     // destination for DEcoded tile data
    protected int bitsPerPixel;
    protected int bytesPerRow;  // row = 8 pixels
    protected long colorCount;
    protected int tileSize;     // size of one encoded tile

/**
*
* Constructor. Every subclass must call this with argument bitsPerPixel.
*
* @param bitsPerPixel   Duh!
*
**/

    public TileCodec(String id, int bitsPerPixel, String description) {
        this.id = id;
        this.bitsPerPixel = bitsPerPixel;
        this.description = description;
        bytesPerRow = bitsPerPixel; // because (bitsPerPixel*8)/8 = bitsPerPixel
        tileSize = bytesPerRow*8;
        colorCount = 1 << bitsPerPixel;
        pixels = new int[8*8];
    }

/**
*
* Decodes a tile.
*
* @param bits   An array of encoded tile data
* @param ofs    Start offset of tile in bits array
*
**/

    public abstract int[] decode(byte[] bits, int ofs, int stride);

/**
*
* Encodes a tile.
*
* @param pixels An array of decoded tile data
*
**/

    public abstract void encode(int[] pixels, byte[] bits, int ofs, int stride);

/**
*
* Gets the # of bits per pixel for the tile format.
*
**/

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

/**
*
* Gets the # of bytes per row (8 pixels) for the tile format.
*
**/

    public int getBytesPerRow() {
        return bytesPerRow;
    }

/**
*
*
*
**/
/*
    public long getColorCount() {
        return colorCount;
    }
*/
// TEMP!!!!!!!!!!
    public int getColorCount() {
        if (bitsPerPixel < 8) return (1 << bitsPerPixel);
        return 256;
    }

/**
*
* Gets the size in bytes of one tile encoded in this format.
*
**/

    public int getTileSize() {
        return tileSize;
    }

/**
*
* Gets the description of the codec.
*
**/

    public String getDescription() {
        return description;
    }

/**
*
* Gets the codec id.
*
**/

    public String getID() {
        return id;
    }

    public String toString() {
        return description;
    }

}