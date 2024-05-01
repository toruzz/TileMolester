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
	public static TMSettings settings;
	
	public TileMolester() {
		System.setProperty( "apple.awt.application.appearance", "system" );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		System.setProperty( "apple.awt.application.name", "Tile Molester" );
		
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