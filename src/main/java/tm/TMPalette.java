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

package tm;

import tm.colorcodecs.*;

/**
*
* Custom palette class.
*
**/

public class TMPalette {

    // the default 256-color palette
    public static final int[] defaultPalette = {
        0x000000, 0xA04020, 0xD0A050, 0xF0F080, 0x860033, 0x0073BF, 0xFFCF00, 0xB4EBEF,
        0x000093, 0x00FF51, 0x00ACFF, 0xA411BC, 0x000000, 0xF28C59, 0x9F00B6, 0x00DC83,
        0x090021, 0xFFFFFF, 0xFF9F00, 0xFF0003, 0xFFAFC2, 0xF0004B, 0xE136FC, 0x6D0473,
        0x000300, 0xFFF94B, 0x9FA300, 0x636000, 0x000000, 0xF05CF9, 0xEF6B84, 0x00FF5D,
        0x000000, 0x99E200, 0xFF2F4C, 0x5EFFD6, 0x002D24, 0xFFFF00, 0x4AC234, 0x4838F5,
        0x4300D6, 0xFF0000, 0xE8890B, 0x00FC84, 0x004066, 0xFFD200, 0xBBD96B, 0xFF0067,
        0xFF6900, 0x330059, 0xFFFF4B, 0x1BACFB, 0x5EAAE3, 0x4F5DF9, 0xB12A8E, 0x6DF674,
        0xEB07E3, 0x9EB76E, 0xEE5FD6, 0x2BF8BA, 0x723338, 0x5D063C, 0xDC9C6F, 0xAA2B12,
        0x176599, 0xC6671B, 0x61EC0D, 0x820194, 0x978F12, 0x77EAB5, 0x4A9936, 0x099FAF,
        0x956558, 0x0FD972, 0xF24CA5, 0x47F7A2, 0x067996, 0xA1ABB4, 0x633056, 0xE02D81,
        0x2A5836, 0x9DCAE0, 0x65C043, 0xC4B9D5, 0x11A983, 0xF6DB7D, 0x8742B1, 0xB17615,
        0x218E7A, 0xF1577E, 0x943F95, 0xEADDED, 0xC0D74E, 0xA84090, 0xA79252, 0xF8EE81,
        0xBCCD29, 0x0EA376, 0xE5A101, 0x284457, 0xB5512D, 0x2FC653, 0xEBA13F, 0x2DFDA6,
        0x479061, 0x41BBDC, 0xC53597, 0x5F4CBE, 0xBF6749, 0x78AE54, 0xB5BD0C, 0x7641F9,
        0x69B4F7, 0xD7049F, 0x7A487F, 0x52AD20, 0x6E7F52, 0x5A9633, 0x607DDB, 0x02355C,
        0xF07278, 0xE8DE01, 0xA19835, 0xA0B90E, 0x98EE91, 0xBC46BD, 0xBD67CE, 0xE87CA2,
        0x7B6CFB, 0x0ACF97, 0xD3682E, 0x5CFB6A, 0x243CA9, 0xE943F2, 0x1D736F, 0x26F742,
        0x7A8E1A, 0xE708FD, 0x4006EB, 0xA2AEF0, 0xF085CB, 0x22DDC2, 0xEB03CB, 0x879218,
        0x5E0F42, 0x3E2F83, 0x73880A, 0x56707B, 0x9526A4, 0xA23E37, 0x2959BE, 0xB115E2,
        0x29F61D, 0xCA4BC2, 0x234876, 0x2DC7E5, 0xC735C2, 0xF41484, 0x6B7D1D, 0x1C4FAD,
        0x79AAC3, 0xA90949, 0xF4CD7B, 0x7BD0AE, 0x25EA81, 0x2B5E23, 0xB50961, 0x53DC47,
        0xF58C41, 0x41DCE8, 0x423AA4, 0xDE3864, 0x2A45A6, 0x011F57, 0x75A7B4, 0x621B26,
        0xDE88C0, 0xC05A68, 0xE9AE99, 0x81E7B6, 0x514F2C, 0x9E72A6, 0x5EF1C2, 0xF7175A,
        0xFD219A, 0x8EF152, 0x2171FC, 0x502258, 0x88C63A, 0x1945F8, 0x3CAA21, 0xD7A067,
        0x52CCF2, 0x4CE400, 0x43CC98, 0x1EDCF4, 0x076F0E, 0xB79188, 0xAF0F2B, 0x0AD644,
        0xD85343, 0xB6ECA3, 0x0662BB, 0x5A816A, 0x26A60E, 0xC21E72, 0xDFC212, 0x286621,
        0xF6FAD6, 0x2BA27D, 0x5F6F12, 0x306F18, 0xF80C80, 0x1066A7, 0xAC784C, 0x9275FA,
        0x28C6D3, 0x2C85D8, 0x8EF002, 0xE12571, 0x7A8AFB, 0x2B6B5E, 0x73862A, 0x44E76F,
        0x9DCDF8, 0x375493, 0xEA65D1, 0xD745F0, 0x3352E7, 0xD648A3, 0xDC30AD, 0x920FD5,
        0xE9572A, 0x35A93C, 0x9BFCA9, 0x37F35D, 0x143E89, 0x7FA505, 0xD85EFA, 0x63DF30,
        0xC8CCB8, 0xDD0EA2, 0x1F28D4, 0xDF6DB3, 0xEDD7A7, 0x909954, 0x88BE41, 0x9DBCA7,
        0x1B3801, 0x91A0A0, 0xCFD4F6, 0x76CDD7, 0x6D318A, 0x22D03B, 0x7750D3, 0x553080
    };

    private static int palNum=0;

    private String id;          // unique ID for this palette
    private int[] entries;      // stored in native format
    private int[] rgbvalues;    // corresponding RGB values

    private ColorCodec codec;   // used to convert from/to native/RGB format
    private int endianness;
    private boolean direct;     // Mewster: I SUPPOSE it means a palette has been loaded from another file
    private int offset;

    private boolean modified;

/**
*
* Creates a new palette with given format and size.
*
**/

    public TMPalette(String id, int size, ColorCodec codec, int endianness) {
        this.id = id;
        this.codec = codec;
        this.endianness = endianness;
        this.direct = true;
        this.offset = 0;
        entries = new int[size];
        rgbvalues = new int[size];
        // set all entries to zero
        for (int i=0; i<size; i++) {
            setEntry(i, 0);
        }
        setModified(false);
    }

/**
*
* Creates a new palette with given format and entries.
*
**/

    public TMPalette(String id, int[] entries, ColorCodec codec, int endianness, boolean direct) {
        this.id = id;
        this.codec = codec;
        this.endianness = endianness;
        this.direct = direct;
        this.offset = 0;
        this.entries = new int[entries.length];
        rgbvalues = new int[entries.length];
        for (int i=0; i<entries.length; i++) {
            setEntry(i, entries[i]);
        }
        setModified(false);
    }

/**
*
* Creates a new palette with given format and entries.
*
**/

    public TMPalette(String id, byte[] bytes, int offset, int size, ColorCodec codec, int endianness, boolean direct, boolean modified) {
        this.id = id;
        this.codec = codec;
        this.endianness = endianness;
        this.offset = offset;
        this.direct = direct;
        this.entries = new int[size];
        rgbvalues = new int[size];
        codec.setEndianness(endianness);
        for (int i=0; i<size; i++) {
            setEntry(i, codec.fromBytes(bytes, offset + (i * codec.getBytesPerPixel())));
        }
        setModified(modified);
    }

/**
*
* Creates a new palette from an existing palette.
* The settings and entries are copied from the given palette.
* TODO: How to create unique ID??
*
**/

    public TMPalette(TMPalette palette) {
        id = "PAL" + (palNum++);    // TODO
        codec = palette.getCodec();
        endianness = palette.getEndianness();
        offset = palette.getOffset();
        direct = palette.isDirect();
        entries = new int[palette.getSize()];
        rgbvalues = new int[palette.getSize()];
        for (int i=0; i<palette.getSize(); i++) {
            entries[i] = palette.getEntry(i);
            rgbvalues[i] = palette.getEntryRGB(i);
        }
        setModified(false);
    }

/**
*
* Gets the file offset where this palette was loaded from (0 if it wasn't
* loaded from a file.)
*
**/

    public int getOffset() {
        return offset;
    }

/**
*
* Returns whether this palette is "direct".
*
**/

    public boolean isDirect() {
        return direct;
    }

/**
*
* Sets the endianness.
* This is needed when converting the array of entries from/to an
* array of bytes.
*
**/

    public void setEndianness(int endianness) {
        this.endianness = endianness;
        setModified(true);
    }

/**
*
* Gets the endianness.
*
**/

    public int getEndianness() {
        return endianness;
    }

/**
*
* Gets the ID for this palette.
*
**/

    public String getID() {
        return id;
    }

/**
*
* Sets a palette entry.
*
**/

    public void setEntry(int index, int value) {
        entries[index] = value;
        rgbvalues[index] = codec.decode(value);
        setModified(true);
    }

/**
*
* Sets a palette entry by RGB value.
*
**/

    public void setEntryRGB(int index, int rgb) {
        entries[index] = codec.encode(rgb);
        rgbvalues[index] = codec.decode(entries[index]);
        setModified(true);
    }

/**
*
* Gets a palette entry.
*
**/

    public int getEntry(int index) {
        if (index >= entries.length) {
            System.out.println("Entry doesn't exist");
            return 0;
        }
        return entries[index];
    }

/**
*
* Gets a palette entry's 24-bit RGB value.
*
**/

    public int getEntryRGB(int index) throws ArrayIndexOutOfBoundsException {
        int val = 0;
        try {
            val = rgbvalues[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            val = 0;
        }
        return val;
    }

/**
*
* Gets the palette entry as a byte array.
*
**/

    public byte[] getEntryBytes(int index) throws ArrayIndexOutOfBoundsException {
        byte[] bytes = new byte[codec.getBytesPerPixel()];
        codec.setEndianness(endianness);
        try {
            codec.toBytes(entries[index], bytes, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return bytes;
        }
        return bytes;
    }

/**
*
* Gets index of rgb value _relative_ to startIndex.
*
**/

    public int indexOf(int startIndex, int rgbval) {
        for (int i=0; i<entries.length-startIndex; i++) {
            if (getEntryRGB(startIndex+i) == rgbval) {
                return i;
            }
        }
        System.out.println("RGB value not in palette");
        return -1;
    }

/**
*
* Gets the array of palette entries.
*
**/

    public int[] getEntries() {
        return entries;
    }

/**
*
* Gets the size of the palette.
*
**/

    public int getSize() {
        return entries.length;
    }

/**
*
* Resizes the palette.
*
**/

    public void setSize(int size) {
        int[] tempEntries = new int[size];
        int[] tempRgbvalues = new int[size];
        if (size > entries.length) {
            // copy all old entries
            for (int i=0; i<entries.length; i++) {
                tempEntries[i] = entries[i];
                tempRgbvalues[i] = rgbvalues[i];
            }
            // clear remaining entries
            for (int i=entries.length; i<size; i++) {
                tempEntries[i] = codec.encode(0x000000);
                tempRgbvalues[i] = 0x000000;
            }
        }
        else {
            // copy as many old entries as possible
            for (int i=0; i<size; i++) {
                tempEntries[i] = entries[i];
                tempRgbvalues[i] = rgbvalues[i];
            }
        }
        entries = tempEntries;
        rgbvalues = tempRgbvalues;
        setModified(true);
    }

/**
*
* Sets the color codec. Converts entries to the new format.
*
**/

    public void setCodec(ColorCodec codec) {
        this.codec = codec;
    //  do conversion
        for (int i=0; i<entries.length; i++) {
            setEntry(i, codec.encode(rgbvalues[i]));
        }
        setModified(true);
    }

/**
*
* Gets the color codec for this palette.
*
**/

    public ColorCodec getCodec() {
        return codec;
    }

/**
*
* Returns the palette entry # whose RGB components match the given value the closest.
* startIndex..colorCount is the range of colors that are tried.
* PS: The returned index is RELATIVE to startIndex.
*
**/

    public int closestMatchingEntry(int startIndex, int colorCount, int argb) {
        int targetR = (argb & 0x00FF0000) >> 16;
        int targetG = (argb & 0x0000FF00) >> 8;
        int targetB = (argb & 0x000000FF);
        int bestEntry=0, bestDiff=1000000;
        for (int i=0; i<colorCount; i++) {
            int val = getEntryRGB(startIndex + i);
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

/**
*
* Convenience method for getting the actual RGB value of the closest matching
* entry, instead of the palette index.
*
**/

    public int closestMatchingEntryRGB(int startIndex, int colorCount, int argb) {
        return getEntryRGB(startIndex + closestMatchingEntry(startIndex, colorCount, argb));
    }

/**
*
* Returns the palette entries as a byte array.
*
**/

    public byte[] entriesToBytes() {
        byte[] bytes = new byte[entries.length * codec.getBytesPerPixel()];
        codec.setEndianness(endianness);
        for (int i=0; i<entries.length; i++) {
            codec.toBytes(entries[i], bytes, i * codec.getBytesPerPixel());
        }
        return bytes;
    }

/**
*
* Sets the modified state of this palette.
*
**/

    public void setModified(boolean modified) {
        this.modified = modified;
    }

/**
*
* Gets the modified state of this palette.
*
**/

    public boolean isModified() {
        return modified;
    }

}