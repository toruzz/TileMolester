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

package tm.treenodes;

/**
*
* A folder.
* Has a name and zero or more children, each of which may be either
* an item or a folder.
*
**/

public class FolderNode extends TMTreeNode {

    private String name;

/**
*
* Creates a folder with the given name.
*
**/

    public FolderNode(String name) {
        super();
        this.name = name;
    }

/**
*
*
*
**/

    public String toString() {
        return name;
    }

/**
*
* Returns the XML-equivalent of this folder.
*
**/

    public String toXML() {
        // TODO: Use getDepth() to indent properly
        StringBuffer s = new StringBuffer();
        s.append(getIndent());
        s.append("<folder>\n");
        s.append(getIndent()).append("  ");
        s.append("<name>").append(name).append("</name>\n");
        TMTreeNode[] children = getChildren();
        for (int i=0; i<children.length; i++) {
            s.append(children[i].toXML());
        }
        s.append(getIndent());
        s.append("</folder>\n");
        return s.toString();
    }

/**
*
*
*
**/

    public void setText(String text) {
        this.name = text;
    }

}