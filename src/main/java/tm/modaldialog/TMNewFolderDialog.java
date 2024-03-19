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
import java.awt.*;

/**
*
* The dialog that's shown when user wants to create a new folder.
*
**/

public class TMNewFolderDialog extends TMModalDialog {

    private JLabel nameLabel;
    private JTextField nameField;

/**
*
* Creates the New Folder dialog.
*
**/

    public TMNewFolderDialog(Frame owner, tm.utils.Xlator xl) {
        super(owner, "New_Folder_Dialog_Title", xl);
    }

/**
*
* Gets the folder name given by the user.
*
**/

    public String getName() {
        return nameField.getText();
    }

/**
*
*
*
**/

    public void setName(String name) {
        nameField.setText(name);
    }

/**
*
*
*
**/

    protected JPanel getDialogPane() {
        JPanel p = new JPanel();
        nameLabel = new JLabel(xlate("Folder_Name_Prompt"));
        p.add(nameLabel);
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(new TMDocumentListener());
        nameField.setColumns(15);
        p.add(nameField);
        p.setPreferredSize(new Dimension(300, 50));

        return p;
    }

/**
*
*
*
**/

    public int showDialog() {
        nameField.setText("");
        maybeEnableOKButton();
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                nameField.requestFocus();
            }
        });
        return super.showDialog();
    }

    public boolean inputOK() {
        return !(nameField.getText().trim().equals(""));
    }

}