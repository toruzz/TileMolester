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
* Planar, palette-indexed 8x8 tile codec. Max. 8 bitplanes.
* bitsPerPixel must be a power of 2. (1, 2, 4, 8) (why??)
* Planes for each row must be stored sequentially.
*
**/

public class PlanarTileCodec extends TileCodec {

    protected int[] bpOffsets;
    protected int[] bp;
    protected static int[][][] bitsToPixelsLookup=null;

/**
*
* Constructor.
*
* @param bpOffsets  Relative offsets for the bitplane values in a row (8 pixels) of encoded tile data. The length of this array is the number of bitplanes in a tile row, which is equal to the # of bits per pixel.
*
**/

    public PlanarTileCodec(String id, int[] bpOffsets, String description) {
        super(id, bpOffsets.length, description);
        this.bpOffsets = bpOffsets;
        bp = new int[8];

        if (bitsToPixelsLookup == null) {
            // Precalculate all bit patterns
            bitsToPixelsLookup = new int[8][256][8];
            for (int i=0; i<8; i++) {
                // do one bitplane
                for (int j=0; j<256; j++) {
                    // do one byte
                    for (int k=7; k>=0; k--) {
                        // do one pixel
                        bitsToPixelsLookup[i][j][7-k] = ((j >> k) & 1) << i;
                    }
                }
            }
        }
    }

/**
*
* Decodes a tile.
*
* @param bits   An array of ints holding encoded tile data in each LSB
* @param ofs    Where to start decoding from in the array
*
**/

    public int[] decode(byte[] bits, int ofs, int stride) {
        int pos=0;
        stride++;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row of pixels
            for (int j=0; j<bitsPerPixel; j++) {
                // get bits for bitplane j
                bp[j] = bits[ofs+bpOffsets[j]] & 0xFF;
            }
            for (int j=0; j<8; j++) {
                // decode one pixel
                int p = 0;
                for (int k=0; k<bitsPerPixel; k++) {
                    // add bitplane k
                    p |= bitsToPixelsLookup[k][bp[k]][j];
                }
                pixels[pos++] = p;
            }
            ofs += stride;
        }
        return pixels;
    }

/**
*
* Encodes a bitplaned tile.
*
**/

    public void encode(int[] pixels, byte[] bits, int ofs, int stride) {
        int pos=0;
        stride++;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row
            for (int j=0; j<bitsPerPixel; j++) {
                // reset bits of bitplane j
                bits[ofs+bpOffsets[j]] = 0;
            }
            for (int j=0; j<8; j++) {
                // encode one pixel
                int p = pixels[pos];
                for (int k=0; k<bitsPerPixel; k++) {
                    // add bitplane k
                    bits[ofs+bpOffsets[k]] |= ((p >> k) & 0x01) << (7-j);
                }
                pos++;
            }
            ofs += stride;
        }
    }

}