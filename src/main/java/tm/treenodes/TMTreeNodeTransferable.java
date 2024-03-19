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

import java.awt.datatransfer.*;

/**
 * Custom Transferable class for TMTreeNodes.
 * Makes it possible to drag and drop such nodes.
 *
 */

public class TMTreeNodeTransferable implements Transferable {

    public static DataFlavor localTMTreeNodeFlavor=null;
    static {
        try {
            localTMTreeNodeFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=treenodes.TMTreeNode", "Local TMTreeNode");
        } catch(Exception e) { e.printStackTrace(); }
    }

    private TMTreeNode node;

    public TMTreeNodeTransferable(TMTreeNode node) {
        this.node = node;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = new DataFlavor[1];
        flavors[0] = localTMTreeNodeFlavor;
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor == localTMTreeNodeFlavor);
    }

    public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException {
        return this.node;
    }

}