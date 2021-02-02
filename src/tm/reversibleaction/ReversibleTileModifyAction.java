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

import java.awt.Point;
import tm.treenodes.BookmarkItemNode;
import tm.canvases.TMEditorCanvas;

/**
*
* Allows undo/redo of drawing operations.
* Records the view settings when the edit was done,
* the grid coordinates of the tiles that were modified,
* the old pixels, and the new pixels.
*
**/

public class ReversibleTileModifyAction extends ReversibleAction {

    TMEditorCanvas canvas;
    BookmarkItemNode bookmark;
    Point[] gridCoords;
    int[] oldPixels;
    int[] newPixels;

    public ReversibleTileModifyAction(
        String presentationName,
        TMEditorCanvas canvas,
        BookmarkItemNode bookmark,
        Point[] gridCoords,
        int[] oldPixels,
        int[] newPixels) {

        super(presentationName);
        this.canvas = canvas;
        this.bookmark = bookmark;
        this.gridCoords = gridCoords;
        this.oldPixels = oldPixels;
        this.newPixels = newPixels;
    }

    public void undo() {
        canvas.getView().gotoBookmark(bookmark);
        for (int i=0; i<gridCoords.length; i++) {
            Point p = gridCoords[i];
            canvas.copyBufferToTilePixels(p.x, p.y, oldPixels, i * 8*8);
            canvas.packTile(p.x, p.y);
        }
        canvas.redraw();
    }

    public void redo() {
        canvas.getView().gotoBookmark(bookmark);
        for (int i=0; i<gridCoords.length; i++) {
            Point p = gridCoords[i];
            canvas.copyBufferToTilePixels(p.x, p.y, newPixels, i * 8*8);
            canvas.packTile(p.x, p.y);
        }
        canvas.redraw();
    }

    public boolean canUndo() {
        return true;
    }

    public boolean canRedo() {
        return true;
    }

}