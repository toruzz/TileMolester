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

package tm.ui;

import tm.fileselection.TMFileFilter;

/**
*
* File filters for the open+save bitmap dialog.
*
**/

public class TMBitmapFilters {

    public final BMPFilter bmp = new BMPFilter();
    public final JPEGFilter jpeg = new JPEGFilter();
    public final GIFFilter gif = new GIFFilter();
    public final PNGFilter png = new PNGFilter();
    public final PCXFilter pcx = new PCXFilter();
    public final TIFFFilter tiff = new TIFFFilter();
    public final SupportedFilter supported = new SupportedFilter();

/**
*
* Recognizes *.bmp files.
*
**/

    private class BMPFilter extends TMFileFilter {

        public BMPFilter() {
            super("bmp", "Windows Bitmap (*.bmp)");
        }

    }

/**
*
* Recognizes *.pcx files.
*
**/

    private class PCXFilter extends TMFileFilter {

        public PCXFilter() {
            super("pcx", "ZSoft Paintbrush (*.pcx)");
        }

    }

/**
*
* Recognizes *.jpg files.
*
**/

    private class JPEGFilter extends TMFileFilter {

        public JPEGFilter() {
            super("jpg,jpeg", "JPEG (*.jpg, *.jpeg)");
        }

    }

/**
*
* Recognizes *.gif files.
*
**/

    private class GIFFilter extends TMFileFilter {

        public GIFFilter() {
            super("gif", "CompuServe Graphics Interchange (*.gif)");
        }

    }

/**
*
* Recognizes *.tiff files.
*
**/

    private class TIFFFilter extends TMFileFilter {

        public TIFFFilter() {
            super("tiff", "TIFF (*.tiff)");
        }

    }

/**
*
* Recognizes *.png files.
*
**/

    private class PNGFilter extends TMFileFilter {

        public PNGFilter() {
            super("png", "Portable Network Graphics (*.png)");
        }

    }

/**
*
* Recognizes all the above file formats.
*
**/

    private class SupportedFilter extends TMFileFilter {

        public SupportedFilter() {
            super("bmp,pcx,gif,jpg,jpeg,png", "All Supported Formats");
        }

    }

}