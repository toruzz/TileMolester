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

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;

/**
*
* Generic treenode class.
*
**/

public abstract class TMTreeNode extends DefaultMutableTreeNode {

    private boolean modified;
    private TMTreeNode parent;

/**
*
* Creates a treenode.
*
**/

    public TMTreeNode() {
        super();
        modified = false;
    }

/**
*
*
*
**/

    public TMTreeNode[] getChildren() {
        TMTreeNode[] ch = new TMTreeNode[getChildCount()];
        for (int i=0; i<ch.length; i++) {
            ch[i]= (TMTreeNode)getChildAt(i);
        }
        return ch;
    }

/**
*
*
*
**/

    public String getIndent() {
        StringBuffer sb = new StringBuffer();
        int depth = getDepth();
        for (int i=0; i<depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    public TMTreeNode getTMParent() {
        return (TMTreeNode)getParent();
    }

    public abstract String toXML();

    public abstract void setText(String text);

    public boolean isLeaf() {
        return (getChildCount() == 0);
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

}