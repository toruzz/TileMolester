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

import tm.utils.Xlator;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
*
* A class providing a general framework for modal dialogs.
* It has an OK and Cancel button. Must be subclassed to
* provide the getDialogPane() method, which creates and
* returns a panel with the actual dialog components where
* input can be given by the user.
*
**/

public abstract class TMModalDialog extends JDialog {

    private int result;
    private JButton okButton;
    private JButton cancelButton;
    private Xlator xl;
    private JPanel dialogPane;

/**
*
*
*
**/

    public TMModalDialog(Frame owner, String title, Xlator xl) {
        super(owner, xl != null ? xl.xlate(title) : title, true);
        this.xl = xl;
        setResizable(false);
        okButton = new JButton(xlate("OK"));
        cancelButton = new JButton(xlate("Cancel"));
        okButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    approveInput();
                }
            }
        );
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelInput();
                }
            }
        );
        JPanel buttonPane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        buttonPane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        buildConstraints(gbc, 0, 0, 1, 1, 45, 100);
        gbl.setConstraints(okButton, gbc);
        buttonPane.add(okButton);
        JLabel filler = new JLabel();
        gbc.anchor = GridBagConstraints.CENTER;
        buildConstraints(gbc, 1, 0, 1, 1, 10, 100);
        gbl.setConstraints(filler, gbc);
        buttonPane.add(filler);
        buildConstraints(gbc, 2, 0, 1, 1, 45, 100);
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(cancelButton, gbc);
        buttonPane.add(cancelButton);

        JPanel contentPane = new JPanel() {
            public Insets getInsets() {
                return new Insets(10,10,10,10);
            }
        };
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(buttonPane, BorderLayout.SOUTH);
        this.dialogPane = getDialogPane();
        contentPane.add(this.dialogPane, BorderLayout.CENTER);
        pack();

    }

/**
*
* Shows the dialog.
*
**/

    public int showDialog() {
        // center the dialog
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int insetx = (screenSize.width - getWidth()) / 2;
        int insety = (screenSize.height - getHeight()) / 2;
        setBounds(insetx, insety,
                  getWidth(), getHeight());

        result = JOptionPane.CANCEL_OPTION;
        setVisible(true);
        return result;
    }

/**
*
*
*
**/

    protected void approveInput() {
        result = JOptionPane.OK_OPTION;
        setVisible(false);
    }

/**
*
*
*
**/

    protected void cancelInput() {
        result = JOptionPane.CANCEL_OPTION;
        setVisible(false);
    }

/**
*
* Method that provides the real content pane of the dialog.
*
**/

    protected abstract JPanel getDialogPane();

/**
*
*
*
**/

    protected static void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

/**
*
* Sets enabled state of OK button.
* Subclasses can use this to keep the user from OK'ing the input when it
* isn't valid/completed.
*
**/

    public void maybeEnableOKButton() {
        okButton.setEnabled(inputOK());
    }

    public abstract boolean inputOK();

    protected class TMDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            maybeEnableOKButton();
        }
        public void insertUpdate(DocumentEvent e) {
            maybeEnableOKButton();
        }
        public void removeUpdate(DocumentEvent e) {
            maybeEnableOKButton();
        }
    }

    public String xlate(String key) {
        try {
            String value = xl.xlate(key);
            return value;
        }
        catch (Exception e) {
            return key;
        }
    }

}