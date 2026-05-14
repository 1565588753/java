package com.internetcafe.ui;

import com.internetcafe.entity.Admin;
import com.internetcafe.service.LoginService;
import com.internetcafe.util.DateUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 网吧计费管理系统主界面
 * 系统的主窗口，包含左侧导航菜单、右侧内容区域和底部状态栏。
 * 管理员登录成功后进入此界面，通过菜单按钮切换不同的功能面板。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class MainFrame extends JFrame {

    /* ==================== 界面常量 ==================== */
    /** 窗口默认宽度 */
    private static final int FRAME_WIDTH = 1200;
    /** 窗口默认高度 */
    private static final int FRAME_HEIGHT = 750;
    /** 侧边栏宽度 */
    private static final int SIDEBAR_WIDTH = 200;
    /** 状态栏高度 */
    private static final int STATUS_BAR_HEIGHT = 25;
    /** 侧边栏深色背景 */
    private static final Color SIDEBAR_BG = new Color(45, 45, 45);
    /** 按钮选中高亮颜色 */
    private static final Color BUTTON_SELECTED = new Color(70, 130, 180);

    /* ==================== 核心字段 ==================== */
    /** 当前已登录的管理员对象 */
    private Admin currentAdmin;
    /** 登录服务对象，用于处理登出操作 */
    private LoginService loginService;
    /** 当前系统登录时间字符串 */
    private String loginTime;

    /* ==================== 面板实例 ==================== */
    /** 用户管理面板 */
    private UserPanel userPanel;
    /** 上机管理面板 */
    private OnlinePanel onlinePanel;
    /** 充值管理面板 */
    private RechargePanel rechargePanel;
    /** 会员管理面板 */
    private VipPanel vipPanel;
    /** 报表统计面板 */
    private ReportPanel reportPanel;
    /** 系统日志面板 */
    private LogPanel logPanel;

    /* ==================== 界面组件 ==================== */
    /** 右侧内容区域，使用CardLayout切换不同功能面板 */
    private JPanel contentPanel;
    /** CardLayout布局管理器，用于切换内容面板 */
    private CardLayout cardLayout;
    /** 侧边栏的所有菜单按钮数组，用于高亮状态管理 */
    private JButton[] menuButtons;
    /** 底部状态栏左侧标签，显示当前管理员用户名 */
    private JLabel statusUserLabel;
    /** 底部状态栏右侧标签，显示登录时间 */
    private JLabel statusTimeLabel;

    /**
     * 构造方法
     * 初始化主界面，创建所有子组件并布局。
     *
     * @param admin 当前已登录的管理员对象
     */
    public MainFrame(Admin admin) {
        this.currentAdmin = admin;
        this.loginService = new LoginService();
        this.loginTime = DateUtil.getCurrentDateTime();

        initFrame();
        initComponents();
        initLayout();
        setVisible(true);
    }

    /**
     * 初始化窗口基本属性
     * 设置标题、大小、位置、关闭操作等。
     */
    private void initFrame() {
        setTitle("网吧计费管理系统 - 主界面");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);
    }

    /**
     * 初始化所有界面组件
     * 创建侧边栏、内容区域、状态栏，并组装到主窗口中。
     */
    private void initComponents() {
        // 创建侧边导航栏
        JPanel sidebarPanel = initSidebar();

        // 创建内容区域（CardLayout）
        contentPanel = initContentPanel();

        // 创建底部状态栏
        JPanel statusBar = initStatusBar();

        // 将各区域添加到主窗口
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * 初始化界面布局
     * 设置窗口在屏幕居中显示。
     */
    private void initLayout() {
        // 窗口已在initFrame中通过setLocationRelativeTo(null)居中
        // 此处预留，用于后续可能的布局调整
    }

    /**
     * 创建并初始化左侧导航栏
     * 包含系统标题和7个功能菜单按钮，点击按钮可切换右侧内容面板。
     *
     * @return 组装完成的侧边栏面板
     */
    private JPanel initSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, getHeight()));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BorderLayout());

        // 顶部标题区域
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(SIDEBAR_BG);
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JLabel titleLabel = new JLabel("网吧计费管理系统");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        sidebar.add(titlePanel, BorderLayout.NORTH);

        // 菜单按钮区域
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(SIDEBAR_BG);
        menuPanel.setLayout(new GridLayout(7, 1, 0, 0));

        // 菜单按钮名称列表
        String[] menuNames = {
            "用户管理",
            "上机管理",
            "充值管理",
            "会员管理",
            "报表统计",
            "系统日志",
            "退出系统"
        };

        menuButtons = new JButton[menuNames.length];

        // 创建并配置每个菜单按钮
        for (int i = 0; i < menuNames.length; i++) {
            JButton button = createMenuButton(menuNames[i]);
            final int index = i;

            button.addActionListener(e -> {
                // 先取消所有按钮的高亮状态
                for (JButton btn : menuButtons) {
                    btn.setBackground(SIDEBAR_BG);
                }
                // 高亮当前选中的按钮
                highlightButton(button);

                // 根据按钮索引切换对应面板
                switch (index) {
                    case 0: switchPanel("userPanel"); break;
                    case 1: switchPanel("onlinePanel"); break;
                    case 2: switchPanel("rechargePanel"); break;
                    case 3: switchPanel("vipPanel"); break;
                    case 4: switchPanel("reportPanel"); break;
                    case 5: switchPanel("logPanel"); break;
                    case 6: handleLogout(); break;
                }
            });

            menuButtons[i] = button;
            menuPanel.add(button);
        }

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // 默认选中第一个按钮（用户管理）
        highlightButton(menuButtons[0]);

        return sidebar;
    }

    /**
     * 创建菜单按钮
     * 设置按钮样式为深色背景、白色文字、左对齐，并添加内边距。
     *
     * @param text 按钮显示的文本
     * @return 样式配置好的按钮对象
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SIDEBAR_BG);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        return button;
    }

    /**
     * 创建并初始化右侧内容面板
     * 使用CardLayout管理多个功能面板，默认显示用户管理面板。
     *
     * @return 组装完成的内容面板
     */
    private JPanel initContentPanel() {
        cardLayout = new CardLayout();
        JPanel panel = new JPanel(cardLayout);
        panel.setBackground(Color.WHITE);

        // 实例化各功能面板
        userPanel = new UserPanel();
        onlinePanel = new OnlinePanel();
        rechargePanel = new RechargePanel();
        vipPanel = new VipPanel();
        reportPanel = new ReportPanel();
        logPanel = new LogPanel();

        // 将各面板添加到CardLayout中，使用名称标识
        panel.add(userPanel, "userPanel");
        panel.add(onlinePanel, "onlinePanel");
        panel.add(rechargePanel, "rechargePanel");
        panel.add(vipPanel, "vipPanel");
        panel.add(reportPanel, "reportPanel");
        panel.add(logPanel, "logPanel");

        // 默认显示用户管理面板
        cardLayout.show(panel, "userPanel");

        return panel;
    }

    /**
     * 创建并初始化底部状态栏
     * 左侧显示当前管理员用户名，右侧显示登录时间。
     *
     * @return 组装完成的状态栏面板
     */
    private JPanel initStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setPreferredSize(new Dimension(getWidth(), STATUS_BAR_HEIGHT));
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        // 左侧：当前管理员信息
        statusUserLabel = new JLabel("当前管理员：" + currentAdmin.getUsername());
        statusUserLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusUserLabel.setForeground(new Color(80, 80, 80));
        statusBar.add(statusUserLabel, BorderLayout.WEST);

        // 右侧：登录时间信息
        statusTimeLabel = new JLabel("登录时间：" + loginTime);
        statusTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusTimeLabel.setForeground(new Color(80, 80, 80));
        statusBar.add(statusTimeLabel, BorderLayout.EAST);

        return statusBar;
    }

    /**
     * 切换右侧内容面板到指定面板
     * 通过CardLayout的show方法切换显示不同的功能面板。
     *
     * @param panelName 面板名称标识，如"userPanel"、"onlinePanel"等
     */
    private void switchPanel(String panelName) {
        if (cardLayout != null && contentPanel != null) {
            cardLayout.show(contentPanel, panelName);
        }
    }

    /**
     * 高亮显示被选中的菜单按钮
     * 将按钮背景色设置为高亮颜色，表示当前处于该功能页面。
     *
     * @param button 需要高亮的按钮对象
     */
    private void highlightButton(JButton button) {
        button.setBackground(BUTTON_SELECTED);
    }

    /**
     * 处理退出系统操作
     * 调用登出服务记录日志，关闭主窗口，重新打开登录窗口。
     */
    private void handleLogout() {
        // 弹出确认对话框
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出系统吗？",
            "退出确认",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            // 执行登出操作，记录登出日志
            loginService.logout();
            // 关闭当前主窗口
            dispose();
            // 打开登录窗口
            new LoginFrame();
        }
    }
}