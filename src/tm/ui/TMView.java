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

package tm.ui;

import tm.FileImage;
import tm.TMPalette;
import tm.treenodes.*;
import tm.reversibleaction.*;
import tm.canvases.TMEditorCanvas;
import tm.tilecodecs.TileCodec;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
*
* A graphical view of a file image.
*
* The frame contains a panel that's used for rendering the tile data, and a
* slider for keeping track of the file position. The keyboard can also
* be used to change the file position (see class ViewKeyListener).
* The filename is shown in the titlebar, along with a * when the file has been modified.
*
**/

public class TMView extends JInternalFrame implements ChangeListener {

    private static int frameCount = 0;
    private JPanel contentPane = new JPanel();
    public JSlider slider = new JSlider(JSlider.VERTICAL);
    private JScrollPane scrollPane;
    private TMEditorCanvas editorCanvas;
    private TMUI ui;
    private FileImage fileImage;

    private int fgColor;
    private int bgColor;

    private int minOffset;  //
    private int maxOffset;  // can't scroll past this

    private boolean keysEnabled=true;

    private Vector undoableActions = new Vector();
    private Vector redoableActions = new Vector();

    private boolean sizeBlockToCanvas=true;

/**
*
* Constructs a TMView for the given FileImage.
*
**/

    public TMView(TMUI ui, FileImage fileImage, TileCodec tileCodec) {
        super(fileImage.getName(), true, true, true, true);
        this.ui = ui;
        this.fileImage = fileImage;
        fileImage.addView(this);
        setDoubleBuffered(true);
        setBackground(Color.gray);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameActivated(InternalFrameEvent e) {
                doViewSelected();
            }

            public void internalFrameClosing(InternalFrameEvent e) {
                doCloseCommand();
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                slider.setSize(slider.getWidth(), editorCanvas.getHeight());
//                slider.setSize(slider.getWidth(), getHeight()-((BasicInternalFrameUI)getUI()).getNorthPane().getHeight());
            }
        });

        // init UI components
        // the panel containing the slider and editor canvas
        contentPane.setBackground(Color.gray);
        contentPane.setLayout(null);
        contentPane.setFocusable(true);
        contentPane.addKeyListener(new ViewKeyListener(this));
        contentPane.setFocusTraversalKeysEnabled(false);    // so VK_TAB key events are caught
        contentPane.addMouseListener(
            new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    editorCanvas.maybeApplySelection();
                }
            }
        );

        // the editor canvas
        editorCanvas = new TMEditorCanvas(ui, this);
        contentPane.add(editorCanvas);
        editorCanvas.setLocation(24,0);

        // the slider
        slider.setFocusable(false);
        slider.setInverted(true);
        contentPane.add(slider);
        slider.setLocation(0, 0);
        slider.setSize(24, 384);
        slider.addChangeListener(this);

        // the scrollpane
        scrollPane = new JScrollPane(contentPane);
        scrollPane.setPreferredSize(new Dimension(384+24,384));
        setContentPane(scrollPane);

        // set some initial view properties
        editorCanvas.setOffset(0);
        minOffset = 0;
        editorCanvas.setGridSize(16, 16);
        editorCanvas.setBlockDimensions(16, 16);
        editorCanvas.setCodec(tileCodec);
        editorCanvas.unpackPixels();
        setScale(3.0);
        updateSlider();
        slider.setValue(minOffset);

        pack();
        setLocation(frameCount * 20, frameCount * 20);
        frameCount += 1;
        setVisible(true);
    }
/**
*
* Gets the FileImage associated with this TMView.
*
**/

    public FileImage getFileImage() {
        return fileImage;
    }

/**
*
* Sets the tile codec that's used in the editor canvas.
* It also maps the current draw colors to the closest
* matching colors in the new color range (when going
* from, say, 4-bit to 2-bit codec).
*
**/

    public void setTileCodec(TileCodec codec) {
        // map fg+bg colors according to old and new color range
        mapDrawColors(codec);
        // set the new codec
        editorCanvas.setCodec(codec);
        // update display
        editorCanvas.unpackPixels();
        editorCanvas.repaint();
        updateSlider();
    }

/**
*
* Converts foreground and background color to new palette range.
* It's only necessary when going from a codec that has a greater bitdepth
* (more colors) than the new codec. Does not apply when going to direct-color modes.
* The two colors that best match the current foreground and background colors are
* found in the new palette and set as the new drawing colors.
* The palette index must be converted also!!
*
**/

    public void mapDrawColors(TileCodec codec) {
        TileCodec oldCodec = editorCanvas.getCodec();
        if ((oldCodec != null)
            && (codec.getBitsPerPixel() != oldCodec.getBitsPerPixel())) {
            // get the old color settings
            int oldColorIndex = getColorIndex();
            // calculate the new color settings
            int newColorCount = codec.getColorCount();
            int newPalIndex = oldColorIndex / newColorCount;
            int newColorIndex = newPalIndex * newColorCount;
            editorCanvas.setPalIndex(newPalIndex);  // NB!

            if (codec.getBitsPerPixel() <= 8) {
                // update the drawing colors
                TMPalette pal = editorCanvas.getPalette();
                setFGColor(pal.closestMatchingEntryRGB(newColorIndex, newColorCount, fgColor));
                setBGColor(pal.closestMatchingEntryRGB(newColorIndex, newColorCount, bgColor));
            }
        }
    }

/**
*
* Called when the user changes the current palette index, to set the
* new foreground and background color. This is done by finding their
* indexes in the old palette range and getting the RGB values of the
* corresponding indexes in the new palette range.
*
**/

    public void mapDrawColorsToPalIndex(int newPalIndex) {
        TileCodec codec = getTileCodec();
        if (codec.getBitsPerPixel() <= 8) {
            TMPalette pal = getPalette();
            int palIndex = getPalIndex();
            if (palIndex != newPalIndex) {  // only if the indexes are different
                int colorCount = codec.getColorCount();
                int colorIndex = palIndex * colorCount;
                int newColorIndex = newPalIndex * colorCount;
                // update the colors
                int fgIndex = pal.indexOf(colorIndex, fgColor);
                int bgIndex = pal.indexOf(colorIndex, bgColor);
                setFGColor(pal.getEntryRGB(newColorIndex + fgIndex));
                setBGColor(pal.getEntryRGB(newColorIndex + bgIndex));
            }
        }
    }

/**
*
* Gets the tile codec for this view.
*
**/

    public TileCodec getTileCodec() {
        return editorCanvas.getCodec();
    }

/**
*
* Sets the palette for this view.
*
**/

    public void setPalette(TMPalette palette) {
        editorCanvas.setPalette(palette);
        editorCanvas.setPalIndex(0);
        editorCanvas.unpackPixels();
        editorCanvas.repaint();

        setFGColor(palette.getEntryRGB(1));
        setBGColor(palette.getEntryRGB(0));
    }

/**
*
* Gets the palette for this view.
*
**/

    public TMPalette getPalette() {
        return editorCanvas.getPalette();
    }

/**
*
* Sets the palette index for this view.
*
**/

    public void setPalIndex(int palIndex) {
        editorCanvas.setPalIndex(palIndex);
        editorCanvas.unpackPixels();
        editorCanvas.repaint();
    }

/**
*
* Gets the palette index for this view.
*
**/

    public int getPalIndex() {
        return editorCanvas.getPalIndex();
    }

/**
*
* Gets the maximum palette index, given the palette size and current codec.
*
**/

    public int getPalIndexMaximum() {
        TileCodec codec = getTileCodec();
        int cc = 256;   // assume >= 8 bpp
        if (codec.getBitsPerPixel() < 8) {
            cc = codec.getColorCount();
        }
        int max = (getPalette().getSize() / cc) - 1;
        return (max >= 0) ? max : 0;
    }

/**
*
* Sets the mode for this view.
* Can be 1-dimensional (MODE_1D) or 2-dimensional (MODE_2D).
*
**/

    public void setMode(int mode) {
        editorCanvas.setMode(mode);
        editorCanvas.unpackPixels();
        editorCanvas.repaint();
    }

    public void setEndianness(int endianess) {
//        this.endianness = endianness;
//        getTileCodec().setEndianness(endianness);
    }

/**
*
* Updates the tick size of the slider according to current tile settings.
*
**/

    private void updateSlider() {
        slider.setMinimum(minOffset);   // not here
        // set slider tick spacings and maximum
        slider.setMinorTickSpacing(editorCanvas.getRowIncrement());
        slider.setMajorTickSpacing(editorCanvas.getPageIncrement());
        maxOffset = getFileImage().getSize();
        if (maxOffset > editorCanvas.getPageIncrement()) {
            maxOffset -= editorCanvas.getPageIncrement();
        }
        else {
            maxOffset = 0;
        }
        if (slider.getValue() > maxOffset) {
            slider.setValue(maxOffset);
        }
        slider.setMaximum(maxOffset);
    }

/**
*
* Sets the size of the tile grid.
*
**/

    public void setGridSize(int cols, int rows) {
        if (cols < 1) cols = 1;
        else if (cols > 1024) cols = 1024;
        if (rows < 1) rows = 1;
        else if (rows > 1024) rows = 1024;
        editorCanvas.setGridSize(cols, rows);
        if (sizeBlockToCanvas) {
            editorCanvas.setBlockDimensions(cols, rows);
        }
        updateSlider();
        editorCanvas.unpackPixels();
        setScale(getScale());

        // update statusbar
        ui.refreshStatusBar();  // TODO: Move to TMUI
    }

/**
*
* Gets the current position in the file.
*
**/

    public int getOffset() {
        return editorCanvas.getOffset();
    }

/**
*
* Gets the minimum file scroll offset.
*
**/

    public int getMinOffset() {
        return minOffset;
    }

/**
*
* Gets the maximum file scroll offset.
*
**/

    public int getMaxOffset() {
        return maxOffset;
    }

/**
*
* Sets the position in the file, relative to start of file.
*
**/

    public void setAbsoluteOffset(int absOfs) {
        if (absOfs < minOffset) {
            absOfs = minOffset; // lower boundary
        }
        else if (absOfs > maxOffset) {
            absOfs = maxOffset; // upper boundary
        }
        slider.setValue(absOfs);
        editorCanvas.setOffset(absOfs);
        editorCanvas.unpackPixels();
        editorCanvas.repaint();

        // Update statusbar
        ui.refreshStatusBar();  // TODO: Move to TMUI
    }

/**
*
* Sets the position in the file, relative to current (previous) offset.
*
**/

    public void setRelativeOffset(int relOfs) {
        setAbsoluteOffset(relOfs + editorCanvas.getOffset());
    }

/**
*
* Sets the drawing scale.
*
**/

    public void setScale(double scale) {
        editorCanvas.setScale(scale);

        slider.setSize(slider.getWidth(), editorCanvas.getHeight());

        // set preferred size of contentPane
        contentPane.setPreferredSize(new Dimension(slider.getWidth()+editorCanvas.getWidth(), editorCanvas.getHeight()));
        // update scrollbars
        contentPane.revalidate();

    }

/**
*
* Gets the drawing scale.
*
**/

    public double getScale() {
        return editorCanvas.getScale();
    }

/**
*
* Gets the # of tiles per row.
*
**/

    public int getCols() {
        return editorCanvas.getCols();
    }

/**
*
* Gets the # of tiles per column.
*
**/

    public int getRows() {
        return editorCanvas.getRows();
    }

/**
*
* Turns the block grid on or off.
*
**/

    public void setBlockGridVisible(boolean showBlockGrid) {
        editorCanvas.setBlockGridVisible(showBlockGrid);
    }

/**
*
* Turns the simple tile grid on or off.
*
**/

    public void setTileGridVisible(boolean showTileGrid) {
        editorCanvas.setTileGridVisible(showTileGrid);
    }

/**
*
* Turns the pixel tile grid on or off.
*
**/

    public void setPixelGridVisible(boolean showPixelGrid) {
        editorCanvas.setPixelGridVisible(showPixelGrid);
    }

/**
*
* Gets the visibility status of the block grid.
*
**/

    public boolean isBlockGridVisible() {
        return editorCanvas.isBlockGridVisible();
    }

/**
*
* Gets the visibility status of the simple tile grid.
*
**/

    public boolean isTileGridVisible() {
        return editorCanvas.isTileGridVisible();
    }

/**
*
* Gets the visibility status of the pixel tile grid.
*
**/

    public boolean isPixelGridVisible() {
        return editorCanvas.isPixelGridVisible();
    }

/**
*
* Called when position of slider changes.
*
**/

    public void stateChanged(ChangeEvent e) {
        int rowSize = editorCanvas.getRowIncrement();
        if (rowSize > 0) {
            int offset = editorCanvas.getOffset();
            int relOfs = offset % rowSize;
            int newOfs = (slider.getValue() / rowSize) * rowSize;
            setAbsoluteOffset(relOfs + newOfs);
        }
    }

/**
*
* Gets the scroll pane.
*
**/

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

/**
*
* Gets the tile pane.
*
**/

    public TMEditorCanvas getEditorCanvas() {
        return editorCanvas;
    }

/**
*
* Gets the TMUI.
*
**/

    public TMUI getTMUI() {
        return ui;
    }

/**
*
* Sets the foreground color.
*
**/

    public void setFGColor(int fgColor) {
        this.fgColor = fgColor;
    }

/**
*
* Sets the background color.
*
**/

    public void setBGColor(int bgColor) {
        this.bgColor = bgColor;
    }

/**
*
* Gets the current foreground color.
*
**/

    public int getFGColor() {
        return fgColor;
    }

/**
*
* Gets the current background color.
*
**/

    public int getBGColor() {
        return bgColor;
    }

    public int getFGColorIndex() {
        return 0;   // TODO
    }

    public int getBGColorIndex() {
        return 0;   // TODO
    }

/**
*
* Enables/disables the keyboard event handler.
* This is desired in some situations, such as when the user is drawing with the
* mouse. We don't want him to be able to change format, scroll up/down etc. in
* the middle of an edit operation (the changes would be lost).
* This is sort of a hack though; what is desired is the behaviour as exhibited by
* menu items when the mouse is pressed; the menu shortcut keys can't be used to
* fire events at that time.
*
**/

    public void setKeysEnabled(boolean keysEnabled) {
        this.keysEnabled = keysEnabled;
    }

/**
*
* Returns whether the view's key listener should handle the keypress or not.
* There are two cases when they shouldn't be handled (i.e. be ignored):
* 1) When setKeysEnabled(true) has been executed programatically by, say, the
* editor canvas mousePressed event; or
* 2) When the user is adjusting the position of the slider with the mouse.
*
**/

    public boolean getKeysEnabled() {
        if (slider.getValueIsAdjusting()) {
            return false;   // don't respond to key presses when slider is being adjusting
        }
        return keysEnabled;
    }

    public int getColorCount() {
        if (getTileCodec().getBitsPerPixel() < 8) {
            return getTileCodec().getColorCount();
        }
        return 256;
    }

    public int getColorIndex() {
        return getColorCount() * getPalIndex();
    }

    public int getMode() {
        return editorCanvas.getMode();
    }

/**
*
*
*
**/
/*
    public void mouseWheelMoved(MouseWheelEvent e) {
        int units = e.getUnitsToScroll();
        if (units < 0) {
            // up
            units = -units;
            for (int i=0; i<units; i++) {
                ui.doMinusRowCommand();
            }
        }
        else {
            // down
            for (int i=0; i<units; i++) {
                ui.doPlusRowCommand();
            }
        }
    }
*/
/**
*
* Undoes the last action.
*
**/

    public void undo() {
        if (!undoableActions.isEmpty()) {
            ReversibleAction ra = (ReversibleAction)undoableActions.remove(undoableActions.size()-1);
            ra.undo();
            redoableActions.add(ra);
        }
    }

/**
*
* Redoes a previously undone action.
*
**/

    public void redo() {
        if (!redoableActions.isEmpty()) {
            ReversibleAction ra = (ReversibleAction)redoableActions.remove(redoableActions.size()-1);
            ra.redo();
            undoableActions.add(ra);
        }
    }

/**
*
* Adds a undoable/redoable action.
*
**/

    public void addReversibleAction(ReversibleAction ra) {
        if (undoableActions.size() > 32) {
            undoableActions.remove(0);
        }
        undoableActions.add(ra);
        redoableActions.clear();
        ui.fileImageModified(getFileImage());
        ui.refreshUndoRedo();
    }

/**
*
*
*
**/

    public boolean canUndo() {
        return !undoableActions.isEmpty();
    }

/**
*
*
*
**/

    public boolean canRedo() {
        return !redoableActions.isEmpty();
    }

/**
*
* Goes to the specified bookmark.
*
**/

    public void gotoBookmark(BookmarkItemNode bookmark) {
        editorCanvas.setGridSize(bookmark.getCols(), bookmark.getRows());
        editorCanvas.setBlockDimensions(bookmark.getBlockWidth(), bookmark.getBlockHeight());
        sizeBlockToCanvas = bookmark.getSizeBlockToCanvas();
        editorCanvas.setRowInterleaveBlocks(bookmark.getRowInterleaved());
        editorCanvas.setMode(bookmark.getMode());
        setAbsoluteOffset(bookmark.getOffset());
        // editorCanvas.setPalette(
        setTileCodec(bookmark.getCodec());
        mapDrawColorsToPalIndex(bookmark.getPalIndex());
        setPalIndex(bookmark.getPalIndex());
        setScale(getScale());
        ui.viewSelected(this);
//        fitTilesInWindow();
    }

/**
*
* Creates a bookmark based on current view settings.
*
**/

    public BookmarkItemNode createBookmark(String description) {
        return new BookmarkItemNode(
            getOffset(),
            getCols(),
            getRows(),
            getBlockWidth(),
            getBlockHeight(),
            getRowInterleaveBlocks(),
            getSizeBlockToCanvas(),
            getMode(),
            getPalIndex(),
            getTileCodec(),
            description
        );
    }

/**
*
* Closes the view.
*
**/

    private void doCloseCommand() {
        ui.doCloseCommand();
    }

/**
*
* Notifies UI that this view has been selected.
*
**/

    private void doViewSelected() {
        ui.viewSelected(this);
    }

/**
*
*
*
**/

    public void fitTilesInWindow() {
        contentPane.setSize(slider.getWidth()+editorCanvas.getWidth(), editorCanvas.getHeight());
        Insets ins = getInsets();
        setSize(contentPane.getWidth()+ins.left+ins.right, 20+contentPane.getHeight()+ins.top+ins.bottom);
    }

/**
*
* Gets the first undoable action.
*
**/

    public ReversibleAction getFirstUndoableAction() {
        return (ReversibleAction)undoableActions.lastElement();
    }

/**
*
* Gets the first redoable action.
*
**/

    public ReversibleAction getFirstRedoableAction() {
        return (ReversibleAction)redoableActions.lastElement();
    }

/**
*
* Disposes of the view.
*
**/

    public void dispose() {
        contentPane.removeKeyListener(contentPane.getKeyListeners()[0]);
        slider.removeChangeListener(slider.getChangeListeners()[0]);
        removeInternalFrameListener(getInternalFrameListeners()[0]);
        removeComponentListener(getComponentListeners()[0]);
//        removeMouseWheelListener(getMouseWheelListeners()[0]);
        removeAll();
        fileImage = null;
        editorCanvas.killViewRef();
        editorCanvas = null;
        setDesktopIcon(null);
        setFrameIcon(null);
        setUI(null);
        super.dispose();
    }

/**
*
* Sets the block dimensions.
*
**/

    public void setBlockDimensions(int blockWidth, int blockHeight) {
        if (blockWidth > getCols()) {
            blockWidth = getCols();
        }
        else if (blockWidth <= 0) {
            blockWidth = 1;
        }
        if (blockHeight > getRows()) {
            blockHeight = getRows();
        }
        else if (blockHeight <= 0) {
            blockHeight = 1;
        }
        editorCanvas.setBlockDimensions(blockWidth, blockHeight);
        editorCanvas.unpackPixels();
        editorCanvas.repaint();
    }

/**
*
* Gets the block width.
*
**/

    public int getBlockWidth() {
        return editorCanvas.getBlockWidth();
    }

/**
*
* Gets the block height.
*
**/

    public int getBlockHeight() {
        return editorCanvas.getBlockHeight();
    }

/**
*
* Gets whether blocks should be row-interleaved.
*
**/

    public boolean getRowInterleaveBlocks() {
        return editorCanvas.getRowInterleaveBlocks();
    }

/**
*
* Sets whether blocks should be row-interleaved.
*
**/

    public void setRowInterleaveBlocks(boolean rowInterleaved) {
        editorCanvas.setRowInterleaveBlocks(rowInterleaved);
        editorCanvas.unpackPixels();
        editorCanvas.repaint();
    }

/**
*
* Sets whether the block size should follow the canvas size.
*
**/

    public void setSizeBlockToCanvas(boolean sizeBlockToCanvas) {
        this.sizeBlockToCanvas = sizeBlockToCanvas;
        if (sizeBlockToCanvas) {
            setBlockDimensions(getCols(), getRows());
        }
    }

/**
*
* Gets whether the block size should follow the canvas size.
*
**/

    public boolean getSizeBlockToCanvas() {
        return sizeBlockToCanvas;
    }

}