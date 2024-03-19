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

package tm.filelistener;

/**
*
* Abstract class that defines the interface for filelisteners.
* A <code>filelistener</code> is an object that is notified when a file has
* been fully loaded into memory, as well as when it is about to be saved.
* At these times it may perform various operations on the data being saved,
* such as repairing checksums.
* The fileformatlistener also has to implement a method that determines if
* the file being saved is indeed of a supported format. This usually involves
* checking the header (verifying ID strings and such).
*
**/

public abstract class TMFileListener {

/**
*
* This method is invoked when a file has been loaded, to give the file
* listener a chance to detect the file format. If it returns <code>true</code>,
* this file listener will receive all subsequent fileLoaded() and fileSaved()
* events for the file.
*
**/

    public abstract boolean doFormatDetect(final byte[] data, String extension);

/**
*
* This method is invoked when the file has been loaded (and doFormatDetect() has
* already returned true.)
*
**/

    public abstract void fileLoaded(byte[] data, String extension);

/**
*
* This method is invoked when the file is about to be saved.
*
**/

    public abstract void fileSaving(byte[] data, String extension);

}