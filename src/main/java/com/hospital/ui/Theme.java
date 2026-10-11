package com.hospital.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hospital Management System UI Theme.
 * Implements a restrained, clinical, professional, and practical hospital aesthetic.
 */
public final class Theme {

    // Exact Specification Color Palette
    public static final Color PRIMARY = new Color(0x24, 0x5B, 0x78);          // #245B78 Deep Medical Blue
    public static final Color SECONDARY = new Color(0x4F, 0x8A, 0x8B);        // #4F8A8B Muted Teal
    public static final Color BG = new Color(0xF4, 0xF7, 0xF8);               // #F4F7F8 Application Background
    public static final Color SURFACE = Color.WHITE;                          // #FFFFFF White Panels
    public static final Color TEXT_MAIN = new Color(0x26, 0x37, 0x46);        // #263746 Main Text
    public static final Color TEXT_MUTED = new Color(0x71, 0x80, 0x96);       // #718096 Secondary Text
    public static final Color BORDER = new Color(0xDC, 0xE4, 0xE8);          // #DCE4E8 Crisp Borders
    public static final Color SUCCESS = new Color(0x3F, 0x80, 0x65);         // #3F8065 Success
    public static final Color WARNING = new Color(0xB9, 0x85, 0x36);         // #B98536 Warning
    public static final Color ERROR = new Color(0xB9, 0x4A, 0x48);           // #B94A48 Error

    // Role Specific Accent Colors
    public static final Color NAVY_BLUE = new Color(0x1B, 0x36, 0x5D);       // Deep Navy Blue #1B365D
    public static final Color DOCTOR_TEAL = new Color(0x0D, 0x94, 0x88);     // Doctor Teal #0D9488
    public static final Color DOCTOR_TEAL_DARK = new Color(0x0F, 0x76, 0x6E);
    public static final Color PATIENT_GREEN = new Color(0x10, 0xB9, 0x81);   // Patient Green #10B981
    public static final Color PATIENT_GREEN_DARK = new Color(0x04, 0x78, 0x57);
    public static final Color RECEPTIONIST_ORANGE = new Color(0xF5, 0x9E, 0x0B); // Receptionist Orange #F59E0B
    public static final Color RECEPTIONIST_ORANGE_DARK = new Color(0xD9, 0x77, 0x06);

    // Sidebar & Navigation Specific Colors
    public static final Color SIDEBAR_BG = new Color(0x1B, 0x45, 0x5B);       // Deep Blue Sidebar
    public static final Color SIDEBAR_ACTIVE = new Color(0x2A, 0x68, 0x88);   // Active item highlight
    public static final Color SIDEBAR_HOVER = new Color(0x23, 0x56, 0x72);    // Hover item highlight
    public static final Color SIDEBAR_TEXT = Color.WHITE;
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0xBD, 0xD4, 0xE0);

    // Table Colors
    public static final Color TABLE_HEADER_BG = new Color(0x24, 0x5B, 0x78);
    public static final Color TABLE_HEADER_TEXT = Color.WHITE;
    public static final Color TABLE_ROW_EVEN = Color.WHITE;
    public static final Color TABLE_ROW_ODD = new Color(0xF9, 0xFB, 0xFC);
    public static final Color TABLE_ROW_SELECTED = new Color(0xE3, 0xEE, 0xF4);

    // Font resolution
    private static final String FONT_FAMILY = resolveFontFamily();

    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private Theme() {
    }

    private static String resolveFontFamily() {
        Set<String> fonts = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        ));
        if (fonts.contains("Segoe UI")) return "Segoe UI";
        if (fonts.contains("Ubuntu")) return "Ubuntu";
        if (fonts.contains("DejaVu Sans")) return "DejaVu Sans";
        if (fonts.contains("Arial")) return "Arial";
        return Font.SANS_SERIF;
    }
}
