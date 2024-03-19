/**
*
* @author Kent Hansen
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
**/

package tm.gfxlibs;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;

public class PCXEncoder {

    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public static byte[] encode(Image image) {
        // get width and height of image
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        // grab the pixels
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return null;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return null;
        }

        // prepare the byte stream
        baos.reset();

        // write the header
        emitByte(10);   // Manufacturer = ZSoft
        emitByte(5);    // Version = 3.0+ (supports 24-bit PCX)
        emitByte(1);    // Encoding = RLE
        emitByte(8);    // BitsPerPixel (per plane)
        emitInt(0);     // Xmin
        emitInt(0);     // Ymin
        emitInt(w-1);   // Xmax
        emitInt(h-1);   // Ymax
        emitInt(0);     // HDpi (not used)
        emitInt(0);     // VDpi (not used)
        emitZeroes(48); // Colormap (not used)
        emitZeroes(1);  // Reserved
        emitByte(3);    // NPlanes
        emitInt(w);     // BytesPerLine
        emitInt(0);     // PaletteInfo (not used)
        emitInt(w);     // HScreenSize (unsure what this is for)
        emitInt(h);     // VScreenSize (unsure what this is for)
        emitZeroes(54); // Filler

        // encode the pixels
        int ofs=0;
        int[] plane = new int[w];
        for (int i=0; i<h; i++) {
            // red plane
            for (int j=0; j<w; j++) {
                plane[j] = getRed(pixels[ofs+j]);
            }
            encodePlane(plane);
            // green plane
            for (int j=0; j<w; j++) {
                plane[j] = getGreen(pixels[ofs+j]);
            }
            encodePlane(plane);
            // blue plane
            for (int j=0; j<w; j++) {
                plane[j] = getBlue(pixels[ofs+j]);
            }
            encodePlane(plane);
            ofs += w;
        }

        return baos.toByteArray();
    }

    private static void emitByte(int b) {
        baos.write(b);
    }

    private static void emitInt(int i) {
        baos.write(i & 0xFF);
        baos.write((i >> 8) & 0xFF);
    }

    private static void emitZeroes(int c) {
        for (int i=0; i<c; i++) {
            emitByte(0x00);
        }
    }

    private static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    private static void encodePlane(int[] plane) {
        int i=0;
        while (i < plane.length) {
            int v = plane[i];
            if ((i != plane.length-1) && (v == plane[i+1])) {
                // run-length encode
                int c = 1;
                while ((c < 0x40) && (i+c < plane.length) && (v == plane[i+c])) {
                    c++;
                }
                i += c;
                emitByte(c | 0xC0);
                emitByte(v);
            }
            else {
                // regular encode
                if ((v & 0xC0) == 0xC0) {
                    emitByte(0xC1);
                }
                emitByte(v);
                i++;
            }
        }
    }

}