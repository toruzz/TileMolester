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

package tm.canvases;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
*
* Provides a surface where decoded graphics data can be rendered.
* This is an abstract class: Subclasses are required to implement the
* unpackPixels() and packPixels() methods for decoding and encoding
* graphics data, respectively.
* Pixels must be decoded to 32-bit ARGB format (see implementation details below).
*
**/

public abstract class TMPixelCanvas extends JPanel {

    protected int canvasWidth;
    protected int canvasHeight;
    protected double scale;

    protected byte[] bits;  // encoded data buffer
    protected int offset;   // starting offset in buffer

    protected int[] pixels;
    protected MemoryImageSource source=null;
    private Image image;
    private DirectColorModel colorModel;

    private boolean showPixelGrid=false;

/**
*
* Creates a pixel pane using <code>bits</code> as the graphics source.
*
**/

    public TMPixelCanvas(byte[] bits) {
        super();
        this.bits = bits;
        setOffset(0);
        setLayout(null);
        colorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000);
        setBackground(Color.gray);
        setCanvasSize(0, 0);
        setScale(1.0);
    }

/**
*
* Creates a pixel pane of the specified size and using <code>bits</code> as the graphics source.
*
**/

    public TMPixelCanvas(byte[] bits, int canvasWidth, int canvasHeight) {
        super();
        this.bits = bits;
        setOffset(0);
        setLayout(null);
        colorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000);
        setBackground(Color.gray);
        setCanvasSize(canvasWidth, canvasHeight);
        setScale(1.0);
    }

/**
*
* Sets the size of the canvas in pixels.
*
**/

    public void setCanvasSize(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        // create canvas image and initialize stuff
        pixels = new int[canvasWidth*canvasHeight];
        source = new MemoryImageSource(canvasWidth, canvasHeight, colorModel, pixels, 0, canvasWidth);
        source.setAnimated(true);
        image = createImage(source);

        setScale(scale);
    }

/**
*
* Sets the offset into the buffer from which tile data is unpacked/packed.
*
**/

    public void setOffset(int offset) {
        this.offset = offset;
    }

/**
*
* Gets the offset.
*
**/

    public int getOffset() {
        return offset;
    }

/**
*
* Sets the scale.
*
**/

    public void setScale(double scale) {
        if (scale < 1.0) scale = 1.0;   // minimum
        else if (scale > 32.0) scale = 32.0;  // maximum
        this.scale = scale;

        // set size
        int scaledWidth = (int)(canvasWidth*scale);
        int scaledHeight = (int)(canvasHeight*scale);
        setSize(scaledWidth, scaledHeight);
    }

/**
*
* Gets the scale.
*
**/

    public double getScale() {
        return scale;
    }

/**
*
* Subclasses required to implement this method. It is responsible for
* filling the canvas with pixels, by decoding data starting at
* &bits[offset].
*
**/

    protected abstract void unpackPixels();

/**
*
* Subclasses required to implement this method. It is responsible for
* encoding the canvas's pixels into some format.
*
**/

    protected abstract void packPixels();

/**
*
* Paints pixels and grid(s).
*
**/

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        // draw gridlines if necessary
        if (showPixelGrid) {
            drawPixelGrid(g);
        }
    }

/**
*
* Draws pixel grid.
*
**/

    private void drawPixelGrid(Graphics g) {
        if (scale < 8.0) return;    // don't show it for scales less than 8
        g.setColor(Color.gray);
        // draw horizontal lines
        for (int i=1; i<canvasHeight; i++) {
            g.fillRect(0, (int)(i*scale), getWidth(), 1);
        }
        // draw vertical lines
        for (int i=1; i<canvasWidth; i++) {
            g.fillRect((int)(i*scale), 0, 1, getHeight());
        }
    }

/**
*
* Turns the pixel grid on (true) or off (false).
*
**/

    public void setPixelGridVisible(boolean showPixelGrid) {
        this.showPixelGrid = showPixelGrid;
    }

/**
*
* Returns pixel grid visibility.
*
**/

    public boolean isPixelGridVisible() {
        return showPixelGrid;
    }

/**
*
* Sets the pixel at location (x, y) in the image to the specified value.
*
**/

    public void setPixel(int x, int y, int argb) {
        pixels[(y*canvasWidth)+x] = argb;
    }

/**
*
* Gets the pixel at location (x, y) in the image.
*
**/

    protected int getPixel(int x, int y) {
        return pixels[(y*canvasWidth)+x];
    }

/**
*
* Inverts the pixel at (x,y).
*
**/

    protected void xorPixel(int x, int y) {
        setPixel(x, y, getPixel(x, y) ^ 0xFFFFFF);
    }

/**
*
* Gets the buffer containing the currently rendered pixels.
*
**/

    protected int[] getPixels() {
        return pixels;
    }

/**
*
* Gets the width of the canvas image.
*
**/

    public int getCanvasWidth() {
        return canvasWidth;
    }

/**
*
* Gets the height of the canvas image.
*
**/

    public int getCanvasHeight() {
        return canvasHeight;
    }

/**
*
* Sets the data buffer that will be used to fetch/encode native graphics data.
*
**/

    public void setBits(byte[] bits) {
        this.bits = bits;
    }

/**
*
* Gets the native graphics data buffer.
*
**/

    public byte[] getBits() {
        return bits;
    }

/**
*
* Updates the pixels and repaints the canvas.
*
**/

    public void redraw() {
        source.newPixels();
        repaint();
    }

/**
*
* Gets the image where the pixels are rendered.
*
**/

    public Image getImage() {
        return image;
    }

/**
*
* Mirrors the canvas (flips horizontally).
*
**/

    public void mirror() {
        for (int y=0; y<canvasHeight; y++) {
            for (int x=0; x<canvasWidth/2; x++) {
                int p1 = getPixel(x, y);
                int p2 = getPixel(canvasWidth-1-x, y);
                setPixel(x, y, p2);
                setPixel(canvasWidth-1-x, y, p1);
            }
        }
        packPixels();
    }

/**
*
* Flips the canvas (vertically).
*
**/

    public void flip() {
        for (int y=0; y<canvasHeight/2; y++) {
            for (int x=0; x<canvasWidth; x++) {
                int p1 = getPixel(x, y);
                int p2 = getPixel(x, canvasHeight-1-y);
                setPixel(x, y, p2);
                setPixel(x, canvasHeight-1-y, p1);
            }
        }
        packPixels();
    }

/**
*
* Rotates the canvas 90 degrees left (counter-clockwise).
*
**/

    public void rotateLeft() {
        int[] pix = getPixels();
        setCanvasSize(canvasHeight, canvasWidth);
        int ofs = 0;
        for (int i=0; i<canvasWidth; i++) {
            for (int j=canvasHeight-1; j>=0; j--) {
                setPixel(i, j, pix[ofs++]);
            }
        }
        packPixels();
        setScale(scale);
    }

/**
*
* Rotates the canvas 90 degrees right (clockwise).
*
**/

    public void rotateRight() {
        int[] pix = getPixels();
        setCanvasSize(canvasHeight, canvasWidth);
        int ofs = 0;
        for (int i=canvasWidth-1; i>=0; i--) {
            for (int j=0; j<canvasHeight; j++) {
                setPixel(i, j, pix[ofs++]);
            }
        }
        packPixels();
        setScale(scale);
    }

/**
*
* Shifts the canvas one column left.
*
**/

    public void shiftLeft() {
        for (int y=0; y<canvasHeight; y++) {
            int p0 = getPixel(0, y);
            for (int x=0; x<canvasWidth-1; x++) {
                setPixel(x, y, getPixel(x+1, y));
            }
            setPixel(canvasWidth-1, y, p0);
        }
        packPixels();
    }

/**
*
* Shifts the canvas one column right.
*
**/

    public void shiftRight() {
        for (int y=0; y<canvasHeight; y++) {
            int p0 = getPixel(canvasWidth-1, y);
            for (int x=canvasWidth-2; x>=0; x--) {
                setPixel(x+1, y, getPixel(x, y));
            }
            setPixel(0, y, p0);
        }
        packPixels();
    }

/**
*
* Shifts the canvas one row up.
*
**/

    public void shiftUp() {
        for (int x=0; x<canvasWidth; x++) {
            int p0 = getPixel(x, 0);
            for (int y=0; y<canvasHeight-1; y++) {
                    setPixel(x, y, getPixel(x, y+1));
            }
            setPixel(x, canvasHeight-1, p0);
        }
        packPixels();
    }

/**
*
* Shifts the canvas one row down.
*
**/

    public void shiftDown() {
        for (int x=0; x<canvasWidth; x++) {
            int p0 = getPixel(x, canvasHeight-1);
            for (int y=canvasHeight-2; y>=0; y--) {
                setPixel(x, y+1, getPixel(x, y));
            }
            setPixel(x, 0, p0);
        }
        packPixels();
    }

}