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
* Listener for Sega Genesis / Master System / 32X (*.smd) files.
*
**/

public class SegaGenesisFileListener extends TMFileListener {

    private static final int[] sega_Mega_Drive_ = {
        0x53,0x45,0x47,0x41,0x20,0x4D,0x45,0x47,0x41,0x20,0x44,0x52,0x49,0x56,0x45,0x20
    };

    private static final int[] sega_Genesis____ = {
        0x53,0x45,0x47,0x41,0x20,0x47,0x45,0x4E,0x45,0x53,0x49,0x53,0x20,0x20,0x20,0x20
    } ;

    private static final int[] sega_32X________ = {
        0x53,0x45,0x47,0x41,0x20,0x33,0x32,0x58,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20
    } ;

    private static final int[][] console_IDs = {
        sega_Mega_Drive_,
        sega_Genesis____,
        sega_32X________
    };

/**
*
* Detect if this is a Sega Genesis / Master System / 32X file.
*
**/

    public boolean doFormatDetect(final byte[] data, String extension) {
        // verify extension
        if (!(extension.equals("smd")
            || extension.equals("md"))) {
            return false;
        }

        if (extension.equals("smd")) {
            int[] offsets = {
                0x2280, 0x0280, 0x2281, 0x0281, 0x2282, 0x0282, 0x2283, 0x0283,
                0x2284, 0x0284, 0x2285, 0x0285, 0x2286, 0x0286, 0x2287, 0x0287
            };
            boolean id_OK = false;
            for (int i=0; i<console_IDs.length; i++) {
                int[] id_String = console_IDs[i];
                int diff = 0;
                for (int j=0; j<id_String.length; j++) {
                    diff += Math.abs((data[offsets[j]] & 0xFF) - id_String[j]);
                }
                if (diff == 0) {
                    id_OK = true;
                    break;
                }
            }
            if (!id_OK) {
                return false;
            }
        }
        else {
            // md extension
        }

        // verify file size
        if (((data.length-512) % 16384) != 0) {
            return false;
        }

        // (data[8] & 0xFF) == 0xAA
        // (data[9] & 0xFF) == 0xBB
        // (data[10] & 0xFF) == 0x06

        return true;
    }

/**
*
* Deinterleaves the data on file load.
*
**/

    public void fileLoaded(byte[] data, String extension) {
        if (extension.equals("smd")) {
            byte[] block = new byte[16384];
            int blockCount = (data.length-512) / 16384;
            for (int i=0; i<blockCount; i++) {
                // decode a block
                int ofs = (i * 16384) + 512;
                // odd - 0..8191
                for (int j=1; j<16384; j+=2) {
                    block[j] = data[ofs++];
                }
                // even - 8192..16383
                for (int j=0; j<16384; j+=2) {
                    block[j] = data[ofs++];
                }
                // store the decoded block
                ofs = (i * 16384) + 512;
                for (int j=0; j<16384; j++) {
                    data[ofs+j] = block[j];
                }
            }
        }
        else {
            // md extension
        }
    }

/**
*
* Reinterleaves the data on file save.
*
**/

    public void fileSaving(byte[] data, String extension) {
        if (extension.equals("smd")) {
            // update checksum
            int checkSum = 0;
            for (int i=1024; i<data.length; i+=2) {
                checkSum += (data[i] & 0xFF) << 8;
                checkSum += data[i+1] & 0xFF;
            }
            data[0x38E] = (byte)((checkSum >> 8) & 0xFF);
            data[0x38F] = (byte)(checkSum & 0xFF);

            // reinterleave data
            byte[] block = new byte[16384];
            int blockCount = (data.length-512) / 16384;
            for (int i=0; i<blockCount; i++) {
                // encode a block
                // odd - 0..8191
                int ofs = (i * 16384) + 512 + 1;
                for (int j=0; j<8192; j++) {
                    block[j] = data[ofs];
                    ofs += 2;
                }
                // even - 8192..16383
                ofs = (i * 16384) + 512;
                for (int j=8192; j<16384; j++) {
                    block[j] = data[ofs];
                    ofs += 2;
                }
                // store the encoded block
                ofs = (i * 16384) + 512;
                for (int j=0; j<16384; j++) {
                    data[ofs+j] = block[j];
                }
            }
        }
        else {
            // md extension
        }
    }

}