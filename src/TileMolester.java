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

	boolean isLinux = TMUI.isLinux;
	boolean isWindows = TMUI.isWindows;

    public TileMolester() {
		try {
			if(isLinux) UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");	
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
		Color[] sysColor = new Color[]{    
			SystemColor.activeCaption,
			SystemColor.activeCaptionBorder,
			SystemColor.activeCaptionText,
			SystemColor.control,
			SystemColor.controlDkShadow,
			SystemColor.controlHighlight,
			SystemColor.controlLtHighlight,
			SystemColor.controlShadow,
			SystemColor.controlText,
			SystemColor.desktop,
			SystemColor.inactiveCaption,
			SystemColor.inactiveCaptionBorder,
			SystemColor.inactiveCaptionText,
			SystemColor.info,
			SystemColor.infoText,
			SystemColor.menu,
			SystemColor.menuText,
			SystemColor.scrollbar,
			SystemColor.text,
			SystemColor.textHighlight,
			SystemColor.textHighlightText,
			SystemColor.textInactiveText,
			SystemColor.textText,
			SystemColor.window,
			SystemColor.windowBorder,
			SystemColor.windowText};
		
		for(Color c: sysColor){
		   System.out.println(c); 
		}

        new TileMolester();
    }

}