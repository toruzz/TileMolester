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

package tm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import tm.threads.ProgressThread;

/**
*
* Modal dialog that takes a worker thread and runs it in the background while
* updating a progress bar to reflect how far along the work is.
* The dialog unblocks when the work is completely done.
*
**/

public class ProgressDialog extends JDialog implements ActionListener {

    private JProgressBar progressBar = new JProgressBar(0, 100);
    private ProgressThread thread;
    private Timer timer;

/**
*
* Creates a ProgressDialog for the given ProgressThread.
*
**/

    public ProgressDialog(Frame owner, ProgressThread thread) {
        super(owner);
        this.thread = thread;

        setModal(true);
        setUndecorated(true);
        getContentPane().add(progressBar);

        setSize(256, 64);
        center();

        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        timer = new Timer(100, this);   // how often the progress bar will be updated
        timer.start();
        thread.start();

        setVisible(true);
    }

/**
*
* Centers the dialog.
*
**/

    private void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int insetx = (screenSize.width - getWidth()) / 2;
        int insety = (screenSize.height - getHeight()) / 2;
        setBounds(insetx, insety,
                  getWidth(), getHeight());
    }

/**
*
* Updates the progress bar every timer tick.
*
**/

    public void actionPerformed(ActionEvent e) {
        progressBar.setValue(thread.getPercentageCompleted());
        if (thread.getPercentageCompleted() == 100) {
            timer.stop();
            setVisible(false);
        }
    }

}