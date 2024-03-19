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

import tm.TMPalette;
import tm.ui.TMView;

/**
*
* Allows undo/redo of a palette entry modification.
*
**/

public class ReversiblePaletteEditAction extends ReversibleAction {

    private int colorIndex;
    private int oldColor;
    private int newColor;
    private TMPalette palette;

    public ReversiblePaletteEditAction(TMView view, TMPalette palette, int colorIndex, int oldColor, int newColor) {
        super("Edit Palette");  // i18n
        this.palette = palette;
        this.colorIndex = colorIndex;
        this.oldColor = oldColor;
        this.newColor = newColor;
    }

    public void undo() {
        palette.setEntryRGB(colorIndex, oldColor);
        // paletteChanged()
    }

    public void redo() {
        palette.setEntryRGB(colorIndex, newColor);
        // paletteChanged()
    }

    public boolean canUndo() { return true; }

    public boolean canRedo() { return true; }

}