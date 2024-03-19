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
* A DirectColorCodec translates pixels exactly the same way as
* a direct-color tile codec.
* The composition of a pixel is described by Red, Green,
* Blue and Alpha masks.
*
**/

public class DirectColorCodec extends ColorCodec {

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

    public DirectColorCodec(String id, int bitsPerPixel, int rmask, int gmask, int bmask, int amask, String description) {
        super(id, bitsPerPixel, description);
        this.rmask = rmask;
        this.gmask = gmask;
        this.bmask = bmask;
        this.amask = amask;
        // calculate the shifts
        rshift = 23 - msb(rmask);
        gshift = 15 - msb(gmask);
        bshift = 7 - msb(bmask);
        ashift = 31 - msb(amask);
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
*
**/

    public int decode(int value) {
        int r, g, b, a;
        // decode R component
        r = value & rmask;
        if (rshift < 0) {
            r >>= -rshift;
        }
        else {
            r <<= rshift;
        }

        // decode G component
        g = value & gmask;
        if (gshift < 0) {
            g >>= -gshift;
        }
        else {
            g <<= gshift;
        }

        // decode B component
        b = value & bmask;
        if (bshift < 0) {
            b >>= -bshift;
        }
        else {
            b <<= bshift;
        }

        // decode A component
        a = value & amask;
        if (ashift < 0) {
            a >>= -ashift;
        }
        else {
            a <<= ashift;
        }

        // final pixel
        return (a | r | g | b);
    }

/**
*
*
**/

    public int encode(int argb) {
        int r, g, b, a;
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
        return (a | r | g | b);
    }

}