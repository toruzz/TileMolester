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

import tm.tilecodecs.TileCodec;
import tm.canvases.TMEditorCanvas;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
*
* Tile Molester status bar.
*
**/

public class TMStatusBar extends JPanel {

    private JLabel offsetLabel = new JLabel(" ");
    private JLabel coordsLabel = new JLabel(" ");
    private JLabel codecLabel = new JLabel(" ");
    private JLabel palOffsetLabel = new JLabel(" ");
    private JLabel modeLabel = new JLabel(" ");
    private JLabel tilesLabel = new JLabel(" ");
    private JLabel messageLabel = new JLabel(" ");

/**
*
* Creates the status bar.
*
**/

    public TMStatusBar() {
        super();
        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(1, 3));
        p1.add(messageLabel);
        p1.add(offsetLabel);
        p1.add(coordsLabel);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(1, 2));
        p2.add(palOffsetLabel);
        p2.add(codecLabel);

        JPanel p3 = new JPanel();
        p3.setLayout(new GridLayout(1, 2));
        p3.add(modeLabel);
        p3.add(tilesLabel);

        setLayout(new GridLayout(1, 4));
        add(p1);
        add(p2);
        add(p3);
//        pane.add(new JLabel("    "));   // just some whitespace

        offsetLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        coordsLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        palOffsetLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        codecLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        modeLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        tilesLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

/**
*
* Sets the text for general message.
*
**/

    public void setMessage(String s) {
        messageLabel.setText(" "+s+" ");
    }

/**
*
* Sets the hex text that indicates the file position.
*
**/

    public void setOffset(int offset) {
        String hexOffset = Integer.toHexString(offset).toUpperCase();
        while (hexOffset.length() < 8) {
            hexOffset = "0" + hexOffset;
        }
        offsetLabel.setText(" "+hexOffset+" "); // i18n
    }

/**
*
* Sets the coordinates.
*
**/

    public void setCoords(int x, int y) {
        coordsLabel.setText(" ("+x+","+y+") ");
    }

/**
*
*
*
**/

    public void setSelectionCoords(int x1, int y1, int x2, int y2) {
        int w = Math.abs(x1 - x2) + 1;
        int h = Math.abs(y1 - y2) + 1;
        coordsLabel.setText(" ("+x1+","+y1+") -> ("+x2+","+y2+") = ("+w+","+h+")");
    }

/**
*
* Sets the text that indicates the graphics codec in use.
*
**/

    public void setCodec(String s) {
        codecLabel.setText(" "+s+" ");   // i18n
    }



/**
*
* Sets the text that indicates the graphics codec in use.
*
**/

    public void setPalOffset(int offset) {
        String hexOffset = Integer.toHexString(offset).toUpperCase();
        while (hexOffset.length() < 8) {
            hexOffset = "0" + hexOffset;
        }
        palOffsetLabel.setText(" Palette: "+hexOffset+" "); // i18n
    }


/**
*
* Sets the text that indicates the current mode.
*
**/

    public void setMode(int mode) {
        if (mode == TileCodec.MODE_1D)
            modeLabel.setText(" 1-Dimensional "); // i18n
        else
            modeLabel.setText(" 2-Dimensional "); // i18n
    }

/**
*
* Sets the text that indicates how many tiles are shown.
*
**/

    public void setTiles(int w, int h) {
        tilesLabel.setText(" "+w+"x"+h+" tiles ");  // i18n
    }

/**
*
* Called when a view has been selected.
*
**/

    public void viewSelected(TMView view) {
        TMEditorCanvas ec = view.getEditorCanvas();
        setMessage("");
        setOffset(view.getOffset());
        if (ec.isSelecting()) {
            setSelectionCoords(ec.getSelX1(), ec.getSelY1(), ec.getCurrentCol(), ec.getCurrentRow());
        }
        else if (ec.isDrawingLine()) {
            setSelectionCoords(ec.getLineX1(), ec.getLineY1(), ec.getLineX2(), ec.getLineY2());
        }
        else {
            setCoords(ec.getCurrentCol(), ec.getCurrentRow());
        }
        if (view.getTileCodec() != null) {
            setCodec(view.getTileCodec().getDescription());
        }
        else {
            setCodec("");
        }
        setPalOffset(view.getPalette().getOffset());
        setMode(view.getMode());
        setTiles(view.getCols(), view.getRows());
    }

/**
 *
 * Convenience method for setting various fields of gridbagconstraints.
 *
 */

    protected static void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

}