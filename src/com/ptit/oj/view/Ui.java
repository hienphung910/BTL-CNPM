package com.ptit.oj.view;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

public class Ui {
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    public static final Font TEXT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component parent, Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "Có lỗi xảy ra: " + e;
        }
        error(parent, message);
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        return label;
    }

    public static JTable table(String[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0));
        table.setDefaultEditor(Object.class, null);
        table.setRowHeight(24);
        table.setGridColor(new Color(0xD9D9D9));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    public static void setRows(JTable table, List<Object[]> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    public static String difficulty(int difficulty) {
        if (difficulty == 1) return "Dễ";
        if (difficulty == 2) return "Vừa";
        if (difficulty == 3) return "Khó";
        return "—";
    }
}
