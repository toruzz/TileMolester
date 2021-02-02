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
* Allows undo/redo of moving a selection.
*
**/

public class ReversibleMoveSelectionAction extends ReversibleAction {

    private TMSelectionCanvas selection;
    private int oldX, oldY;
    private int newX, newY;

    public ReversibleMoveSelectionAction(TMSelectionCanvas selection, int oldX, int oldY, int newX, int newY) {
        super("Move Selection");   // i18n
        this.selection = selection;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    public void undo() {
        int dim = selection.getScaledTileDim();
        selection.setLocation(oldX * dim, oldY * dim);
    }

    public void redo() {
        int dim = selection.getScaledTileDim();
        selection.setLocation(newX * dim, newY * dim);

    }

    public boolean canUndo() { return true; }
    public boolean canRedo() { return true; }

}