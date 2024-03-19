package tm.reversibleaction;

import tm.canvases.TMEditorCanvas;
import tm.canvases.TMSelectionCanvas;

/**
 *
 * Allows the deletion of a selection
 *
 **/

public class ReversibleApplySelectionAction extends ReversibleAction {
    private TMSelectionCanvas selection;
    private TMEditorCanvas owner;
    private ReversibleTileModifyAction modifiedTiles;

    public ReversibleApplySelectionAction(TMSelectionCanvas selection, TMEditorCanvas owner)
    {
        super("Apply_Selection");
        this.selection = selection;
        this.owner = owner;
        this.modifiedTiles = owner.encodeSelection(false);
    }

    public void undo()
    {
        int x = selection.getX() / selection.getScaledTileDim();
        int y = selection.getY() / selection.getScaledTileDim();
        modifiedTiles.undo();
        owner.showSelection(selection, x, y);
    }

    public void redo()
    {
        owner.encodeSelection(false);
    }

    public boolean canUndo()
    {
        return true;
    }

    public boolean canRedo()
    {
        return true;
    }
}
