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
import java.awt.*;
import java.awt.event.*;

/**
*
* The dialog that's shown when user wants to define a new codec.
*
**/

public class TMCustomCodecDialog extends JDialog {

    int result;

    // labels
    private JLabel bppLabel = new JLabel("Bits per pixel:");
    private JLabel rmaskLabel = new JLabel("Red bitmask:");
    private JLabel gmaskLabel = new JLabel("Green bitmask:");
    private JLabel bmaskLabel = new JLabel("Blue bitmask:");
    private JLabel amaskLabel = new JLabel("Alpha bitmask:");
    private JLabel endiannessLabel = new JLabel("Endianness:");

    // input fields
    private JFormattedTextField bppField = new JFormattedTextField();
    private JFormattedTextField rmaskField = new JFormattedTextField();
    private JFormattedTextField gmaskField = new JFormattedTextField();
    private JFormattedTextField bmaskField = new JFormattedTextField();
    private JFormattedTextField amaskField = new JFormattedTextField();

    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");

/**
*
* Creates the custom codec dialog.
*
**/

    public TMCustomCodecDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        JPanel pane = new JPanel();
        setContentPane(pane);
        pane.setLayout(new GridLayout(6, 2));
        pane.add(bppLabel);
        pane.add(bppField);
        pane.add(rmaskLabel);
        pane.add(rmaskField);
        pane.add(gmaskLabel);
        pane.add(gmaskField);
        pane.add(bmaskLabel);
        pane.add(bmaskField);
        pane.add(amaskLabel);
        pane.add(amaskField);
        pane.add(okButton);
        pane.add(cancelButton);

        okButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okClicked();
                }
            }
        );
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelClicked();
                }
            }
        );
    }

/**
*
*
*
**/

    public int getBitsPerPixel() {
        return 0;
    }

/**
*
*
*
**/

    public int getRedMask() {
        return 0;
    }

/**
*
*
*
**/

    public int getGreenMask() {
        return 0;
    }

/**
*
*
*
**/

    public int getBlueMask() {
        return 0;
    }

/**
*
*
*
**/

    public int getAlphaMask() {
        return 0;
    }

/**
*
*
*
**/

    public int getEndianness() {
        return 0;
    }

/**
*
*
*
**/

    public String getDescription() {
        return "";
    }

/**
*
*
*
**/

    public void okClicked() {
        result = JOptionPane.OK_OPTION;
        setVisible(false);
    }

/**
*
*
*
**/

    public void cancelClicked() {
        setVisible(false);
    }

/**
*
*
*
**/

    public int showDialog() {
        result = JOptionPane.CANCEL_OPTION;
        setVisible(true);
        return result;
    }

}