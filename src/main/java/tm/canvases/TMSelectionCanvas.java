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

package tm.canvases;

import tm.TMPalette;
import tm.tilecodecs.TileCodec;
import tm.reversibleaction.ReversibleMoveSelectionAction;
import tm.ui.TMUI;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
*
* A tile canvas that can be moved around.
*
**/

public class TMSelectionCanvas extends TMTileCanvas implements MouseInputListener {

    private TMUI ui;
    private static JPopupMenu selectionPopup = null;

    private int dx;
    private int dy;
    private int oldX;
    private int oldY;

/**
*
* Creates a selection canvas by copying the specified tile grid.
*
**/

    public TMSelectionCanvas(TMUI ui, TMTileCanvas canvas, int x1, int y1, int w, int h) {
        super(null);
        this.ui = ui;
        if (selectionPopup == null)
            initSelectionPopup();
        addMouseListener(this);
        addMouseMotionListener(this);
        setMode(TileCodec.MODE_1D); // all copied data is stored in 1-Dimensional mode
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        setVisible(false);
        // create buffer to hold tile data
        byte[] selbits = new byte[w * h * canvas.getCodec().getTileSize()];
        // init the canvas
        setBits(selbits);
        setGridSize(w, h);
        setCodec(canvas.getCodec());
        if (canvas.getPalette() != null) {
            setPalette(new TMPalette(canvas.getPalette()));
            setPalIndex(canvas.getPalIndex());
        }

        // copy pixels
        for (int i=0; i<h*8; i++) {
            for (int j=0; j<w*8; j++) {
                setPixel(j, i, canvas.getPixel(j+x1*8, i+y1*8));
            }
        }

        packPixels();
    }

/**
*
* Sets up the selection popup menu.
*
**/

    private void initSelectionPopup() {
        selectionPopup = new JPopupMenu();
        JMenuItem menuItem;
        // Cut
        menuItem = new JMenuItem("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doCutCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Copy
        menuItem = new JMenuItem("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doCopyCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Paste
        menuItem = new JMenuItem("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doPasteCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Clear
        menuItem = new JMenuItem("Clear");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doClearCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Select All
        menuItem = new JMenuItem("Select All");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doSelectAllCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        //
        selectionPopup.addSeparator();
        // Mirror
        menuItem = new JMenuItem("Mirror");
        menuItem.setMnemonic(KeyEvent.VK_M);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doMirrorCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Flip
        menuItem = new JMenuItem("Flip");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doFlipCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        //
        selectionPopup.addSeparator();
        // Rotate Right
        menuItem = new JMenuItem("Rotate Right");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doRotateRightCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Rotate Left
        menuItem = new JMenuItem("Rotate Left");
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doRotateLeftCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        //
        selectionPopup.addSeparator();
        // Shift Left
        menuItem = new JMenuItem("Shift Left");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doShiftLeftCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Shift Right
        menuItem = new JMenuItem("Shift Right");
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doShiftRightCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Shift Up
        menuItem = new JMenuItem("Shift Up");
        menuItem.setMnemonic(KeyEvent.VK_U);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doShiftUpCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        // Shift Down
        menuItem = new JMenuItem("Shift Down");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doShiftDownCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
        //
        selectionPopup.addSeparator();
        // Stretch...
        menuItem = new JMenuItem("Stretch...");
        menuItem.setMnemonic(KeyEvent.VK_E);
        menuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ui.doStretchCommand();
                }
            }
        );
        selectionPopup.add(menuItem);
    }

/**
*
* Paints the selection.
*
**/

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw frame
        g.setColor(Color.white);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
   }

/**
*
* Called when user has pressed mouse button and is moving mouse.
*
**/

    public void mouseDragged(MouseEvent e) {
        // reposition
        setLocation(getX()+e.getX()-dx, getY()+e.getY()-dy);
    }

/**
*
* Mouse event handlers.
*
**/

    public void mouseMoved(MouseEvent e) { }

    public void mouseClicked(MouseEvent e) { }

    public void mouseEntered(MouseEvent e) { }

    public void mouseExited(MouseEvent e) { }

    public void mousePressed(MouseEvent e) {
        dx = e.getX();
        dy = e.getY();
        oldX = getX();
        oldY = getY();
    }

    public void mouseReleased(MouseEvent e) {
        if (hasMoved()) {
            snapToGrid();

            int dim = getScaledTileDim();
            ui.getSelectedView().addReversibleAction(
                    new ReversibleMoveSelectionAction(
                            this,
                            oldX / dim,
                            oldY / dim,
                            getX() / dim,
                            getY() / dim
                    )
            );
        }
        maybeShowPopup(e);
    }
/**
 *
 * Checks if the selection has been moved
 *
 **/
    public boolean hasMoved()
    {
        return (oldX != getX() && oldY != getY());
    }

/**
*
* Aligns the selection to a tile boundary.
*
**/

    public void snapToGrid() {
        int dim = getScaledTileDim();
        // snap to atomic tile grid
        int x = getX();
        int y = getY();
        setLocation((x / dim) * dim, (y / dim) * dim); //TODO: fix the snapping
    }

/**
*
* Shows the popup menu if the mouse event was a popup trigger.
*
**/

    public void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            selectionPopup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

}