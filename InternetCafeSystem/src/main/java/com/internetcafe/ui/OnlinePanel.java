package com.internetcafe.ui;

import com.internetcafe.entity.OnlineRecord;
import com.internetcafe.entity.User;
import com.internetcafe.service.ChargeService;
import com.internetcafe.service.UserService;
import com.internetcafe.util.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * 上机管理面板
 * 网吧系统的核心计费面板，负责用户上机、下机、实时计费显示等功能。
 * 面板采用 BorderLayout 布局，顶部为查找用户和开始上机区域，
 * 中间为当前正在上机的活跃用户列表，底部为上机历史记录列表。
 * 通过 javax.swing.Timer 实现每秒刷新活跃用户的上机时长和当前消费。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class OnlinePanel extends JPanel {

    /** 计费服务类，负责上机、下机、费用计算等核心计费业务逻辑 */
    private ChargeService chargeService;

    /** 用户服务类，负责用户信息的查询操作 */
    private UserService userService;

    /** 活跃上机记录表格数据模型，存储当前正在上机的用户记录 */
    private DefaultTableModel activeTableModel;

    /** 历史记录表格数据模型，存储已下机的上机历史记录 */
    private DefaultTableModel historyTableModel;

    /** 活跃上机记录表格，展示当前正在上机的用户 */
    private JTable activeTable;

    /** 历史记录表格，展示已完成的上机历史 */
    private JTable historyTable;

    /** 定时刷新器，每秒更新一次活跃记录的时长和费用 */
    private Timer refreshTimer;

    /** 当前通过搜索找到的用户对象，用于开始上机操作 */
    private User currentSelectedUser;

    /* ==================== 顶部区域组件 ==================== */

    /** 搜索输入框，用于输入用户ID或用户名进行查找 */
    private JTextField searchField;

    /** 机器编号输入框 */
    private JTextField machineNoField;

    /** 搜索结果显示标签：用户真实姓名 */
    private JLabel userInfoLabel;

    /** 搜索结果显示标签：账户余额 */
    private JLabel balanceLabel;

    /** 搜索结果显示标签：会员等级名称 */
    private JLabel vipLevelLabel;

    /** 用户信息展示面板，搜索到用户后显示其详细信息 */
    private JPanel userInfoPanel;

    /**
     * 构造方法
     * 初始化服务实例、构建界面布局、启动定时刷新器。
     */
    public OnlinePanel() {
        this.chargeService = new ChargeService();
        this.userService = new UserService();

        initComponents();
        initLayout();
        startTimer();
    }

    /**
     * 初始化所有界面组件
     * 依次创建顶部搜索上机区域、中间活跃记录区域、底部历史记录区域。
     */
    private void initComponents() {
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
    }

    /**
     * 初始化布局
     * 设置面板使用 BorderLayout 布局管理器。
     */
    private void initLayout() {
        setLayout(new BorderLayout(5, 5));
    }

    /**
     * 初始化顶部搜索和开始上机区域
     * 包含用户搜索栏、用户信息展示区、机器编号输入和开始上机按钮。
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("开始上机"),
                BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));

        /* 第一行：搜索用户区域 */
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel searchLabel = new JLabel("用户ID/用户名：");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(searchLabel);

        searchField = new JTextField(10);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("查找用户");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchButton.addActionListener(e -> searchUser());
        searchPanel.add(searchButton);

        topPanel.add(searchPanel, BorderLayout.NORTH);

        /* 第二行：用户信息展示区域 */
        userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        userInfoPanel.setVisible(false);

        userInfoLabel = new JLabel();
        userInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        userInfoLabel.setForeground(new Color(0, 100, 0));
        userInfoPanel.add(userInfoLabel);

        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        balanceLabel.setForeground(new Color(0, 100, 0));
        userInfoPanel.add(balanceLabel);

        vipLevelLabel = new JLabel();
        vipLevelLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        vipLevelLabel.setForeground(new Color(0, 100, 0));
        userInfoPanel.add(vipLevelLabel);

        topPanel.add(userInfoPanel, BorderLayout.CENTER);

        /* 第三行：机器编号和开始上机按钮 */
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel machineLabel = new JLabel("机器编号：");
        machineLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        actionPanel.add(machineLabel);

        machineNoField = new JTextField(5);
        machineNoField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        actionPanel.add(machineNoField);

        JButton startButton = new JButton("开始上机");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        startButton.setBackground(new Color(60, 179, 113));
        startButton.setForeground(Color.WHITE);
        startButton.addActionListener(e -> startOnline());
        actionPanel.add(startButton);

        topPanel.add(actionPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 初始化中间区域的活跃上机记录表格
     * 创建包含当前上机用户信息的 DefaultTableModel，
     * 将 JTable 放入 JScrollPane 中，设置单行选择模式且不可编辑。
     * 同时添加强制下机和刷新按钮。
     */
    private void initCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("当前上机用户"),
                BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));

        /* 活跃记录表格列名 */
        String[] activeColumnNames = {
                "记录ID", "用户ID", "用户名", "上机时间", "机器号",
                "已上机时长", "当前消费", "状态"
        };

        activeTableModel = new DefaultTableModel(activeColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activeTable = new JTable(activeTableModel);
        activeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        activeTable.getTableHeader().setReorderingAllowed(false);
        activeTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        activeTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));

        JScrollPane activeScrollPane = new JScrollPane(activeTable);
        activeScrollPane.setPreferredSize(new Dimension(950, 200));
        centerPanel.add(activeScrollPane, BorderLayout.CENTER);

        /* 按钮区域 */
        JPanel activeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton stopButton = new JButton("强制下机");
        stopButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        stopButton.setBackground(new Color(220, 80, 80));
        stopButton.setForeground(Color.WHITE);
        stopButton.addActionListener(e -> stopOnline());
        activeButtonPanel.add(stopButton);

        JButton refreshActiveButton = new JButton("刷新");
        refreshActiveButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshActiveButton.addActionListener(e -> loadActiveRecords());
        activeButtonPanel.add(refreshActiveButton);

        JButton refreshAllButton = new JButton("全部刷新");
        refreshAllButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshAllButton.addActionListener(e -> {
            loadActiveRecords();
            loadHistoryRecords();
        });
        activeButtonPanel.add(refreshAllButton);

        centerPanel.add(activeButtonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * 初始化底部区域的上机历史记录表格
     * 创建包含所有上机历史记录的 DefaultTableModel，
     * 将 JTable 放入 JScrollPane 中，设置单行选择模式且不可编辑。
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("上机历史记录"),
                BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));

        /* 历史记录表格列名 */
        String[] historyColumnNames = {
                "记录ID", "用户ID", "用户名", "上机时间", "下机时间",
                "时长(分钟)", "消费金额", "机器号", "状态"
        };

        historyTableModel = new DefaultTableModel(historyColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setPreferredSize(new Dimension(950, 180));
        bottomPanel.add(historyScrollPane, BorderLayout.CENTER);

        /* 刷新按钮 */
        JPanel historyButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton refreshHistoryButton = new JButton("刷新记录");
        refreshHistoryButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshHistoryButton.addActionListener(e -> loadHistoryRecords());
        historyButtonPanel.add(refreshHistoryButton);

        bottomPanel.add(historyButtonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 启动定时刷新器
     * 创建一个每秒执行一次的 javax.swing.Timer，
     * 用于实时更新活跃上机记录中的上机时长和当前消费金额。
     * Timer 在面板显示时启动，在面板隐藏时停止。
     */
    private void startTimer() {
        refreshTimer = new Timer(1000, e -> refreshTimerData());
        refreshTimer.start();
    }

    /**
     * 加载活跃上机记录
     * 从 ChargeService 获取所有状态为1（上机中）的记录，
     * 并填充到活跃记录表格中。
     */
    private void loadActiveRecords() {
        try {
            List<OnlineRecord> activeRecords = chargeService.getActiveRecords();

            activeTableModel.setRowCount(0);

            if (activeRecords == null || activeRecords.isEmpty()) {
                return;
            }

            for (OnlineRecord record : activeRecords) {
                /* 计算实时上机时长 */
                long durationMinutes = DateUtil.getMinutesBetween(
                        record.getLoginTime(), DateUtil.getCurrentDateTime());
                if (durationMinutes < 0) {
                    durationMinutes = 0;
                }

                /* 计算实时消费金额 */
                BigDecimal realTimeCost = chargeService.calculateFee(
                        record.getUserId(), durationMinutes);

                String statusText = getStatusText(record.getStatus());

                Object[] rowData = {
                        record.getId(),
                        record.getUserId(),
                        record.getUsername() != null ? record.getUsername() : "",
                        record.getLoginTime() != null ? record.getLoginTime() : "",
                        record.getMachineNo() != null ? record.getMachineNo() : "",
                        formatDuration(durationMinutes),
                        realTimeCost != null ? realTimeCost.toString() : "0.00",
                        statusText
                };
                activeTableModel.addRow(rowData);
            }
        } catch (Exception e) {
            System.err.println("加载活跃上机记录失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载上机历史记录
     * 从 ChargeService 获取所有上机记录（包含已下机和异常下机的记录），
     * 过滤掉当前正在上机中的记录，仅展示已完成的历史记录。
     */
    private void loadHistoryRecords() {
        try {
            /* 获取所有上机记录 */
            List<OnlineRecord> allRecords = chargeService.getActiveRecords();

            historyTableModel.setRowCount(0);

            /* 同时从DAO获取所有记录作为历史补充 */
            java.util.List<OnlineRecord> historyRecords =
                    new com.internetcafe.dao.OnlineRecordDao().findAll();

            if (historyRecords == null || historyRecords.isEmpty()) {
                return;
            }

            for (OnlineRecord record : historyRecords) {
                /* 只展示非活跃状态（已下机或异常下机）的记录 */
                if (record.getStatus() != null && record.getStatus() == 1) {
                    continue;
                }

                String statusText = getStatusText(record.getStatus());

                Object[] rowData = {
                        record.getId(),
                        record.getUserId(),
                        record.getUsername() != null ? record.getUsername() : "",
                        record.getLoginTime() != null ? record.getLoginTime() : "",
                        record.getLogoutTime() != null ? record.getLogoutTime() : "",
                        record.getDuration() != null ? record.getDuration() : 0,
                        record.getCost() != null ? record.getCost().toString() : "0.00",
                        record.getMachineNo() != null ? record.getMachineNo() : "",
                        statusText
                };
                historyTableModel.addRow(rowData);
            }
        } catch (Exception e) {
            System.err.println("加载上机历史记录失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 搜索用户
     * 根据搜索输入框中输入的用户ID或用户名查找用户。
     * 优先按用户ID进行精确匹配，如果输入的不是纯数字则按用户名查找。
     * 查找成功后显示用户的真实姓名、余额和会员等级信息。
     * 查找失败则弹出提示信息并隐藏用户信息面板。
     */
    private void searchUser() {
        String input = searchField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入用户ID或用户名进行查找！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = null;

            /* 判断输入是否为纯数字，如果是则按ID查找，否则按用户名查找 */
            if (input.matches("\\d+")) {
                Integer userId = Integer.parseInt(input);
                user = userService.getUserById(userId);
            } else {
                user = userService.getUserByUsername(input);
            }

            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "未找到用户：【" + input + "】，请检查输入是否正确！",
                        "查找失败",
                        JOptionPane.WARNING_MESSAGE);
                currentSelectedUser = null;
                userInfoPanel.setVisible(false);
                return;
            }

            /* 保存找到的用户对象 */
            currentSelectedUser = user;

            /* 更新用户信息展示 */
            userInfoLabel.setText("用户：" + (user.getRealName() != null ? user.getRealName() : user.getUsername()));
            balanceLabel.setText("余额：" + (user.getBalance() != null ? user.getBalance().toString() : "0.00") + "元");
            vipLevelLabel.setText("会员等级：" + (user.getVipLevelName() != null ? user.getVipLevelName() : "普通会员"));

            userInfoPanel.setVisible(true);
            userInfoPanel.revalidate();
            userInfoPanel.repaint();

            System.out.println("查找用户成功：用户ID=" + user.getId() + "，用户名=" + user.getUsername());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "查找用户时发生异常：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("查找用户异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 开始上机
     * 对当前查找到的用户执行上机操作。
     * 执行前会进行以下校验：
     * 1. 确保已通过搜索选中了一个用户
     * 2. 确保机器编号不为空
     * 校验通过后调用 ChargeService.startOnline() 创建上机记录，
     * 成功后刷新活跃记录表格并显示成功提示，失败则显示错误信息。
     */
    private void startOnline() {
        /* 校验：是否已查找到用户 */
        if (currentSelectedUser == null) {
            JOptionPane.showMessageDialog(this,
                    "请先查找并选择一个用户！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        /* 校验：机器编号是否为空 */
        String machineNo = machineNoField.getText().trim();
        if (machineNo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入机器编号！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            /* 调用计费服务开始上机 */
            Integer recordId = chargeService.startOnline(currentSelectedUser.getId(), machineNo);

            JOptionPane.showMessageDialog(this,
                    "上机成功！\n用户：" + currentSelectedUser.getRealName()
                            + "\n机器编号：" + machineNo
                            + "\n记录ID：" + recordId,
                    "上机成功",
                    JOptionPane.INFORMATION_MESSAGE);

            /* 清空机器编号输入框 */
            machineNoField.setText("");

            /* 刷新活跃上机记录表格 */
            loadActiveRecords();

            System.out.println("开始上机成功：用户ID=" + currentSelectedUser.getId()
                    + "，机器编号=" + machineNo + "，记录ID=" + recordId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "上机失败：" + e.getMessage(),
                    "上机失败",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("开始上机失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 强制下机
     * 对当前在活跃记录表格中选中的上机用户执行强制下机操作。
     * 弹出确认对话框，用户确认后调用 ChargeService.stopOnline() 完成结算。
     * 操作成功后刷新活跃记录和历史记录表格。
     * 如果未选中任何行，则弹出提示信息。
     */
    private void stopOnline() {
        int selectedRow = activeTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先在当前上机用户列表中选择一个用户！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer recordId = (Integer) activeTableModel.getValueAt(selectedRow, 0);
        String username = (String) activeTableModel.getValueAt(selectedRow, 2);

        int confirmResult = JOptionPane.showConfirmDialog(this,
                "确定要对用户【" + username + "】（记录ID：" + recordId + "）执行强制下机吗？\n"
                        + "系统将自动结算费用并从余额中扣除。",
                "确认强制下机",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            /* 调用计费服务执行下机结算 */
            BigDecimal cost = chargeService.stopOnline(recordId);

            JOptionPane.showMessageDialog(this,
                    "强制下机成功！\n用户：" + username
                            + "\n本次消费：" + (cost != null ? cost.toString() : "0.00") + "元",
                    "下机成功",
                    JOptionPane.INFORMATION_MESSAGE);

            /* 刷新两个表格 */
            loadActiveRecords();
            loadHistoryRecords();

            System.out.println("强制下机成功：记录ID=" + recordId + "，消费金额=" + cost);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "强制下机失败：" + e.getMessage(),
                    "下机失败",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("强制下机失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时器数据刷新
     * 由 javax.swing.Timer 每秒调用一次，用于实时更新活跃记录表格中
     * 每条记录的"已上机时长"和"当前消费"两列。
     * 时长通过 DateUtil.getMinutesBetween() 计算登录时间到当前时间的分钟差，
     * 费用通过 ChargeService.calculateFee() 根据用户会员等级和时长计算。
     * 此方法不会重新从数据库加载数据，仅更新表格中已有的行数据。
     */
    private void refreshTimerData() {
        int rowCount = activeTableModel.getRowCount();

        if (rowCount == 0) {
            return;
        }

        try {
            for (int i = 0; i < rowCount; i++) {
                /* 获取当前行的登录时间和用户ID */
                String loginTime = (String) activeTableModel.getValueAt(i, 3);
                Integer userId = (Integer) activeTableModel.getValueAt(i, 1);

                if (loginTime == null || loginTime.isEmpty() || userId == null) {
                    continue;
                }

                /* 计算实时上机时长（分钟） */
                long durationMinutes = DateUtil.getMinutesBetween(
                        loginTime, DateUtil.getCurrentDateTime());
                if (durationMinutes < 0) {
                    durationMinutes = 0;
                }

                /* 计算实时消费金额 */
                BigDecimal realTimeCost = chargeService.calculateFee(userId, durationMinutes);

                /* 更新表格中的时长和消费列 */
                activeTableModel.setValueAt(formatDuration(durationMinutes), i, 5);
                activeTableModel.setValueAt(
                        realTimeCost != null ? realTimeCost.toString() : "0.00", i, 6);
            }
        } catch (Exception e) {
            /* 单行更新失败不影响其他行的刷新 */
            System.err.println("刷新计时器数据时发生异常：" + e.getMessage());
        }
    }

    /**
     * 格式化上机时长
     * 将分钟数转换为可读的 "X小时Y分钟" 格式字符串。
     * 不足1小时时仅显示分钟数，不足1分钟时显示"不足1分钟"。
     *
     * @param minutes 上机时长（分钟）
     * @return 格式化后的时长字符串，如"2小时30分钟"、"45分钟"、"不足1分钟"
     */
    private String formatDuration(long minutes) {
        if (minutes <= 0) {
            return "不足1分钟";
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours > 0 && remainingMinutes > 0) {
            return hours + "小时" + remainingMinutes + "分钟";
        } else if (hours > 0) {
            return hours + "小时";
        } else {
            return remainingMinutes + "分钟";
        }
    }

    /**
     * 获取状态文本描述
     * 将数据库中的状态码转换为可读的中文描述。
     *
     * @param status 状态码：1=上机中, 2=已下机, 3=异常下机
     * @return 对应的中文状态描述
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "上机中";
            case 2:
                return "已下机";
            case 3:
                return "异常下机";
            default:
                return "未知状态";
        }
    }

    /**
     * 面板变为可见时的回调
     * 当用户切换到上机管理面板时，加载活跃记录数据并启动定时器。
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            /* 面板显示时加载数据并启动定时器 */
            loadActiveRecords();
            loadHistoryRecords();
            if (refreshTimer != null && !refreshTimer.isRunning()) {
                refreshTimer.start();
            }
        } else {
            /* 面板隐藏时停止定时器，节省系统资源 */
            if (refreshTimer != null && refreshTimer.isRunning()) {
                refreshTimer.stop();
            }
        }
    }
}