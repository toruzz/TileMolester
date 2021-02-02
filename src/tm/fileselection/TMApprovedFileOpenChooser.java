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

package tm.fileselection;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;

/**
*
* A filechooser that verifies that the selected file exists and
* that it is readable before it lets the user proceed.
*
**/

    public  class TMApprovedFileOpenChooser extends JFileChooser {

        public void approveSelection() {
            File file = getSelectedFile();
            // verify that it has proper extension
            FileFilter ff = getFileFilter();
            if (!ff.accept(file)) {
                String absPath = file.getAbsolutePath();
                if (!absPath.endsWith(".")) absPath += ".";
                String[] exts = ((TMFileFilter)ff).getExtensions();
                File tempFile=null;
                for (int i=0; i<exts.length; i++) {
                    String tempPath = absPath + exts[i];
                    tempFile = new File(tempPath);
                    if (tempFile.exists()) {
                        setSelectedFile(tempFile);
                        break;
                    }
                }
            }
            // verify that file exists and can be read
            file = getSelectedFile();
            if (!file.exists()) {
                JOptionPane.showMessageDialog(getParent(),
                    "The file '"+file.getName()+"' does not exist.",    // i18n
                    "",
                    JOptionPane.ERROR_MESSAGE);
            }
            else if (!file.canRead()) {
                JOptionPane.showMessageDialog(getParent(),
                    "The file '"+file.getName()+"' cannot be read.\nIt may be in use by another application.",  // i18n
                    "",
                    JOptionPane.ERROR_MESSAGE);
            }
            else {
                super.approveSelection();
            }
        }

    }
