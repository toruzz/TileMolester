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
* A filechooser that verifies that the selected file doesn't exist;
* if it does, the user is asked whether he wants to overwrite it
* before he can proceed.
*
**/

    public class TMApprovedFileSaveChooser extends JFileChooser {

        public void approveSelection() {
            File file = getSelectedFile();
            // verify that it has proper extension
            FileFilter ff = getFileFilter();
            if (!ff.accept(file)) {
                String absPath = file.getAbsolutePath();
                if (!absPath.endsWith(".")) absPath += ".";
                setSelectedFile(new File(absPath+((TMFileFilter)ff).getDefaultExtension()));
            }
            file = getSelectedFile();
            if (file.exists()) {
                int retVal = JOptionPane.showConfirmDialog(getParent(),
                    "The file '"+file.getName()+"' already exists.\nDo you wish to overwrite it?",  // i18n
                    "",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (retVal == JOptionPane.NO_OPTION)
                    return;
                // delete and recreate the file
                file.delete();
                try {
                    file.createNewFile();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    file.createNewFile();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!file.canWrite()) {
                JOptionPane.showMessageDialog(getParent(),
                    "The file '"+file.getName()+"' cannot be written to.",  // i18n
                    "",
                    JOptionPane.ERROR_MESSAGE);
            }
            super.approveSelection();
        }
    }
