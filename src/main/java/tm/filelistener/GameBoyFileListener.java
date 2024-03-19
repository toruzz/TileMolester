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
* Listener for Game Boy (*.gb, *.gbc) files.
*
**/

public class GameBoyFileListener extends TMFileListener {

    private static final int[] scrollingNintendoGraphic = {
        0xCE,0xED,0x66,0x66,0xCC,0x0D,0x00,0x0B,0x03,0x73,0x00,0x83,0x00,0x0C,0x00,0x0D,
        0x00,0x08,0x11,0x1F,0x88,0x89,0x00,0x0E,0xDC,0xCC,0x6E,0xE6,0xDD,0xDD,0xD9,0x99,
        0xBB,0xBB,0x67,0x63,0x6E,0x0E,0xEC,0xCC,0xDD,0xDC,0x99,0x9F,0xBB,0xB9,0x33,0x3E
    };

    // cartridge types (TODO: add more types)
    private static final int ROM_ONLY = 0x00;
    private static final int ROM_MBC1 = 0x01;
    private static final int ROM_MBC1_RAM = 0x02;
    private static final int ROM_MBC1_RAM_BATTERY = 0x03;
    private static final int ROM_MBC2 = 0x05;
    private static final int ROM_MBC2_BATTERY = 0x06;
    private static final int ROM_RAM = 0x08;
    private static final int ROM_RAM_BATTERY = 0x09;
    private static final int ROM_HUC1_RAM_BATTERY = 0xFF;

    // cartridge sizes
    private static final int SIZE_256K = 0x00;
    private static final int SIZE_512K = 0x01;
    private static final int SIZE_1M = 0x02;
    private static final int SIZE_2M = 0x03;
    private static final int SIZE_4M = 0x04;
    private static final int SIZE_8M = 0x05;
    private static final int SIZE_16M = 0x06;
    private static final int SIZE_9M = 0x52;
    private static final int SIZE_10M = 0x53;
    private static final int SIZE_12M = 0x54;

/**
*
* Detects if this is a GameBoy file.
* The criteria is that the extension is either gb, gbc or sgb and that
* the scrolling Nintendo graphic data is correct.
*
**/

    public boolean doFormatDetect(final byte[] data, String extension) {
        // verify extension
        if (!(extension.equals("gb")
            || extension.equals("gbc")
            || extension.equals("sgb"))) {
            return false;
        }

        // verify scrolling Nintendo graphic
        for (int i=0; i<scrollingNintendoGraphic.length; i++) {
            if ((data[0x104+i] & 0xFF) != scrollingNintendoGraphic[i]) {
                return false;
            }
        }

        // verify cartridge type
/*        int ct = data[0x147] & 0xFF;
        switch (ct) {
            case ROM_ONLY:
            case ROM_MBC1:
            case ROM_MBC1_RAM:
            case ROM_MBC1_RAM_BATTERY:
            case ROM_MBC2:
            case ROM_MBC2_BATTERY:
            case ROM_RAM:
            case ROM_RAM_BATTERY:
            case ROM_HUC1_RAM_BATTERY:
            break;
            default:
            return false;
        }
*/
        // verify ROM size in header
        int rs = data[0x148] & 0xFF;
        switch (rs) {
            case SIZE_256K:
            case SIZE_512K:
            case SIZE_1M:
            case SIZE_2M:
            case SIZE_4M:
            case SIZE_8M:
            case SIZE_16M:
            case SIZE_9M:
            case SIZE_10M:
            case SIZE_12M:
            break;
            default:
            return false;
        }

        // TODO: verify actual file size

        return true;
    }

/**
*
* Does nothing on file load.
*
**/

    public void fileLoaded(byte[] data, String extension) {
    }

/**
*
* Recalculates the complement check and checksum on file save.
*
**/

    public void fileSaving(byte[] data, String extension) {
        // update complement check
        int r = 25;
        for(int i=0x134; i<0x14D; i++) {
            r += data[i] & 0xFF;
        }
        data[0x14D] = (byte)(0x100-r);

        // update checksum
        int checkSum = 0;
        data[0x14E] = 0;
        data[0x14F] = 0;
        int len = data.length & 0x0FFF8000;
        for (int i=0; i<len; i++) {
            checkSum += data[i] & 0xFF;
        }
        data[0x14E] = (byte)((checkSum >> 8) & 0xFF);
        data[0x14F] = (byte)(checkSum & 0xFF);
    }

}