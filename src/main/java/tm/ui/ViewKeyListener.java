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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;

/**
*
* Class for handling keypresses in the window.
*
**/

public class ViewKeyListener extends KeyAdapter {

    private TMView view;

/**
*
* Creates the listener.
*
**/

    public ViewKeyListener(TMView view) {
        super();
        this.view = view;
    }

/**
*
* Handles a keypress.
*
**/

    public void keyPressed(KeyEvent e) {
        // check that the event should indeed be processed
        if (!view.getKeysEnabled()) return;

        TMUI ui = view.getTMUI();
        TileCodec tc = view.getTileCodec();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
                if (e.isShiftDown()) {
                    ui.doDecreaseHeightCommand();
                }
                else {
                    ui.doMinusRowCommand();
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
                if (e.isShiftDown()) {
                    ui.doIncreaseHeightCommand();
                }
                else {
                    ui.doPlusRowCommand();
                }
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                if (e.isShiftDown()) {
                    ui.doDecreaseWidthCommand();
                }
                else {
                    ui.doMinusTileCommand();
                }
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                if (e.isShiftDown()) {
                    ui.doIncreaseWidthCommand();
                }
                else {
                    ui.doPlusTileCommand();
                }
                break;
            case KeyEvent.VK_HOME:
                ui.doHomeCommand();
                break;
            case KeyEvent.VK_END:
                ui.doEndCommand();
                break;
            case 109:
            case KeyEvent.VK_MINUS:
                ui.doMinusByteCommand();
                break;
            case 107:
            case KeyEvent.VK_PLUS:
                ui.doPlusByteCommand();
                break;
            case KeyEvent.VK_PAGE_UP:
                ui.doMinusPageCommand();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                ui.doPlusPageCommand();
                break;
            case KeyEvent.VK_TAB:
                if (e.isShiftDown()) {
                    ui.doTileCodecCommand(ui.getTileCodecPredecessor(tc));
                }
                else {
                    ui.doTileCodecCommand(ui.getTileCodecSuccessor(tc));
                }
                break;
            case KeyEvent.VK_ESCAPE:
                view.getEditorCanvas().encodeSelection();   // TODO
                break;
        }
    }

}