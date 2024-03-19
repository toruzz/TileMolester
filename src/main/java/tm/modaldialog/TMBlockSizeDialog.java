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
* The dialog where user can enter new dimensions for the block.
*
**/

public class TMBlockSizeDialog extends TMModalDialog {

    private JLabel colsLabel;
    private JLabel rowsLabel;
    private JTextField colsField;
    private JTextField rowsField;

/**
*
* Creates the block size dialog.
*
**/

    public TMBlockSizeDialog(Frame owner, tm.utils.Xlator xl) {
        super(owner, "Block_Size_Dialog_Title", xl);
    }

/**
*
*
*
**/

    public int getCols() {
        return Integer.parseInt(colsField.getText());
    }

/**
*
*
*
**/

    public int getRows() {
        return Integer.parseInt(rowsField.getText());
    }

/**
*
*
*
**/

    protected JPanel getDialogPane() {
        colsLabel = new JLabel(xlate("Columns_Prompt"));
        rowsLabel = new JLabel(xlate("Rows_Prompt"));
        colsField = new JTextField();
        rowsField = new JTextField();

        JPanel colsPane = new JPanel();
        colsPane.setLayout(new BoxLayout(colsPane, BoxLayout.X_AXIS));
        colsPane.add(colsLabel);
        colsPane.add(colsField);

        JPanel rowsPane = new JPanel();
        rowsPane.setLayout(new BoxLayout(rowsPane, BoxLayout.X_AXIS));
        rowsPane.add(rowsLabel);
        rowsPane.add(rowsField);

        JPanel p = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        p.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        buildConstraints(gbc, 0, 0, 1, 1, 100, 50);
        gbl.setConstraints(colsPane, gbc);
        p.add(colsPane);

        buildConstraints(gbc, 0, 1, 1, 1, 100, 50);
        gbl.setConstraints(rowsPane, gbc);
        p.add(rowsPane);

        p.setPreferredSize(new Dimension(200, 60));

        colsField.setColumns(2);
        rowsField.setColumns(2);

        colsField.addKeyListener(new DecimalNumberVerifier());
        colsField.getDocument().addDocumentListener(new TMDocumentListener());
        rowsField.addKeyListener(new DecimalNumberVerifier());
        rowsField.getDocument().addDocumentListener(new TMDocumentListener());

        return p;
    }

    public int showDialog(int initialCols, int initialRows) {
        colsField.setText(Integer.toString(initialCols));
        rowsField.setText(Integer.toString(initialRows));
        maybeEnableOKButton();
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                colsField.requestFocus();
            }
        });
        return super.showDialog();
    }

    public boolean inputOK() {
        return (!colsField.getText().equals("") && !rowsField.getText().equals("")
            && (getCols() > 0) && (getRows() > 0));
    }

}