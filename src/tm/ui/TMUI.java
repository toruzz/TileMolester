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

import tm.*;
import tm.colorcodecs.*;
import tm.tilecodecs.*;
import tm.fileselection.*;
import tm.modaldialog.*;
import tm.treenodes.*;
import tm.utils.*;
import tm.threads.*;
import tm.filelistener.*;
import tm.canvases.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import java.util.Locale;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
*
* The main UI window.
* Has a desktop for child frames, a menu, toolbars, a palette panel, and a statusbar.
* The code is mainly dominated by
* 1) setting up the various menus and toolbars; and
* 2) providing action handlers for menu items and tool buttons.
*
**/

public class TMUI extends JFrame {

    // tool types
    public static final int SELECT_TOOL = 1;
    public static final int ZOOM_TOOL = 2;
    public static final int PICKUP_TOOL = 3;
    public static final int BRUSH_TOOL = 4;
    public static final int LINE_TOOL = 5;
    public static final int FILL_TOOL = 6;
    public static final int REPLACE_TOOL = 7;
    public static final int MOVE_TOOL = 8;

    private int tool = SELECT_TOOL;
    private int previousTool;

    private int maxRecentFiles = 10;
    private Vector recentFiles = new Vector();
    private Vector colorcodecs;
    private Vector tilecodecs;
    private Vector filefilters;
    private Vector palettefilters;
    private Vector filelisteners;

    private TMSelectionCanvas copiedSelection=null;

    // UI components
    private mxScrollableDesktop desktop = new mxScrollableDesktop();
    private TMStatusBar statusBar = new TMStatusBar();
    private JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
    private JToolBar toolPalette = new JToolBar(JToolBar.VERTICAL);
    private JToolBar selectionToolBar = new JToolBar(JToolBar.VERTICAL);
    private JToolBar navBar = new JToolBar(JToolBar.HORIZONTAL);
    private JMenuBar menuBar = new JMenuBar();
    private JPanel toolPane = new JPanel();     // the drawing tools and such
    private JPanel toolBarPane = new JPanel();  // the program toolbars
    private JPanel bottomPane = new JPanel();   // palette and statusbar
    private TMPalettePane palettePane;

    // file choosers
    private TMApprovedFileOpenChooser fileOpenChooser = new TMApprovedFileOpenChooser();
    private TMApprovedFileSaveChooser fileSaveChooser = new TMApprovedFileSaveChooser();
    private TMApprovedFileOpenChooser bitmapOpenChooser = new TMApprovedFileOpenChooser();
    private TMApprovedFileSaveChooser bitmapSaveChooser = new TMApprovedFileSaveChooser();
    private TMApprovedFileOpenChooser paletteOpenChooser = new TMApprovedFileOpenChooser();

    private TMBitmapFilters bmf = new TMBitmapFilters();
    private TMFileFilter allFilter = new AllFilter();

    // custom dialogs
    private TMGoToDialog goToDialog;
    private TMNewFileDialog newFileDialog;
    private TMCustomCodecDialog customCodecDialog;
    private TMStretchDialog stretchDialog;
    private TMCanvasSizeDialog canvasSizeDialog;
    private TMBlockSizeDialog blockSizeDialog;
    private TMAddToTreeDialog addBookmarkDialog;
    private TMAddToTreeDialog addPaletteDialog;
    private TMOrganizeTreeDialog organizeBookmarksDialog;
    private TMOrganizeTreeDialog organizePalettesDialog;
    private TMNewPaletteDialog newPaletteDialog;
    private TMPaletteSizeDialog paletteSizeDialog;
    private TMImportInternalPaletteDialog importInternalPaletteDialog;

    // toolbar buttons
    ClassLoader cl = getClass().getClassLoader();
    private ToolButton newButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/New24.gif")));
    private ToolButton openButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Open24.gif")));
    private ToolButton saveButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Save24.gif")));
    private ToolButton cutButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Cut24.gif")));
    private ToolButton copyButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Copy24.gif")));
    private ToolButton pasteButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Paste24.gif")));
    private ToolButton undoButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Undo24.gif")));
    private ToolButton redoButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Redo24.gif")));
    private ToolButton gotoButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Import24.gif")));
    private ToolButton addBookmarkButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Bookmarks24.gif")));
    private ToolButton decWidthButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/DecWidth24.gif")));
    private ToolButton incWidthButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/IncWidth24.gif")));
    private ToolButton decHeightButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/DecHeight24.gif")));
    private ToolButton incHeightButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/IncHeight24.gif")));

    // navigation bar buttons
    private ToolButton minusPageButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Rewind24.gif")));
    private ToolButton minusRowButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/StepBack24.gif")));
    private ToolButton minusTileButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Back24.gif")));
    private ToolButton minusByteButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Minus24.gif")));
    private ToolButton plusByteButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Plus24.gif")));
    private ToolButton plusTileButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Forward24.gif")));
    private ToolButton plusRowButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/StepForward24.gif")));
    private ToolButton plusPageButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/FastForward24.gif")));

    // tool palette buttons
    private ToolToggleButton selectButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Selection24.gif")));
    private ToolToggleButton zoomButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Zoom24.gif")));
    private ToolToggleButton pickupButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Dropper24.gif")));
    private ToolToggleButton brushButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Brush24.gif")));
    private ToolToggleButton lineButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Line24.gif")));
    private ToolToggleButton fillButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Fill24.gif")));
    private ToolToggleButton replaceButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/ColorReplacer24.gif")));
    private ToolToggleButton moveButton = new ToolToggleButton(new ImageIcon(cl.getResource("tm/icons/Mover24.gif")));

    // selection palette buttons
    private ToolButton mirrorButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Mirror24.gif")));
    private ToolButton flipButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/Flip24.gif")));
    private ToolButton rotateRightButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/RotateRight24.gif")));
    private ToolButton rotateLeftButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/RotateLeft24.gif")));
    private ToolButton shiftLeftButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/ShiftLeft24.gif")));
    private ToolButton shiftRightButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/ShiftRight24.gif")));
    private ToolButton shiftUpButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/ShiftUp24.gif")));
    private ToolButton shiftDownButton = new ToolButton(new ImageIcon(cl.getResource("tm/icons/ShiftDown24.gif")));

    // File menu
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem newMenuItem = new JMenuItem("New...");
    private JMenuItem openMenuItem = new JMenuItem("Open...");
    private JMenu reopenMenu = new JMenu("Reopen");
    private JMenuItem closeMenuItem = new JMenuItem("Close");
    private JMenuItem closeAllMenuItem = new JMenuItem("Close All");
    private JMenuItem saveMenuItem = new JMenuItem("Save");
    private JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    private JMenuItem saveAllMenuItem = new JMenuItem("Save All");
    private JMenuItem exitMenuItem = new JMenuItem("Exit");
    // Edit menu
    private JMenu editMenu = new JMenu("Edit");
    private JMenuItem undoMenuItem = new JMenuItem("Undo");
    private JMenuItem redoMenuItem = new JMenuItem("Redo");
    private JMenuItem cutMenuItem = new JMenuItem("Cut");
    private JMenuItem copyMenuItem = new JMenuItem("Copy");
    private JMenuItem pasteMenuItem = new JMenuItem("Paste");
    private JMenuItem clearMenuItem = new JMenuItem("Clear");
    private JMenuItem selectAllMenuItem = new JMenuItem("Select All");
    private JMenuItem copyToMenuItem = new JMenuItem("Copy To...");
    private JMenuItem pasteFromMenuItem = new JMenuItem("Paste From...");
    // Image menu
    private JMenu imageMenu = new JMenu("Image");
    private JMenuItem mirrorMenuItem = new JMenuItem("Mirror");
    private JMenuItem flipMenuItem = new JMenuItem("Flip");
    private JMenuItem rotateRightMenuItem = new JMenuItem("Rotate Right");
    private JMenuItem rotateLeftMenuItem = new JMenuItem("Rotate Left");
    private JMenuItem shiftLeftMenuItem = new JMenuItem("Shift Left");
    private JMenuItem shiftRightMenuItem = new JMenuItem("Shift Right");
    private JMenuItem shiftUpMenuItem = new JMenuItem("Shift Up");
    private JMenuItem shiftDownMenuItem = new JMenuItem("Shift Down");
    private JMenuItem canvasSizeMenuItem = new JMenuItem("Canvas Size...");
    private JMenuItem stretchMenuItem = new JMenuItem("Stretch...");
    // View menu
    private JMenu viewMenu = new JMenu("View");
    private JCheckBoxMenuItem statusBarMenuItem = new JCheckBoxMenuItem("Statusbar");
    private JCheckBoxMenuItem toolBarMenuItem = new JCheckBoxMenuItem("Toolbar");
    private JMenu tileCodecMenu = new JMenu("Codec");
    private JMenu zoomMenu = new JMenu("Zoom");
    private JMenuItem zoomInMenuItem = new JMenuItem("In");
    private JMenuItem zoomOutMenuItem = new JMenuItem("Out");
    private JMenuItem _100MenuItem = new JMenuItem("100%");
    private JMenuItem _200MenuItem = new JMenuItem("200%");
    private JMenuItem _400MenuItem = new JMenuItem("400%");
    private JMenuItem _800MenuItem = new JMenuItem("800%");
    private JMenuItem _1600MenuItem = new JMenuItem("1600%");
    private JMenuItem _3200MenuItem = new JMenuItem("3200%");
    private JMenu blockSizeMenu = new JMenu("Block Size");
    private JCheckBoxMenuItem sizeBlockToCanvasMenuItem = new JCheckBoxMenuItem("Full Canvas");
    private JMenuItem customBlockSizeMenuItem = new JMenuItem("Custom...");
    private JRadioButtonMenuItem rowInterleaveBlocksMenuItem = new JRadioButtonMenuItem("Row-interleave Blocks");
    private JMenu modeMenu = new JMenu("Mode");
    private JRadioButtonMenuItem _1DimensionalMenuItem = new JRadioButtonMenuItem("1-Dimensional");
    private JRadioButtonMenuItem _2DimensionalMenuItem = new JRadioButtonMenuItem("2-Dimensional");
    private JCheckBoxMenuItem blockGridMenuItem = new JCheckBoxMenuItem("Block Grid");
    private JCheckBoxMenuItem tileGridMenuItem = new JCheckBoxMenuItem("Tile Grid");
    private JCheckBoxMenuItem pixelGridMenuItem = new JCheckBoxMenuItem("Pixel Grid");
    // Navigate menu
    private JMenu navigateMenu = new JMenu("Navigate");
    private JMenuItem goToMenuItem = new JMenuItem("Go To...");
    private JMenuItem goToAgainMenuItem = new JMenuItem("Go To Again");
    private JMenuItem addToBookmarksMenuItem = new JMenuItem("Add To Bookmarks...");
    private JMenuItem organizeBookmarksMenuItem = new JMenuItem("Organize Bookmarks...");
    // private JMenuItem saveBookmarksMenuItem = new JMenuItem("Save Bookmarks");
    // Palette menu
    private JMenu paletteMenu = new JMenu("Palette");
    private JMenuItem editColorsMenuItem = new JMenuItem("Edit Colors...");
    private JMenu colorCodecMenu = new JMenu("Format");
    private JMenu paletteEndiannessMenu = new JMenu("Endianness");
    private JRadioButtonMenuItem paletteLittleEndianMenuItem = new JRadioButtonMenuItem("Little");
    private JRadioButtonMenuItem paletteBigEndianMenuItem = new JRadioButtonMenuItem("Big");
    private JRadioButtonMenuItem dummyPaletteMenuItem = new JRadioButtonMenuItem();
    private JMenuItem paletteSizeMenuItem = new JMenuItem("Size...");
    private JMenuItem newPaletteMenuItem = new JMenuItem("New...");
    private JMenu importPaletteMenu = new JMenu("Import From");
    private JMenuItem importInternalPaletteMenuItem = new JMenuItem("This File...");
    private JMenuItem importExternalPaletteMenuItem = new JMenuItem("Another File...");
    private JMenuItem addToPalettesMenuItem = new JMenuItem("Add To Palettes...");
    private JMenuItem organizePalettesMenuItem = new JMenuItem("Organize Palettes...");
//    private JMenuItem savePalettesMenuItem = new JMenuItem("Save Palettes");
//    private JMenuItem exportPaletteMenuItem = new JMenuItem("Export...");  // tpl, c, asm, java?
    // Window menu
    private JMenu windowMenu = new JMenu("Window");
    private JMenuItem newWindowMenuItem = new JMenuItem("New Window");
    private JMenuItem tileMenuItem = new JMenuItem("Tile");
    private JMenuItem cascadeMenuItem = new JMenuItem("Cascade");
    private JMenuItem arrangeIconsMenuItem = new JMenuItem("Arrange Icons");
    // Help menu
    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem helpTopicsMenuItem = new JMenuItem("Help Topics");
    private JMenuItem tipMenuItem = new JMenuItem("Tip of the Millennium...");
    private JMenuItem aboutMenuItem = new JMenuItem("About Tile Molester...");

    // button groups
    private ButtonGroup toolButtonGroup = new ButtonGroup();
    private ButtonGroup colorCodecButtonGroup = new ButtonGroup();
    private ButtonGroup tileCodecButtonGroup = new ButtonGroup();
    private ButtonGroup paletteButtonGroup = new ButtonGroup();
    private ButtonGroup modeButtonGroup = new ButtonGroup();
    private ButtonGroup paletteEndiannessButtonGroup = new ButtonGroup();

    private Hashtable tileCodecButtonHashtable = new Hashtable();
    private Hashtable colorCodecButtonHashtable = new Hashtable();
    private Hashtable paletteButtonHashtable = new Hashtable();
    private Hashtable fileListenerHashtable = new Hashtable();

    private Xlator xl;
    private File settingsFile = new File("settings.xml");

    private Locale locale = Locale.getDefault();
    private boolean viewStatusBar=true;
    private boolean viewToolBar=true;

    private String lastPath = "";


/**
*
* Creates a Tile Molester UI.
*
**/

    public TMUI() {

        super("Tile Molester");

        //I tried to change the UI but it seems innatural
        /*try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }*/

        setIconImage(new ImageIcon(cl.getResource("tm/icons/TMIcon.gif")).getImage());

        if (settingsFile.exists()) {
            // load settings from file
            loadSettings();
        }
        else {
            // assume this is the first time program is run
            selectLanguage();
            // File bookmarksDir = new File("bookmarks");
            // bookmarksDir.mkdir();
            // copy bookmarks.dtd to bookmarksDir
            // File palettesDir = new File("palettes");
            // palettesDir.mkdir();
            // copy palettes.dtd to palettesDir
/*            File resourcesDir = new File("resources");
            if (!resourcesDir.exists()) {
                resourcesDir.mkdir();
                File resourceDTDFile = new File(resourcesDir, "tmres.dtd");
                try {
                    FileWriter fw = new FileWriter(resourceDTDFile);
                    fw.write(""+
                        "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"+
                        "<!ELEMENT tmres (bookmarks?, palettes?)>\n"+
                        "<!ELEMENT bookmarks (folder | bookmark)*>\n"+
                        "<!ELEMENT palettes (folder | palette)*>\n"+
                        "<!ELEMENT folder (name, (folder | bookmark | palette)*)>\n"+
                        "<!ELEMENT bookmark (description)>\n"+
                        "<!ATTLIST bookmark\n"+
                        "            offset          CDATA       #REQUIRED\n"+
                        "            width           CDATA       #REQUIRED\n"+
                        "            height          CDATA       #REQUIRED\n"+
                        "            mode            (1D | 2D)   #REQUIRED\n"+
                        "            codec           CDATA       #REQUIRED\n"+
                        "            palIndex        CDATA       #REQUIRED\n"+
                        "            palette         CDATA       #IMPLIED\n"+
                        ">\n"+
                        "<!ELEMENT palette (description, data?)>\n"+
                        "<!ATTLIST palette\n"+
                        "            id              ID          #IMPLIED\n"+
                        "            direct          (yes | no)  #REQUIRED\n"+
                        "            codec           CDATA       #REQUIRED\n"+
                        "            size            CDATA       #REQUIRED\n"+
                        "            offset          CDATA       #IMPLIED\n"+
                        "            endianness      (little | big) \"little\"\n"+
                        ">\n"+
                        "<!ELEMENT data (#PCDATA)>\n"+
                        "<!ELEMENT name (#PCDATA)>\n"+
                        "<!ELEMENT description (#PCDATA)>\n"+
                    "");
                    fw.close();
                }
                catch (Exception e) { }
            }*/
        }
        setLocale(locale);
        Locale.setDefault(locale);
        JComponent.setDefaultLocale(this.locale);

        // show splash screen
        new TMSplashScreen(this);

// create a translator
        try {
            xl = new Xlator("languages/language", locale);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                xlate("Error reading language file:")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

///////// Translate items

        // File menu
        fileMenu.setText(xlate("File"));
        newMenuItem.setText(xlate("New"));
        openMenuItem.setText(xlate("Open"));
        reopenMenu.setText(xlate("Reopen"));
        closeMenuItem.setText(xlate("Close"));
        closeAllMenuItem.setText(xlate("Close_All"));
        saveMenuItem.setText(xlate("Save"));
        saveAsMenuItem.setText(xlate("Save_As"));
        saveAllMenuItem.setText(xlate("Save_All"));
        exitMenuItem.setText(xlate("Exit"));
        // Edit menu
        editMenu.setText(xlate("Edit"));
        undoMenuItem.setText(xlate("Undo"));
        redoMenuItem.setText(xlate("Redo"));
        cutMenuItem.setText(xlate("Cut"));
        copyMenuItem.setText(xlate("Copy"));
        pasteMenuItem.setText(xlate("Paste"));
        clearMenuItem.setText(xlate("Clear"));
        selectAllMenuItem.setText(xlate("Select_All"));
        copyToMenuItem.setText(xlate("Copy_To"));
        pasteFromMenuItem.setText(xlate("Paste_From"));
        pasteFromMenuItem.setText(xlate("New_Selection"));
        pasteFromMenuItem.setText(xlate("Apply_Selection"));
        // Image menu
        imageMenu.setText(xlate("Image"));
        mirrorMenuItem.setText(xlate("Mirror"));
        flipMenuItem.setText(xlate("Flip"));
        rotateRightMenuItem.setText(xlate("Rotate_Right"));
        rotateLeftMenuItem.setText(xlate("Rotate_Left"));
        shiftLeftMenuItem.setText(xlate("Shift_Left"));
        shiftRightMenuItem.setText(xlate("Shift_Right"));
        shiftUpMenuItem.setText(xlate("Shift_Up"));
        shiftDownMenuItem.setText(xlate("Shift_Down"));
        canvasSizeMenuItem.setText(xlate("Canvas_Size"));
        stretchMenuItem.setText(xlate("Stretch"));
        // View menu
        viewMenu.setText(xlate("View"));
        statusBarMenuItem.setText(xlate("Statusbar"));
        toolBarMenuItem.setText(xlate("Toolbar"));
        tileCodecMenu.setText(xlate("Codec"));
        zoomMenu.setText(xlate("Zoom"));
        zoomInMenuItem.setText(xlate("In"));
        zoomOutMenuItem.setText(xlate("Out"));
        _100MenuItem.setText(xlate("100%"));
        _200MenuItem.setText(xlate("200%"));
        _400MenuItem.setText(xlate("400%"));
        _800MenuItem.setText(xlate("800%"));
        _1600MenuItem.setText(xlate("1600%"));
        _3200MenuItem.setText(xlate("3200%"));
        modeMenu.setText(xlate("Mode"));
        _1DimensionalMenuItem.setText(xlate("1_Dimensional"));
        _2DimensionalMenuItem.setText(xlate("2_Dimensional"));
        blockSizeMenu.setText(xlate("Block_Size"));
        sizeBlockToCanvasMenuItem.setText(xlate("Full_Canvas"));
        customBlockSizeMenuItem.setText(xlate("Custom_Block_Size"));
        rowInterleaveBlocksMenuItem.setText(xlate("Row_Interleave_Blocks"));
        blockGridMenuItem.setText(xlate("Block_Grid"));
        tileGridMenuItem.setText(xlate("Tile_Grid"));
        pixelGridMenuItem.setText(xlate("Pixel_Grid"));
        // Navigate menu
        navigateMenu.setText(xlate("Navigate"));
        goToMenuItem.setText(xlate("Go_To"));
        goToAgainMenuItem.setText(xlate("Go_To_Again"));
        addToBookmarksMenuItem.setText(xlate("Add_To_Bookmarks"));
        organizeBookmarksMenuItem.setText(xlate("Organize_Bookmarks"));
        // Palette menu
        paletteMenu.setText(xlate("Palette"));
        editColorsMenuItem.setText(xlate("Edit_Colors"));
        colorCodecMenu.setText(xlate("Format"));
        paletteEndiannessMenu.setText(xlate("Endianness"));
        paletteLittleEndianMenuItem.setText(xlate("Little_Endian"));
        paletteBigEndianMenuItem.setText(xlate("Big_Endian"));
        paletteSizeMenuItem.setText(xlate("Size"));
        newPaletteMenuItem.setText(xlate("New"));
        importPaletteMenu.setText(xlate("Import_From"));
        importInternalPaletteMenuItem.setText(xlate("This_File"));
        importExternalPaletteMenuItem.setText(xlate("Another_File"));
        addToPalettesMenuItem.setText(xlate("Add_To_Palettes"));
        organizePalettesMenuItem.setText(xlate("Organize_Palettes"));
        // Window menu
        windowMenu.setText(xlate("Window"));
        newWindowMenuItem.setText(xlate("New_Window"));
        tileMenuItem.setText(xlate("Tile"));
        cascadeMenuItem.setText(xlate("Cascade"));
        arrangeIconsMenuItem.setText(xlate("Arrange_Icons"));
        // Help menu
        helpMenu.setText(xlate("Help"));
        helpTopicsMenuItem.setText(xlate("Help_Topics"));
        tipMenuItem.setText(xlate("Tip_of_the_Millennium"));
        aboutMenuItem.setText(xlate("About_Tile_Molester"));

        UIManager.put("OptionPane.yesButtonText", xlate("Yes"));
        UIManager.put("OptionPane.noButtonText", xlate("No"));
        UIManager.put("OptionPane.cancelButtonText", xlate("Cancel"));
        UIManager.put("OptionPane.okButtonText", xlate("OK"));

        fileOpenChooser.setDialogTitle(xlate("Open_File_Dialog_Title"));
        fileSaveChooser.setDialogTitle(xlate("Save_As_Dialog_Title"));
        bitmapOpenChooser.setDialogTitle(xlate("Paste_From_Dialog_Title"));
        bitmapSaveChooser.setDialogTitle(xlate("Copy_To_Dialog_Title"));
        paletteOpenChooser.setDialogTitle(xlate("Open_Palette_Dialog_Title"));

///////// Read specs
        try {
            TMSpecReader.readSpecsFromFile(new File("tmspec.xml"));
        }
        catch (SAXParseException e) {
            JOptionPane.showMessageDialog(this,
                xlate("Parser_Parse_Error")+"\n"+
                e.getMessage()+"\n"+
                "("+e.getSystemId()+",\n"+
                "line "+e.getLineNumber()+")\n",
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (SAXException e) {
            JOptionPane.showMessageDialog(this,
                xlate("Parser_Parse_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (ParserConfigurationException e) {
            JOptionPane.showMessageDialog(this,
                xlate("Parser_Config_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                xlate("Parser_IO_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        colorcodecs = TMSpecReader.getColorCodecs();
        tilecodecs = TMSpecReader.getTileCodecs();
        filefilters = TMSpecReader.getFileFilters();
        palettefilters = TMSpecReader.getPaletteFilters();
        filelisteners = TMSpecReader.getFileListeners();

        tilecodecs.add(new _3BPPLinearTileCodec());
//////////

        // create dialogs.
        goToDialog = new TMGoToDialog(this, xl);
        newFileDialog = new TMNewFileDialog(this, xl);
//        customCodecDialog = new TMCustomCodecDialog(this, "Custom Codec", true, xl);
        stretchDialog = new TMStretchDialog(this, xl);
        canvasSizeDialog = new TMCanvasSizeDialog(this, xl);
        blockSizeDialog = new TMBlockSizeDialog(this, xl);
        addBookmarkDialog = new TMAddToTreeDialog(this, "Add_To_Bookmarks_Dialog_Title", xl);
        addPaletteDialog = new TMAddToTreeDialog(this, "Add_To_Palettes_Dialog_Title", xl);
        organizeBookmarksDialog = new TMOrganizeTreeDialog(this, "Organize_Bookmarks_Dialog_Title", xl);
        organizePalettesDialog = new TMOrganizeTreeDialog(this, "Organize_Palettes_Dialog_Title", xl);
        newPaletteDialog = new TMNewPaletteDialog(this, xl);
        paletteSizeDialog = new TMPaletteSizeDialog(this, xl);
        importInternalPaletteDialog = new TMImportInternalPaletteDialog(this, xl);

        newPaletteDialog.setCodecs(colorcodecs);
        importInternalPaletteDialog.setCodecs(colorcodecs);

        //Set up the GUI.
        // main contentpane
        JPanel pane = new JPanel();
        setContentPane(pane);
        pane.setDoubleBuffered(true);
        pane.setLayout(new BorderLayout());

        // main toolbar
        initToolBar();
        initNavBar();
        toolBarPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolBarPane.add(toolBar);
        toolBarPane.add(navBar);
        pane.add(toolBarPane, BorderLayout.NORTH);

        // desktop
        pane.add(new JScrollPane(desktop), BorderLayout.CENTER);

        // palette pane & statusbar
        palettePane = new TMPalettePane(this);
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        bottomPane.setLayout(new BorderLayout());
        bottomPane.add(palettePane, BorderLayout.CENTER);
        bottomPane.add(statusBar, BorderLayout.SOUTH);
        pane.add(bottomPane, BorderLayout.SOUTH);

        // tool palettes
        initToolPalette();
        initSelectionToolBar();
        JPanel barPane = new JPanel();
        barPane.setLayout(new GridLayout(1, 2));
        barPane.add(selectionToolBar);
        barPane.add(toolPalette);
        toolPane.setLayout(new BorderLayout());
        toolPane.add(barPane, BorderLayout.NORTH);
        pane.add(toolPane, BorderLayout.WEST);

        // menus
        initMenuBar();
        setJMenuBar(menuBar);
        buildReopenMenu();

        initTileCodecUIStuff();
        buildColorCodecsMenu();
        initPaletteOpenChooser();

        // Set up file save chooser.
        fileSaveChooser.setAcceptAllFileFilterUsed(false);
        fileSaveChooser.addChoosableFileFilter(allFilter);
        fileSaveChooser.setFileFilter(allFilter);

        // Set up bitmap open chooser.
        bitmapOpenChooser.setAcceptAllFileFilterUsed(false);
        bitmapOpenChooser.addChoosableFileFilter(bmf.supported);
        bitmapOpenChooser.addChoosableFileFilter(bmf.gif);
        bitmapOpenChooser.addChoosableFileFilter(bmf.jpeg);
        bitmapOpenChooser.addChoosableFileFilter(bmf.png);
        bitmapOpenChooser.addChoosableFileFilter(bmf.bmp);
        bitmapOpenChooser.addChoosableFileFilter(bmf.pcx);
        bitmapOpenChooser.setFileFilter(bmf.supported);

        // Set up bitmap save chooser.
        bitmapSaveChooser.setAcceptAllFileFilterUsed(false);
        bitmapSaveChooser.addChoosableFileFilter(bmf.gif);
        bitmapSaveChooser.addChoosableFileFilter(bmf.jpeg);
        bitmapSaveChooser.addChoosableFileFilter(bmf.png);
        bitmapSaveChooser.addChoosableFileFilter(bmf.bmp);
        bitmapSaveChooser.addChoosableFileFilter(bmf.pcx);
        bitmapSaveChooser.setFileFilter(bmf.png);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                doExitCommand();
            }
            public void windowActivated(WindowEvent e) {
// HACK to fix the GUI after running FCEU in fullscreen mode
//                int state = getExtendedState();
//                setExtendedState(JFrame.ICONIFIED);
//                setExtendedState(state);
            }
        });

        // Center the frame
        int inset = 128;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  screenSize.width - inset*2,
                  screenSize.height-inset*2);

        //Make dragging faster:
        desktop.putClientProperty("JDesktopPane.dragMode", "outline");

        // MDI menus and such shouldn't be shown until file loaded.
        disableMDIStuff();

        toolBarPane.setVisible(viewToolBar);

        // Show and maximize.
        setVisible(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

/**
*
* Saves program settings to file.
*
**/

    public void saveSettings() {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
        sb.append("<!DOCTYPE settings SYSTEM \"settings.dtd\">\n");
        sb.append("<settings>\n");

        sb.append(makePropertyTag("locale", locale.toString()));
        sb.append(makePropertyTag("viewStatusBar", ""+viewStatusBar));
        sb.append(makePropertyTag("viewToolBar", ""+viewToolBar));
        sb.append(makePropertyTag("maxRecentFiles", ""+maxRecentFiles));

        File recentFile = null;
        //To remember last path
        for (int i=0; i<recentFiles.size(); i++) {
            recentFile = (File)recentFiles.get(i);
            sb.append(makePropertyTag("recentFile", recentFile.getAbsolutePath()));
        }
        sb.append(makePropertyTag("lastPath", lastPath));

        sb.append("</settings>\n");

        // write to file
        try {
            FileWriter fw = new FileWriter(settingsFile);
            fw.write(sb.toString());
            fw.close();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                xlate("Save_Settings_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
        }
    }

/**
*
* Makes a property tag with the given key and value.
*
**/

    public String makePropertyTag(String key, String value) {
        return "  <property key=\""+key+"\" value=\""+value+"\"/>\n";
    }

/**
*
* Loads program settings from file.
*
**/

    public void loadSettings() {
        Document doc = null;
        try {
            doc = XMLParser.parse(settingsFile);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                xlate("Load_Settings_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
        }
        if (doc == null) return;

        Element settings = doc.getDocumentElement();
        NodeList properties = settings.getElementsByTagName("property");
        // process all the properties
        for (int i=0; i<properties.getLength(); i++) {
            // get property (key, value) pair
            Element property = (Element)properties.item(i);
            String key = property.getAttribute("key");
            String value = property.getAttribute("value");
            // handle property
            if (key.equals("locale")) {
                StringTokenizer st = new StringTokenizer(value, "_");
                if (st.countTokens() != 2) continue;
                String language = st.nextToken();
                String country = st.nextToken();
                locale = new Locale(language, country);
            }
            if (key.equals("viewStatusBar")) {
                viewStatusBar = value.equals("true");
            }
            else if (key.equals("viewToolBar")) {
                viewToolBar = value.equals("true");
            }
            else if (key.equals("maxRecentFiles")) {
                maxRecentFiles = Integer.parseInt(value);
            }
            else if (key.equals("recentFile")) {
                File file = new File(value);
                if (file.exists()) {
                    recentFiles.add(file);
                }
            }
            else if (key.equals("lastPath")) {
                lastPath = value;
            }
        }
    }

///////////////////////////////////////////////////////////////////////////////
// Begin long bunch of code for setting up menus, panels etc...

/**
*
* Sets up the toolbar.
*
**/

    private void initToolBar() {
        // New
        newButton.setToolTipText(newMenuItem.getText());
        newButton.setFocusable(false);
        newButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doNewCommand();
                }
            }
        );
        toolBar.add(newButton);
        // Open
        openButton.setToolTipText(openMenuItem.getText());
        openButton.setFocusable(false);
        openButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doOpenCommand();
                }
            }
        );
        toolBar.add(openButton);
        // Save
        saveButton.setToolTipText(saveMenuItem.getText());
        saveButton.setFocusable(false);
        saveButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSaveCommand();
                }
            }
        );
        toolBar.add(saveButton);
        //
        toolBar.addSeparator();
        // Cut
        cutButton.setToolTipText(cutMenuItem.getText());
        cutButton.setFocusable(false);
        cutButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCutCommand();
                }
            }
        );
        toolBar.add(cutButton);
        // Copy
        copyButton.setToolTipText(copyMenuItem.getText());
        copyButton.setFocusable(false);
        copyButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCopyCommand();
                }
            }
        );
        toolBar.add(copyButton);
        // Paste
        pasteButton.setToolTipText(pasteMenuItem.getText());
        pasteButton.setFocusable(false);
        pasteButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPasteCommand();
                }
            }
        );
        toolBar.add(pasteButton);
        //
        toolBar.addSeparator();
        // Undo
        undoButton.setToolTipText(undoMenuItem.getText());
        undoButton.setFocusable(false);
        undoButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doUndoCommand();
                }
            }
        );
        toolBar.add(undoButton);
        // Redo
        redoButton.setToolTipText(redoMenuItem.getText());
        redoButton.setFocusable(false);
        redoButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRedoCommand();
                }
            }
        );
        toolBar.add(redoButton);
        //
        toolBar.addSeparator();
        // Go To
        gotoButton.setToolTipText(goToMenuItem.getText());
        gotoButton.setFocusable(false);
        gotoButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doGoToCommand();
                }
            }
        );
        toolBar.add(gotoButton);
        // Add To Bookmarks...
        addBookmarkButton.setToolTipText(addToBookmarksMenuItem.getText());
        addBookmarkButton.setFocusable(false);
        addBookmarkButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doAddToBookmarksCommand();
                }
            }
        );
        toolBar.add(addBookmarkButton);
        //
        toolBar.addSeparator();
        // Decrease Width
        decWidthButton.setToolTipText(xlate("Decrease_Width"));
        decWidthButton.setFocusable(false);
        decWidthButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doDecreaseWidthCommand();
                }
            }
        );
        toolBar.add(decWidthButton);
        // Increase Width
        incWidthButton.setToolTipText(xlate("Increase_Width"));
        incWidthButton.setFocusable(false);
        incWidthButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doIncreaseWidthCommand();
                }
            }
        );
        toolBar.add(incWidthButton);
        // Decrease Height
        decHeightButton.setToolTipText(xlate("Decrease_Height"));
        decHeightButton.setFocusable(false);
        decHeightButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doDecreaseHeightCommand();
                }
            }
        );
        toolBar.add(decHeightButton);
        // Increase Height
        incHeightButton.setToolTipText(xlate("Increase_Height"));
        incHeightButton.setFocusable(false);
        incHeightButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doIncreaseHeightCommand();
                }
            }
        );
        toolBar.add(incHeightButton);
        //
        toolBar.setFocusable(false);
        toolBar.setFloatable(false);
    }

/**
*
* Sets up the navigation bar.
*
**/

    public void initNavBar() {
        // Page Back
        minusPageButton.setToolTipText(xlate("Page_Back"));
        minusPageButton.setFocusable(false);
        minusPageButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doMinusPageCommand();
                }
            }
        );
        navBar.add(minusPageButton);
        // Page Forward
        plusPageButton.setToolTipText(xlate("Page_Forward"));
        plusPageButton.setFocusable(false);
        plusPageButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPlusPageCommand();
                }
            }
        );
        navBar.add(plusPageButton);
        // Row Back
        minusRowButton.setToolTipText(xlate("Row_Back"));
        minusRowButton.setFocusable(false);
        minusRowButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doMinusRowCommand();
                }
            }
        );
        navBar.add(minusRowButton);
        // Row Forward
        plusRowButton.setToolTipText(xlate("Row_Forward"));
        plusRowButton.setFocusable(false);
        plusRowButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPlusRowCommand();
                }
            }
        );
        navBar.add(plusRowButton);
        // Tile Back
        minusTileButton.setToolTipText(xlate("Tile_Back"));
        minusTileButton.setFocusable(false);
        minusTileButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doMinusTileCommand();
                }
            }
        );
        navBar.add(minusTileButton);
        // Tile Forward
        plusTileButton.setToolTipText(xlate("Tile_Forward"));
        plusTileButton.setFocusable(false);
        plusTileButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPlusTileCommand();
                }
            }
        );
        navBar.add(plusTileButton);
        // Byte Back
        minusByteButton.setToolTipText(xlate("Byte_Back"));
        minusByteButton.setFocusable(false);
        minusByteButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doMinusByteCommand();
                }
            }
        );
        navBar.add(minusByteButton);
        // Byte Forward
        plusByteButton.setToolTipText(xlate("Byte_Forward"));
        plusByteButton.setFocusable(false);
        plusByteButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPlusByteCommand();
                }
            }
        );
        navBar.add(plusByteButton);
        //
        navBar.setFloatable(false);
        navBar.setFocusable(false);
    }

/**
*
* Sets up the tool palette.
*
**/

    private void initToolPalette() {
        // Selection
        selectButton.setToolTipText(xlate("Selection"));
        selectButton.setFocusable(false);
        selectButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = SELECT_TOOL;
                }
            }
        );
        toolPalette.add(selectButton);
        // Zoom
        zoomButton.setToolTipText(xlate("Zoom"));
        zoomButton.setFocusable(false);
        zoomButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = ZOOM_TOOL;
                }
            }
        );
        toolPalette.add(zoomButton);
        // Dropper
        pickupButton.setToolTipText(xlate("Dropper"));
        pickupButton.setFocusable(false);
        pickupButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = PICKUP_TOOL;
                }
            }
        );
        toolPalette.add(pickupButton);
        // Brush
        brushButton.setToolTipText(xlate("Brush"));
        brushButton.setFocusable(false);
        brushButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = BRUSH_TOOL;
                }
            }
        );
        toolPalette.add(brushButton);
        // Line
        lineButton.setToolTipText(xlate("Line"));
        lineButton.setFocusable(false);
        lineButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = LINE_TOOL;
                }
            }
        );
        toolPalette.add(lineButton);
        // Flood Fill
        fillButton.setToolTipText(xlate("Flood_Fill"));
        fillButton.setFocusable(false);
        fillButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = FILL_TOOL;
                }
            }
        );
        toolPalette.add(fillButton);
        // Color Replacer
        replaceButton.setToolTipText(xlate("Color_Replacer"));
        replaceButton.setFocusable(false);
        replaceButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = REPLACE_TOOL;
                }
            }
        );
        toolPalette.add(replaceButton);
        // Mover
        moveButton.setToolTipText(xlate("Mover"));
        moveButton.setFocusable(false);
        moveButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool = MOVE_TOOL;
                }
            }
        );
        toolPalette.add(moveButton);
        //
        toolPalette.setFloatable(false);
        toolPalette.setFocusable(false);

        toolButtonGroup.add(selectButton);
        toolButtonGroup.add(zoomButton);
        toolButtonGroup.add(pickupButton);
        toolButtonGroup.add(brushButton);
        toolButtonGroup.add(lineButton);
        toolButtonGroup.add(fillButton);
        toolButtonGroup.add(replaceButton);
        toolButtonGroup.add(moveButton);
        selectButton.setSelected(true); // starting tool
        toolPalette.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

/**
*
* Sets up the selection palette.
*
**/

    public void initSelectionToolBar() {
        // Mirror
        mirrorButton.setToolTipText(mirrorMenuItem.getText());
        mirrorButton.setFocusable(false);
        mirrorButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doMirrorCommand();
                }
            }
        );
        selectionToolBar.add(mirrorButton);
        // Flip
        flipButton.setToolTipText(flipMenuItem.getText());
        flipButton.setFocusable(false);
        flipButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doFlipCommand();
                }
            }
        );
        selectionToolBar.add(flipButton);
        // Rotate Right
        rotateRightButton.setToolTipText(rotateRightMenuItem.getText());
        rotateRightButton.setFocusable(false);
        rotateRightButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRotateRightCommand();
                }
            }
        );
        selectionToolBar.add(rotateRightButton);
        // Rotate Left
        rotateLeftButton.setToolTipText(rotateLeftMenuItem.getText());
        rotateLeftButton.setFocusable(false);
        rotateLeftButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRotateLeftCommand();
                }
            }
        );
        selectionToolBar.add(rotateLeftButton);
        // Shift Left
        shiftLeftButton.setToolTipText(shiftLeftMenuItem.getText());
        shiftLeftButton.setFocusable(false);
        shiftLeftButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftLeftCommand();
                }
            }
        );
        selectionToolBar.add(shiftLeftButton);
        // Shift Right
        shiftRightButton.setToolTipText(shiftRightMenuItem.getText());
        shiftRightButton.setFocusable(false);
        shiftRightButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftRightCommand();
                }
            }
        );
        selectionToolBar.add(shiftRightButton);
        // Shift Up
        shiftUpButton.setToolTipText(shiftUpMenuItem.getText());
        shiftUpButton.setFocusable(false);
        shiftUpButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftUpCommand();
                }
            }
        );
        selectionToolBar.add(shiftUpButton);
        // Shift Down
        shiftDownButton.setToolTipText(shiftDownMenuItem.getText());
        shiftDownButton.setFocusable(false);
        shiftDownButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftDownCommand();
                }
            }
        );
        selectionToolBar.add(shiftDownButton);
        //
        selectionToolBar.setFloatable(false);
        selectionToolBar.setFocusable(false);
        selectionToolBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

/**
*
* Sets up the menu bar.
*
**/

    private void initMenuBar() {
        // File menu
        fileMenu.setMnemonic(KeyEvent.VK_F);
        // New
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
        newMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doNewCommand();
                }
            }
        );
        fileMenu.add(newMenuItem);
        // Open
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
        openMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doOpenCommand();
                }
            }
        );
        fileMenu.add(openMenuItem);
        // Reopen
        reopenMenu.setMnemonic(KeyEvent.VK_R);
        fileMenu.add(reopenMenu);
        // Close
        closeMenuItem.setMnemonic(KeyEvent.VK_C);
        closeMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCloseCommand();
                }
            }
        );
        fileMenu.add(closeMenuItem);
        // Close All
        closeAllMenuItem.setMnemonic(KeyEvent.VK_E);
        closeAllMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCloseAllCommand();
                }
            }
        );
        fileMenu.add(closeAllMenuItem);
        //
        fileMenu.addSeparator();
        // Save
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        saveMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSaveCommand();
                }
            }
        );
        fileMenu.add(saveMenuItem);
        // Save As
        saveAsMenuItem.setMnemonic(KeyEvent.VK_A);
        saveAsMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSaveAsCommand();
                }
            }
        );
        fileMenu.add(saveAsMenuItem);
        // Save All
        saveAllMenuItem.setMnemonic(KeyEvent.VK_L);
        saveAllMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSaveAllCommand();
                }
            }
        );
        fileMenu.add(saveAllMenuItem);
        //
        fileMenu.addSeparator();
        // Exit
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doExitCommand();
                }
            }
        );
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        // Edit menu
        editMenu.setMnemonic(KeyEvent.VK_E);
        // Undo
        undoMenuItem.setMnemonic(KeyEvent.VK_U);
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK));
        undoMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doUndoCommand();
                }
            }
        );
        editMenu.add(undoMenuItem);
        // Redo
        redoMenuItem.setMnemonic(KeyEvent.VK_R);
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
        redoMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRedoCommand();
                }
            }
        );
        editMenu.add(redoMenuItem);
        //
        editMenu.addSeparator();
        // Cut
        cutMenuItem.setMnemonic(KeyEvent.VK_T);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
        cutMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCutCommand();
                }
            }
        );
        editMenu.add(cutMenuItem);
        // Copy
        copyMenuItem.setMnemonic(KeyEvent.VK_C);
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
        copyMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCopyCommand();
                }
            }
        );
        editMenu.add(copyMenuItem);
        // Paste
        pasteMenuItem.setMnemonic(KeyEvent.VK_P);
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
        pasteMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPasteCommand();
                }
            }
        );
        editMenu.add(pasteMenuItem);
        // Clear
        clearMenuItem.setMnemonic(KeyEvent.VK_L);
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        clearMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doClearCommand();
                }
            }
        );
        editMenu.add(clearMenuItem);
        //
        editMenu.addSeparator();
        // Select All
        selectAllMenuItem.setMnemonic(KeyEvent.VK_S);
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
        selectAllMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSelectAllCommand();
                }
            }
        );
        editMenu.add(selectAllMenuItem);
        //
        editMenu.addSeparator();
        // Copy To...
        copyToMenuItem.setMnemonic(KeyEvent.VK_O);
        copyToMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCopyToCommand();
                }
            }
        );
        editMenu.add(copyToMenuItem);
        // Paste From...
        pasteFromMenuItem.setMnemonic(KeyEvent.VK_F);
        pasteFromMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPasteFromCommand();
                }
            }
        );
        editMenu.add(pasteFromMenuItem);
        menuBar.add(editMenu);
        // View menu
        viewMenu.setMnemonic(KeyEvent.VK_V);
        // Codec submenu
        viewMenu.add(tileCodecMenu);
        // Zoom submenu
        zoomMenu.setMnemonic(KeyEvent.VK_Z);
        // In
        zoomInMenuItem.setMnemonic(KeyEvent.VK_I);
        zoomInMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomInCommand();
                }
            }
        );
        zoomMenu.add(zoomInMenuItem);
        // Out
        zoomOutMenuItem.setMnemonic(KeyEvent.VK_O);
        zoomOutMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomOutCommand();
                }
            }
        );
        zoomMenu.add(zoomOutMenuItem);
        //
        zoomMenu.addSeparator();
        // 100%
        _100MenuItem.setMnemonic(KeyEvent.VK_1);
        _100MenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomCommand(1.0);
                }
            }
        );
        zoomMenu.add(_100MenuItem);
        // 200%
        _200MenuItem.setMnemonic(KeyEvent.VK_2);
        _200MenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomCommand(2.0);
                }
            }
        );
        zoomMenu.add(_200MenuItem);
        // 400%
        _400MenuItem.setMnemonic(KeyEvent.VK_4);
        _400MenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomCommand(4.0);
                }
            }
        );
        zoomMenu.add(_400MenuItem);
        // 800%
        _800MenuItem.setMnemonic(KeyEvent.VK_8);
        _800MenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomCommand(8.0);
                }
            }
        );
        zoomMenu.add(_800MenuItem);
        // 1600%
        _1600MenuItem.setMnemonic(KeyEvent.VK_6);
        _1600MenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomCommand(16.0);
                }
            }
        );
        zoomMenu.add(_1600MenuItem);
        // 3200%
        _3200MenuItem.setMnemonic(KeyEvent.VK_3);
        _3200MenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doZoomCommand(32.0);
                }
            }
        );
        zoomMenu.add(_3200MenuItem);
        viewMenu.add(zoomMenu);
        // Mode submenu
        modeMenu.setMnemonic(KeyEvent.VK_M);
        // 1-Dimensional
        _1DimensionalMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doModeCommand(TileCodec.MODE_1D);
                    _1DimensionalMenuItem.setSelected(true);
                }
            }
        );
        modeMenu.add(_1DimensionalMenuItem);
        // 2-Dimensional
        _2DimensionalMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doModeCommand(TileCodec.MODE_2D);
                    _2DimensionalMenuItem.setSelected(true);
                }
            }
        );
        modeMenu.add(_2DimensionalMenuItem);
        viewMenu.add(modeMenu);
        // create button group for modes
        modeButtonGroup.add(_1DimensionalMenuItem);
        modeButtonGroup.add(_2DimensionalMenuItem);
        //
        viewMenu.addSeparator();
        // Block Size
        blockSizeMenu.setMnemonic(KeyEvent.VK_B);
        // Full Canvas
        sizeBlockToCanvasMenuItem.setMnemonic(KeyEvent.VK_F);
        sizeBlockToCanvasMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSizeBlockToCanvasCommand();
                }
            }
        );
        blockSizeMenu.add(sizeBlockToCanvasMenuItem);
        //
        blockSizeMenu.addSeparator();
        // Custom Block Size
        customBlockSizeMenuItem.setMnemonic(KeyEvent.VK_C);
        customBlockSizeMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCustomBlockSizeCommand();
                }
            }
        );
        blockSizeMenu.add(customBlockSizeMenuItem);
        viewMenu.add(blockSizeMenu);
        // Row-interleave Blocks
        rowInterleaveBlocksMenuItem.setMnemonic(KeyEvent.VK_R);
        rowInterleaveBlocksMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRowInterleaveBlocksCommand();
                }
            }
        );
        viewMenu.add(rowInterleaveBlocksMenuItem);
        //
        viewMenu.addSeparator();
        // Block Grid
        blockGridMenuItem.setMnemonic(KeyEvent.VK_V);
        blockGridMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doBlockGridCommand();
                }
            }
        );
        viewMenu.add(blockGridMenuItem);
        // Tile Grid
        tileGridMenuItem.setMnemonic(KeyEvent.VK_A);
        tileGridMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doTileGridCommand();
                }
            }
        );
        viewMenu.add(tileGridMenuItem);
        // Pixel Grid
        pixelGridMenuItem.setMnemonic(KeyEvent.VK_P);
        pixelGridMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPixelGridCommand();
                }
            }
        );
        viewMenu.add(pixelGridMenuItem);
        //
        viewMenu.addSeparator();
        // Statusbar
        statusBarMenuItem.setMnemonic(KeyEvent.VK_S);
        statusBarMenuItem.setSelected(viewStatusBar);
        statusBarMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doStatusBarCommand();
                }
            }
        );
        viewMenu.add(statusBarMenuItem);
        // Toolbar
        toolBarMenuItem.setMnemonic(KeyEvent.VK_T);
        toolBarMenuItem.setSelected(viewToolBar);
        toolBarMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doToolBarCommand();
                }
            }
        );
        viewMenu.add(toolBarMenuItem);
        menuBar.add(viewMenu);
        // Image menu
        imageMenu.setMnemonic(KeyEvent.VK_I);
        // Mirror
        mirrorMenuItem.setMnemonic(KeyEvent.VK_M);
        mirrorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK));
        mirrorMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doMirrorCommand();
                }
            }
        );
        imageMenu.add(mirrorMenuItem);
        // Flip
        flipMenuItem.setMnemonic(KeyEvent.VK_F);
        flipMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
        flipMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doFlipCommand();
                }
            }
        );
        imageMenu.add(flipMenuItem);
        //
        imageMenu.addSeparator();
        // Rotate Right
        rotateRightMenuItem.setMnemonic(KeyEvent.VK_O);
        rotateRightMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRotateRightCommand();
                }
            }
        );
        imageMenu.add(rotateRightMenuItem);
        // Rotate Left
        rotateLeftMenuItem.setMnemonic(KeyEvent.VK_A);
        rotateLeftMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doRotateLeftCommand();
                }
            }
        );
        imageMenu.add(rotateLeftMenuItem);
        //
        imageMenu.addSeparator();
        // Shift Left
        shiftLeftMenuItem.setMnemonic(KeyEvent.VK_L);
//        shiftLeftMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.SHIFT_MASK));
        shiftLeftMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftLeftCommand();
                }
            }
        );
        imageMenu.add(shiftLeftMenuItem);
        // Shift Right
        shiftRightMenuItem.setMnemonic(KeyEvent.VK_R);
//        shiftRightMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.SHIFT_MASK));
        shiftRightMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftRightCommand();
                }
            }
        );
        imageMenu.add(shiftRightMenuItem);
        // Shift Up
        shiftUpMenuItem.setMnemonic(KeyEvent.VK_U);
//        shiftUpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.SHIFT_MASK));
        shiftUpMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftUpCommand();
                }
            }
        );
        imageMenu.add(shiftUpMenuItem);
        // Shift Down
        shiftDownMenuItem.setMnemonic(KeyEvent.VK_D);
//        shiftDownMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.SHIFT_MASK));
        shiftDownMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doShiftDownCommand();
                }
            }
        );
        imageMenu.add(shiftDownMenuItem);
        //
        imageMenu.addSeparator();
        // Canvas Size...
        canvasSizeMenuItem.setMnemonic(KeyEvent.VK_S);
        canvasSizeMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCanvasSizeCommand();
                }
            }
        );
        imageMenu.add(canvasSizeMenuItem);
        // Stretch...
        stretchMenuItem.setMnemonic(KeyEvent.VK_E);
        stretchMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doStretchCommand();
                }
            }
        );
        imageMenu.add(stretchMenuItem);
        menuBar.add(imageMenu);
        // Navigate menu
        navigateMenu.setMnemonic(KeyEvent.VK_N);
        // Go To
        goToMenuItem.setMnemonic(KeyEvent.VK_G);
        goToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));
        goToMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doGoToCommand();
                }
            }
        );
        navigateMenu.add(goToMenuItem);
        // Go To Again
        goToAgainMenuItem.setMnemonic(KeyEvent.VK_A);
        goToAgainMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        goToAgainMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doGoToAgainCommand();
                }
            }
        );
        navigateMenu.add(goToAgainMenuItem);
        //
        navigateMenu.addSeparator();
        // Add To Bookmarks
        addToBookmarksMenuItem.setMnemonic(KeyEvent.VK_A);
        addToBookmarksMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doAddToBookmarksCommand();
                }
            }
        );
        navigateMenu.add(addToBookmarksMenuItem);
        // Organize Bookmarks
        organizeBookmarksMenuItem.setMnemonic(KeyEvent.VK_O);
        organizeBookmarksMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doOrganizeBookmarksCommand();
                }
            }
        );
        navigateMenu.add(organizeBookmarksMenuItem);
        //
        menuBar.add(navigateMenu);
        // Palette menu
        paletteMenu.setMnemonic(KeyEvent.VK_P);
        // Edit Colors...
        editColorsMenuItem.setMnemonic(KeyEvent.VK_E);
        editColorsMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doEditColorsCommand();
                }
            }
        );
        paletteMenu.add(editColorsMenuItem);
        // Format submenu
        colorCodecMenu.setMnemonic(KeyEvent.VK_F);
        paletteMenu.add(colorCodecMenu);
        // Endianness submenu
        paletteEndiannessMenu.setMnemonic(KeyEvent.VK_N);
        // Little endian
        paletteLittleEndianMenuItem.setMnemonic(KeyEvent.VK_L);
        paletteLittleEndianMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPaletteEndiannessCommand(ColorCodec.LITTLE_ENDIAN);
                }
            }
        );
        paletteEndiannessMenu.add(paletteLittleEndianMenuItem);
        // Big endian
        paletteBigEndianMenuItem.setMnemonic(KeyEvent.VK_B);
        paletteBigEndianMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPaletteEndiannessCommand(ColorCodec.BIG_ENDIAN);
                }
            }
        );
        paletteEndiannessMenu.add(paletteBigEndianMenuItem);
        // create button group for palette endianness
        paletteEndiannessButtonGroup.add(paletteLittleEndianMenuItem);
        paletteEndiannessButtonGroup.add(paletteBigEndianMenuItem);
        //
        paletteMenu.add(paletteEndiannessMenu);
        // Size...
        paletteSizeMenuItem.setMnemonic(KeyEvent.VK_S);
        paletteSizeMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doPaletteSizeCommand();
                }
            }
        );
        paletteMenu.add(paletteSizeMenuItem);
        //
        paletteMenu.addSeparator();
        // New...
        newPaletteMenuItem.setMnemonic(KeyEvent.VK_N);
        newPaletteMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doNewPaletteCommand();
                }
            }
        );
        paletteMenu.add(newPaletteMenuItem);
        // Import From submenu
        importPaletteMenu.setMnemonic(KeyEvent.VK_I);
        // Import From This File...
        importInternalPaletteMenuItem.setMnemonic(KeyEvent.VK_T);
        importInternalPaletteMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doImportInternalPaletteCommand();
                }
            }
        );
        importPaletteMenu.add(importInternalPaletteMenuItem);
        // Import From Another File...
        importExternalPaletteMenuItem.setMnemonic(KeyEvent.VK_A);
        importExternalPaletteMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doImportExternalPaletteCommand();
                }
            }
        );
        importPaletteMenu.add(importExternalPaletteMenuItem);
        paletteMenu.add(importPaletteMenu);
        //
        paletteMenu.addSeparator();
        // Add To Palettes...
        addToPalettesMenuItem.setMnemonic(KeyEvent.VK_A);
        addToPalettesMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doAddToPalettesCommand();
                }
            }
        );
        paletteMenu.add(addToPalettesMenuItem);
        // Organize Palettes...
        organizePalettesMenuItem.setMnemonic(KeyEvent.VK_O);
        organizePalettesMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doOrganizePalettesCommand();
                }
            }
        );
        paletteMenu.add(organizePalettesMenuItem);
        menuBar.add(paletteMenu);
        // Window menu
        windowMenu.setMnemonic(KeyEvent.VK_W);
        // New Window
        newWindowMenuItem.setMnemonic(KeyEvent.VK_N);
        newWindowMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doNewWindowCommand();
                }
            }
        );
        windowMenu.add(newWindowMenuItem);
        //
        windowMenu.addSeparator();
        // Tile
        tileMenuItem.setMnemonic(KeyEvent.VK_T);
        tileMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doTileCommand();
                }
            }
        );
        windowMenu.add(tileMenuItem);
        // Cascade
        cascadeMenuItem.setMnemonic(KeyEvent.VK_C);
        cascadeMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doCascadeCommand();
                }
            }
        );
        windowMenu.add(cascadeMenuItem);
        // Arrange Icons
        arrangeIconsMenuItem.setMnemonic(KeyEvent.VK_I);
        arrangeIconsMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doArrangeIconsCommand();
                }
            }
        );
        windowMenu.add(arrangeIconsMenuItem);
        menuBar.add(windowMenu);
        // Help menu
        helpMenu.setMnemonic(KeyEvent.VK_H);
        // Help Topics
        helpTopicsMenuItem.setMnemonic(KeyEvent.VK_H);
        helpTopicsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpTopicsMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doHelpTopicsCommand();
                }
            }
        );
        helpMenu.add(helpTopicsMenuItem);
        // Tip of the Millennium
        tipMenuItem.setMnemonic(KeyEvent.VK_M);
        tipMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doTipCommand();
                }
            }
        );
        helpMenu.add(tipMenuItem);
        // About
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        aboutMenuItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doAboutCommand();
                }
            }
        );
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
    }

//////////////////////////////////////////////////////////////////////////////
// Begin code for handling menu commands

/**
*
* Handles menu command "New".
* Prompts the user to enter the desired file size, then creates a new
* FileImage and a default view + palette.
*
**/

    public void doNewCommand() {
        // Show dialog for creating new file
        //TMNewFileDialog newFileDialog = new TMNewFileDialog(this, xl);
        int retVal = newFileDialog.showDialog();
        if (retVal == JOptionPane.OK_OPTION) {
            // create fileimage
            FileImage img = new FileImage(newFileDialog.getFileSize());
            new TMFileResources(img, this);
            // create view for it
            TileCodec tc = (TileCodec)tilecodecs.get(0);    // default
            TMPalette pal = new TMPalette("PAL000", TMPalette.defaultPalette, getColorCodecByID("CF01"), ColorCodec.LITTLE_ENDIAN, true);
            addViewToDesktop(createView(img, tc, pal, TileCodec.MODE_1D));
        }
    }

/**
*
* Handles menu command "Open...".
* User selects file from standard file dialog, the file is
* opened and a default view + palette is assigned.
*
**/

    public void doOpenCommand() {
        // set to directory of selected file, if there is one
        System.out.println(lastPath);
        TMView view = getSelectedView();
        if (view != null) {
            this.fileOpenChooser.setCurrentDirectory(view.getFileImage().getFile().getParentFile());
        } else if (new File(this.lastPath).exists()) {
            this.fileOpenChooser.setCurrentDirectory(new File(this.lastPath));
        } else {
            this.fileOpenChooser.setCurrentDirectory(new File("."));
        }

        // have the user select a file
        int retVal = fileOpenChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            // get the selected file and open it
            File file = fileOpenChooser.getSelectedFile();
            //updates the last path opened
            lastPath = file.getPath().substring(0,file.getPath().lastIndexOf(File.separator));
            openFile(file);
        }
    }

/**
*
* Handles menu command "Close".
* Closes a view. If it is the last (only) view of a FileImage,
* and the file is modified, the user is prompted to save the file.
*
**/

    public void doCloseCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            FileImage img = view.getFileImage();

            // check if it's the last view
            if (img.getViews().length == 1) {
                saveResources(img); // TODO
                // check if saving required/desired
                if (img.isModified()) {
                    int retVal = JOptionPane.showConfirmDialog(this,
                    xlate("Save_Changes_To")+" "+img.getName()+"?", "Tile Molester",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (retVal == JOptionPane.YES_OPTION) {
                        doSaveCommand();
                    }
                    else if (retVal == JOptionPane.NO_OPTION) {
                    }
                    else if (retVal == JOptionPane.CANCEL_OPTION) {
                        return; // return to program without saving and/or closing
                    }
                }
                // remove potential file listener
                fileListenerHashtable.remove(img.getContents());
            }

            // update recent files
            File f = new File(img.getFile().getAbsolutePath());
            addToRecentFiles(f);
            buildReopenMenu();

            // remove view from the FileImage and desktop
            img.removeView(view);
            view.dispose();
            desktop.remove(view);
            desktop.revalidate();
            desktop.repaint();

            img = null;
            view = null;
            System.gc();
        }

        desktop.setSelectedFrame(null);
        JInternalFrame[] frames = desktop.getAllFrames();
        if (frames.length == 0) {
            // no more frames left on the desktop, hide MDI menus and toolbars
            disableMDIStuff();
            setTitle("Tile Molester");
        }
        else {
            // select a random frame (Swing doesn't do it for you...)
            try {
                frames[0].setSelected(true);
            }
            catch (Exception e) { }
        }
    }

/**
*
* Saves the resources for the given fileimage to a file in XML format.
*
**/

    public void saveResources(FileImage img) {
		// TODO: should only save if # bookmarks | # of palettes > 0?
        File resourceFile = TMFileResources.getResourceFileFor(img.getFile());
        try {
            File res = new File("./resources");
            if (!res.exists()) {
                res.mkdir();
            }
            FileWriter fw = new FileWriter(resourceFile);
            fw.write(img.getResources().toXML());
            fw.close();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                xlate("Save_Resources_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveBookmarks() {
        // TODO
    }

    public void savePalettes() {
        // TODO
    }

/**
*
* Handles menu command "Close All".
* Does the same as "Close", only for all the current frames.
*
**/

    public void doCloseAllCommand() {
        JInternalFrame[] frames = desktop.getAllFrames();
        for (int i=0; i<frames.length; i++) {
            TMView view = (TMView)frames[i];
            FileImage img = view.getFileImage();
            if (img.getViews().length == 1) {
                // check if saving required/desired
                if (img.isModified()) {
                    int retVal = JOptionPane.showConfirmDialog(this,
                    xlate("Save_Changes_To")+" "+img.getName()+"?", "Tile Molester",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (retVal == JOptionPane.YES_OPTION) {
                        try {
                            view.setSelected(true);
                        }
                        catch (java.beans.PropertyVetoException x) {
                            x.printStackTrace();
                        }
                        doSaveCommand();
                    }
                    else if (retVal == JOptionPane.NO_OPTION) {
                    }
                    else if (retVal == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                // remove potential file listener
                fileListenerHashtable.remove(img.getContents());
            }
        }

        // remove them all
        for (int i=0; i<frames.length; i++) {
            TMView view = (TMView)frames[i];
            FileImage img = view.getFileImage();

            saveResources(img); // TODO

            addToRecentFiles(new File(img.getFile().getAbsolutePath()));

            // remove the view
            img.removeView(view);
            view.dispose();
            desktop.remove(view);
        }

        buildReopenMenu();
        desktop.setSelectedFrame(null);
        desktop.revalidate();
        desktop.repaint();
        disableMDIStuff();
        setTitle("Tile Molester");

        System.gc();
    }

/**
*
* Handles menu command "Save".
*
**/

    public void doSaveCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            FileImage img = view.getFileImage();
            File file = img.getFile();
            String ext = TMFileFilter.getExtension(file);

            saveResources(img); // TODO

    //        if (img.isModified()) {
                if (file.exists()) {
                    if (!file.canWrite()) {
                        JOptionPane.showMessageDialog(this,
                            xlate("File_Write_Error")+"\n"+file.getName(),
                            "Tile Molester",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        FileSaverThread thread = null;
                        byte[] contents = img.getContents();
                        try {
                            thread = new FileSaverThread(contents, file);
                        }
                        catch (Exception e) {
                            JOptionPane.showMessageDialog(this,
                                xlate("File_Save_Error")+"\n"+e.getMessage(),
                                "Tile Molester",
                                JOptionPane.ERROR_MESSAGE);
                                return;
                        }

                        // see if a filelistener should be notified
                        TMFileListener fl = (TMFileListener)fileListenerHashtable.get(contents);
                        if (fl != null) {
                            fl.fileSaving(contents, ext);
                        }

                        // save it!
                        new ProgressDialog(this, thread);
                        img.setModified(false);
                        setSaveButtonsEnabled(false);

                        if (fl != null) {
                            fl.fileLoaded(contents, ext);
                        }
                    }
                }
                else {
                    doSaveAsCommand();
                }
    //        }
        }
    }

/**
*
* Handles menu command "Save As...".
*
**/

    public void doSaveAsCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            fileSaveChooser.setCurrentDirectory(view.getFileImage().getFile().getParentFile());
            fileSaveChooser.setSelectedFile(view.getFileImage().getFile());
            int retVal = fileSaveChooser.showSaveDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fileSaveChooser.getSelectedFile();
                view.getFileImage().setFile(file);
                doSaveCommand();
                setTitle("Tile Molester - "+view.getTitle());
            }
        }
        setSaveButtonsEnabled(false);
    }

/**
*
* Handles menu command "Save All".
*
**/

    public void doSaveAllCommand() {
        JInternalFrame[] frames = desktop.getAllFrames();
        for (int i=0; i<frames.length; i++) {
            TMView view = (TMView)frames[i];
            if (view.getFileImage().isModified()) {
                try {
                    view.setSelected(true);
                }
                catch (java.beans.PropertyVetoException x) {
                    x.printStackTrace();
                }
                doSaveCommand();
            }
        }
        setSaveButtonsEnabled(false);
    }

/**
*
* Handles menu command "Exit".
*
**/

    public void doExitCommand() {
        doCloseAllCommand();
        // if all frames were closed, the operation was successful and we can exit.
        if (desktop.getAllFrames().length == 0) {
            saveSettings();
            System.exit(0);
        }
    }

/**
*
* Handles menu command "Undo".
* Extracts the top item in the Undo stack and undoes it.
* Moves the item to the Redo stack.
*
**/

    public void doUndoCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.undo();
            refreshUndoRedo();
            fileImageModified(view.getFileImage());
        }
    }

/**
*
* Handles menu command "Redo".
* Extracts the top item in the Redo stack and redoes it.
* Moves the item to the Undo stack.
*
**/

    public void doRedoCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.redo();
            refreshUndoRedo();
            fileImageModified(view.getFileImage());
        }
    }

/**
*
* Handles menu command "Cut".
* The current selection of the selected frame is cut to the
* central selection.
*
**/

    public void doCutCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            // get the frame's selection
            copiedSelection = view.getEditorCanvas().cutSelection();
            // enable relevant buttons
            pasteButton.setEnabled(true);
            pasteMenuItem.setEnabled(true);
        }
    }

/**
*
* Handles menu command "Copy".
*
**/

    public void doCopyCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            // get the frame's selection
            copiedSelection = view.getEditorCanvas().copySelection();
            // enable relevant buttons
            pasteButton.setEnabled(true);
            pasteMenuItem.setEnabled(true);
        }
    }

/**
*
* Handles menu command "Paste".
*
**/

    public void doPasteCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            if (copiedSelection != null) {
                // paste selection
                view.getEditorCanvas().paste(copiedSelection);
            }
        }
    }

/**
*
* Handles menu command "Clear".
*
**/

    public void doClearCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().clearSelection();
        }
    }

/**
*
* Handles menu command "Go To...".
* Shows a dialog where the user can enter an absolute or relative
* file offset to jump to. Then jumps to that offset.
*
**/

    public void doGoToCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMGoToDialog goToDialog = new TMGoToDialog(this, xl);
            int retVal = goToDialog.showDialog();
            if (retVal == JOptionPane.OK_OPTION) {
                if (goToDialog.getMode() == TMGoToDialog.ABSOLUTE_MODE) {
                    view.setAbsoluteOffset(goToDialog.getOffset());
                }
                else {  // RELATIVE_MODE
                    view.setRelativeOffset(goToDialog.getOffset());
                }
                view.repaint();
            }
        }
    }

/**
*
* Handles menu command "Go To Again".
* Only applicable when the preceding "Go To..." was of relative type.
*
**/

    public void doGoToAgainCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            if (goToDialog.getMode() == TMGoToDialog.ABSOLUTE_MODE) {
                view.setAbsoluteOffset(goToDialog.getOffset());
            }
            else {  // RELATIVE_MODE
                view.setRelativeOffset(goToDialog.getOffset());
            }
            view.repaint();
        }
    }

/**
*
* Handles menu command "Select All".
*
**/

    public void doSelectAllCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().selectAll();
        }
    }

/**
*
* Handles menu command "Copy To...".
*
**/

    public void doCopyToCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            int retVal = bitmapSaveChooser.showSaveDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = bitmapSaveChooser.getSelectedFile();
                try {
                    TMBitmapExporter.saveTileCanvasToFile(view.getEditorCanvas().getSelectionCanvas(), file);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        xlate("Save_Bitmap_Error")+"\n"+e.getMessage(),
                        "Tile Molester",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

/**
*
* Handles menu command "Paste From...".
*
**/

    public void doPasteFromCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            if (new File(this.lastPath).exists()) {
                this.bitmapOpenChooser.setCurrentDirectory(new File(this.lastPath));
            } else {
                this.bitmapOpenChooser.setCurrentDirectory(new File("."));
            }
            int retVal = bitmapOpenChooser.showOpenDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = bitmapOpenChooser.getSelectedFile();
                // paste it
                TMTileCanvas bitmapCanvas = null;
                try {
                    bitmapCanvas = TMBitmapImporter.loadTileCanvasFromFile(file);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        xlate("Load_Bitmap_Error")+"\n"+e.getMessage(),
                        "Tile Molester",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                TMSelectionCanvas selCanvas = new TMSelectionCanvas(
                    this, bitmapCanvas, 0, 0,
                    bitmapCanvas.getCols(),
                    bitmapCanvas.getRows()
                );
                view.getEditorCanvas().paste(selCanvas);
            }
        }
    }

/**
*
* Handles menu command "Tile".
* Code ruthlessly stolen from some guy on the Java forums. Thanks and sorry. :)
*
**/

    public void doTileCommand() {
        JInternalFrame[] frames = desktop.getAllFrames();
        // count frames that aren't iconized
        int frameCount = 0;
        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isIcon())
                frameCount++;
        }
        int rows = (int)Math.sqrt(frameCount);
        int cols = frameCount / rows;
        int extra = frameCount % rows;
        // number of columns with an extra row
        int width = desktop.getWidth() / cols;
        int height = desktop.getHeight() / rows;
        int r = 0;
        int c = 0;
        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isIcon()) {
                frames[i].reshape(c * width, r * height, width, height);
                r++;
                if (r == rows) {
                    r = 0;
                    c++;
                    if (c == cols - extra) {
                        // start adding an extra row
                        rows++;
                        height = desktop.getHeight() / rows;
                    }
                }
            }
        }
        desktop.revalidate();
    }

/**
*
* Handles menu command "Cascade".
* Code ruthlessly stolen from some guy on the Java forums. Thanks and sorry. :)
*
**/

    public void doCascadeCommand() {
        int FRAME_OFFSET = 30;
        int xpos = 0, ypos = 0;
        JInternalFrame frames[] = desktop.getAllFrames();
        int cascadeWidth = desktop.getBounds().width - 5;
        int cascadeHeight = desktop.getBounds().height - 5;
        int frameHeight = cascadeHeight - frames.length * FRAME_OFFSET;
        int frameWidth = cascadeWidth - frames.length * FRAME_OFFSET;
        for (int i = frames.length - 1; i >= 0; i--) {
            if (!frames[i].isIcon()) {
                frames[i].setLocation(xpos, ypos);
                xpos += FRAME_OFFSET;
                ypos += FRAME_OFFSET;
            }
        }
        desktop.revalidate();
    }

/**
*
* Handles menu command "Arrange Icons".
*
**/

    public void doArrangeIconsCommand() {
        JInternalFrame[] frames = desktop.getAllFrames();
        int xpos=0;
        int ypos=0;
        for (int i=0; i<frames.length; i++) {
            if (frames[i].isIcon()) {
                JInternalFrame.JDesktopIcon icon = frames[i].getDesktopIcon();
                icon.setLocation(xpos, desktop.getHeight() - icon.getHeight());
                xpos += icon.getWidth();
            }
        }
        desktop.revalidate();
    }

/**
*
* Handles menu command "Help Topics".
*
**/

    public void doHelpTopicsCommand() {
        File localizedHelpFile = new File("docs/help_"+locale.toString()+".htm");
        if (localizedHelpFile.exists()) {
            BrowserControl.displayURL("file://"+localizedHelpFile.getAbsolutePath());
        }
        else {
            BrowserControl.displayURL("file://docs/help.htm");
        }
    }

/**
*
* Handles menu command "Tip".
* Just a bogus dialog.
*
**/

    public void doTipCommand() {
    // Show Tip dialog
        JOptionPane.showConfirmDialog(this,
            xlate("Drugs_Message"), "Tile Molester",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

/**
*
* Handles menu command "About".
* Displays a small dialog with info about the program.
*
**/

public void doAboutCommand()
{
    JOptionPane.showMessageDialog(this,
            "Tile Molester v0.19\nby SnowBro 2003-2005 (v0.16)\nby Dr. MefistO 2013 (v0.17.2)\nby Mewster 2014-2015 (v0.19)", "Tile Molester",
            1);
}

/**
*
* Handles menu command "Tile Codec".
* Changes the tile codec for the current view to the specified one.
*
**/

    public void doTileCodecCommand(TileCodec codec) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setTileCodec(codec);
            refreshPalettePane();
            refreshStatusBar();
            refreshTileCodecSelection(view);
        }
    }

/**
*
* Handles menu command "Zoom".
* Zooms the current frame to the given scale (1.0 = 100%, 2.0 = 200% and so on)
*
**/

    public void doZoomCommand(double scale) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setScale(scale);
        }
    }

/**
*
* Handles menu command "Zoom In".
* Scale += 1.0
*
**/

    public void doZoomInCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setScale(view.getScale()+1.0);
        }
    }

/**
*
* Handles menu command "Zoom Out".
* Scale -= 1.0
*
**/

    public void doZoomOutCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setScale(view.getScale()-1.0);
        }
    }

/**
*
* Handles menu command "Block Grid".
*
**/

    public void doBlockGridCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setBlockGridVisible(!view.isBlockGridVisible());
            blockGridMenuItem.setSelected(view.isBlockGridVisible());
            view.repaint();
        }
    }

/**
*
* Handles menu command "Tile Grid".
*
**/

    public void doTileGridCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setTileGridVisible(!view.isTileGridVisible());
            tileGridMenuItem.setSelected(view.isTileGridVisible());
            view.repaint();
        }
    }

/**
*
* Handles menu command "Pixel Grid".
*
**/

    public void doPixelGridCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setPixelGridVisible(!view.isPixelGridVisible());
            pixelGridMenuItem.setSelected(view.isPixelGridVisible());
            view.repaint();
        }
    }

/**
*
* Handles menu command "Statusbar".
* Toggles the statusbar visibility.
*
**/

    public void doStatusBarCommand() {
        viewStatusBar = !viewStatusBar;
        statusBar.setVisible(viewStatusBar);
        statusBarMenuItem.setSelected(viewStatusBar);
    }

/**
*
* Handles menu command "Toolbar".
* Toggles the toolbar visibility.
*
**/

    public void doToolBarCommand() {
        viewToolBar = !viewToolBar;
        toolBarPane.setVisible(viewToolBar);
        toolBarMenuItem.setSelected(viewToolBar);
    }

/**
*
* Handles menu command "New Window".
* Creates a new view for the current one.
* Duplicates view settings (offset, codec, width/height etc.)
*
**/

    public void doNewWindowCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            FileImage img = view.getFileImage();
            TMView newView = createView(img, view.getTileCodec(), view.getPalette(), view.getMode());
            // TODO: Copy palette
            newView.setPalIndex(view.getPalIndex());
            newView.setFGColor(view.getFGColor());
            newView.setBGColor(view.getBGColor());
            newView.setAbsoluteOffset(view.getOffset());
            newView.setGridSize(view.getCols(), view.getRows());
            addViewToDesktop(newView);
        }
    }

/**
*
* Handles menu command "Mirror".
*
**/

    public void doMirrorCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().flipSelectionHorizontally();
        }
    }

/**
*
* Handles menu command "Flip".
*
**/

    public void doFlipCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().flipSelectionVertically();
        }
    }

/**
*
* Handles menu command "Rotate +90".
*
**/

    public void doRotateRightCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().rotateSelectionClockwise();
        }
    }

/**
*
* Handles menu command "Rotate Left".
*
**/

    public void doRotateLeftCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().rotateSelectionCounterClockwise();
        }
    }

/**
*
* Handles menu command "Shift Left".
*
**/

    public void doShiftLeftCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().shiftSelectionLeft();
        }
    }

/**
*
* Handles menu command "Shift Right".
*
**/

    public void doShiftRightCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().shiftSelectionRight();
        }
    }

/**
*
* Handles menu command "Shift Up".
*
**/

    public void doShiftUpCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().shiftSelectionUp();
        }
    }

/**
*
* Handles menu command "Shift Down".
*
**/

    public void doShiftDownCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.getEditorCanvas().shiftSelectionDown();
        }
    }

/**
*
* Handles menu command "Stretch".
*
**/

    public void doStretchCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMStretchDialog stretchDialog = new TMStretchDialog(this, xl);
            int retVal = stretchDialog.showDialog(view.getEditorCanvas().getSelectionCanvas().getCols(), view.getEditorCanvas().getSelectionCanvas().getRows());
            if (retVal == JOptionPane.OK_OPTION) {
                view.getEditorCanvas().stretchSelection(stretchDialog.getCols(), stretchDialog.getRows());
            }
        }
    }

/**
*
* Handles menu command "Canvas Size".
*
**/

    public void doCanvasSizeCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMCanvasSizeDialog canvasSizeDialog = new TMCanvasSizeDialog(this, xl);
            int retVal = canvasSizeDialog.showDialog(view.getCols(), view.getRows());
            if (retVal == JOptionPane.OK_OPTION) {
                view.setGridSize(canvasSizeDialog.getCols(), canvasSizeDialog.getRows());
                view.setScale(view.getScale());
            }
        }
    }

/**
*
* Handles menu command "Mode".
* Switches to the specified tile mode for the current frame.
* The valid modes are MODE_1D and MODE_2D.
*
**/

    public void doModeCommand(int mode) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setMode(mode);
            refreshStatusBar();
        }
    }

/**
*
*
*
**/

    public void doSizeBlockToCanvasCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setSizeBlockToCanvas(!view.getSizeBlockToCanvas());
            sizeBlockToCanvasMenuItem.setSelected(view.getSizeBlockToCanvas());
        }
    }

/**
*
*
*
**/

    public void doCustomBlockSizeCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMBlockSizeDialog blockSizeDialog = new TMBlockSizeDialog(this, xl);
            int retVal = blockSizeDialog.showDialog(view.getBlockWidth(), view.getBlockHeight());
            if (retVal == JOptionPane.OK_OPTION) {
                view.setSizeBlockToCanvas(false);
                sizeBlockToCanvasMenuItem.setSelected(false);
                view.setBlockDimensions(blockSizeDialog.getCols(), blockSizeDialog.getRows());
            }
        }
    }

/**
*
*
*
**/

    public void doRowInterleaveBlocksCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRowInterleaveBlocks(!view.getRowInterleaveBlocks());
            rowInterleaveBlocksMenuItem.setSelected(view.getRowInterleaveBlocks());
        }
    }

/**
*
*
*
**/

    public void doReopenCommand(File recentFile) {
        if (recentFile.exists() && recentFile.canRead()) {
            fileOpenChooser.setFileFilter(getTileCodecFilterForFile(recentFile));
            openFile(recentFile);
            recentFiles.remove(recentFile);
            buildReopenMenu();
        }
    }

/**
*
* Handles the menu command "Custom Codec".
*
**/

    public void doCustomCodecCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            customCodecDialog.setVisible(true);
            int retVal = 0; // TODO
            if (retVal == JOptionPane.OK_OPTION) {
                // create the codec
                int bpp = customCodecDialog.getBitsPerPixel();
                int rmask = customCodecDialog.getRedMask();
                int gmask = customCodecDialog.getBlueMask();
                int bmask = customCodecDialog.getGreenMask();
                int amask = customCodecDialog.getAlphaMask();
                // int endianness = customCodecDialog.getEndianness();
                String desc = customCodecDialog.getDescription();
                DirectColorTileCodec codec = new DirectColorTileCodec("", bpp, rmask, gmask, bmask, amask, desc);
                addTileCodec(codec);
                view.setTileCodec(codec);
            }
        }
    }

/**
*
* Navigation button press handlers.
*
**/

    public void doHomeCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setAbsoluteOffset(view.getMinOffset());
        }
    }

    public void doMinusPageCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(-view.getEditorCanvas().getPageIncrement());
        }
    }

    public void doMinusRowCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(-view.getEditorCanvas().getRowIncrement());
        }
    }

    public void doMinusTileCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(-view.getEditorCanvas().getTileIncrement());
        }
    }

    public void doMinusByteCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(-1);
        }
    }

    public void doPlusByteCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(1);
        }
    }

    public void doPlusTileCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(view.getEditorCanvas().getTileIncrement());
        }
    }

    public void doPlusRowCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(view.getEditorCanvas().getRowIncrement());
        }
    }

    public void doPlusPageCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(view.getEditorCanvas().getPageIncrement());
        }
    }

    public void doEndCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setRelativeOffset(view.getMaxOffset());
        }
    }

/**
*
* Handles the menu command "Add To Bookmarks".
*
**/

    public void doAddToBookmarksCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMAddToTreeDialog addBookmarkDialog = new TMAddToTreeDialog(this, "Add_To_Bookmarks_Dialog_Title", xl);
            int retVal = addBookmarkDialog.showDialog(view.getFileImage().getResources().getBookmarksRoot());
            if (retVal == JOptionPane.OK_OPTION) {
                // Create & Add the bookmark
                FolderNode folder = addBookmarkDialog.getFolder();
                String description = addBookmarkDialog.getDescription();
                BookmarkItemNode bookmark = view.createBookmark(addBookmarkDialog.getDescription());

                /* view.addReversibleAction(
                    new ReversibleAddBookmarkAction(
                        bookmark
                    )
                ); */

                folder.add(bookmark);

                refreshBookmarksMenu();
            }
        }
    }

/**
*
* Handles the menu command "Organize Bookmarks".
*
**/

    public void doOrganizeBookmarksCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMOrganizeTreeDialog organizeBookmarksDialog = new TMOrganizeTreeDialog(this, "Organize_Bookmarks_Dialog_Title", xl);
            organizeBookmarksDialog.showDialog(view.getFileImage().getResources().getBookmarksRoot());
            refreshBookmarksMenu();
        }
    }

/**
*
* Handles the menu command "Add To Palettes".
*
**/

    public void doAddToPalettesCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMAddToTreeDialog addPaletteDialog = new TMAddToTreeDialog(this, "Add_To_Palettes_Dialog_Title", xl);
            int retVal = addPaletteDialog.showDialog(view.getFileImage().getResources().getPalettesRoot());
            if (retVal == JOptionPane.OK_OPTION) {
                // Add the palette
                FolderNode folder = addPaletteDialog.getFolder();
                String description = addPaletteDialog.getDescription();
                PaletteItemNode palNode = new PaletteItemNode(view.getPalette(), description);
                folder.add(palNode);

                /* view.addReversibleAction(
                    new ReversibleAddPaletteAction(
                        palette
                    )
                ); */

                refreshPalettesMenu();
            }
        }
    }

/**
*
* Handles the menu command "Organize Palettes".
*
**/

    public void doOrganizePalettesCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMOrganizeTreeDialog organizePalettesDialog = new TMOrganizeTreeDialog(this, "Organize_Palettes_Dialog_Title", xl);
            organizePalettesDialog.showDialog(view.getFileImage().getResources().getPalettesRoot());
            refreshPalettesMenu();
        }
    }

/**
*
* Handles the menu command "Edit Colors".
*
**/

    public void doEditColorsCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            JOptionPane.showMessageDialog(
                this,
                "Todo.\nDouble-click on a color in the palette below to edit it.",
                "Tile Molester",
                JOptionPane.INFORMATION_MESSAGE
            );
            // let user edit the color
/*            Color newColor = JColorChooser.showDialog(this, "Edit Color", new Color(view.getFGColor()));
            if (newColor != null) {
                int rgb = newColor.getRGB();
                int colorIndex = vizualiser.getIndexOfColorAt(e.getX(), e.getY());

                /* view.addReversibleAction(
                    new ReversiblePaletteEditAction(
                        view,
                        view.getPalette(),
                        colorIndex,
                        view.getPalette().getEntryRGB(colorIndex),
                        rgb
                    )
                ;*/
/*
                view.getPalette().setEntryRGB(colorIndex, rgb);
                ui.setFGColor(rgb);
                view.getEditorCanvas().unpackPixels();
                view.getEditorCanvas().redraw();
                repaint();
            }*/
        }
    }

/**
*
* Handles the menu command "Format Palette".
*
**/

    public void doColorCodecCommand(ColorCodec codec) {
        TMView view = getSelectedView();
        if (view != null) {
            view.getPalette().setCodec(codec);
            view.getEditorCanvas().unpackPixels();
            view.getEditorCanvas().redraw();
            refreshPalettePane();
        }
    }

/**
*
* Handles the menu command "Set Palette Size".
*
**/

    public void doPaletteSizeCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMPaletteSizeDialog paletteSizeDialog = new TMPaletteSizeDialog(this, xl);
            int retVal = paletteSizeDialog.showDialog(view.getPalette().getSize());
            if (retVal == JOptionPane.OK_OPTION) {
                view.getPalette().setSize(paletteSizeDialog.getPaletteSize());
                // TODO: Check some stuff
                refreshPalettePane();
            }
        }
    }

/**
*
* Handles the menu command "New Palette".
*
**/

    public void doNewPaletteCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMNewPaletteDialog newPaletteDialog = new TMNewPaletteDialog(this, xl);
            //newPaletteDialog.setCodecs(colorcodecs);
            int retVal = newPaletteDialog.showDialog();
            if (retVal == JOptionPane.OK_OPTION) {
                // get input
                int size = newPaletteDialog.getPaletteSize();
                ColorCodec codec = newPaletteDialog.getCodec();
                int endianness = newPaletteDialog.getEndianness();

                // create the palette
                TMPalette palette = new TMPalette("ID", size, codec, endianness);

                // set the new palette
                view.setPalette(palette);
                refreshPalettePane();
                refreshPalettesMenu();
            }
        }
    }

/**
*
* Handles the menu command "Import Palette From This File".
*
**/

    public void doImportInternalPaletteCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            //TMImportInternalPaletteDialog importInternalPaletteDialog = new TMImportInternalPaletteDialog(this, xl);
            //importInternalPaletteDialog.setCodecs(colorcodecs);
            int retVal = importInternalPaletteDialog.showDialog();
            if (retVal == JOptionPane.OK_OPTION) {
                // get input
                int offset = importInternalPaletteDialog.getOffset();
                int size = importInternalPaletteDialog.getPaletteSize();
                ColorCodec codec = importInternalPaletteDialog.getCodec();
                int endianness = importInternalPaletteDialog.getEndianness();
                boolean copy = importInternalPaletteDialog.getCopy();

                // create the palette
                byte[] data = view.getFileImage().getContents();
                TMPalette palette = new TMPalette("ID", data, offset, size, codec, endianness, copy, false);

                // set the new palette
                view.setPalette(palette);
                refreshPalettePane();
                refreshPalettesMenu();
            }
        }
    }

/**
*
* Handles the menu command "Import Palette From Another File".
*
**/

    public void doImportExternalPaletteCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            if (new File(this.lastPath).exists()) {
                this.paletteOpenChooser.setCurrentDirectory(new File(this.lastPath));
            } else {
                this.paletteOpenChooser.setCurrentDirectory(new File("."));
            }
            // have the user select a file
            int retVal = paletteOpenChooser.showOpenDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                // get the selected file
                File file = paletteOpenChooser.getSelectedFile();

                // figure out palette info based on file filter
                FileFilter ff = paletteOpenChooser.getFileFilter();
                if (!(ff instanceof TMPaletteFileFilter)) {
                    ff = getPaletteFilterForFile(file);
                }
                TMPaletteFileFilter pf = (TMPaletteFileFilter)ff;
                int size = pf.getSize();
                ColorCodec codec = getColorCodecByID(pf.getCodecID());
                int offset = pf.getOffset();
                int endianness = pf.getEndianness();

                // create buffer to hold the palette data
                byte[] data = new byte[size * codec.getBytesPerPixel()];

                // read the palette data
                RandomAccessFile raf=null;
                try {
                    raf = new RandomAccessFile(file, "r");
                    raf.seek(offset);
                    raf.read(data);
                    raf.close();
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        xlate("Palette_Read_Error")+"\n"+e.getMessage(),
                        "Tile Molester",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // create the palette
                TMPalette palette = new TMPalette("ID", data, 0, size, codec, endianness, true, false);

                // set the new palette
                view.setPalette(palette);
                refreshPalettePane();
                refreshPalettesMenu();
            }
        }
    }

/**
*
* Handles menu command "Palette Endianness".
*
**/

    public void doPaletteEndiannessCommand(int endianness) {
        TMView view = getSelectedView();
        if (view != null) {
            view.getPalette().setEndianness(endianness);
        }
    }

/**
*
* Called when user has selected a bookmark to jump to from the Navigate menu.
*
**/

    public void doGotoBookmarkCommand(BookmarkItemNode bookmark) {
        TMView view = getSelectedView();
        if (view != null) {
            view.gotoBookmark(bookmark);
        }
    }

/**
*
* Called when user has selected a palette to use from the Palette menu.
*
**/

    public void doSelectPaletteCommand(TMPalette palette) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setPalette(palette);
            refreshPalettePane();
            refreshPaletteEndiannessSelection(view);
            refreshColorCodecSelection(view);
        }
    }

/**
*
*
*
**/

    public void doDecreaseWidthCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setGridSize(view.getCols()-1, view.getRows());
            view.setScale(view.getScale());
        }
    }

/**
*
*
*
**/

    public void doIncreaseWidthCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setGridSize(view.getCols()+1, view.getRows());
            view.setScale(view.getScale());
        }
    }

/**
*
*
*
**/

    public void doDecreaseHeightCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setGridSize(view.getCols(), view.getRows()-1);
            view.setScale(view.getScale());
        }
    }

/**
*
*
*
**/

    public void doIncreaseHeightCommand() {
        TMView view = getSelectedView();
        if (view != null) {
            view.setGridSize(view.getCols(), view.getRows()+1);
            view.setScale(view.getScale());
        }
    }

//////////////////////////////////////////////////////////////////////////////

/**
*
* Call this when a fileimage has been modified.
*
**/

    public void fileImageModified(FileImage img) {
        img.setModified(true);
        setSaveButtonsEnabled(true);
        saveAllMenuItem.setEnabled(true);
    }

/**
*
* Sets enabled state of save buttons.
*
**/

    public void setSaveButtonsEnabled(boolean b) {
        saveButton.setEnabled(b);
        saveMenuItem.setEnabled(b);
    }

/**
*
* Sets enabled state of undo buttons.
*
**/

    public void setUndoButtonsEnabled(boolean b) {
        undoButton.setEnabled(b);
        undoMenuItem.setEnabled(b);
    }

/**
*
* Sets enabled state of redo buttons.
*
**/

    public void setRedoButtonsEnabled(boolean b) {
        redoButton.setEnabled(b);
        redoMenuItem.setEnabled(b);
    }

/**
*
* Hides/disables MDI-specific menus and buttons.
*
**/

    public void disableMDIStuff() {
        // Hide MDI menus
        menuBar.remove(editMenu);
        menuBar.remove(viewMenu);
        menuBar.remove(imageMenu);
        menuBar.remove(navigateMenu);
        menuBar.remove(paletteMenu);
        menuBar.remove(windowMenu);
        // Hide some File menu items
        closeMenuItem.setVisible(false);
        closeAllMenuItem.setVisible(false);
        saveMenuItem.setVisible(false);
        saveAsMenuItem.setVisible(false);
        saveAllMenuItem.setVisible(false);
        // Hide some Toolbar buttons
        saveButton.setVisible(false);
        cutButton.setVisible(false);
        copyButton.setVisible(false);
        pasteButton.setVisible(false);
        undoButton.setVisible(false);
        redoButton.setVisible(false);
        gotoButton.setVisible(false);
        addBookmarkButton.setVisible(false);
        decWidthButton.setVisible(false);
        incWidthButton.setVisible(false);
        decHeightButton.setVisible(false);
        incHeightButton.setVisible(false);
        // Hide navigation bar
        navBar.setVisible(false);
        // Hide tool pane
        toolPane.setVisible(false);
        // hide bottom pane
        bottomPane.setVisible(false);
    }

/**
*
* Shows/enables MDI-specific menus and buttons.
*
**/

    public void enableMDIStuff() {
        // Show MDI menus
        menuBar.remove(helpMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(imageMenu);
        menuBar.add(navigateMenu);
        menuBar.add(paletteMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);
        // Show File menu items
        closeMenuItem.setVisible(true);
        closeAllMenuItem.setVisible(true);
        saveMenuItem.setVisible(true);
        saveAsMenuItem.setVisible(true);
        saveAllMenuItem.setVisible(true);
        saveAllMenuItem.setEnabled(false);
        // TODO: Enable previously hidden menu items w/ key accelerators
        // Show Toolbar buttons
        saveButton.setVisible(true);
        cutButton.setVisible(true);
        copyButton.setVisible(true);
        pasteButton.setVisible(true);
        undoButton.setVisible(true);
        redoButton.setVisible(true);
        gotoButton.setVisible(true);
        addBookmarkButton.setVisible(true);
        decWidthButton.setVisible(true);
        incWidthButton.setVisible(true);
        decHeightButton.setVisible(true);
        incHeightButton.setVisible(true);
        // disable some buttons
        saveButton.setEnabled(false);
        pasteButton.setEnabled(false);
        pasteMenuItem.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        // Show navigation bar
        navBar.setVisible(true);
        // Show tool pane
        toolPane.setVisible(true);
        // Maybe show statusbar
        statusBar.setVisible(viewStatusBar);
        // show bottom pane
        bottomPane.setVisible(true);
    }

/**
*
* Adds a codec to the list of available codecs and creates a menu item for it.
*
**/

    public void addTileCodec(TileCodec codec) {
        TileCodecMenuItem codecMenuItem = new TileCodecMenuItem(codec);
        tileCodecMenu.add(codecMenuItem);
        tileCodecButtonGroup.add(codecMenuItem);
        tileCodecButtonHashtable.put(codec, codecMenuItem);
    }

/**
*
* Menu item that represents a tile codec.
*
**/

    private class TileCodecMenuItem extends JRadioButtonMenuItem {

        private TileCodec codec;

    // Creates a TileCodecMenuItem for the given codec.
        public TileCodecMenuItem(TileCodec codec) {
            super(codec.getDescription());  // use description as button text
            this.codec = codec;
            // TODO: setToolTipText(exampleFormats)
            addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doTileCodecCommand(((TileCodecMenuItem)e.getSource()).getCodec());
                        setSelected(true);
                    }
                }
            );
        }

/**
*
* Gets the codec that the menu item represents.
*
**/

        public TileCodec getCodec() {
            return codec;
        }

    }

/**
*
* Gets the "successor" of the given codec, which is the next codec in
* the global list of codecs (with wraparound).
*
**/

    public TileCodec getTileCodecSuccessor(TileCodec codec) {
        int i = tilecodecs.indexOf(codec);
        if (i == tilecodecs.size()-1) {
            return (TileCodec)tilecodecs.get(0);
        }
        else {
            return (TileCodec)tilecodecs.get(i+1);
        }
    }

/**
*
* Gets the "predecessor" of the given codec, which is the previous codec in
* the global list of codecs (with wraparound).
*
**/

    public TileCodec getTileCodecPredecessor(TileCodec codec) {
        int i = tilecodecs.indexOf(codec);
        if (i == 0) {
            return (TileCodec)tilecodecs.get(tilecodecs.size()-1);
        }
        else {
            return (TileCodec)tilecodecs.get(i-1);
        }
    }

/**
*
* Just a JButton with very small insets, to avoid lots of whitespace around the
* ImageIcon.
*
**/

    private class ToolButton extends JButton {
        Insets insets=null;
        public ToolButton(String text) {
            super(text);
            insets = new Insets(2,2,2,2);
        }
        public ToolButton(ImageIcon icon) {
            super(icon);
            insets = new Insets(2,2,2,2);
        }
        public Insets getInsets() {
            return insets;
        }
    }

/**
*
* Just a JToggleButton with very small insets, to avoid lots of whitespace
* around the ImageIcon.
*
**/

    private class ToolToggleButton extends JToggleButton {
        Insets insets=null;
        public ToolToggleButton(String text) {
            super(text);
            insets = new Insets(2,2,2,2);
        }
        public ToolToggleButton(ImageIcon icon) {
            super(icon);
            insets = new Insets(2,2,2,2);
        }
        public Insets getInsets() {
            return insets;
        }
    }

/**
*
* Gets the foreground color for the current view.
*
**/

    public int getFGColor() {
        TMView view = getSelectedView();
        if (view != null) {
            return view.getFGColor();
        }
        return 0;
    }

/**
*
* Gets the background color for the current view.
*
**/

    public int getBGColor() {
        TMView view = getSelectedView();
        if (view != null) {
            return view.getBGColor();
        }
        return 0;
    }

/**
*
* Sets the foreground color for the current view.
*
**/

    public void setFGColor(int fgColor) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setFGColor(fgColor);
            palettePane.setFGColor(fgColor);
        }
    }

/**
*
* Sets the background color for the current view.
*
**/

    public void setBGColor(int bgColor) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setBGColor(bgColor);
            palettePane.setBGColor(bgColor);
        }
    }

/**
*
* Sets the palette index for the current view.
*
**/

    public void setPalIndex(int palIndex) {
        TMView view = getSelectedView();
        if (view != null) {
            view.setPalIndex(palIndex);
        }
    }

/**
*
* Gets the current tool.
*
**/

    public int getTool() {
        return tool;
    }

/**
*
* Gets the desktop.
*
**/

    public JDesktopPane getDesktop() {
        return desktop;
    }

    public static int getColorIndex(int palIndex, int bpp) {
        if (bpp > 8) bpp = 8;
        int cols = 1 << bpp;
        return palIndex * cols;
    }

/**
*
* Creates a view with the given resources/attributes.
*
**/

    public TMView createView(FileImage img, TileCodec tc, TMPalette pal, int mode) {
        TMView view = new TMView(this, img, tc);
        view.setMode(mode);
        view.setPalette(pal);
        return view;
    }

/**
*
* Adds a view to the desktop.
*
**/

    public void addViewToDesktop(TMView view) {
        desktop.add(view);
        try {
            view.setSelected(true);
        }
        catch (java.beans.PropertyVetoException x) {
            x.printStackTrace();
        }
        desktop.revalidate();
        desktop.repaint();

        if (desktop.getAllFrames().length == 1) {
            // this is the first frame, show the MDI toolbars and menus
            enableMDIStuff();
        }
    }

/**
*
* Initializes the View->Codec menu based on the tilecodecs present, and sets up
* the fileOpenChooser accordingly.
*
**/

    private void initTileCodecUIStuff() {
        buildTileCodecsMenu();
        initFileOpenChooser();
    }

/**
*
* Builds the View->Codec menu.
*
**/

    private void buildTileCodecsMenu() {
        tileCodecMenu.setMnemonic(KeyEvent.VK_C);
        tileCodecMenu.removeAll();
        for (int i=0; i<tilecodecs.size(); i++) {
            addTileCodec((TileCodec)tilecodecs.get(i));
        }

    }

/**
*
* Builds the Palette->Format menu.
*
**/

    private void buildColorCodecsMenu() {
        colorCodecMenu.setMnemonic(KeyEvent.VK_F);
        colorCodecMenu.removeAll();
        for (int i=0; i<colorcodecs.size(); i++) {
            addColorCodec((ColorCodec)colorcodecs.get(i));
        }
    }

/**
*
* Adds a codec to the list of available codecs and creates a menu item for it.
*
**/

    public void addColorCodec(ColorCodec codec) {
        ColorCodecMenuItem codecMenuItem = new ColorCodecMenuItem(codec);
        colorCodecMenu.add(codecMenuItem);
        colorCodecButtonGroup.add(codecMenuItem);
        colorCodecButtonHashtable.put(codec, codecMenuItem);
    }

/**
*
* Menu item that represents a color codec.
*
**/

    private class ColorCodecMenuItem extends JRadioButtonMenuItem {

        private ColorCodec codec;

    // Creates a ColorCodecMenuItem for the given codec.
        public ColorCodecMenuItem(ColorCodec codec) {
            super(codec.getDescription());  // use description as button text
            this.codec = codec;
            addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doColorCodecCommand(((ColorCodecMenuItem)e.getSource()).getCodec());
                        setSelected(true);
                    }
                }
            );
        }

/**
*
* Gets the codec that the menu item represents.
*
**/

        public ColorCodec getCodec() {
            return codec;
        }

    }


/**
*
* Sets up the file open chooser.
*
**/

    private void initFileOpenChooser() {
        fileOpenChooser.setAcceptAllFileFilterUsed(false);
        fileOpenChooser.resetChoosableFileFilters();
        // TODO: Sort alphabetically by description...
        String extlist = "";
        for (int i=0; i<filefilters.size(); i++) {
            TMTileCodecFileFilter cff = (TMTileCodecFileFilter)filefilters.get(i);
            fileOpenChooser.addChoosableFileFilter(cff);
            if (i > 0) extlist += ",";
            extlist += cff.getExtlist();
        }
        TMFileFilter supportedFilter = new TMFileFilter(extlist, xlate("All_Supported_Formats"));
        fileOpenChooser.addChoosableFileFilter(supportedFilter);
        fileOpenChooser.addChoosableFileFilter(allFilter);
        fileOpenChooser.setFileFilter(supportedFilter);
    }

/**
*
* Sets up the palette open chooser.
*
**/

    private void initPaletteOpenChooser() {
        paletteOpenChooser.setAcceptAllFileFilterUsed(false);
        paletteOpenChooser.resetChoosableFileFilters();
        String extlist = "";
        for (int i=0; i<palettefilters.size(); i++) {
            TMPaletteFileFilter pff = (TMPaletteFileFilter)palettefilters.get(i);
            paletteOpenChooser.addChoosableFileFilter(pff);
            if (i > 0) extlist += ",";
            extlist += pff.getExtlist();
        }
        TMFileFilter supportedFilter = new TMFileFilter(extlist, xlate("All_Supported_Formats"));
        paletteOpenChooser.addChoosableFileFilter(supportedFilter);
        paletteOpenChooser.setFileFilter(supportedFilter);
    }

/**
*
* Gets the color codec that has the specified ID, or null if no such codec exists.
*
**/

    public ColorCodec getColorCodecByID(String codecID) {
        for (int i=0; i<colorcodecs.size(); i++) {
            ColorCodec cc = (ColorCodec)colorcodecs.get(i);
            if (cc.getID().equals(codecID)) {
                return cc;
            }
        }
        return null;
    }

/**
*
* Gets the tile codec that has the specified ID, or null if no such codec exists.
*
**/

    public TileCodec getTileCodecByID(String codecID) {
        for (int i=0; i<tilecodecs.size(); i++) {
            TileCodec tc = (TileCodec)tilecodecs.get(i);
            if (tc.getID().equals(codecID)) {
                return tc;
            }
        }
        return null;
    }

/**
*
* Gets the default tile codec file filter for the specified file
* based on its extension.
*
**/

    private TMTileCodecFileFilter getTileCodecFilterForFile(File file) {
        for (int i=0; i<filefilters.size(); i++) {
            TMTileCodecFileFilter cff = (TMTileCodecFileFilter)filefilters.get(i);
            if (cff.accept(file)) {
                return cff;
            }
        }
        return (TMTileCodecFileFilter)filefilters.get(0);
    }

/**
*
* Gets the default palette file filter for the specified file
* based on its extension.
*
**/

    private TMPaletteFileFilter getPaletteFilterForFile(File file) {
        for (int i=0; i<palettefilters.size(); i++) {
            TMPaletteFileFilter pff = (TMPaletteFileFilter)palettefilters.get(i);
            if (pff.accept(file)) {
                return pff;
            }
        }
        return (TMPaletteFileFilter)palettefilters.get(0);
    }

/**
*
* Recognizes all files.
*
**/

    private class AllFilter extends TMFileFilter {

        public boolean accept(File f) {
            return true;
        }

        public String getDescription() {
            return xlate("All_Files");
        }

    }

/**
*
* Builds the menu containing all the bookmarks.
*
**/

    private void buildBookmarksMenu(FolderNode root) {
        // remove old bookmark menuitems, if any
        while (navigateMenu.getItemCount() > 5) {
            navigateMenu.remove(5);
        }

        TMTreeNode[] children = root.getChildren();
        if (children.length == 0) {
            // no bookmarks exist
        }
        else {
            // add all the bookmarks
            navigateMenu.addSeparator();
            for (int i=0; i<children.length; i++) {
                addToBookmarksMenu(children[i], navigateMenu);
            }
        }
    }

/**
*
* Recursive routine that adds the given node to the given menu.
* If the node is internal it is expanded into a menu of its own.
*
**/

    public void addToBookmarksMenu(TMTreeNode node, JMenu menu) {
        if (node instanceof BookmarkItemNode) {
            menu.add(new BookmarkMenuItem((BookmarkItemNode)node));
        }
        else {
            // folder
            JMenu subMenu = new JMenu(node.toString());
            TMTreeNode[] children = node.getChildren();
            if (children.length == 0) {
                // no bookmarks exist in this folder
                JMenuItem emptyItem = new JMenuItem("("+xlate("Empty")+")");
                emptyItem.setEnabled(false);
                subMenu.add(emptyItem);
            }
            else {
                // add all the child bookmarks/folders
                for (int i=0; i<children.length; i++) {
                    addToBookmarksMenu(children[i], subMenu);
                }
            }
            menu.add(subMenu);
        }
    }

/**
*
* Menu item that represents a bookmark.
*
**/

    private class BookmarkMenuItem extends JMenuItem {

        private BookmarkItemNode bookmark;

        // creates a bookmarkmenu item for the given bookmark.
        public BookmarkMenuItem(BookmarkItemNode bookmark) {
            super(bookmark.getDescription());
            this.bookmark = bookmark;
            addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doGotoBookmarkCommand(((BookmarkMenuItem)e.getSource()).getBookmark());
                    }
                }
            );
            setToolTipText(bookmark.getToolTipText());
        }

        public BookmarkItemNode getBookmark() {
            return bookmark;
        }

    }

/**
*
* Builds the menu containing all the palettes.
*
**/

    private void buildPalettesMenu(FolderNode root) {
        // remove old palette menuitems, if any
        while (paletteMenu.getItemCount() > 10) {
            paletteMenu.remove(10);
        }

        paletteButtonHashtable.clear();
        paletteButtonGroup = new ButtonGroup();

        TMTreeNode[] children = root.getChildren();
        if (children.length == 0) {
            // no palettes exist (shouldn't be possible)
        }
        else {
            // add all the palettes
            paletteMenu.addSeparator();
            for (int i=0; i<children.length; i++) {
                addToPalettesMenu(children[i], paletteMenu);
            }
        }
        paletteButtonGroup.add(dummyPaletteMenuItem);
    }

/**
*
* Recursive routine that adds the given node to the given menu.
* If the node is internal it is expanded into a menu of its own.
*
**/

    public void addToPalettesMenu(TMTreeNode node, JMenu menu) {
        if (node instanceof PaletteItemNode) {
            // palette
            PaletteItemNode paletteNode = (PaletteItemNode)node;
            PaletteMenuItem paletteMenuItem = new PaletteMenuItem(paletteNode);
            menu.add(paletteMenuItem);
            paletteButtonGroup.add(paletteMenuItem);
            paletteButtonHashtable.put(paletteNode.getPalette(), paletteMenuItem);
        }
        else {
            // folder
            JMenu subMenu = new JMenu(node.toString());
            TMTreeNode[] children = node.getChildren();
            if (children.length == 0) {
                // no palettes exist in this folder
                JMenuItem emptyItem = new JMenuItem("("+xlate("Empty")+")");
                emptyItem.setEnabled(false);
                subMenu.add(emptyItem);
            }
            else {
                // add all the child palettes/folders
                for (int i=0; i<children.length; i++) {
                    addToPalettesMenu(children[i], subMenu);
                }
            }
            menu.add(subMenu);
        }
    }

/**
*
* Menu item that represents a bookmark.
*
**/

    private class PaletteMenuItem extends JRadioButtonMenuItem {

        private PaletteItemNode paletteNode;

        // creates a palettemenu item for the given palette node.
        public PaletteMenuItem(PaletteItemNode paletteNode) {
            super(paletteNode.getDescription());
            this.paletteNode = paletteNode;
            addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doSelectPaletteCommand(((PaletteMenuItem)e.getSource()).getPalette());
                    }
                }
            );
            setToolTipText(paletteNode.getToolTipText());
        }

        public TMPalette getPalette() {
            return paletteNode.getPalette();
        }

    }

/**
*
* Updates various UI components (menus, statusbar, palette) to reflect the
* settings of the current frame.
*
**/

    public void viewSelected(TMView view) {
        setSaveButtonsEnabled(view.getFileImage().isModified());

        // (un)check some menu items
        TMEditorCanvas ec = view.getEditorCanvas();
        blockGridMenuItem.setSelected(ec.isBlockGridVisible());
        tileGridMenuItem.setSelected(ec.isTileGridVisible());
        pixelGridMenuItem.setSelected(ec.isPixelGridVisible());
        rowInterleaveBlocksMenuItem.setSelected(ec.getRowInterleaveBlocks());

        refreshModeSelection(view);
        refreshTileCodecSelection(view);
        refreshBlockSizeSelection(view);
        refreshPalettePane();
        refreshStatusBar();
        refreshBookmarksMenu();
        refreshPalettesMenu();
        refreshUndoRedo();

        setTitle("Tile Molester - "+view.getTitle());
    }

/**
*
* Selects the correct menu item, according to the view's block size.
*
**/

    public void refreshBlockSizeSelection(TMView view) {
        sizeBlockToCanvasMenuItem.setSelected(view.getSizeBlockToCanvas());
    }

/**
*
* Selects the correct menu item, according to the view's mode.
*
**/

    public void refreshModeSelection(TMView view) {
        // select the correct mode menu item
        if (view.getMode() == TileCodec.MODE_1D) {
            _1DimensionalMenuItem.setSelected(true);
        }
        else {
            _2DimensionalMenuItem.setSelected(true);
        }
    }

/**
*
* Selects the correct menu item, according to the view's tile codec.
*
**/

    public void refreshTileCodecSelection(TMView view) {
        ((TileCodecMenuItem)tileCodecButtonHashtable.get(view.getTileCodec())).setSelected(true);
    }

/**
*
* Reloads the palette.
*
**/

    public void refreshPalettePane() {
        TMView view = getSelectedView();
        if (view != null) {
            palettePane.viewSelected(view);
        }
    }

/**
*
* Updates the Undo/Redo buttons text+status.
*
**/

    public void refreshUndoRedo() {
        TMView view = getSelectedView();
        if (view != null) {
            setUndoButtonsEnabled(view.canUndo());
            if (view.canUndo()) {
                undoMenuItem.setText(xlate("Undo")+" "+xlate(view.getFirstUndoableAction().getPresentationName()));
            }
            else {
                undoMenuItem.setText(xlate("Cant_Undo"));
            }
            undoButton.setToolTipText(undoMenuItem.getText());

            setRedoButtonsEnabled(view.canRedo());
            if (view.canRedo()) {
                redoMenuItem.setText(xlate("Redo")+" "+xlate(view.getFirstRedoableAction().getPresentationName()));
            }
            else {
                redoMenuItem.setText(xlate("Cant_Redo"));
            }
            redoButton.setToolTipText(redoMenuItem.getText());
        }
    }

/**
*
* Sets the statusbar fields according to current view settings.
*
**/

    public void refreshStatusBar() {
        TMView view = getSelectedView();
        if (view != null) {
            statusBar.viewSelected(view);
        }
    }

/**
*
* Builds the bookmarks menu according to current file image.
*
**/

    public void refreshBookmarksMenu() {
        TMView view = getSelectedView();
        if (view != null) {
            buildBookmarksMenu(view.getFileImage().getResources().getBookmarksRoot());
        }
    }

/**
*
* Builds the palettes menu according to current file image.
*
**/

    public void refreshPalettesMenu() {
        TMView view = getSelectedView();
        if (view != null) {
            buildPalettesMenu(view.getFileImage().getResources().getPalettesRoot());
            refreshPaletteSelection(view);
            refreshPaletteEndiannessSelection(view);
            refreshColorCodecSelection(view);
        }
    }

/**
*
* Refreshes the palette selection.
*
**/

    public void refreshPaletteSelection(TMView view) {
        PaletteMenuItem item = (PaletteMenuItem)paletteButtonHashtable.get(view.getPalette());
        if (item != null) {
            item.setSelected(true);
        }
        else {
            dummyPaletteMenuItem.setSelected(true);
        }
    }

/**
*
* Refreshes the palette endianness.
*
**/

    public void refreshPaletteEndiannessSelection(TMView view) {
        if (view.getPalette().getEndianness() == ColorCodec.LITTLE_ENDIAN) {
            paletteLittleEndianMenuItem.setSelected(true);
        }
        else {
            paletteBigEndianMenuItem.setSelected(true);
        }
    }

/**
*
* Selects the correct menu item, according to the view's color codec.
*
**/

    public void refreshColorCodecSelection(TMView view) {
        ((ColorCodecMenuItem)colorCodecButtonHashtable.get(view.getPalette().getCodec())).setSelected(true);
    }

/**
*
* Opens the specified file.
*
**/

    public void openFile(File file) {
        System.gc();
        // read file
        FileLoaderThread thread = null;
        try {
            thread = new FileLoaderThread(file);
        }
        catch (OutOfMemoryError e) {
            JOptionPane.showMessageDialog(this,
                xlate("Out_Of_Memory")+"\n"+file.length()+" bytes needed to load file.", // i18n
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                xlate("Load_File_Error")+"\n"+e.getMessage(),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        ProgressDialog dialog = new ProgressDialog(this, thread);
        byte[] contents = thread.getContents();

        // see if a filelistener should receive notification
        String ext = TMFileFilter.getExtension(file);
        for (int i=0; i<filelisteners.size(); i++) {
            TMFileListener fl = (TMFileListener)filelisteners.get(i);
            if (fl.doFormatDetect(contents, ext)) {
                fileListenerHashtable.put(contents, fl);
                fl.fileLoaded(contents, ext);
                break;
            }
        }

        // create fileimage
        FileImage img = new FileImage(file, contents);
        // create resources for it
        File resourceFile = TMFileResources.getResourceFileFor(file);
        if (resourceFile.exists()) {
            // load the resources from XML document
            try {
                new TMFileResources(resourceFile, img, this);
            }
            catch (SAXException e) {
                JOptionPane.showMessageDialog(this,
                    xlate("Parser_Parse_Error")+"\n"+e.getMessage(),
                    "Tile Molester",
                    JOptionPane.ERROR_MESSAGE);
            }
            catch (ParserConfigurationException e) {
                JOptionPane.showMessageDialog(this,
                    xlate("Parser_Config_Error")+"\n"+e.getMessage(),
                    "Tile Molester",
                    JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    xlate("Parser_IO_Error")+"\n"+e.getMessage(),
                    "Tile Molester",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            // create default resources
            new TMFileResources(img, this);
        }
        // figure out mode and codec based on file filter
        FileFilter ff = fileOpenChooser.getFileFilter();
        if (!(ff instanceof TMTileCodecFileFilter)) {
            ff = getTileCodecFilterForFile(file);
        }
        int mode = ((TMTileCodecFileFilter)ff).getDefaultMode();
        TileCodec tc = getTileCodecByID(((TMTileCodecFileFilter)ff).getCodecID());
        TMPalette pal = new TMPalette("PAL000", TMPalette.defaultPalette, getColorCodecByID("CF01"), ColorCodec.LITTLE_ENDIAN, true);

        addViewToDesktop(createView(img, tc, pal, mode));

        // Remove file from recentFiles, if it's there
        for (int i=0; i<recentFiles.size(); i++) {
            File f = (File)recentFiles.get(i);
            if (f.compareTo(file) == 0) {
                recentFiles.remove(f);
                buildReopenMenu();
                break;
            }
        }

        thread.killContentsRef();
        thread = null;
        System.gc();
    }

/**
*
* Builds the menu containing most recently opened (closed) files.
*
**/

    public void buildReopenMenu() {
        reopenMenu.removeAll();
        if (recentFiles.size() == 0) {
            JMenuItem emptyItem = new JMenuItem("("+xlate("Empty")+")");
            emptyItem.setEnabled(false);
            reopenMenu.add(emptyItem);
        }
        else {
            for (int i=0; i<recentFiles.size(); i++) {
                File recentFile = (File)recentFiles.get(i);
                reopenMenu.add(new RecentFileMenuItem(recentFile));
            }
        }
    }

/**
*
* Menu item that represents a recently opened (closed) file.
*
**/

    private class RecentFileMenuItem extends JMenuItem {

        private File recentFile;

        // creates a recentfilemenuitem for the given file.
        public RecentFileMenuItem(File recentFile) {
            super(recentFile.getName());
            setToolTipText(recentFile.getAbsolutePath());
            this.recentFile = recentFile;
            addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doReopenCommand(((RecentFileMenuItem)e.getSource()).getRecentFile());
                    }
                }
            );
        }

        public File getRecentFile() {
            return recentFile;
        }

    }

/**
*
*
*
**/

    public ColorCodec[] getColorCodecs() {
        ColorCodec[] ccs = new ColorCodec[colorcodecs.size()];
        for (int i=0; i<ccs.length; i++) {
            ccs[i] = (ColorCodec)colorcodecs.get(i);
        }
        return ccs;
    }

/**
*
* Lets the user select a locale from a combobox.
*
**/

    public void selectLanguage() {
        // figure out available translations
        File dir = new File("./languages");
        File[] files = dir.listFiles(new PropertiesFilter());
        if ((files != null) && (files.length > 0)) {
            Locale[] locales = new Locale[files.length];
            String[] displayNames = new String[locales.length];
            int defaultIndex=0;
            for (int i=0; i<files.length; i++) {
                String name = files[i].getName();
                String language = name.substring(name.indexOf('_')+1, name.lastIndexOf('_'));
                String country = name.substring(name.lastIndexOf('_')+1, name.lastIndexOf('.'));
                locales[i] = new Locale(language, country);
                displayNames[i] = locales[i].getDisplayName();
                if (language.equals("en")) defaultIndex=i;
            }

            // ask user to select language
            String selectedName = (String)JOptionPane.showInputDialog(this,
                "Choose a locale:", "Tile Molester",
                JOptionPane.INFORMATION_MESSAGE, null,
                displayNames, displayNames[defaultIndex]);
            if (selectedName != null) {
                // find selected one
                for (int i=0; i<locales.length; i++) {
                    if (selectedName.equals(locales[i].getDisplayName())) {
                        // select this locale
                        this.locale = locales[i];
                        break;
                    }
                }
            }
        }
        else {
            JOptionPane.showMessageDialog(this,
                xlate("No language files found.\nPlease check your installation."),
                "Tile Molester",
                JOptionPane.ERROR_MESSAGE);
        }
    }

/**
*
* File filter that recognizes filenames of the form
* language_xx_yy.properties
*
**/

    private class PropertiesFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().startsWith("language")
                && (name.indexOf('_') != -1)
                && name.toLowerCase().endsWith(".properties"));
        }
    }

/**
*
* Attempts to translate the given key string by consulting a ResourceBundle.
* If no corresponding value is found, the key itself is returned.
*
**/

    public String xlate(String key) {
        try {
            String value = xl.xlate(key);
            return value;
        }
        catch (Exception e) {
            return key;
        }
    }

/**
*
* Gets the selected view frame.
*
**/

    public TMView getSelectedView() {
        return (TMView)desktop.getSelectedFrame();
    }

/**
*
* Adds the given file to the list of recently opened (closed) files.
*
**/
    public void addToRecentFiles(File f) {
        // make sure it's not already in the list
        for (int i=0; i<recentFiles.size(); i++) {
            File rf = (File)recentFiles.get(i);
            if (rf.compareTo(f) == 0) {
                recentFiles.remove(i);
                break;
            }
        }
        // add it
        recentFiles.insertElementAt(f, 0);
        // check for "overflow"
        if (recentFiles.size() > maxRecentFiles) {
            recentFiles.remove(maxRecentFiles-1);
        }
    }

}