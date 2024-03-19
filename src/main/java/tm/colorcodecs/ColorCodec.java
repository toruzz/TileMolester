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
* Abstract superclass for color codecs.
* A "color codec" defines how values of a particular format are translated
* to 32-bit ARGB pixel values (decoded), and back again (encoded).
*
**/

public abstract class ColorCodec {

    public static final int LITTLE_ENDIAN=1;
    public static final int BIG_ENDIAN=2;
    public static final int MIDDLE_ENDIAN=3;

    protected int bitsPerPixel;
    protected int bytesPerPixel;
    protected String id;
    protected String description;

    private static int endianness;
    private int startShift;
    private int shiftStep;

/**
*
* Creates a ColorCodec with the given id, bits per pixel and description.
*
**/

    public ColorCodec(String id, int bitsPerPixel, String description) {
        this.id = id;
        this.bitsPerPixel = bitsPerPixel;
        this.description = description;
        bytesPerPixel = getBytesRequired(bitsPerPixel);
        setEndianness(LITTLE_ENDIAN);   // default
    }

/**
* Sets the endianness.
* It determines the byte order related to methods toBytes() and fromBytes().
**/

    public void setEndianness(int endianness) {
        this.endianness = endianness;
        if (endianness == LITTLE_ENDIAN) {
            startShift = 0;
            shiftStep = 8;
        }
        else {
            // BIG_ENDIAN
            startShift = (bytesPerPixel-1) * 8;
            shiftStep = -8;
        }
    }

/**
*
* Gets the ID.
*
**/

    public String getID() {
        return id;
    }

/**
*
* Gets the number of bits per pixel.
*
**/

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

/**
*
* Gets the number of bytes required to hold one pixel.
*
**/

    public int getBytesPerPixel() {
        return bytesPerPixel;
    }

/**
*
* Gets the description.
*
**/

    public String getDescription() {
        return description;
    }

/**
*
* This method takes a value assumed to be in the color codec's native format
* and returns the equivalent 32-bit ARGB value.
*
**/

    public abstract int decode(int value);

/**
*
* This method takes a value assumed to be in 32-bit ARGB format and returns
* the equivalent native format value.
*
**/

    public abstract int encode(int argb);

/**
*
* Converts given value to a series of bytes, according to current endianness.
* @param bytes Array to store resulting bytes in
* @param offset Starting offset in array
*
**/

    public byte[] toBytes(int value, byte[] bytes, int offset) {
        int shift = startShift;
        for (int i=0; i<bytesPerPixel; i++) {
            bytes[offset+i] = (byte)(value >> shift);
            shift += shiftStep;
        }
        return bytes;
    }

/**
*
* Converts bytes from given array to a value, according to current endianness.
* @param offset Where to start reading the bytes that make up the value.
*
**/

    public int fromBytes(byte[] bytes, int offset) {
        int shift = startShift;
        int value = 0;
        for (int i=0; i<bytesPerPixel; i++) {
            value |= (bytes[offset+i] & 0xFF) << shift;
            shift += shiftStep;
        }
        return value;
    }

/**
*
* Gets the least number of whole bytes that are required to store
* <code>bits</code> bits of information.
*
**/

    private static int getBytesRequired(int bits) {
        int bytes = bits / 8;
        int extrabits = bits % 8;
        if (extrabits != 0) bytes++;
        return bytes;
    }

    public String toString() {
        return description;
    }

}