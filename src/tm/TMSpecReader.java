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
import tm.fileselection.*;
import tm.utils.*;
import java.util.StringTokenizer;
import java.util.Vector;
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
* This class has methods for reading an XML document containing specifications
* for the various program resources.
*
**/

public class TMSpecReader {

    private static Vector colorcodecs;
    private static Vector tilecodecs;
    private static Vector filefilters;
    private static Vector palettefilters;
    private static Vector filelisteners;
    private static Element tmspec;

/**
*
* Reads specs from the specified file.
*
**/

    public static void readSpecsFromFile(File file)
    throws SAXException, ParserConfigurationException, IOException {
        colorcodecs = new Vector();
        tilecodecs = new Vector();
        filefilters = new Vector();
        palettefilters = new Vector();
        filelisteners = new Vector();
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

        tmspec = doc.getDocumentElement();   // root element (<tmspec> tag)

        readDirectColorFormats();
        readIndexedColorFormats();

        readPlanarTileFormats();
        readLinearTileFormats();
        readDirectColorTileFormats();
        readCompositeTileFormats();

        readFileFilters();
        readPaletteFilters();
        readFileListeners();
    }

/**
*
* Reads the directcolor format tags and creates directcolorcodecs for them.
*
**/

    private static void readDirectColorFormats() {
        NodeList directColorTags = tmspec.getElementsByTagName("directcolor");
        for (int i=0; i<directColorTags.getLength(); i++) {
            Element dc = (Element)directColorTags.item(i);
            String id = dc.getAttribute("id");
            int bpp = Integer.parseInt(dc.getAttribute("bpp"));
            int rmask = (int)Long.parseLong(dc.getAttribute("rmask"), 16);
            int gmask = (int)Long.parseLong(dc.getAttribute("gmask"), 16);
            int bmask = (int)Long.parseLong(dc.getAttribute("bmask"), 16);
            int amask = 0;
            if (!dc.getAttribute("amask").equals(""))
                amask = (int)Long.parseLong(dc.getAttribute("amask"), 16);
            String desc = XMLParser.getNodeValue(dc.getElementsByTagName("description").item(0));
            colorcodecs.add(new DirectColorCodec(id, bpp, rmask, gmask, bmask, amask, desc));
        }
    }

/**
*
* Reads the indexedcolor format tags and creates indexedcolorcodecs for them.
*
**/

    private static void readIndexedColorFormats() {
        NodeList indexedColorTags = tmspec.getElementsByTagName("indexedcolor");
        for (int i=0; i<indexedColorTags.getLength(); i++) {
            Element ic = (Element)indexedColorTags.item(i);
            String id = ic.getAttribute("id");
            int bpp = Integer.parseInt(ic.getAttribute("bpp"));
            String desc = XMLParser.getNodeValue(ic.getElementsByTagName("description").item(0));
            int endianness = ic.getAttribute("endianness").equals("little") ? ColorCodec.LITTLE_ENDIAN : ColorCodec.BIG_ENDIAN;
            String hexString = XMLParser.getNodeValue(ic.getElementsByTagName("data").item(0));
            byte[] data = HexStringConverter.hexStringToBytes(hexString);

            // pack the data into array of 32-bit ARGB ints
            DirectColorCodec codec = new DirectColorCodec("", 24, 0xFF0000, 0x00FF00, 0x0000FF, 0, "");
            codec.setEndianness(endianness);
            int[] entries = new int[data.length / codec.getBytesPerPixel()];
            for (int j=0; j<entries.length; j++) {
                entries[j] = codec.fromBytes(data, j * codec.getBytesPerPixel());
            }

            colorcodecs.add(new IndexedColorCodec(id, bpp, entries, desc));
        }
    }

/**
*
* Reads the planartile format tags and creates planartilecodecs for them.
*
**/

    private static void readPlanarTileFormats() {
        NodeList planarTileTags = tmspec.getElementsByTagName("planartile");
        for (int i=0; i<planarTileTags.getLength(); i++) {
            Element plc = (Element)planarTileTags.item(i);
            String id = plc.getAttribute("id");
            int bpp = Integer.parseInt(plc.getAttribute("bpp"));
            String desc = XMLParser.getNodeValue(plc.getElementsByTagName("description").item(0));
            StringTokenizer st = new StringTokenizer(plc.getAttribute("planeorder"), ",");
            int[] ofs = new int[st.countTokens()];
            for (int j=0; j<ofs.length; j++) {
                ofs[j] = Integer.parseInt(st.nextToken());
            }
            tilecodecs.add(new PlanarTileCodec(id, ofs, desc));
        }
    }

/**
*
* Reads the lineartile tags and creates lineartilecodecs for them.
*
**/

    private static void readLinearTileFormats() {
        NodeList linearTileTags = tmspec.getElementsByTagName("lineartile");
        for (int i=0; i<linearTileTags.getLength(); i++) {
            Element lnc = (Element)linearTileTags.item(i);
            String id = lnc.getAttribute("id");
            int bpp = Integer.parseInt(lnc.getAttribute("bpp"));
            int ordering = LinearTileCodec.IN_ORDER;
            if(lnc.getAttribute("ordering").equals("reverse"))
                ordering = LinearTileCodec.REVERSE_ORDER;
            String desc = XMLParser.getNodeValue(lnc.getElementsByTagName("description").item(0));
            tilecodecs.add(new LinearTileCodec(id, bpp, ordering, desc));
        }
    }

/**
*
* Reads the directcolortile tags and creates directcolortilecodecs for them.
*
**/

    private static void readDirectColorTileFormats() {
        NodeList directColorTileTags = tmspec.getElementsByTagName("directcolortile");
        for (int i=0; i<directColorTileTags.getLength(); i++) {
            Element dcc = (Element)directColorTileTags.item(i);
            String id = dcc.getAttribute("id");
            // String formatID = dcc.getAttribute("colorformat"); TODO
            int bpp = Integer.parseInt(dcc.getAttribute("bpp"));
            int rmask = (int)Long.parseLong(dcc.getAttribute("rmask"), 16);
            int gmask = (int)Long.parseLong(dcc.getAttribute("gmask"), 16);
            int bmask = (int)Long.parseLong(dcc.getAttribute("bmask"), 16);
            int amask = 0;
            if (!dcc.getAttribute("amask").equals(""))
                amask = (int)Long.parseLong(dcc.getAttribute("amask"), 16);
            String desc = XMLParser.getNodeValue(dcc.getElementsByTagName("description").item(0));
            tilecodecs.add(new DirectColorTileCodec(id, bpp, rmask, gmask, bmask, amask, desc));
        }
    }

/**
*
* Reads the compositetile tags and creates compositetilecodecs for them.
*
**/

    private static void readCompositeTileFormats() {
        NodeList compositeTileTags = tmspec.getElementsByTagName("compositetile");
        for (int i=0; i<compositeTileTags.getLength(); i++) {
            Element cpc = (Element)compositeTileTags.item(i);
            String id = cpc.getAttribute("id");
            String desc = XMLParser.getNodeValue(cpc.getElementsByTagName("description").item(0));
            // build array of planar codecs
            StringTokenizer st = new StringTokenizer(cpc.getAttribute("formats"), ",");
            TileCodec[] codecs = new TileCodec[st.countTokens()];
            int cc=0;
            int bpp=0;
            for (int j=0; j<codecs.length; j++) {
                String cid = st.nextToken();
                // find the codec with correct id
                for (int k=0; k<tilecodecs.size(); k++) {
                    TileCodec tc = (TileCodec)tilecodecs.get(k);
                    if (tc.getID().equals(cid)) {
                        codecs[j] = tc;
                        cc++;
                        bpp += tc.getBitsPerPixel();
                        break;
                    }
                }
            }
            if (cc != codecs.length) continue;  // one or more codec IDs invalid
            tilecodecs.add(new CompositeTileCodec(id, bpp, codecs, desc));
        }
    }

/**
*
* Reads the filefilter tags and creates TMTileCodecFilters for them.
*
**/

    private static void readFileFilters() {
        NodeList fftags = tmspec.getElementsByTagName("filefilter");
        for (int i=0; i<fftags.getLength(); i++) {
            Element ff = (Element)fftags.item(i);
            String extlist = ff.getAttribute("extensions");
            String desc = XMLParser.getNodeValue(ff.getElementsByTagName("description").item(0));
            String codecID = ff.getAttribute("tileformat");
            int defaultMode = TileCodec.MODE_1D;
            if (ff.getAttribute("mode").equals("2D"))
                defaultMode = TileCodec.MODE_2D;
            filefilters.add(new TMTileCodecFileFilter(extlist, desc, codecID, defaultMode));
        }
    }

/**
*
* Reads the palettefilter tags and creates TMPaletteFilters for them.
*
**/

    private static void readPaletteFilters() {
        NodeList pftags = tmspec.getElementsByTagName("palettefilter");
        for (int i=0; i<pftags.getLength(); i++) {
            Element pf = (Element)pftags.item(i);
            String extlist = pf.getAttribute("extensions");
            String desc = XMLParser.getNodeValue(pf.getElementsByTagName("description").item(0));
            String codecID = pf.getAttribute("colorformat");
            int size = Integer.parseInt(pf.getAttribute("size"));
            int offset = Integer.parseInt(pf.getAttribute("offset"));
            int endianness = ColorCodec.LITTLE_ENDIAN;
            if (pf.getAttribute("endianness").equals("big")) {
                endianness = ColorCodec.BIG_ENDIAN;
            }
            palettefilters.add(new TMPaletteFileFilter(extlist, desc, codecID, size, offset, endianness));
        }
    }

/**
*
* Reads the filelistener tags and creates TMFileListeners for them.
*
**/

    private static void readFileListeners() {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        NodeList fltags = tmspec.getElementsByTagName("filelistener");
        for (int i=0; i<fltags.getLength(); i++) {
            Element fl = (Element)fltags.item(i);
            String extlist = fl.getAttribute("extensions");
            String classname = fl.getAttribute("classname");
            Class c;
            try {
                c = loader.loadClass(classname);
            }
            catch (Exception e) {
                continue;
            }
            Object o;
            try {
                o = c.newInstance();
            }
            catch (Exception e) {
                continue;
            }
            filelisteners.add(o);
        }
    }

/**
*
* Gets the vector of color codecs that's been created based on the XML.
*
**/

    public static Vector getColorCodecs() {
        return colorcodecs;
    }

/**
*
* Gets the vector of tile codecs that's been created based on the XML.
*
**/

    public static Vector getTileCodecs() {
        return tilecodecs;
    }

/**
*
* Gets the vector of file filters that's been created based on the XML.
*
**/

    public static Vector getFileFilters() {
        return filefilters;
    }

/**
*
* Gets the vector of palette filters that's been created based on the XML.
*
**/

    public static Vector getPaletteFilters() {
        return palettefilters;
    }

/**
*
* Gets the vector of file listeners that's been created based on the XML.
*
**/

    public static Vector getFileListeners() {
        return filelisteners;
    }

}