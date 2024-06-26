package tm.utils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import tm.ui.TMTheme;


public class mxScrollableDesktop extends JDesktopPane {
	
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(TMTheme.getThemeColors().get("WIN_BG"));
		g.fillRect(0, 0, getWidth(), getHeight());
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