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

package tm.reversibleaction;

import tm.canvases.TMSelectionCanvas;

/**
*
* Allows undo/redo of selection stretching.
*
**/

public class ReversibleStretchAction extends ReversibleAction {

    private TMSelectionCanvas canvas;
    private int oldCols;
    private int oldRows;
    private int newCols;
    private int newRows;
    private byte[] oldBits;

    public ReversibleStretchAction(TMSelectionCanvas canvas, int newCols, int newRows) {
        super("Stretch Selection"); // i18n
        this.canvas = canvas;
        oldCols = canvas.getCols();
        oldRows = canvas.getRows();
        oldBits = canvas.getBits();
        this.newCols = newCols;
        this.newRows = newRows;
    }

    public void undo() {
        canvas.setGridSize(oldCols, oldRows);
        canvas.setBits(oldBits);
        canvas.unpackPixels();
        canvas.redraw();
    }

    public void redo() {
        canvas.stretchTo(newCols, newRows);
        canvas.redraw();
    }

    public boolean canUndo() { return true; }
    public boolean canRedo() { return true; }

}