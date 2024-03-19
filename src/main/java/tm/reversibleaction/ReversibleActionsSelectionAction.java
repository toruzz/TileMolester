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

import tm.treenodes.BookmarkItemNode;
import tm.canvases.TMTileCanvas;
import tm.canvases.TMEditorCanvas;


/**
*
* Allows undo/redo of an action that modifies the entire image/selection,
* such as flip and rotate.
*
**/

public class ReversibleActionsSelectionAction extends ReversibleAction {

    public static final int FLIP_ACTION = 1;
    public static final int MIRROR_ACTION = 2;
    public static final int ROTATELEFT_ACTION = 3;
    public static final int ROTATERIGHT_ACTION = 4;
    public static final int SHIFTLEFT_ACTION = 5;
    public static final int SHIFTRIGHT_ACTION = 6;
    public static final int SHIFTUP_ACTION = 7;
    public static final int SHIFTDOWN_ACTION = 8;

    private TMTileCanvas canvas;
    private int action;
    private BookmarkItemNode bookmark;

    public ReversibleActionsSelectionAction(String presentationName, TMTileCanvas canvas, int action) {
        super(presentationName);
        this.canvas = canvas;
        this.action = action;
        if (canvas instanceof TMEditorCanvas) {
            bookmark = ((TMEditorCanvas)canvas).getView().createBookmark("");
        }
    }

    // undo by performing the "inverse" action.
    public void undo() {
        if (canvas instanceof TMEditorCanvas) {
            ((TMEditorCanvas)canvas).getView().gotoBookmark(bookmark);
        }
        if (action == FLIP_ACTION) {
            canvas.flip();
        }
        else if (action == MIRROR_ACTION) {
            canvas.mirror();
        }
        else if (action == ROTATELEFT_ACTION) {
            canvas.rotateRight();
            if (canvas instanceof TMEditorCanvas) {
                bookmark = ((TMEditorCanvas)canvas).getView().createBookmark("");
            }
        }
        else if (action == ROTATERIGHT_ACTION) {
            canvas.rotateLeft();
            if (canvas instanceof TMEditorCanvas) {
                bookmark = ((TMEditorCanvas)canvas).getView().createBookmark("");
            }
        }
        else if (action == SHIFTLEFT_ACTION) {
            canvas.shiftRight();
        }
        else if (action == SHIFTRIGHT_ACTION) {
            canvas.shiftLeft();
        }
        else if (action == SHIFTUP_ACTION) {
            canvas.shiftDown();
        }
        else if (action == SHIFTDOWN_ACTION) {
            canvas.shiftUp();
        }
        canvas.redraw();
    }

    // redo by simply performing the original action again.
    public void redo() {
        if (canvas instanceof TMEditorCanvas) {
            ((TMEditorCanvas)canvas).getView().gotoBookmark(bookmark);
        }
        if (action == FLIP_ACTION) {
            canvas.flip();
        }
        else if (action == MIRROR_ACTION) {
            canvas.mirror();
        }
        else if (action == ROTATELEFT_ACTION) {
            canvas.rotateLeft();
            if (canvas instanceof TMEditorCanvas) {
                bookmark = ((TMEditorCanvas)canvas).getView().createBookmark("");
            }
        }
        else if (action == ROTATERIGHT_ACTION) {
            canvas.rotateRight();
            if (canvas instanceof TMEditorCanvas) {
                bookmark = ((TMEditorCanvas)canvas).getView().createBookmark("");
            }
        }
        else if (action == SHIFTLEFT_ACTION) {
            canvas.shiftLeft();
        }
        else if (action == SHIFTRIGHT_ACTION) {
            canvas.shiftRight();
        }
        else if (action == SHIFTUP_ACTION) {
            canvas.shiftUp();
        }
        else if (action == SHIFTDOWN_ACTION) {
            canvas.shiftDown();
        }
        canvas.redraw();
    }

    public boolean canUndo() { return true; }

    public boolean canRedo() { return true; }

}