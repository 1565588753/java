package com.internetcafe.ui;

import com.internetcafe.entity.Admin;
import com.internetcafe.service.LoginService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 登录窗口
 * 网吧计费管理系统的管理员登录界面，提供用户名和密码验证功能。
 * 采用GridBagLayout布局，界面美观大方，居中显示且不可调整大小。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class LoginFrame extends JFrame {

    /** 用户名输入文本框 */
    private JTextField usernameField;

    /** 密码输入密码框 */
    private JPasswordField passwordField;

    /** 登录按钮 */
    private JButton loginButton;

    /** 重置按钮 */
    private JButton resetButton;

    /** 登录服务实例，负责处理登录认证逻辑 */
    private LoginService loginService;

    /**
     * 构造方法
     * 初始化登录窗口的所有组件和布局
     */
    public LoginFrame() {
        initComponents();
        initLayout();
        initListeners();
        centerOnScreen();
    }

    /**
     * 初始化所有界面组件
     */
    private void initComponents() {
        setTitle("网吧计费管理系统 - 登录");
        setSize(450, 350);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        loginButton = new JButton("登录");
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);

        resetButton = new JButton("重置");
        resetButton.setBackground(Color.GRAY);
        resetButton.setForeground(Color.WHITE);

        loginService = new LoginService();
    }

    /**
     * 设置GridBagLayout布局并排列所有组件
     * 布局结构从上到下依次为：标题、副标题、用户名行、密码行、按钮行
     */
    private void initLayout() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 30, 5, 30);

        JLabel titleLabel = new JLabel("网吧计费管理系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 30, 0, 30);
        contentPane.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Internet Cafe Billing System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 30, 20, 30);
        contentPane.add(subtitleLabel, gbc);

        JLabel usernameLabel = new JLabel("用户名：");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 30, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 5, 30);
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("密　码：");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 30, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 30);
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        Dimension buttonSize = new Dimension(100, 32);
        loginButton.setPreferredSize(buttonSize);
        resetButton.setPreferredSize(buttonSize);

        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        resetButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        loginButton.setFocusPainted(false);
        resetButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(resetButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 50, 20, 50);
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(buttonPanel, gbc);
    }

    /**
     * 初始化事件监听器
     * 包括登录按钮点击事件、重置按钮点击事件以及密码框回车键事件
     */
    private void initListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performReset();
            }
        });

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
    }

    /**
     * 执行登录操作
     * 验证用户名和密码是否为空，调用LoginService进行认证，
     * 登录成功后跳转到主界面，失败则弹出错误提示。
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入用户名！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入密码！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        try {
            boolean success = loginService.login(username, password);
            if (success) {
                Admin admin = loginService.getCurrentAdmin();
                dispose();
                new MainFrame(admin).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "用户名或密码错误！",
                        "登录失败",
                        JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "登录过程中发生异常：" + ex.getMessage(),
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * 执行重置操作
     * 清空用户名和密码输入框，并将焦点设置到用户名输入框。
     */
    private void performReset() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }

    /**
     * 将窗口居中显示在屏幕上
     * 通过获取屏幕尺寸和窗口尺寸计算出居中位置。
     */
    private void centerOnScreen() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    /**
     * 程序入口方法
     * 在EDT线程中创建并显示登录窗口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new LoginFrame().setVisible(true);
            }
        });
    }
}