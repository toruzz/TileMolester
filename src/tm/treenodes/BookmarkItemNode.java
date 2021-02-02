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

package tm.treenodes;

import tm.tilecodecs.TileCodec;

/**
*
* A file bookmark.
* Records all info required to restore the view presented to the user at the
* time the bookmark was added.
*
**/

public class BookmarkItemNode extends TMTreeNode {

    private int offset;         // file offset
    private int cols;
    private int rows;
    private int blockWidth;
    private int blockHeight;
    private boolean rowInterleaved;
    private boolean sizeBlockToCanvas;
    private int mode;           // MODE_1D or MODE_2D
    private TileCodec codec;
    private String description; // entered by the user to describe/name the bookmark
    private String paletteID;
    private int palIndex;

    public BookmarkItemNode(int offset,
                            int cols,
                            int rows,
                            int blockWidth,
                            int blockHeight,
                            boolean rowInterleaved,
                            boolean sizeBlockToCanvas,
                            int mode,
                            int palIndex,
                            TileCodec codec,
                            String description
                            ) {
        super();
        this.offset = offset;
        this.cols = cols;
        this.rows = rows;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.rowInterleaved = rowInterleaved;
        this.sizeBlockToCanvas = sizeBlockToCanvas;
        this.mode = mode;
        this.palIndex = palIndex;
        this.codec = codec;
        this.description = description;
    }

/**
*
* Gets the file offset that marks the beginning of the bookmark.
*
**/

    public int getOffset() {
        return offset;
    }

/**
*
* Gets the # of columns.
*
**/

    public int getCols() {
        return cols;
    }

/**
*
* Gets the # of rows.
*
**/

    public int getRows() {
        return rows;
    }

/**
*
* Gets the block width.
*
**/

    public int getBlockWidth() {
        return blockWidth;
    }

/**
*
* Gets the block height.
*
**/

    public int getBlockHeight() {
        return blockHeight;
    }

/**
*
*
*
**/

    public boolean getRowInterleaved() {
        return rowInterleaved;
    }

/**
*
*
*
**/

    public boolean getSizeBlockToCanvas() {
        return sizeBlockToCanvas;
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
* Gets the palette index.
*
**/

    public int getPalIndex() {
        return palIndex;
    }

/**
*
* Gets the codec.
*
**/

    public TileCodec getCodec() {
        return codec;
    }

/**
*
* Gets the bookmark description.
*
**/

    public String getDescription() {
        return description;
    }

/**
*
* Sets the bookmark description.
*
**/

    public void setDescription(String description) {
        this.description = description;
        setModified(true);
    }

/**
*
* Returns the XML-equivalent of this bookmark.
*
**/

    public String toXML() {
        StringBuffer s = new StringBuffer();
        s.append(getIndent());
        s.append("<bookmark");
        s.append(" offset=\"").append(offset).append("\"");
        s.append(" columns=\"").append(cols).append("\"");
        s.append(" rows=\"").append(rows).append("\"");
        s.append(" blockwidth=\"").append(blockWidth).append("\"");
        s.append(" blockheight=\"").append(blockHeight).append("\"");
        s.append(" rowinterleaved=\"").append(rowInterleaved).append("\"");
        s.append(" sizeblocktocanvas=\"").append(sizeBlockToCanvas).append("\"");
        if (mode == TileCodec.MODE_1D) {
            s.append(" mode=\"1D\"");
        }
        else {
            s.append(" mode=\"2D\"");
        }
        s.append(" palIndex=\"").append(palIndex).append("\"");
        s.append(" codec=\"").append(codec.getID()).append("\"");
        s.append(">\n");
        s.append(getIndent()).append("  ");
        s.append("<description>").append(description).append("</description>\n");
        s.append(getIndent());
        s.append("</bookmark>\n");
        return s.toString();
    }

/**
*
*
*
**/

    public String toString() {
        return description;
    }

/**
*
*
*
**/

    public void setText(String text) {
        this.description = text;
    }

/**
*
*
*
**/

    public String getToolTipText() {
        return "Offset: "+offset+" ... Columns: "+cols+" ... Rows: "+rows+" ... Codec: "+codec.getDescription();
    }

}