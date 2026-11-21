package com.ptit.oj.view.judge;

import com.ptit.oj.controller.JudgeService;
import com.ptit.oj.controller.judge.Language;
import com.ptit.oj.model.JudgeResult;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.TestCase;
import com.ptit.oj.model.User;
import com.ptit.oj.view.Ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Tạo hộp thoại làm bài: đọc đề và ví dụ, chọn ngôn ngữ, nhập mã, nộp bài và xem kết quả.
 * Chuyển yêu cầu chấm cho JudgeService và tiếp nhận thao tác chuyển sang bài tiếp theo.
 */
public class ProblemDetailView extends JDialog implements ActionListener {

    // Dịch vụ nghiệp vụ và dữ liệu của người dùng, bài tập đang làm.
    private JudgeService service;
    private User user;
    private Problem problem;

    // Các điều khiển phục vụ chọn ngôn ngữ, soạn mã, nộp bài và hiển thị kết quả.
    private JButton btnBack = new JButton("← Lớp học");
    private JComboBox<String> cboLanguage = new JComboBox<>();
    private JButton btnTemplate = new JButton("Chèn code mẫu");
    private JButton btnSubmit = new JButton("Nộp bài");
    private JLabel lblStatus = new JLabel(" ");
    private JTextArea txtCode = new JTextArea();
    private JudgeResultView resultView = new JudgeResultView();
    // Ghi nhận yêu cầu chuyển bài để màn hình cha xử lý sau khi hộp thoại đóng.
    private boolean nextRequested = false;

    /**
     * Khởi tạo hộp thoại, bố trí đề bài và vùng nộp bài, đăng ký sự kiện rồi kiểm tra toolchain.
     */
    public ProblemDetailView(Window owner, JudgeService service, User user, Problem problem,
                             String topicName, Problem nextProblem) {
        super(owner, "Làm bài: " + problem.getId() + " — " + problem.getTitle(), ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.user = user;
        this.problem = problem;

        // Phần đầu cửa sổ: nút quay lại, mã bài, chủ đề và độ khó.
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        header.add(btnBack);
        header.add(Ui.title("Làm bài lớp: CodeHub · " + problem.getId()));
        header.add(new JLabel(topicName));
        header.add(new JLabel(Ui.difficulty(problem.getDifficulty())));

        // Phần đề bài: chỉ hiển thị nội dung và các test mẫu, bỏ qua test ẩn.
        JTextArea txtStatement = readOnlyArea(problem.getStatement());
        txtStatement.setFont(Ui.TEXT);
        txtStatement.setLineWrap(true);
        txtStatement.setWrapStyleWord(true);

        JPanel samples = new JPanel(new GridLayout(0, 2, 6, 4));
        samples.setBorder(BorderFactory.createTitledBorder("Ví dụ"));
        samples.add(new JLabel("Input"));
        samples.add(new JLabel("Output"));
        boolean hasSample = false;
        for (TestCase tc : problem.getTestCases()) {
            if (!tc.isSample()) continue;
            hasSample = true;
            samples.add(new JScrollPane(readOnlyArea(tc.getInput().trim())));
            samples.add(new JScrollPane(readOnlyArea(tc.getExpectedOutput().trim())));
        }

        JPanel statementCard = new JPanel(new BorderLayout(0, 6));
        statementCard.setBorder(BorderFactory.createTitledBorder("Đề bài: " + problem.getId() + " — "
                + problem.getTitle() + "   (" + formatLimit() + ")"));
        statementCard.add(new JScrollPane(txtStatement), BorderLayout.CENTER);
        if (hasSample) {
            statementCard.add(samples, BorderLayout.SOUTH);
        }

        // Phần nộp bài: danh sách ngôn ngữ, trình soạn mã, các nút và vùng kết quả.
        for (Language l : service.getLanguages()) {
            cboLanguage.addItem(l.getName());
        }
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.add(cboLanguage);
        bar.add(btnTemplate);
        bar.add(btnSubmit);
        bar.add(lblStatus);
        txtCode.setFont(Ui.MONO);
        txtCode.setTabSize(4);
        resultView.setNextProblem(nextProblem);
        resultView.setVisible(false);

        JPanel submitCard = new JPanel(new BorderLayout(0, 6));
        submitCard.setBorder(BorderFactory.createTitledBorder("Nộp bài"));
        submitCard.add(bar, BorderLayout.NORTH);
        submitCard.add(new JScrollPane(txtCode), BorderLayout.CENTER);
        submitCard.add(resultView, BorderLayout.SOUTH);

        // Ghép đề bài và vùng nộp bài thành hai cột trong cùng hộp thoại.
        JPanel body = new JPanel(new GridLayout(1, 2, 8, 0));
        body.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        body.add(statementCard);
        body.add(submitCard);
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        // Đăng ký một bộ xử lý sự kiện chung cho các điều khiển tương tác.
        btnBack.addActionListener(this);
        cboLanguage.addActionListener(this);
        btnTemplate.addActionListener(this);
        btnSubmit.addActionListener(this);
        resultView.getBtnNext().addActionListener(this);

        checkLanguage();
        setSize(1180, 740);
        setLocationRelativeTo(owner);
    }

    /**
     * Phân phối thao tác nút bấm/chọn ngôn ngữ: quay lại, chèn mẫu, nộp bài hoặc làm bài tiếp.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            dispose();
        } else if (e.getSource() == cboLanguage) {
            checkLanguage();
        } else if (e.getSource() == btnTemplate) {
            setCode(templateOf((String) cboLanguage.getSelectedItem()));
        } else if (e.getSource() == btnSubmit) {
            doSubmit();
        } else if (e.getSource() == resultView.getBtnNext()) {
            // Đóng hộp thoại để trang lớp học xử lý yêu cầu mở bài tiếp theo.
            nextRequested = true;
            dispose();
        }
    }

    /**
     * Định dạng giới hạn thời gian sang giây và ghép với giới hạn bộ nhớ MB.
     */
    private String formatLimit() {
        long ms = problem.getTimeLimitMs();
        String seconds;
        if (ms % 1000 == 0) {
            seconds = String.valueOf(ms / 1000);
        } else {
            seconds = String.valueOf(ms / 1000.0);
        }
        return seconds + "s · " + problem.getMemoryLimitMb() + " MB";
    }

    /**
     * Tạo ô văn bản chỉ đọc với font đơn cách và con trỏ ở đầu nội dung.
     */
    private JTextArea readOnlyArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(Ui.MONO);
        area.setCaretPosition(0);
        return area;
    }

    /**
     * Trả chương trình mẫu đọc hai số và in tổng theo ngôn ngữ được chọn.
     */
    private String templateOf(String language) {
        if ("Java".equals(language)) {
            return "import java.util.Scanner;\n\n"
                    + "public class Solution {\n"
                    + "    public static void main(String[] args) {\n"
                    + "        Scanner sc = new Scanner(System.in);\n"
                    + "        long a = sc.nextLong();\n"
                    + "        long b = sc.nextLong();\n"
                    + "        System.out.println(a + b);\n"
                    + "    }\n"
                    + "}\n";
        }
        if ("C++".equals(language)) {
            return "#include <iostream>\n\n"
                    + "int main() {\n"
                    + "    long long a, b;\n"
                    + "    std::cin >> a >> b;\n"
                    + "    std::cout << a + b << std::endl;\n"
                    + "    return 0;\n"
                    + "}\n";
        }
        if ("Python".equals(language)) {
            return "import sys\n\n"
                    + "data = sys.stdin.read().split()\n"
                    + "print(int(data[0]) + int(data[1]))\n";
        }
        if ("JavaScript".equals(language)) {
            return "const data = require(\"fs\").readFileSync(0, \"utf8\").trim().split(/\\s+/);\n"
                    + "console.log((BigInt(data[0]) + BigInt(data[1])).toString());\n";
        }
        return "";
    }

    /**
     * Tra cứu đối tượng Language từ lựa chọn hiện tại trong hộp chọn.
     */
    private Language currentLanguage() {
        return service.findLanguage((String) cboLanguage.getSelectedItem());
    }

    /**
     * Cho phép nộp bài khi có ngôn ngữ và toolchain tương ứng; nếu thiếu thì hiện thông báo.
     */
    private void checkLanguage() {
        Language language = currentLanguage();
        if (language == null) {
            btnSubmit.setEnabled(false);
            lblStatus.setText(" ");
        } else if (language.isAvailable()) {
            btnSubmit.setEnabled(true);
            lblStatus.setText(" ");
        } else {
            btnSubmit.setEnabled(false);
            lblStatus.setText("Máy này chưa cài toolchain cho " + language.getName() + ".");
        }
    }

    /**
     * Thay mã nguồn trong trình soạn thảo và đưa con trỏ về đầu.
     */
    public void setCode(String code) {
        txtCode.setText(code);
        txtCode.setCaretPosition(0);
    }

    /**
     * Kiểm tra ngôn ngữ và mã nguồn, hiển thị trạng thái đang chấm rồi gọi JudgeService đồng bộ.
     * Hiển thị kết quả hoặc lỗi, sau đó khôi phục nút nộp bài và con trỏ.
     */
    public void doSubmit() {
        // Bước 1: lấy dữ liệu trên giao diện và kiểm tra điều kiện nộp bài cơ bản.
        Language language = currentLanguage();
        String code = txtCode.getText();
        if (language == null) {
            Ui.error(this, "Chưa chọn ngôn ngữ.");
            return;
        }
        if (code.trim().isEmpty()) {
            Ui.error(this, "Mã nguồn đang rỗng.");
            return;
        }

        // Bước 2: khóa nút nộp và hiển thị trạng thái chờ trong khi chấm đồng bộ.
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang chấm...");
        lblStatus.setText("Đang biên dịch và chấm bài...");
        resultView.setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Vẽ lại ngay để trạng thái đang chấm xuất hiện trước lời gọi chấm đồng bộ.
        validate();
        getRootPane().paintImmediately(0, 0, getRootPane().getWidth(), getRootPane().getHeight());

        // Bước 3: gọi dịch vụ chấm và lưu bài nộp, sau đó cập nhật kết quả hoặc báo lỗi.
        try {
            JudgeResult result = service.submitCode(problem, user, language, code);
            resultView.setResult(result.getSubmission());
            resultView.setVisible(true);
        } catch (RuntimeException ex) {
            Ui.error(this, ex);
        }

        // Bước 4: trả giao diện về trạng thái sẵn sàng cho lần nộp tiếp theo.
        setCursor(Cursor.getDefaultCursor());
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Nộp bài");
        lblStatus.setText(" ");
    }

    /**
     * Cho màn hình cha biết người dùng đã yêu cầu mở bài tiếp theo hay chưa.
     */
    public boolean isNextRequested() {
        return nextRequested;
    }
}
