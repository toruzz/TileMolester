package tm.reversibleaction;

import tm.canvases.TMEditorCanvas;
import tm.canvases.TMSelectionCanvas;

/**
 * Manages the Paste undo/redo action
 */
public class ReversiblePasteSelectionAction extends ReversibleAction{
    private TMSelectionCanvas pastedSel;
    private TMEditorCanvas owner;

    public ReversiblePasteSelectionAction(TMSelectionCanvas pastedSel, TMEditorCanvas owner)
    {
        super("Paste");
        this.pastedSel = pastedSel;
        this.owner = owner;

        owner.showSelection(pastedSel, 0, 0);
    }

    public boolean canUndo()
    {
        return true;
    }

    public boolean canRedo()
    {
        return true;
    }

    public void undo()
    {
        owner.remove(pastedSel);
        owner.redraw();
    }

    public void redo()
    {
        owner.showSelection(pastedSel,0,0);
    }
}
