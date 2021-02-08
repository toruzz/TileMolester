package tm.utils;

import javax.swing.*;
import javax.swing.event.*;

import tm.ui.TMUI;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import tm.*;
import tm.colorcodecs.*;
import tm.tilecodecs.*;
import tm.fileselection.*;
import tm.modaldialog.*;
import tm.treenodes.*;
import tm.utils.*;
import tm.threads.*;
import tm.filelistener.*;
import tm.canvases.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolBarUI;

import java.util.Locale;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class mxScrollableDesktop extends JDesktopPane {
		

	@Override
    protected void paintComponent(Graphics g) {
		Color WindowBG = Color.GRAY;

		// TODO: This shouldn't be duplicated. Make it cleaner.
		Document doc = null;
        try {
            doc = XMLParser.parse(new File("settings.xml"));
        }
        catch (Exception e) {
        }
        if (doc != null){

			Element settings = doc.getDocumentElement();
			NodeList properties = settings.getElementsByTagName("property");
			for (int i=0; i<properties.getLength(); i++) {
				Element property = (Element)properties.item(i);
				String key = property.getAttribute("key");
				String value = property.getAttribute("value");
				if (key.equals("WindowBG")) {
					WindowBG = Color.decode(value);
				}
			}
			
			super.paintComponent(g);
			g.setColor(WindowBG);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	
    public mxScrollableDesktop() {
        super();

        DesktopManager mgr = new DefaultDesktopManager() {
            public void endDraggingFrame(JComponent f) {
                super.endDraggingFrame(f);
                revalidate();
            }

            public void endResizingFrame(JComponent f) {
                super.endResizingFrame(f);
                revalidate();
            }
        };

		setDesktopManager(mgr);
		
		
    }

/**
* Set the preferred size of the desktop to the right-bottom-corner of the
* internal-frame with the "largest" right-bottom-corner.
*
* @return The preferred desktop dimension.
*/
    public Dimension getPreferredSize() {
        JInternalFrame [] array = getAllFrames();
        int maxX = 0;
        int maxY = 0;
        for (int i = 0; i<array.length; i++) {
            int x = array[i].getX() + array[i].getWidth();
            if (x > maxX) maxX = x;
            int y = array[i].getY() + array[i].getHeight();
            if (y > maxY) maxY = y;
        }
        return new Dimension(maxX, maxY);
    }

}