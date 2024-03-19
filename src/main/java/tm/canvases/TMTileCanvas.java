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

import tm.TMPalette;
import tm.tilecodecs.TileCodec;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
*
* A tile canvas is a surface where a grid of atomic tiles can be rendered.
*
**/

public class TMTileCanvas extends TMPixelCanvas {

    protected int mode=TileCodec.MODE_1D;

    protected int[] pixdata = new int[8*8];

    protected int cols=0;
    protected int rows=0;

    protected TileCodec codec=null;
    protected TMPalette palette=null;
    protected int palIndex=0;

    private boolean showTileGrid=false;

/**
*
* Creates a tile pane, with <code>bits</code> as the encoded tile data source.
*
**/

    public TMTileCanvas(byte[] bits) {
        super(bits);
        this.cols = 0;
        this.rows = 0;
    }

/**
*
* Creates a tile pane of the specified size, and with <code>bits</code>
* as the encoded tile data source.
* Doesn't work quite right?
**/

    public TMTileCanvas(byte[] bits, int cols, int rows) {
        super(bits, cols*8, rows*8);
        this.cols = cols;
        this.rows = rows;
    }

/**
*
* Sets the tile mode. This determines how tile data is interpreted.
* Valid modes are MODE_1D and MODE_2D.
*
**/

    public void setMode(int mode) {
        this.mode = mode;
    }

/**
*
* Gets the mode.
*
**/

    public int getMode() {
        return mode;
    }

/**
*
* Sets the codec that's used for decoding+encoding individual tiles.
*
**/

    public void setCodec(TileCodec codec) {
        this.codec = codec;
    }

/**
*
* Gets the tile codec.
*
**/

    public TileCodec getCodec() {
        return codec;
    }

/**
*
* Sets the palette that's used for mapping colors when bitsPerPixel <= 8.
*
**/

    public void setPalette(TMPalette palette) {
        this.palette = palette;
    }

/**
*
* Gets the palette.
*
**/

    public TMPalette getPalette() {
        return palette;
    }

/**
*
* Sets the palette index.
*
**/

    public void setPalIndex(int palIndex) {
        this.palIndex = palIndex;
    }

/**
*
* Gets the palette index.
*
**/

    public int getPalIndex() {
        return palIndex;
    }

/**
*
* Sets the size of the tile canvas.
*
**/

    public void setGridSize(int cols, int rows) {
        setCanvasSize(cols*8, rows*8);
        this.cols = cols;
        this.rows = rows;
    }

/**
*
* Encodes the specified tile. TODO
*
**/

    public void packTile(int x, int y) {
        int pixOfs;
        int bitsOfs, pos;
        // encode single atomic tile
        bitsOfs = getTileBitsOffset(x, y);
        if (bitsOfs >= 0) {
            // copy pixels
            pixOfs = (y * 8 * canvasWidth) + (x * 8);
            pos = 0;
            if (codec.getBitsPerPixel() <= 8) {
                int colorCount = codec.getColorCount();
                int colorIndex = palIndex * colorCount;
                // map RGB values to palette indices
                for (int p=0; p<8; p++) {
                    for (int q=0; q<8; q++) {
                        pixdata[pos++] = palette.indexOf(colorIndex, pixels[pixOfs++]);
                    }
                    pixOfs += canvasWidth - 8;
                }
            }
            else {
                // non-palettized: color is actual 32-bit ARGB value
                for (int p=0; p<8; p++) {
                    for (int q=0; q<8; q++) {
                        pixdata[pos++] = pixels[pixOfs++];
                    }
                    pixOfs += canvasWidth - 8;
                }
            }
            codec.encode(pixdata, bits, bitsOfs, getStride());
        }
        else {
            // not valid tile, do nothing
        }
    }

/**
*
* Gets the number of columns in the tile grid.
*
**/

    public int getCols() {
        return cols;
    }

/**
*
* Gets the number of rows in the tile grid.
*
**/

    public int getRows() {
        return rows;
    }

/**
*
* Decodes tile data to pixel buffer.
*
**/

    public void unpackPixels() {
        if (codec == null) return;
        int[] decodedTile;
        int pixOfs = 0;
        int bitsOfs, tileOfs, pos;
        int colorIndex = palIndex * codec.getColorCount(); // only valid for palettized codecs
        int bpp = codec.getBitsPerPixel();
        int stride = getStride();
        // render grid of atomic tiles
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                bitsOfs = getTileBitsOffset(j, i);
                if (bitsOfs >= 0) {
                    decodedTile = codec.decode(bits, bitsOfs, stride);
                    // copy pixels
                    tileOfs = pixOfs;
                    pos = 0;
                    if ((bpp <= 8) && (palette != null)) {
                        // map palette indices to RGB values
                        for (int p=0; p<8; p++) {
                            for (int q=0; q<8; q++) {
                                pixels[tileOfs++] = palette.getEntryRGB(colorIndex + decodedTile[pos++]);
                            }
                            tileOfs += canvasWidth - 8;
                        }
                    }
                    else {
                        // non-palettized: color is actual 32-bit ARGB value
                        for (int p=0; p<8; p++) {
                            for (int q=0; q<8; q++) {
                                pixels[tileOfs++] = decodedTile[pos++];
                            }
                            tileOfs += canvasWidth - 8;
                        }
                    }
                }
                else {
                    // not valid tile, fill with gray pixels
                    tileOfs = pixOfs;
                    for (int p=0; p<8; p++) {
                        for (int q=0; q<8; q++) {
                            pixels[tileOfs++] = 0x7F7F7F;
                        }
                        tileOfs += canvasWidth - 8;
                    }
                }
                pixOfs += 8;    // next tile column
            }
            pixOfs += canvasWidth*7;    // next tile row
        }
        source.newPixels();
    }

/**
*
* Encodes tile data.
*
**/

    public void packPixels() {
        if (codec == null) return;
        int pixOfs = 0;
        int bitsOfs, tileOfs, pos;
        int colorCount = codec.getColorCount();    // only valid for palettized codecs
        int colorIndex = palIndex * colorCount;
        int bpp = codec.getBitsPerPixel();
        int stride = getStride();
        // encode grid of atomic tiles
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                bitsOfs = getTileBitsOffset(j, i);
                if (bitsOfs >= 0) {
                    // copy pixels
                    tileOfs = pixOfs;
                    pos = 0;
                    if ((bpp <= 8) && (palette != null)) {
                        // map RGB values to palette indices
                        for (int p=0; p<8; p++) {
                            for (int q=0; q<8; q++) {
                                pixdata[pos++] = palette.indexOf(colorIndex, pixels[tileOfs++]);
                            }
                            tileOfs += canvasWidth - 8;
                        }
                    }
                    else {
                        // non-palettized: color is actual 32-bit ARGB value
                        for (int p=0; p<8; p++) {
                            for (int q=0; q<8; q++) {
                                pixdata[pos++] = pixels[tileOfs++];
                            }
                            tileOfs += canvasWidth - 8;
                        }
                    }
                    codec.encode(pixdata, bits, bitsOfs, stride);
                }
                else {
                    // not valid tile, do nothing
                }
                pixOfs += 8;    // next tile column
            }
            pixOfs += canvasWidth*7;    // next tile row
        }
    }

/**
*
* Paints tiles and grid.
*
**/

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw gridlines if necessary
        if (showTileGrid) {
            drawTileGrid(g);
        }
    }

/**
*
* Draws atomic tile grid.
*
**/

    private void drawTileGrid(Graphics g) {
        g.setColor(Color.red);     // gridline color
        // draw horizontal lines
        for (int i=1; i<rows; i++) {
            g.fillRect(0, (int)(i*scale*8), getWidth(), 1);
        }
        // draw vertical lines
        for (int i=1; i<cols; i++) {
            g.fillRect((int)(i*scale*8), 0, 1, getHeight());
        }
    }

/**
*
* Gets the length of a side (in pixels) in an 8x8 tile, subject to the current scaling.
*
**/

    public int getScaledTileDim() {
        return (int)(scale*8);
    }

/**
*
* Turns the tile grid on (true) or off (false).
*
**/

    public void setTileGridVisible(boolean showTileGrid) {
        this.showTileGrid = showTileGrid;
    }

/**
*
* Returns tile grid visibility.
*
**/

    public boolean isTileGridVisible() {
        return showTileGrid;
    }

/**
*
* Gets the starting offset of the data for the tile at position (x,y)
* in the grid.
*
**/

    protected int getTileBitsOffset(int x, int y) {
        int relOfs = (y * getRowSize()) + (x * getTileIncrement());
        int absOfs = relOfs + offset;
        // range check
        int limit = 0;
        if (mode == TileCodec.MODE_1D) {
            limit = bits.length - codec.getTileSize();
        }
        else {
            limit = bits.length - getRowIncrement();
        }
        if (absOfs <= limit) return absOfs;
        return -1;
    }

/**
*
* Gets the size of one row of tiles.
*
**/

    public int getRowSize() {
        if (codec != null)
            return cols * codec.getTileSize();
        else
            return 0;
    }

/**
*
* Gets the size of one page of tiles (entire tile grid).
*
**/

    public int getPageSize() {
        if (codec != null)
            return rows * getRowSize();
        else
            return 0;
    }

/**
*
* Gets the amount to increment data offset per tile.
*
**/

    public int getTileIncrement() {
        return (mode == TileCodec.MODE_1D) ? codec.getTileSize() : codec.getBytesPerRow();
    }

/**
*
* Gets the amount to increment data offset per row.
*
**/

    public int getRowIncrement() {
        return codec.getTileSize() * cols;
    }

/**
*
* Gets the amount to increment data offset per page.
*
**/

    public int getPageIncrement() {
        return getRowIncrement() * rows;
    }

/**
*
* Gets the stride.
*
**/

    public int getStride() {
        return (mode == TileCodec.MODE_1D) ? 0 : cols-1;
    }

/**
*
* Stretches the canvas from current (cols,rows) to specified (cols,rows).
*
**/

    public void stretchTo(int cols, int rows) {
        // get current pixels & dimensions
        int[] pix = getPixels();
        int oldW = getCanvasWidth();
        int oldH = getCanvasHeight();

        // set new size
        setGridSize(cols, rows);
        int w = getCanvasWidth();
        int h = getCanvasHeight();

        // create buffer to hold tile data
        byte[] selbits = new byte[cols * rows * codec.getTileSize()];
        setBits(selbits);

        // calculate step size for each dimension
        double stepX = (double)oldW / (double)w;
        double stepY = (double)oldH / (double)h;

        // stretch the old image onto the new canvas
        double x;
        double y=0;
        for (int i=0; i<h; i++) {
            x=0;
            for (int j=0; j<w; j++) {
                int p = pix[(((int)y)*oldW)+(int)x];
                setPixel(j, i, p);
                x += stepX;
            }
            y += stepY;
        }
        packPixels();
    }

/**
*
* Rotates the grid of tiles 90 degrees left (counter-clockwise).
*
**/

    public void rotateLeft() {
        int[] pix = getPixels();
        setGridSize(rows, cols);
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
* Rotates the grid of tiles 90 degrees right (clockwise).
*
**/

    public void rotateRight() {
        int[] pix = getPixels();
        setGridSize(rows, cols);
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
* Copies a tile.
*
**/

    public void copyTile(byte[] from, byte[] to, int offsetFrom, int offsetTo, int strideFrom, int strideTo, int bytesPerRow) {
        for (int i=0; i<8; i++) {
            // copy one row
            for (int j=0; j<bytesPerRow; j++) {
                to[offsetTo++] = from[offsetFrom++];
            }
            offsetFrom += strideFrom;
            offsetTo += strideTo;
        }
    }

}