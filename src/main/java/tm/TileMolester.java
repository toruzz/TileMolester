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

import javax.swing.UIManager;

import tm.ui.TMUI;
import java.awt.Color;
import java.awt.SystemColor;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
*
* Tile Molester main class.
* A quite pointless class really. The application is very UI-centric,
* so the TMUI class evolved into the real application backbone.
* This class just gets the show started.
*
**/

public class TileMolester {

	/**
	*
	* Constructor.
	*
	**/

	Logger mLog = Logger.getGlobal();

	boolean isLinux = TMUI.isLinux;
	boolean isMacOs = TMUI.isMacOs;
	boolean isWindows = TMUI.isWindows;

    public TileMolester() {
		try {
			if(isLinux) UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//			if(isMacOs) UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			if(isMacOs) UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			if(isWindows) UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		new TMUI();
    }

	/**
	*
	* Starts up the program.
	*
	**/

    public static void main(String[] args) {
        new TileMolester();
    }

}