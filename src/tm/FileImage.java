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

import tm.treenodes.*;
import tm.ui.TMView;
import java.io.File;
import java.util.Vector;

/**
*
* A FileImage object represents a file that has been loaded into the editor.
*
**/

public class FileImage {

    private byte[] contents;
    private File file;
    private Vector views = new Vector();
    private static int fileNum=0;
    private boolean modified;
    private TMFileResources resources;

/**
*
* Creates a FileImage from a file on disk.
*
* The contents of the file have already been read into buffer contents.
*
**/

    public FileImage(File file, byte[] contents) {
        this.file = file;
        this.contents = contents;
        this.resources = null;
        setModified(false);
    }

/**
*
* Creates a blank FileImage of the requested size.
*
**/

    public FileImage(int size) throws OutOfMemoryError {
        file = new File(System.getProperty("user.dir") + (fileNum++));
        this.resources = null;
        try {
            contents = new byte[size];
        }
        catch (OutOfMemoryError e) {
            throw e;
        }
        // fill with zeroes
        for (int i=0; i<size; i++) {
            contents[i] = 0x00;
        }
        setModified(false);
    }

/**
*
* Sets the file.
*
**/

    public void setFile(File file) {
        this.file = file;
    }

/**
*
* Gets the file.
*
**/

    public File getFile() {
        return file;
    }

/**
*
* Gets the binary contents of this FileImage.
*
**/

    public byte[] getContents() {
        return contents;
    }

/**
*
* Gets the size (in # of bytes) of the file contents.
*
**/

    public int getSize() {
        return contents.length;
    }

/**
*
* Gets the name of the file.
*
**/

    public String getName() {
        return file.getName();
    }

/**
*
* Gets the TMViews associated with this FileImage.
*
**/

    public TMView[] getViews() {
        TMView[] vs = new TMView[views.size()];
        for (int i=0; i<vs.length; i++) {
            vs[i] = (TMView)views.get(i);
        }
        return vs;
    }

/**
*
* Adds a TMView to this FileImage.
*
**/

    public void addView(TMView view) {
        views.add(view);
    }

/**
*
* Removes a TMView for this FileImage.
*
**/

    public void removeView(TMView view) {
        views.remove(view);
        if (views.size() == 0) {
            contents = null;    // kill the contents reference
            resources = null;
            file = null;
        }
    }

/**
*
* Sets the modified flag.
*
**/

    public void setModified(boolean modified) {
        this.modified = modified;
        // update view titles
        TMView[] views = getViews();
        for (int i=0; i<views.length; i++) {
            TMView v = views[i];
            String t = v.getTitle();
            if (modified) {
                v.setTitle(getName()+"*");
                v.getTMUI().setSaveButtonsEnabled(true);
            }
            else {
                v.setTitle(getName());
            }
        }
    }

/**
*
* Gets the modified flag.
*
**/

    public boolean isModified() {
        return modified;
    }

/**
*
* Sets the resources associated with this fileimage.
* The resources contain bookmarks and palettes.
*
**/

    public void setResources(TMFileResources resources) {
        this.resources = resources;
    }

/**
*
* Gets the resources related to this fileimage.
*
**/

    public TMFileResources getResources() {
        return resources;
    }

}