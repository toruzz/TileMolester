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

package tm;

import tm.ui.TMSettings;
import tm.ui.TMTheme;
import tm.ui.TMUI;

import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import com.formdev.flatlaf.util.SystemInfo;
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
	ClassLoader cl = getClass().getClassLoader();
	public static TMSettings settings;
	
	public TileMolester() {
		if(SystemInfo.isMacOS) {
			System.setProperty( "apple.awt.application.appearance", "system" );
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
			System.setProperty( "apple.awt.application.name", "Tile Molester" );

			final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
			final Image macIcon = defaultToolkit.getImage(cl.getResource("icons/TMIcon_dock.png"));
			final Taskbar taskbar = Taskbar.getTaskbar();
	
			try {
				taskbar.setIconImage(macIcon);
			} catch (final UnsupportedOperationException e) {
				System.out.println("taskbar.setIconImage not supported");
			} catch (final SecurityException e) {
				System.out.println("There was a security exception for taskbar.setIconImage");
			}
		}
		
		settings = new TMSettings();
		new TMTheme();
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