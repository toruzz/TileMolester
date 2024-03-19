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

import tm.canvases.TMEditorCanvas;
import tm.canvases.TMSelectionCanvas;

/**
*
* Allows undo/redo of making a new selection.
*
**/

public class ReversibleNewSelectionAction extends ReversibleAction {
    private TMSelectionCanvas newSelection;
    private TMEditorCanvas owner;
    private int newX, newY;

    public ReversibleNewSelectionAction(TMSelectionCanvas newSelection, TMEditorCanvas owner) {
        super("New_Selection");   // i18n
        this.newSelection = newSelection;
        this.owner = owner;
        newX = newSelection.getX() / newSelection.getScaledTileDim();
        newY = newSelection.getY() / newSelection.getScaledTileDim();
    }

    public void undo() {
        owner.encodeSelection(false);    // newSelection
        owner.repaint();
    }

    public void redo() {
        owner.makeSelection(newSelection);
        owner.repaint();
    }

    public boolean canUndo() { return true; }
    public boolean canRedo() { return true; }

}