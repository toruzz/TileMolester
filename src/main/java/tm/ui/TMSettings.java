package tm.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tm.utils.XMLParser;
import tm.utils.Xlator;

public class TMSettings extends JFrame {
	private Xlator xl;
	private File settingsFile = new File("settings.xml");
	public Locale locale = Locale.getDefault();
	public boolean viewStatusBar = true;
	public boolean viewToolBar = true;
	private boolean darkMode;
	public int maxRecentFiles = 10;
	public Vector recentFiles = new Vector();
	public String lastPath = "";

	public TMSettings() {
		super();

		if (settingsFile.exists()) {
			// load settings from file
			loadSettings();
		} else {
			// assume this is the first time program is run
			selectLanguage();
			// File bookmarksDir = new File("bookmarks");
			// bookmarksDir.mkdir();
			// copy bookmarks.dtd to bookmarksDir
			// File palettesDir = new File("palettes");
			// palettesDir.mkdir();
			// copy palettes.dtd to palettesDir
			/*
			 * File resourcesDir = new File("resources");
			 * if (!resourcesDir.exists()) {
			 * resourcesDir.mkdir();
			 * File resourceDTDFile = new File(resourcesDir, "resources\\tmres.dtd");
			 * try {
			 * FileWriter fw = new FileWriter(resourceDTDFile);
			 * fw.write(""+
			 * "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
			 * "<!ELEMENT tmres (bookmarks?, palettes?)>\n"+
			 * "<!ELEMENT bookmarks (folder | bookmark)*>\n"+
			 * "<!ELEMENT palettes (folder | palette)*>\n"+
			 * "<!ELEMENT folder (name, (folder | bookmark | palette)*)>\n"+
			 * "<!ELEMENT bookmark (description)>\n"+
			 * "<!ATTLIST bookmark\n"+
			 * "            offset          CDATA       #REQUIRED\n"+
			 * "            width           CDATA       #REQUIRED\n"+
			 * "            height          CDATA       #REQUIRED\n"+
			 * "            mode            (1D | 2D)   #REQUIRED\n"+
			 * "            codec           CDATA       #REQUIRED\n"+
			 * "            palIndex        CDATA       #REQUIRED\n"+
			 * "            palette         CDATA       #IMPLIED\n"+
			 * ">\n"+
			 * "<!ELEMENT palette (description, data?)>\n"+
			 * "<!ATTLIST palette\n"+
			 * "            id              ID          #IMPLIED\n"+
			 * "            direct          (yes | no)  #REQUIRED\n"+
			 * "            codec           CDATA       #REQUIRED\n"+
			 * "            size            CDATA       #REQUIRED\n"+
			 * "            offset          CDATA       #IMPLIED\n"+
			 * "            endianness      (little | big) \"little\"\n"+
			 * ">\n"+
			 * "<!ELEMENT data (#PCDATA)>\n"+
			 * "<!ELEMENT name (#PCDATA)>\n"+
			 * "<!ELEMENT description (#PCDATA)>\n"+
			 * "");
			 * fw.close();
			 * }
			 * catch (Exception e) { }
			 * }
			 */
		}
	}

	/**
	 *
	 * Loads program settings from file.
	 *
	 **/

	public void loadSettings() {
		boolean loadedLocale = false;
		Document doc = null;
		try {
			doc = XMLParser.parse(settingsFile);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					xlate("Load_Settings_Error") + "\n" + e.getMessage(),
					"Tile Molester",
					JOptionPane.ERROR_MESSAGE);
		}
		if (doc == null)
			return;

		Element settings = doc.getDocumentElement();
		NodeList properties = settings.getElementsByTagName("property");
		// process all the properties
		for (int i = 0; i < properties.getLength(); i++) {
			// get property (key, value) pair
			Element property = (Element) properties.item(i);
			String key = property.getAttribute("key");
			String value = property.getAttribute("value");
			// handle property
			if (key.equals("locale")) {
				StringTokenizer st = new StringTokenizer(value, "_");
				if (st.countTokens() != 2)
					continue;
				String language = st.nextToken();
				String country = st.nextToken();
				locale = new Locale(language, country);
				loadedLocale = true;
			}
			if (key.equals("viewStatusBar")) {
				viewStatusBar = value.equals("true");
			} else if (key.equals("viewToolBar")) {
				viewToolBar = value.equals("true");
			} else if (key.equals("darkMode")) {
				darkMode = value.equals("true");
				TMTheme.setDarkMode(darkMode);
				
			} else if (key.equals("maxRecentFiles")) {
				maxRecentFiles = Integer.parseInt(value);
			} else if (key.equals("recentFile")) {
				File file = new File(value);
				if (file.exists()) recentFiles.add(file);
			} else if (key.equals("lastPath")) {
				lastPath = value;
			}
		}
		if (!loadedLocale) selectLanguage();
	}


	/**
	 *
	 * Lets the user select a locale from a combobox.
	 *
	 **/

	 public void selectLanguage() {
		// figure out available translations
		File dir = new File("languages");
		File[] files = dir.listFiles(new PropertiesFilter());
		if ((files != null) && (files.length > 0)) {
			Locale[] locales = new Locale[files.length];
			String[] displayNames = new String[locales.length];
			int defaultIndex = 0;
			for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				String language = name.substring(name.indexOf('_') + 1, name.lastIndexOf('_'));
				String country = name.substring(name.lastIndexOf('_') + 1, name.lastIndexOf('.'));
				locales[i] = new Locale(language, country);
				displayNames[i] = locales[i].getDisplayName();
				if (language.equals("en"))
					defaultIndex = i;
			}

			// ask user to select language
			String selectedName = (String) JOptionPane.showInputDialog(this,
					"Choose a locale:", "Tile Molester",
					JOptionPane.INFORMATION_MESSAGE, null,
					displayNames, displayNames[defaultIndex]);
			if (selectedName != null) {
				// find selected one
				for (int i = 0; i < locales.length; i++) {
					if (selectedName.equals(locales[i].getDisplayName())) {
						// select this locale
						this.locale = locales[i];
						break;
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(this,
					xlate("No language files found.\nPlease check your installation."),
					"Tile Molester",
					JOptionPane.ERROR_MESSAGE);
		}
	}



	/**
	 *
	 * Saves program settings to file.
	 *
	 **/

	public void saveSettings() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!DOCTYPE settings SYSTEM \"settings.dtd\">\n");
		sb.append("<settings>\n");

		sb.append(makePropertyTag("locale", locale.toString()));
		sb.append(makePropertyTag("viewStatusBar", "" + viewStatusBar));
		sb.append(makePropertyTag("viewToolBar", "" + viewToolBar));
		sb.append(makePropertyTag("darkMode", "" + TMTheme.darkMode));
		sb.append(makePropertyTag("maxRecentFiles", "" + maxRecentFiles));

		File recentFile = null;
		// To remember last path
		for (int i = 0; i < recentFiles.size(); i++) {
			recentFile = (File) recentFiles.get(i);
			sb.append(makePropertyTag("recentFile", recentFile.getAbsolutePath()));
		}
		sb.append(makePropertyTag("lastPath", lastPath));

		sb.append("</settings>\n");

		// write to file
		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(settingsFile), StandardCharsets.UTF_8);

			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					xlate("Save_Settings_Error") + "\n" + e.getMessage(),
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
		return "  <property key=\"" + key + "\" value=\"" + value + "\"/>\n";
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
		} catch (Exception e) {
			return key;
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


	public int getMaxRecentFiles() {
		return maxRecentFiles;
	}

	public void setRecentFiles(Vector newRecentFiles) {
		recentFiles = newRecentFiles;
	}

	public Vector getRecentFiles() {
		return recentFiles;
	}

	public Locale getLocale() {
		return locale;
	}

	
	public void setLastPath(String path) {
		lastPath = path;
	}

	public String getLastPath() {
		return lastPath;
	}

	public void setViewStatusBar(boolean newViewStatusBar) {
		viewStatusBar = newViewStatusBar;
	}

	public void setViewToolBar(boolean newViewToolBar) {
		viewToolBar = newViewToolBar;
	}


}
