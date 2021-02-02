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

import tm.colorcodecs.*;
import tm.tilecodecs.*;
import tm.treenodes.*;
import tm.utils.*;
import tm.ui.TMUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
*
* Holds resources for a file image.
* Resources include bookmarks and palettes.
*
**/

public class TMFileResources {

    private FolderNode bookmarkRoot;
    private FolderNode paletteRoot;

    private FileImage fileImage;
    private TMUI ui;

/**
*
* Create initial resources for the specified fileimage.
*
**/

    public TMFileResources(FileImage fileImage, TMUI ui) {
        this.bookmarkRoot = new FolderNode(ui.xlate("Bookmarks"));
        this.paletteRoot = new FolderNode(ui.xlate("Palettes"));
        fileImage.setResources(this);
    }

/**
*
* Loads resources for a fileimage from an XML document.
*
* Resources are in an XML tree structure like so:
* +resources
*   +resourcetype1
*     +resource1
*     +resource2
*   +resourcetype2
*   ....
*   +resourcetype3
*
**/

    public TMFileResources(File file, FileImage fileImage, TMUI ui)
    throws SAXException, ParserConfigurationException, IOException {
        Document doc = null;
        try {
            doc = XMLParser.parse(file);
        } catch (SAXException e) {
            throw e;
        }
        catch (ParserConfigurationException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }

        if (doc == null) return;
        this.fileImage = fileImage;
        this.ui = ui;

        Element root = doc.getDocumentElement();
        bookmarkRoot = parseBookmarks(root);
        paletteRoot = parsePalettes(root);

        fileImage.setResources(this);
    }

/**
*
* Parses the bookmarks into a tree of TMTreeNodes.
* Bookmarks are in an XML tree structure like so:
* +folder1
*   +bookmark1
*   +bookmark2
* +folder2
* +folder3
*   +bookmark1
* ...
*
**/

    public FolderNode parseBookmarks(Element root) {
        Element e = getChildTag(root, "bookmarks", 0);
        FolderNode bookmarkRoot = new FolderNode(ui.xlate("Bookmarks"));
        if (e != null) {
            // parse bookmarks into tree
            NodeList children = e.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    addToBookmarksTree((Element)children.item(i), bookmarkRoot);
                }
            }
        }
        return bookmarkRoot;
    }

/**
*
* Recursively adds folders and bookmarks to tree.
*
**/

    public void addToBookmarksTree(Element e, FolderNode folder) {
        if (e.getTagName().equals("folder")) {
            // subfolder
            String name = XMLParser.getNodeValue(getChildTag(e, "name", 0));
            FolderNode subFolder = new FolderNode(name);
            folder.add(subFolder);
            NodeList children = e.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    addToBookmarksTree((Element)children.item(i), subFolder);
                }
            }
        }
        else if (e.getTagName().equals("bookmark")) {
            // read a bookmark
            int offset = Integer.parseInt(e.getAttribute("offset"));
            int cols = Integer.parseInt(e.getAttribute("columns"));
            int rows = Integer.parseInt(e.getAttribute("rows"));
            int blockWidth = Integer.parseInt(e.getAttribute("blockwidth"));
            int blockHeight = Integer.parseInt(e.getAttribute("blockheight"));
            boolean rowInterleaved = e.getAttribute("rowinterleaved").equals("true");
            boolean sizeBlockToCanvas = e.getAttribute("sizeblocktocanvas").equals("true");
            int mode = TileCodec.MODE_1D;
            if (e.getAttribute("mode").equals("2D")) {
                mode = TileCodec.MODE_2D;
            }
            int palIndex = Integer.parseInt(e.getAttribute("palIndex"));
            // String palID = e.getAttribute("palette");
            String codecID = e.getAttribute("codec");
            TileCodec codec = ui.getTileCodecByID(codecID);
            String desc = XMLParser.getNodeValue(getChildTag(e, "description", 0));
            BookmarkItemNode bookmark =
                new BookmarkItemNode(
                    offset,
                    cols,
                    rows,
                    blockWidth,
                    blockHeight,
                    rowInterleaved,
                    sizeBlockToCanvas,
                    mode,
                    palIndex,
                    codec,
                    desc
                );
            folder.add(bookmark);
        }
    }

/**
*
* Parses the palettes into a tree of TMTreeNodes.
* Highly analoguous to bookmark parsing.
*
**/

    public FolderNode parsePalettes(Element root) {
        Element e = getChildTag(root, "palettes", 0);
        FolderNode paletteRoot = new FolderNode(ui.xlate("Palettes"));
        if (e != null) {
            // parse palettes into tree
            NodeList children = e.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    addToPalettesTree((Element)children.item(i), paletteRoot);
                }
            }
        }
        return paletteRoot;
    }

/**
*
* Recursively processes and adds folders and palettes to tree.
*
**/

    public void addToPalettesTree(Element e, FolderNode folder) {
        if (e.getTagName().equals("folder")) {
            // subfolder
            String name = XMLParser.getNodeValue(getChildTag(e, "name", 0));
            FolderNode subFolder = new FolderNode(name);
            folder.add(subFolder);
            NodeList children = e.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    addToPalettesTree((Element)children.item(i), subFolder);
                }
            }
        }
        else if (e.getTagName().equals("palette")) {
            // read palette
            String id = e.getAttribute("id");
            boolean direct = e.getAttribute("direct").equals("yes");
            String codecID = e.getAttribute("codec");
            ColorCodec codec = ui.getColorCodecByID(codecID);
            int size = Integer.parseInt(e.getAttribute("size"));
            int offset = 0;
            int endianness = ColorCodec.LITTLE_ENDIAN;
            if (e.getAttribute("endianness").equals("big")) {
                endianness = ColorCodec.BIG_ENDIAN;
            }
            byte[] data;
            if (direct) {
                // data is in XML, parse it
                String hexString = XMLParser.getNodeValue(getChildTag(e, "data", 0));
                // alt: hexString = ((CDATASection)palette.getFirstChild()).getData());
                data = HexStringConverter.hexStringToBytes(hexString);
            }
            else {
                // data is in file
                offset = Integer.parseInt(e.getAttribute("offset"));
                data = fileImage.getContents();
            }
            String desc = XMLParser.getNodeValue(getChildTag(e, "description", 0));
            TMPalette pal = new TMPalette(id, data, offset, size, codec, endianness, direct, false);
            PaletteItemNode palette =
                new PaletteItemNode(
                    pal,
                    desc
                );
            folder.add(palette);
        }
    }

/**
*
* Gets the root of the tree of bookmarks & bookmark folders.
*
**/

    public FolderNode getBookmarksRoot() {
        return bookmarkRoot;
    }

/**
*
* Gets the root of the tree of palettes & palette folders.
*
**/

    public FolderNode getPalettesRoot() {
        return paletteRoot;
    }

/**
*
* Returns XML representation of resources.
*
**/

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
        sb.append("<!DOCTYPE tmres SYSTEM \"tmres.dtd\">\n");
        sb.append("<tmres>\n");

        sb.append(bookmarksToXML());
        sb.append(palettesToXML());

        sb.append("</tmres>\n");
        return sb.toString();
    }

/**
*
* Return XML representation of bookmarks.
*
**/

    public String bookmarksToXML() {
        StringBuffer sb = new StringBuffer();
        sb.append(" <bookmarks>\n");
        TMTreeNode[] children = bookmarkRoot.getChildren();
        for (int i=0; i<children.length; i++) {
            sb.append(children[i].toXML());
        }
        sb.append(" </bookmarks>\n");
        return sb.toString();
    }

/**
*
* Returns XML representation of palettes.
*
**/

    public String palettesToXML() {
        StringBuffer sb = new StringBuffer();
        sb.append(" <palettes>\n");
        TMTreeNode[] children = paletteRoot.getChildren();
        for (int i=0; i<children.length; i++) {
            sb.append(children[i].toXML());
        }
        sb.append(" </palettes>\n");
        return sb.toString();
    }

/**
*
* Gets the default resource filename for the given file.
* Currently, this is [filename-extension+".xml"]
*
**/

    public static File getResourceFileFor(File file) {
        // determine name of XML resource file based on filename
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(0, i);
        }
        name += ".xml";
        return new File("./resources/"+name);
    }

    private Element getChildTag(Element e, String Tag, int i) {
        return (Element)e.getElementsByTagName(Tag).item(i);
    }

}