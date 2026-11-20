package com.ptit.oj.view.account;

import com.ptit.oj.controller.JudgeService;
import com.ptit.oj.controller.PasswordService;
import com.ptit.oj.model.User;
import com.ptit.oj.view.Ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterView extends JDialog implements ActionListener {

    private static final int FIELD_COLUMNS = 18;

    private JudgeService service;
    private JTextField txtUsername = new JTextField(FIELD_COLUMNS);
    private JTextField txtFullName = new JTextField(FIELD_COLUMNS);
    private JTextField txtStudentCode = new JTextField(FIELD_COLUMNS);
    private JTextField txtClassName = new JTextField(FIELD_COLUMNS);
    private JPasswordField txtPassword = new JPasswordField(FIELD_COLUMNS);
    private JPasswordField txtConfirm = new JPasswordField(FIELD_COLUMNS);
    private JButton btnCreate = new JButton("Tạo tài khoản");
    private JButton btnBack = new JButton("Đã có tài khoản? Đăng nhập");
    private String createdUsername;

    public RegisterView(Window owner, JudgeService service) {
        super(owner, "Đăng ký tài khoản", ModalityType.APPLICATION_MODAL);
        this.service = service;

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.add(Ui.title("Đăng ký tài khoản"));
        header.add(new JLabel("Tạo tài khoản sinh viên mới"));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Tên đăng nhập"));
        form.add(txtUsername);
        form.add(new JLabel("Họ và tên"));
        form.add(txtFullName);
        form.add(new JLabel("Mã sinh viên"));
        form.add(txtStudentCode);
        form.add(new JLabel("Lớp"));
        form.add(txtClassName);
        form.add(new JLabel("Mật khẩu"));
        form.add(txtPassword);
        form.add(new JLabel("Nhập lại mật khẩu"));
        form.add(txtConfirm);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 6));
        buttons.add(btnCreate);
        buttons.add(btnBack);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        btnCreate.addActionListener(this);
        btnBack.addActionListener(this);
        getRootPane().setDefaultButton(btnCreate);

        pack();
        setLocationRelativeTo(owner);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCreate) {
            handleSubmit();
        } else if (e.getSource() == btnBack) {
            dispose();
        }
    }

    private void handleSubmit() {
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());
        if (password.length() < PasswordService.MIN_PASSWORD_LENGTH) {
            Ui.error(this, "Mật khẩu phải có ít nhất " + PasswordService.MIN_PASSWORD_LENGTH + " ký tự.");
            return;
        }
        if (!password.equals(confirm)) {
            Ui.error(this, "Hai lần nhập mật khẩu không khớp.");
            return;
        }
        try {
            User created = service.getAuth().register(txtUsername.getText(), password,
                    txtFullName.getText(), txtStudentCode.getText(), txtClassName.getText());
            createdUsername = created.getUsername();
            dispose();
        } catch (RuntimeException ex) {
            Ui.error(this, ex);
        }
    }

    public String getCreatedUsername() {
        return createdUsername;
    }
}
