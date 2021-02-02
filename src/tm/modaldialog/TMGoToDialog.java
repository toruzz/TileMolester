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

package tm.modaldialog;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
*
* The dialog where user can enter desired file offset.
*
**/

public class TMGoToDialog extends TMModalDialog {

    // available modes
    public static int ABSOLUTE_MODE = 1;
    public static int RELATIVE_MODE = 2;

    private JTextField ofsField;
    private JRadioButton hexButton;
    private JRadioButton decButton;
    private JRadioButton absButton;
    private JRadioButton relButton;

/**
*
* Creates the goto dialog.
*
**/

    public TMGoToDialog(Frame owner, tm.utils.Xlator xl) {
        super(owner, "Go_To_Dialog_Title", xl);
    }

/**
*
* Gets the selected mode.
*
**/

    public int getMode() {
        return (absButton.isSelected()) ? ABSOLUTE_MODE : RELATIVE_MODE;
    }

/**
*
* Gets the offset that was entered.
*
**/

    public int getOffset() {
        if (inputOK())
            return Integer.parseInt(ofsField.getText(), getRadix());
        return 0;
    }

/**
*
*
*
**/

    protected JPanel getDialogPane() {
        JPanel p = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        p.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        JPanel ofsPane = new JPanel();
        ofsPane.setBorder(new TitledBorder(new EtchedBorder(), xlate("Offset")));
        ofsField = new JTextField();
        ofsPane.add(ofsField);

        JPanel radixPane = new JPanel();
        radixPane.setBorder(new TitledBorder(new EtchedBorder(), xlate("Radix")));
        radixPane.setLayout(new BoxLayout(radixPane, BoxLayout.Y_AXIS));
        hexButton = new JRadioButton(xlate("Hex"));
        decButton = new JRadioButton(xlate("Dec"));
        radixPane.add(hexButton);
        radixPane.add(decButton);

        JPanel modePane = new JPanel();
        modePane.setBorder(new TitledBorder(new EtchedBorder(), xlate("Mode")));
        modePane.setLayout(new BoxLayout(modePane, BoxLayout.Y_AXIS));
        absButton = new JRadioButton(xlate("Absolute"));
        relButton = new JRadioButton(xlate("Relative"));
        modePane.add(absButton);
        modePane.add(relButton);

        buildConstraints(gbc, 0, 0, 1, 1, 50, 100);
        gbl.setConstraints(ofsPane, gbc);
        p.add(ofsPane);

        buildConstraints(gbc, 1, 0, 1, 1, 25, 100);
        gbl.setConstraints(radixPane, gbc);
        p.add(radixPane);

        buildConstraints(gbc, 2, 0, 1, 1, 25, 100);
        gbl.setConstraints(modePane, gbc);
        p.add(modePane);

        ButtonGroup modeButtonGroup = new ButtonGroup();
        modeButtonGroup.add(absButton);
        modeButtonGroup.add(relButton);
        absButton.setSelected(true);

        ButtonGroup radixButtonGroup = new ButtonGroup();
        radixButtonGroup.add(hexButton);
        radixButtonGroup.add(decButton);
        hexButton.setSelected(true);

        p.setPreferredSize(new Dimension(300, 100));

        ofsField.setText("");
        ofsField.setColumns(10);
        ofsField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((Character.digit(c, getRadix()) != -1) ||
                    (c == KeyEvent.VK_BACK_SPACE) ||
                    (c == KeyEvent.VK_DELETE))) {
                    getToolkit().beep();
                    e.consume();
                }
            }
        });

        ofsField.getDocument().addDocumentListener(new TMDocumentListener());

        hexButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (inputOK()) {
                    int ofs = Integer.parseInt(ofsField.getText(), 10);
                    ofsField.setText(Integer.toString(ofs, 16));
                }
            }
        });
        decButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (inputOK()) {
                    int ofs = Integer.parseInt(ofsField.getText(), 16);
                    ofsField.setText(Integer.toString(ofs, 10));
                }
            }
        });

        return p;
    }

/**
*
*
*
**/

    public int showDialog() {
        ofsField.setText("");
        maybeEnableOKButton();
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                ofsField.requestFocus();
            }
        });
        return super.showDialog();
    }

    private int getRadix() {
        return (hexButton.isSelected()) ? 16 : 10;
    }

    public boolean inputOK() {
        return !(ofsField.getText().equals(""));
    }

}