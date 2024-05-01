package tm.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

public class TMTheme {
	private static boolean isWindows = SystemInfo.isWindows;
	private static boolean isLinux = SystemInfo.isLinux;
	private static boolean isMacOs = SystemInfo.isMacOS;
	static public boolean darkMode = true;
	static public int theme = getCurrentTheme();

	public static final int WINUNIX_LIGHT_THEME = 1;
	public static final int WINUNIX_DARK_THEME = 2;
	public static final int MACOS_LIGHT_THEME = 3;
	public static final int MACOS_DARK_THEME = 4;

	public static final String WINDOWS_LIGHT_BAR_BG = "#F0F0F0";
	public static final String WINDOWS_LIGHT_WIN_BG = "#FFFFFF";
	public static final String WINDOWS_LIGHT_FRAME_BG = "#C8C8C8";
	public static final String WINDOWS_LIGHT_ACCENT = "#1C78CE";

	public static final String WINDOWS_DARK_BAR_BG = "#1E1E1E";
	public static final String WINDOWS_DARK_WIN_BG = "#46494B";
	public static final String WINDOWS_DARK_FRAME_BG = "#232627";
	public static final String WINDOWS_DARK_ACCENT = "#1C78CE";

	public static final String MACOS_LIGHT_BAR_BG = "#F0F0F0";
	public static final String MACOS_LIGHT_WIN_BG = "#FFFFFF";
	public static final String MACOS_LIGHT_FRAME_BG = "#C8C8C8";
	public static final String MACOS_LIGHT_ACCENT = "#1C78CE";

	public static final String MACOS_DARK_BAR_BG = "#292929";
	public static final String MACOS_DARK_WIN_BG = "#161616";
	public static final String MACOS_DARK_FRAME_BG = "#232627";
	public static final String MACOS_DARK_ACCENT = "#1C78CE";

	public TMTheme() {
		loadTheme();
		FlatSVGIcon.ColorFilter.getInstance().add(Color.decode("#212121"), Color.decode("#292929"), Color.decode("#e1e1e1"));

	}

	public static void loadTheme() {
		loadTheme(TMTheme.theme);
	}

	public static void loadTheme(int theme) {
		try {
			switch (theme) {
				case WINUNIX_LIGHT_THEME:
				FlatMacLightLaf.setup();
				UIManager.setLookAndFeel(new FlatMacLightLaf());
					break;
				case WINUNIX_DARK_THEME:
					FlatDarkLaf.setup();
					UIManager.setLookAndFeel(new FlatDarkLaf());
					break;
				case MACOS_LIGHT_THEME:
					FlatMacLightLaf.setup();
					UIManager.setLookAndFeel(new FlatMacLightLaf());
					break;
				case MACOS_DARK_THEME:
					FlatMacDarkLaf.setup();
					UIManager.setLookAndFeel(new FlatMacDarkLaf());
					break;
			}

			
		UIManager.put( "InternalFrameTitlePane.arc", 8 );
		UIManager.put( "InternalFrameTitlePane.background", Color.RED );
		UIManager.put( "selectedTitleColor", Color.RED );
		UIManager.put( "InternalFrameTitlePane.selectedTitleColor", Color.RED );
		UIManager.put( "Frame.selectedTitleColor", Color.RED );
		UIManager.put( "JFrame.selectedTitleColor", Color.RED );
		
			com.formdev.flatlaf.FlatLaf.updateUI();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getCurrentTheme() {
		if((isWindows || isLinux) && darkMode) return WINUNIX_DARK_THEME;
		if((isWindows || isLinux) && !darkMode) return WINUNIX_LIGHT_THEME;
		if(isMacOs && darkMode) return MACOS_DARK_THEME;
		if(isMacOs && !darkMode) return MACOS_LIGHT_THEME;
		return WINUNIX_DARK_THEME;
	}

	public static void setDarkMode(boolean darkMode) {
		TMTheme.darkMode = darkMode;
		TMTheme.theme = getCurrentTheme();
		loadTheme(TMTheme.theme);
	}


	public static Map<String, Color> getThemeColors() {
        Map<String, Color> colors = new HashMap<>();

        switch (TMTheme.theme) {
			default:
            case WINUNIX_LIGHT_THEME:
                colors.put("BAR_BG", Color.decode(WINDOWS_LIGHT_BAR_BG));
                colors.put("WIN_BG", Color.decode(WINDOWS_LIGHT_WIN_BG));
                colors.put("FRAME_BG", Color.decode(WINDOWS_LIGHT_FRAME_BG));
                colors.put("ACCENT", Color.decode(WINDOWS_LIGHT_ACCENT));
                break;
            case WINUNIX_DARK_THEME:
                colors.put("BAR_BG", Color.decode(WINDOWS_DARK_BAR_BG));
                colors.put("WIN_BG", Color.decode(WINDOWS_DARK_WIN_BG));
                colors.put("FRAME_BG", Color.decode(WINDOWS_DARK_FRAME_BG));
                colors.put("ACCENT", Color.decode(WINDOWS_DARK_ACCENT));
                break;
            case MACOS_LIGHT_THEME:
                colors.put("BAR_BG", Color.decode(MACOS_LIGHT_BAR_BG));
                colors.put("WIN_BG", Color.decode(MACOS_LIGHT_WIN_BG));
                colors.put("FRAME_BG", Color.decode(MACOS_LIGHT_FRAME_BG));
                colors.put("ACCENT", Color.decode(MACOS_LIGHT_ACCENT));
                break;
            case MACOS_DARK_THEME:
                colors.put("BAR_BG", Color.decode(MACOS_DARK_BAR_BG));
                colors.put("WIN_BG", Color.decode(MACOS_DARK_WIN_BG));
                colors.put("FRAME_BG", Color.decode(MACOS_DARK_FRAME_BG));
                colors.put("ACCENT", Color.decode(MACOS_DARK_ACCENT));
                break;
        }

        return colors;
    }
}
