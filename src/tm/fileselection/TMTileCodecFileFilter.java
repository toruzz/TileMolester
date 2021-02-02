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

public class TMTileCodecFileFilter extends TMFileFilter {

    private int defaultMode;
    private String codecID;

/**
*
*
*
**/

    public TMTileCodecFileFilter(String extlist, String description, String codecID, int defaultMode) {
        super(extlist, description);
        setCodecID(codecID);
        setDefaultMode(defaultMode);
    }

/**
*
*
*
**/

    public void setCodecID(String codecID) {
        this.codecID = codecID;
    }

/**
*
*
*
**/

    public void setDefaultMode(int defaultMode) {
        this.defaultMode = defaultMode;
    }

/**
*
*
*
**/

    public int getDefaultMode() {
        return defaultMode;
    }

/**
*
*
*
**/

    public String getCodecID() {
        return codecID;
    }

}