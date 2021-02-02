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

import tm.utils.DecimalNumberVerifier;

import javax.swing.*;
import java.awt.*;

/**
*
* The dialog that's shown when user wants to create a new ("blank") file.
*
**/

public class TMNewFileDialog extends TMModalDialog {

    private JLabel sizeLabel;
    private JTextField sizeField;

/**
*
* Creates the New File dialog.
*
**/

    public TMNewFileDialog(Frame owner, tm.utils.Xlator xl) {
        super(owner, "New_File_Dialog_Title", xl);
    }

/**
*
* Gets the filesize given by the user.
*
**/

    public int getFileSize() {
        return Integer.parseInt(sizeField.getText());
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
        sizeLabel = new JLabel(xlate("Size_Prompt"));
        buildConstraints(gbc, 0, 0, 1, 1, 50, 100);
        gbl.setConstraints(sizeLabel, gbc);
        p.add(sizeLabel);
        sizeField = new JTextField();
        buildConstraints(gbc, 1, 0, 1, 1, 50, 100);
        gbl.setConstraints(sizeField, gbc);
        p.add(sizeField);
        p.setPreferredSize(new Dimension(200, 50));

        sizeField.setColumns(7);
        sizeField.addKeyListener(new DecimalNumberVerifier());
        sizeField.getDocument().addDocumentListener(new TMDocumentListener());

        return p;
    }

    public int showDialog() {
        sizeField.setText("");
        maybeEnableOKButton();
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                sizeField.requestFocus();
            }
        });
        return super.showDialog();
    }

    public boolean inputOK() {
        return !(sizeField.getText().equals("") || (getFileSize() == 0));
    }

}