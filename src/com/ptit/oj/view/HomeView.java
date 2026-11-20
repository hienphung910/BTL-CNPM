package com.ptit.oj.view;

import com.ptit.oj.controller.JudgeService;
import com.ptit.oj.model.User;
import com.ptit.oj.view.account.AccountMenuView;
import com.ptit.oj.view.account.LoginView;
import com.ptit.oj.view.history.LeaderboardView;
import com.ptit.oj.view.history.SubmissionsView;
import com.ptit.oj.view.problem.AdminPanelView;
import com.ptit.oj.view.problem.ProblemListView;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeView extends JFrame implements ActionListener {

    private JudgeService service;
    private User user;
    private String token;

    private JButton btnProblems = new JButton("Lớp học");
    private JButton btnStatus = new JButton("Trạng thái");
    private JButton btnMySubmissions = new JButton("Bài nộp");
    private JButton btnLeaderboard = new JButton("Xếp hạng");
    private JButton btnAdmin = new JButton("Quản trị");
    private JButton btnAccount;

    private CardLayout cards = new CardLayout();
    private JPanel content = new JPanel(cards);

    private ProblemListView problemListView;
    private SubmissionsView statusView;
    private SubmissionsView mySubmissionsView;
    private LeaderboardView leaderboardView;
    private AdminPanelView adminPanelView;

    public HomeView(JudgeService service, User user, String token) {
        super("CodeHub - Luyện tập lập trình");
        this.service = service;
        this.user = user;
        this.token = token;

        problemListView = new ProblemListView(service, user);
        statusView = new SubmissionsView(service, user, false);
        mySubmissionsView = new SubmissionsView(service, user, true);
        leaderboardView = new LeaderboardView(service);
        content.add(problemListView, "Lớp học");
        content.add(statusView, "Trạng thái");
        content.add(mySubmissionsView, "Bài nộp");
        content.add(leaderboardView, "Xếp hạng");

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        menu.add(Ui.title("CodeHub   "));
        menu.add(btnProblems);
        menu.add(btnStatus);
        menu.add(btnMySubmissions);
        menu.add(btnLeaderboard);
        if (user.canCreateProblem()) {
            adminPanelView = new AdminPanelView(service, user);
            content.add(adminPanelView, "Quản trị");
            menu.add(btnAdmin);
        }

        btnAccount = new JButton(user.getFullName());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        right.add(btnAccount);

        JPanel top = new JPanel(new BorderLayout());
        top.add(menu, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        btnProblems.addActionListener(this);
        btnStatus.addActionListener(this);
        btnMySubmissions.addActionListener(this);
        btnLeaderboard.addActionListener(this);
        btnAdmin.addActionListener(this);
        btnAccount.addActionListener(this);

        setPage("Lớp học");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnProblems) {
            setPage("Lớp học");
        } else if (e.getSource() == btnStatus) {
            setPage("Trạng thái");
        } else if (e.getSource() == btnMySubmissions) {
            setPage("Bài nộp");
        } else if (e.getSource() == btnLeaderboard) {
            setPage("Xếp hạng");
        } else if (e.getSource() == btnAdmin) {
            setPage("Quản trị");
        } else if (e.getSource() == btnAccount) {
            openAccountMenu();
        }
    }

    public void setPage(String page) {
        if (service.getAuth().authenticate(token) == null) {
            Ui.error(this, "Phiên đăng nhập đã hết hạn. Mời bạn đăng nhập lại.");
            logout();
            return;
        }
        try {
            if (page.equals("Lớp học")) {
                problemListView.load();
            } else if (page.equals("Trạng thái")) {
                statusView.load();
            } else if (page.equals("Bài nộp")) {
                mySubmissionsView.load();
            } else if (page.equals("Xếp hạng")) {
                leaderboardView.load();
            } else if (page.equals("Quản trị")) {
                adminPanelView.load();
            }
        } catch (RuntimeException ex) {
            Ui.error(this, ex);
        }
        cards.show(content, page);
    }

    private void openAccountMenu() {
        AccountMenuView accountMenu = new AccountMenuView(this, user);
        accountMenu.setVisible(true);
        if (accountMenu.isLogoutRequested()) {
            logout();
        }
    }

    private void logout() {
        service.getAuth().logout(token);
        statusView.stopAutoRefresh();
        LoginView loginView = new LoginView(service);
        loginView.setVisible(true);
        dispose();
    }
}
