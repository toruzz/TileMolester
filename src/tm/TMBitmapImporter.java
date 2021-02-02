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

import tm.tilecodecs.*;
import tm.canvases.TMTileCanvas;
import tm.osbaldeston.image.BMP;
import tm.gfxlibs.*;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
*
* Allows a bitmap to be loaded and converted to a tile canvas.
*
**/

public class TMBitmapImporter {

/**
*
* Static method that takes a file, tries to load the file as a bitmap and
* convert it to a tile canvas. The file extension is used to determine what
* bitmap format to use when loading the file.
*
**/

    public static TMTileCanvas loadTileCanvasFromFile(File file)
        throws Exception {
        Image img = null;
        String ext = getExtension(file);
        // use proper decoder based on file extension
        if (ext.equals("bmp")) {
            // use BMP decoder
            BMP bmp = new BMP(file);
            img = bmp.getImage();
        }
        else if (ext.equals("pcx")) {
            // use PCX decoder
            try {
                img = PCXReader.loadImage(new FileInputStream(file));
            }
            catch (Exception e) {
                throw e;
            }
        }
        else {
            // use Java's decoders
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            img = icon.getImage();
        }
        if (img == null) {
            throw new Exception();    // couldn't load the image
        }

        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int[] pixels = new int[w * h];

        // grab the pixels
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw e;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            throw new Exception();
        }

        DirectColorTileCodec codec = new DirectColorTileCodec("", 32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000, "");
        // copy to a new canvas
        int cols = w / 8;
        int rows = h / 8;
        byte[] bits = new byte[cols * rows * codec.getTileSize()];
        TMTileCanvas tc = new TMTileCanvas(bits);
        tc.setGridSize(cols, rows);
        tc.setPalette(null);
        tc.setMode(TileCodec.MODE_1D);
        tc.setCodec(codec);
        int w2 = tc.getCanvasWidth();
        int h2 = tc.getCanvasHeight();
        for (int i=0; i<h2; i++) {
            for (int j=0; j<w2; j++) {
                tc.setPixel(j, i, pixels[(i * w) + j]);
            }
        }

        tc.packPixels();

        return tc;
    }

/**
*
* Gets file extension.
*
**/

    public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}