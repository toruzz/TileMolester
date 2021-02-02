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

import java.awt.*;
import javax.swing.*;

/**
*
* Fancy about dialog. TODO.
*
**/

public class TMAboutDialog extends JDialog {

    public TMAboutDialog(Frame owner) {
        super(owner, "About Tile Molester");
        setLayout(new BorderLayout());
    }

}