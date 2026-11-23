package com.ptit.oj.view.problem;

import com.ptit.oj.controller.JudgeService;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.Topic;
import com.ptit.oj.model.User;
import com.ptit.oj.view.Ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AdminPanelView extends JPanel implements ActionListener {
    private static final int EDIT_COLUMN = 6;

    private JudgeService service;
    private User user;

    private JButton btnAdd = new JButton("Add Problem");
    private JComboBox<String> cboTopic = new JComboBox<>();

    private List<String> topicKeys = new ArrayList<>();
    private JTable table = Ui.table(
            new String[] {"Mã", "Tên bài", "Chủ đề", "Độ khó", "Giới hạn", "Điểm", "Thao tác"});
    private JLabel lblEmpty = new JLabel(" ");

    private List<Problem> shown = new ArrayList<>();

    public AdminPanelView(JudgeService service, User user) {
        super(new BorderLayout(0, 6));
        this.service = service;
        this.user = user;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        topicKeys.add("");
        cboTopic.addItem("Tất cả chủ đề");
        for (Topic t : service.getTopics()) {
            topicKeys.add(t.getId());
            cboTopic.addItem(t.getName());
        }
        topicKeys.add(LearningPath.OTHER_TOPIC);
        cboTopic.addItem("Bài khác");

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.add(btnAdd);
        bar.add(new JLabel("Chủ đề"));
        bar.add(cboTopic);
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 2));
        header.add(Ui.title("Quản trị"));
        header.add(new JLabel("Quản lý bài tập và sắp xếp lộ trình học."));
        header.add(bar);

        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(EDIT_COLUMN).setMaxWidth(80);
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblEmpty, BorderLayout.SOUTH);

        btnAdd.addActionListener(this);
        cboTopic.addActionListener(this);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (row >= 0 && row < shown.size() && column == EDIT_COLUMN) {
                    openMetaEditor(shown.get(row));
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            openEditor();
        } else if (e.getSource() == cboTopic) {
            load();
        }
    }

    public void load() {
        List<Topic> topics = service.getTopics();
        String key = "";
        if (cboTopic.getSelectedIndex() >= 0) {
            key = topicKeys.get(cboTopic.getSelectedIndex());
        }

        shown = new ArrayList<>();
        List<Object[]> rows = new ArrayList<>();
        for (Problem p : LearningPath.orderByPath(service.getProblems(), topics)) {
            if (!key.isEmpty() && !LearningPath.topicKey(p, topics).equals(key)) continue;
            shown.add(p);
            String limit = p.getTimeLimitMs() + " ms · " + p.getMemoryLimitMb() + " MB";
            rows.add(new Object[] {p.getId(), p.getTitle(), LearningPath.topicName(p, topics),
                    Ui.difficulty(p.getDifficulty()), limit, formatPoints(p.getMaxPoints()), "Sửa"});
        }
        Ui.setRows(table, rows);

        if (!rows.isEmpty()) {
            lblEmpty.setText(" ");
        } else if (key.isEmpty()) {
            lblEmpty.setText("Chưa có bài tập nào. Bấm \"Add Problem\" để tạo bài đầu tiên.");
        } else {
            lblEmpty.setText("Chủ đề này chưa có bài nào.");
        }
    }

    private String formatPoints(double points) {
        if (points == Math.floor(points)) {
            return String.valueOf((long) points);
        }
        return String.valueOf(points);
    }

    private void openEditor() {
        ProblemEditorView editor = new ProblemEditorView(SwingUtilities.getWindowAncestor(this), service, user);
        editor.setVisible(true);
        load();
    }

    private void openMetaEditor(Problem problem) {
        ProblemMetaEditorView editor =
                new ProblemMetaEditorView(SwingUtilities.getWindowAncestor(this), service, user, problem);
        editor.setVisible(true);
        load();
    }
}
