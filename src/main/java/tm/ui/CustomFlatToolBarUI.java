package tm.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.formdev.flatlaf.ui.FlatStylingSupport.Styleable;
import com.formdev.flatlaf.ui.FlatToolBarUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

final class CustomFlatToolBarUI extends FlatToolBarUI {

	/** @since 3 */
	@Styleable
	protected Color buttonGroupBackground;

	@Override
	protected void installDefaults() {
		super.installDefaults();

		buttonGroupBackground = UIManager.getColor("ToolBar.buttonGroupBackground");
	}

	@Override
	protected void uninstallDefaults() {
		super.uninstallDefaults();

		buttonGroupBackground = null;
	}

	protected void paintButtonGroup(Graphics g) {
		if (hoverButtonGroupBackground == null && buttonGroupBackground == null)
			return;

		Set<ButtonGroup> groups = new HashSet<>();
		for (Component b : toolBar.getComponents()) {
			if (b instanceof AbstractButton button) {
				ButtonGroup group = getButtonGroup(button);
				if (group != null)
					groups.add(group);
			}
		}
		
		Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
		
		for(ButtonGroup group : groups) {
			boolean hovered = false;
			
			// get bounds of buttons in group
			ArrayList<Rectangle> rects = new ArrayList<>();
			Enumeration<AbstractButton> e = group.getElements();
			while (e.hasMoreElements()) {
				AbstractButton gb = e.nextElement();
				if (gb.getParent() == toolBar)
					rects.add(gb.getBounds());
				if(gb.getModel().isRollover())
					hovered = true;
			}
			
			// sort button bounds
			boolean horizontal = (toolBar.getOrientation() == JToolBar.HORIZONTAL);
			rects.sort((r1, r2) -> horizontal ? r1.x - r2.x : r1.y - r2.y);
			
			Color color = hovered && hoverButtonGroupBackground != null ? hoverButtonGroupBackground : buttonGroupBackground;
			g.setColor(FlatUIUtils.deriveColor(color, toolBar.getBackground()));

			// paint button group hover background
			int maxSepWidth = UIScale.scale(10);
			Rectangle gr = null;
			for (Rectangle r : rects) {
				if (gr == null) {
					// first button
					gr = r;
				} else if (horizontal ? (gr.x + gr.width + maxSepWidth >= r.x) : (gr.y + gr.height + maxSepWidth >= r.y)) {
					// button joins previous button
					gr = gr.union(r);
				} else {
					// paint group
					FlatUIUtils.paintComponentBackground((Graphics2D) g, gr.x, gr.y, gr.width, gr.height, 0,
							UIScale.scale(hoverButtonGroupArc));
					gr = r;
				}
			}
			if (gr != null)
				FlatUIUtils.paintComponentBackground((Graphics2D) g, gr.x, gr.y, gr.width, gr.height, 0,
						UIScale.scale(hoverButtonGroupArc));
		}

		FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
	}

	private ButtonGroup getButtonGroup(AbstractButton b) {
		ButtonModel model = b.getModel();
		return (model instanceof DefaultButtonModel) ? ((DefaultButtonModel) model).getGroup() : null;
	}
}
