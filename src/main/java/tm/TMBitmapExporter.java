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

import tm.gfxlibs.*;
import tm.canvases.TMTileCanvas;
import tm.osbaldeston.image.BMP;
import javax.imageio.*;
import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
*
* Allows a tile canvas to be saved to a bitmap.
*
**/

public class TMBitmapExporter {

/**
*
* Static method that takes a tile canvas and a file and saves the tiles as a
* bitmap. The bitmap format is determined by the extension of the file.
*
**/

    public static void saveTileCanvasToFile(TMTileCanvas canvas, File file)
        throws Exception {
        Image img = canvas.getImage();
        String ext = getExtension(file);
        // use proper encoder based on file extension
        if (ext.equals("bmp")) {
            BMP bmp = new BMP(img);
            bmp.write(file);
        }
        else if (ext.equals("gif")) {
            try {
                FileOutputStream fis = new FileOutputStream(file);
                GIFOutputStream.writeGIF(fis, img, GIFOutputStream.STANDARD_256_COLORS);
                fis.close();
            }
            catch (Exception e) {
               throw e;
            }
        }
        else if (ext.equals("jpg")) {
            ImageWriter jpegEncoder = (ImageWriter)ImageIO.getImageWritersByFormatName("jpeg").next();
            if (jpegEncoder != null) {
                try {
                    FileImageOutputStream fios = new FileImageOutputStream(file);
                    jpegEncoder.setOutput(fios);
                    jpegEncoder.write(convertToRenderedImage(img));
                    fios.close();
                }
                catch (Exception e) {
                    throw e;
                }
            }
        }
        else if (ext.equals("png")) {
            try {
                byte[] pngBytes = new PngEncoder(img).pngEncode();
                FileOutputStream fis = new FileOutputStream(file);
                fis.write(pngBytes);
                fis.close();
            }
            catch (Exception e) {
                throw e;
            }
        }
        else if (ext.equals("pcx")) {
            try {
                byte[] pcxBytes = PCXEncoder.encode(img);
                FileOutputStream fis = new FileOutputStream(file);
                fis.write(pcxBytes);
                fis.close();
            }
            catch (Exception e) {
                throw e;
            }
        }
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

/**
*
* Converts an Image to a RenderedImage so that it can be fed to the JPEG encoder.
*
**/

    public static RenderedImage convertToRenderedImage(Image img) {
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(img, 0,0,img.getWidth(null), img.getHeight(null), null);
        return bi;
    }

}