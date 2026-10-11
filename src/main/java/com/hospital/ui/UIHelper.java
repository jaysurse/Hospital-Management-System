package com.hospital.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * UI factory creating styled, consistent, and practical hospital desktop components.
 */
public final class UIHelper {

    private UIHelper() {
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, Theme.PRIMARY, Color.WHITE, new Color(0x1B, 0x48, 0x60));
    }

    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, Theme.SECONDARY, Color.WHITE, new Color(0x3E, 0x73, 0x74));
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, Theme.SUCCESS, Color.WHITE, new Color(0x32, 0x67, 0x51));
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, Theme.ERROR, Color.WHITE, new Color(0x9E, 0x3D, 0x3B));
    }

    public static JButton createTealButton(String text) {
        return createStyledButton(text, Theme.DOCTOR_TEAL, Color.WHITE, Theme.DOCTOR_TEAL_DARK);
    }

    public static JButton createGreenButton(String text) {
        return createStyledButton(text, Theme.PATIENT_GREEN, Color.WHITE, Theme.PATIENT_GREEN_DARK);
    }

    public static JButton createOrangeButton(String text) {
        return createStyledButton(text, Theme.RECEPTIONIST_ORANGE, Color.WHITE, Theme.RECEPTIONIST_ORANGE_DARK);
    }

    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BOLD);
        btn.setForeground(Theme.PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(Theme.PRIMARY, 1, true),
            new EmptyBorder(7, 14, 7, 14)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0xEE, 0xF5, 0xF8));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    private static JButton createStyledButton(String text, Color baseColor, Color textColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BOLD);
        btn.setForeground(textColor);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_REGULAR);
        tf.setForeground(Theme.TEXT_MAIN);
        tf.setCaretColor(Theme.PRIMARY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_REGULAR);
        pf.setForeground(Theme.TEXT_MAIN);
        pf.setCaretColor(Theme.PRIMARY);
        pf.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_REGULAR);
        cb.setForeground(Theme.TEXT_MAIN);
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(Theme.BORDER, 1, true));
        return cb;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JTable createTable(TableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Theme.TABLE_ROW_EVEN : Theme.TABLE_ROW_ODD);
                    c.setForeground(Theme.TEXT_MAIN);
                } else {
                    c.setBackground(Theme.TABLE_ROW_SELECTED);
                    c.setForeground(Theme.TEXT_MAIN);
                }
                return c;
            }
        };

        table.setRowHeight(32);
        table.setFont(Theme.FONT_REGULAR);
        table.setGridColor(Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(Theme.TABLE_ROW_SELECTED);
        table.setSelectionForeground(Theme.TEXT_MAIN);
        table.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_SUBHEADER);
        header.setBackground(Theme.TABLE_HEADER_BG);
        header.setForeground(Theme.TABLE_HEADER_TEXT);
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Default cell padding
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);
        table.setDefaultRenderer(Integer.class, defaultRenderer);
        table.setDefaultRenderer(Double.class, defaultRenderer);

        return table;
    }

    /**
     * Compact, non-decorative, clinical stat card with left indicator stripe.
     */
    public static JPanel createStatCard(String label, String value, String subtext, Color indicatorColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(indicatorColor != null ? indicatorColor : Theme.PRIMARY);
                g2.fillRect(0, 0, 4, getHeight()); // Clean 4px indicator strip
                g2.dispose();
            }
        };
        card.setLayout(null);
        card.setBackground(Theme.SURFACE);
        card.setBorder(new LineBorder(Theme.BORDER, 1, true));
        card.setPreferredSize(new Dimension(230, 95));

        JLabel lblTitle = new JLabel(label.toUpperCase());
        lblTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblTitle.setForeground(Theme.TEXT_MUTED);
        lblTitle.setBounds(18, 12, 200, 16);
        card.add(lblTitle);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 24));
        lblValue.setForeground(Theme.TEXT_MAIN);
        lblValue.setBounds(18, 32, 200, 32);
        card.add(lblValue);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setBounds(18, 66, 200, 16);
        card.add(lblSub);

        return card;
    }

    /**
     * Vector Medical Cross Icon for Clinical Branding.
     */
    public static Icon createMedicalCrossIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);

                int barThick = Math.max(3, size / 3);
                int barLen = size;
                int offset = (size - barThick) / 2;

                // Horizontal bar
                g2.fillRect(x, y + offset, barLen, barThick);
                // Vertical bar
                g2.fillRect(x + offset, y, barThick, barLen);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Pill badge renderer for clinical statuses (Scheduled, Completed, Paid, Pending, etc.)
     */
    public static TableCellRenderer createStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String text = value != null ? value.toString() : "";
                label.setText(text);
                label.setFont(Theme.FONT_SMALL_BOLD);

                if (!isSelected) {
                    if ("Completed".equalsIgnoreCase(text) || "PAID".equalsIgnoreCase(text)) {
                        label.setBackground(new Color(0xE8, 0xF5, 0xEE));
                        label.setForeground(Theme.SUCCESS);
                    } else if ("Scheduled".equalsIgnoreCase(text) || "PENDING".equalsIgnoreCase(text)) {
                        label.setBackground(new Color(0xFD, 0xF6, 0xE7));
                        label.setForeground(Theme.WARNING);
                    } else if ("Cancelled".equalsIgnoreCase(text)) {
                        label.setBackground(new Color(0xFD, 0xEE, 0xEE));
                        label.setForeground(Theme.ERROR);
                    } else {
                        label.setBackground(Theme.TABLE_ROW_EVEN);
                        label.setForeground(Theme.TEXT_MAIN);
                    }
                    label.setOpaque(true);
                }
                return label;
            }
        };
    }
}
