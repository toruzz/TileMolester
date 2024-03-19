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
* 16, 24, and 32-bit direct-color (ARGB) tile codec.
* # of bits per color component must not exceed 8.
* The color component masks must be directly adjacent.
*
**/

public class DirectColorTileCodec extends TileCodec {

    public static final int LITTLE_ENDIAN=1;
    public static final int BIG_ENDIAN=2;

    private static int endianness;   // LITTLE_ENDIAN | BIG_ENDIAN

    private int rmask;      // bitmask for Red component
    private int gmask;      // bitmask for Green component
    private int bmask;      // bitmask for Blue component
    private int amask;      // bitmask for Alpha component

    // how much each component must be shifted to be transformed to a 32-bit ARGB int-packed Java pixel.
    // these are pre-calculated in the constructor and used for decoding/encoding individual pixels.
    private int rshift;
    private int gshift;
    private int bshift;
    private int ashift;

    // number of bytes that hold one compressed pixel.
    private int bytesPerPixel;

    private int startShift;
    private int shiftStep;

/**
*
* Creates a direct-color tile codec.
*
**/

    public DirectColorTileCodec(String id, int bpp, int rmask, int gmask, int bmask, int amask, String description) {
        super(id, getByteIntegralBitCount(bpp), description);
        bytesPerPixel = bitsPerPixel / 8;   // 2, 3 or 4
        this.rmask = rmask;
        this.gmask = gmask;
        this.bmask = bmask;
        this.amask = amask;
        // calculate the shifts
        rshift = 23 - msb(rmask);
        gshift = 15 - msb(gmask);
        bshift = 7 - msb(bmask);
        ashift = 31 - msb(amask);

        setEndianness(LITTLE_ENDIAN);   // default
    }

/**
* Sets the endianness.
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
* Gets the position of the most significant set bit in the given int.
**/

    private static int msb(int mask) {
        for (int i=31; i>=0; i--) {
            if ((mask & 0x80000000) != 0) {
                return i;
            }
            mask <<= 1;
        }
        return -1;  // no bits set
    }

/**
*
* Decodes a tile.
*
**/

    public int[] decode(byte[] bits, int ofs, int stride) {
        int v, r, g, b, a, s;
        int pos=0;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row of pixels
            for (int j=0; j<8; j++) {

                // get encoded pixel
                s = startShift;
                v = 0;
                for (int k=0; k<bytesPerPixel; k++) {
                    v |= (bits[ofs++] & 0xFF) << s;
                    s += shiftStep;
                }

                // decode R component
                r = v & rmask;
                if (rshift < 0) {
                    r >>= -rshift;
                }
                else {
                    r <<= rshift;
                }

                // decode G component
                g = v & gmask;
                if (gshift < 0) {
                    g >>= -gshift;
                }
                else {
                    g <<= gshift;
                }

                // decode B component
                b = v & bmask;
                if (bshift < 0) {
                    b >>= -bshift;
                }
                else {
                    b <<= bshift;
                }

                // decode A component
                a = v & amask;
                if (ashift < 0) {
                    a >>= -ashift;
                }
                else {
                    a <<= ashift;
                }

                // final pixel
                pixels[pos++] = a | r | g | b;
            }
            ofs += stride;
        }
        return pixels;
    }

/**
*
* Encodes a tile.
*
**/

    public void encode(int[] pixels, byte[] bits, int ofs, int stride) {
        int v, r, g, b, a, s, argb;
        int pos=0;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row of pixels
            for (int j=0; j<8; j++) {

                // get decoded pixel
                argb = pixels[pos++];

                // encode R component
                r = argb;
                if (rshift < 0) {
                    r <<= -rshift;
                }
                else {
                    r >>= rshift;
                }
                r &= rmask;

                // encode G component
                g = argb;
                if (gshift < 0) {
                    g <<= -gshift;
                }
                else {
                    g >>= gshift;
                }
                g &= gmask;

                // encode B component
                b = argb;
                if (bshift < 0) {
                    b <<= -bshift;
                }
                else {
                    b >>= bshift;
                }
                b &= bmask;

                // encode A component
                a = argb;
                if (ashift < 0) {
                    a <<= -ashift;
                }
                else {
                    a >>= ashift;
                }
                a &= amask;

                // final value
                s = startShift;
                v = a | r | g | b;
                for (int k=0; k<bytesPerPixel; k++) {
                    bits[ofs++] = (byte)(v >> s);
                    s += shiftStep;
                }

            }
            ofs += stride;
        }
    }

/**
*
* Gets the least number of whole bytes that are required to store
* <code>bits</code> bits of information.
*
**/

    private static int getByteIntegralBitCount(int bits) {
        int bytes = bits / 8;
        int extrabits = bits % 8;
        if (extrabits != 0) bytes++;
        return bytes * 8;
    }

}