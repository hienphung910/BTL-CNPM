package com.ptit.oj.view.judge;

import com.ptit.oj.model.JudgeResult;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.Submission;
import com.ptit.oj.model.TestCaseResult;
import com.ptit.oj.model.Verdict;
import com.ptit.oj.view.Ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Hiển thị kết quả chấm trên Swing: verdict chung, điểm, số test đúng và bảng từng test.
 * Dùng trong màn hình làm bài và chi tiết lịch sử; không hiển thị input hoặc đáp án test.
 */
public class JudgeResultView extends JPanel {

    // Màu trạng thái và các thành phần Swing hiển thị kết quả chấm.
    private static final Color GREEN = new Color(0x2E7D32);
    private static final Color RED = new Color(0xC62828);

    private JLabel lblVerdict = new JLabel(" ");
    private JLabel lblSummary = new JLabel(" ");
    private JLabel lblNext = new JLabel(" ");
    private JButton btnNext = new JButton("Làm bài tiếp →");
    private JPanel pnlNext = new JPanel(new BorderLayout(8, 0));
    private JTextArea txtMessage = new JTextArea(4, 40);
    private JScrollPane scrMessage = new JScrollPane(txtMessage);
    private JTable table = Ui.table(new String[] {"Test", "Kết quả", "Thời gian", "Điểm", "Thông báo"});

    // Bài tiếp theo trong lộ trình; null khi không cần hiển thị gợi ý chuyển bài.
    private Problem nextProblem;

    /**
     * Dựng khu vực tổng kết, thông báo lỗi, nút làm bài tiếp và bảng kết quả từng test.
     */
    public JudgeResultView() {
        super(new BorderLayout(0, 6));

        // Dựng hàng tổng kết verdict và điểm ở phía trên bảng kết quả.
        lblVerdict.setFont(lblVerdict.getFont().deriveFont(Font.BOLD, 15f));
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        summary.add(lblVerdict);
        summary.add(lblSummary);

        // Chuẩn bị gợi ý bài tiếp theo; chỉ hiển thị sau khi bài nộp đạt AC.
        lblNext.setForeground(GREEN);
        pnlNext.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        pnlNext.add(lblNext, BorderLayout.CENTER);
        pnlNext.add(btnNext, BorderLayout.EAST);
        pnlNext.setVisible(false);

        // Vùng thông báo chung chỉ đọc, dùng để hiển thị lỗi biên dịch hoặc lỗi hệ thống.
        txtMessage.setEditable(false);
        txtMessage.setFont(Ui.MONO);
        scrMessage.setVisible(false);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(summary, BorderLayout.NORTH);
        top.add(pnlNext, BorderLayout.CENTER);
        top.add(scrMessage, BorderLayout.SOUTH);

        // Đặt kích thước bảng và vùng cuộn để xem kết quả của từng test.
        table.getColumnModel().getColumn(4).setPreferredWidth(320);
        JScrollPane scrTable = new JScrollPane(table);
        scrTable.setPreferredSize(new Dimension(0, 230));

        add(top, BorderLayout.NORTH);
        add(scrTable, BorderLayout.CENTER);
    }

    /**
     * Ghi nhận bài tiếp theo để gợi ý khi bài hiện tại đạt AC.
     */
    public void setNextProblem(Problem nextProblem) {
        this.nextProblem = nextProblem;
    }

    /**
     * Cung cấp nút làm bài tiếp để màn hình cha đăng ký xử lý sự kiện.
     */
    public JButton getBtnNext() {
        return btnNext;
    }

    /**
     * Hiển thị verdict, điểm và các kết quả test; chỉ hiện gợi ý bài tiếp khi đạt AC.
     */
    public void setResult(Submission submission) {
        JudgeResult result = submission.getResult();
        Verdict verdict = result.getOverallVerdict();
        boolean accepted = verdict.isAccepted();

        // Cập nhật mã, màu verdict và dòng tổng kết số test đúng cùng điểm bài nộp.
        lblVerdict.setText(verdict.getCode());
        if (accepted) {
            lblVerdict.setForeground(GREEN);
        } else {
            lblVerdict.setForeground(RED);
        }
        lblSummary.setText("Đúng " + result.getPassedCount() + "/" + result.getTotalTests() + " test    "
                + formatScore(result.getScore()) + "/" + formatPoints(submission.getProblem().getMaxPoints())
                + " điểm");

        // Hiển thị lỗi chung nếu có, ví dụ thông báo của trình biên dịch khi CE.
        String message = result.getGlobalMessage();
        if (message == null) {
            message = "";
        }
        txtMessage.setText(message);
        txtMessage.setCaretPosition(0);
        scrMessage.setVisible(!message.isEmpty());

        // Tạo hàng dữ liệu từng test; không đưa input hoặc đáp án vào bảng.
        List<Object[]> rows = new ArrayList<>();
        for (TestCaseResult t : result.getTestResults()) {
            rows.add(new Object[] {t.getTestCase().getId(), t.getVerdict().getCode(),
                    t.getRuntimeMs() + " ms", formatPoints(t.getEarnedPoints()), t.getMessage()});
        }
        Ui.setRows(table, rows);

        // Chỉ gợi ý làm bài tiếp khi đạt AC và còn bài trong lộ trình.
        pnlNext.setVisible(accepted && nextProblem != null);
        if (nextProblem != null) {
            lblNext.setText("Chính xác! Sẵn sàng cho bài tiếp theo: "
                    + nextProblem.getId() + " — " + nextProblem.getTitle());
        }
        revalidate();
        repaint();
    }

    /**
     * Làm tròn tổng điểm đến một chữ số thập phân khi hiển thị.
     */
    private String formatScore(double score) {
        return String.valueOf(Math.round(score * 10) / 10.0);
    }

    /**
     * Làm tròn điểm đến hai chữ số thập phân và bỏ phần thập phân nếu là số nguyên.
     */
    private String formatPoints(double points) {
        double rounded = Math.round(points * 100) / 100.0;
        if (rounded == Math.floor(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }
}
