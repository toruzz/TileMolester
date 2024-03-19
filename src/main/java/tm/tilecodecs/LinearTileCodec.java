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
* Linear palette-indexed 8x8 tile codec. Max. 8 bits per pixel.
* bitsPerPixel mod 2 = 0 (so only 1, 2, 4, 8bpp possible).
*
* Some notes on "pixel ordering":
*
* I define pixel order for a row like so: 0 1 2 3 4 5 6 7.
* In other words, pixels are numbered from leftmost to rightmost,
* as they appear on screen.
* I define bit order in a byte like so: 7 6 5 4 3 2 1 0.
* In other words, bits are numbered from rightmost (high) to leftmost (low).
*
* For formats with less than 8 bits per pixel, it is then an
* issue how bits are extracted from the bytes of encoded tile data
* to form the order of pixels stated above.
*
* Note that this applies at the PIXEL-level, not bit-, byte- or row-level;
* this is why ordering is not an issue for 8bpp tiles, since each byte
* contains only one pixel. It is assumed that the bytes themselves are
* always stored in-order (0, 1, 2, 3, ...). Similary, it is assumed that
* the individual bits of a pixel are always stored from high -> low.
*
* The familiar terms "little endian" and "big endian" are not appropriate to
* use here, as I consider them to apply at the byte-level. To avoid any
* confusion or ambiguity, I therefore use the expressions "in-order" and
* "reverse-order" to refer to the ordering of pixels within a byte.
* This is still a bit tricky since the order of pixels and bits above are
* reverses of eachother to begin with, but shouldn't be too confusing:
*
* I define "in-order" to mean that the order of pixel data as stored in a byte
* complies with the order of pixels to be rendered, from left to right. For
* 1bpp, this is simple: in-order means that bit 7 corresponds to pixel 0,
* bit 6 to pixel 1 and so on. Reverse-order means that bit 7 corresponds to
* pixel 7, bit 6 corresponds to pixel 6 and so on (in other words, the bits
* have to be reversed from left to right in order for the pixel order to be
* correct). Diagrammatically:
*
* Pixels: 01234567
*         |||||||| (In-order)
* Bits:   76543210
*
* Pixels: 01234567
*         |||||||| (Reverse-order)
* Bits:   01234567
*
* This extends quite intuitively to the cases of 2 and 4 bits per pixel.
* I will give one example of each.
*
* 2bpp, in-order:
*
* Pixels: 0  1  2  3
*         |  |  |  |
* Bits:   76 54 32 10
*
* 4bpp, reverse-order:
*
* Pixels: 0     1
*         |     |
* Bits:   3210  7654
*
**/

public class LinearTileCodec extends TileCodec {

    public static final int IN_ORDER=1;
    public static final int REVERSE_ORDER=2;

    protected int ordering;
    protected int pixelsPerByte;
    protected int pixelMask;
    protected int startPixel;
    protected int boundary;
    protected int step;

/**
* Constructor.
**/

    public LinearTileCodec(String id, int bitsPerPixel, int ordering, String description) {
        super(id, bitsPerPixel, description);    // <= 8
        this.ordering = ordering;   // IN_ORDER or REVERSE_ORDER
        pixelsPerByte = 8 / bitsPerPixel;
        pixelMask = (int)(colorCount - 1);

        if (IN_ORDER == ordering) {
            startPixel = pixelsPerByte-1;
            boundary = -1;
            step = -1;
        }
        else {  // REVERSE_ORDER
            startPixel = 0;
            boundary = pixelsPerByte;
            step = 1;
        }
    }

/**
*
* Decodes a tile.
*
**/

    public int[] decode(byte[] bits, int ofs, int stride) {
        int pos=0;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row
            for (int k=0; k<bytesPerRow; k++) {
                // do one byte
                int b = bits[ofs++] & 0xFF; // TODO: rowbyteoffset[k]
                for (int m = startPixel; m != boundary; m += step) {
                    // decode one pixel
                    pixels[pos++] = (b >> bitsPerPixel*m) & pixelMask;
                }
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
        int pos = 0;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row
            for (int k=0; k<bytesPerRow; k++) {
                // do one byte
                byte b = 0;
                for (int m = startPixel; m != boundary; m += step) {
                    // encode one pixel
                    b |= (pixels[pos++] & pixelMask) << (m*bitsPerPixel);
                }
                bits[ofs++] = b;    // TODO: rowbyteoffset[k]
            }
            ofs += stride;
        }
    }

}