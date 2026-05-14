package com.internetcafe.ui;

import com.internetcafe.dao.ConsumeRecordDao;
import com.internetcafe.dao.RechargeRecordDao;
import com.internetcafe.entity.ConsumeRecord;
import com.internetcafe.entity.RechargeRecord;
import com.internetcafe.entity.User;
import com.internetcafe.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * 充值管理面板
 * 提供用户账户充值功能，以及充值记录和消费记录的查询展示。
 * 面板采用 BorderLayout 布局，顶部为充值操作区，
 * 中间区域分为上下两部分：充值记录和消费记录表格。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class RechargePanel extends JPanel {

    /** 用户服务类，负责用户查询和充值业务逻辑 */
    private UserService userService;

    /** 充值记录数据访问对象，负责充值记录的查询 */
    private RechargeRecordDao rechargeRecordDao;

    /** 消费记录数据访问对象，负责消费记录的查询 */
    private ConsumeRecordDao consumeRecordDao;

    /** 当前查找到的用户对象，null 表示尚未查找或未找到 */
    private User currentFoundUser;

    /** 顶部搜索输入框，用于输入用户ID或用户名 */
    private JTextField searchField;

    /** 用户信息展示标签，显示查找到的用户名称、余额和会员等级 */
    private JLabel userInfoLabel;

    /** 充值金额输入框 */
    private JTextField amountField;

    /** 充值记录表格数据模型 */
    private DefaultTableModel rechargeTableModel;

    /** 充值记录展示表格 */
    private JTable rechargeTable;

    /** 消费记录表格数据模型 */
    private DefaultTableModel consumeTableModel;

    /** 消费记录展示表格 */
    private JTable consumeTable;

    /** 当前操作员名称 */
    private static final String OPERATOR_NAME = "管理员";

    /**
     * 构造方法
     * 初始化服务实例、DAO 对象，构建界面布局并加载初始数据。
     */
    public RechargePanel() {
        this.userService = new UserService();
        this.rechargeRecordDao = new RechargeRecordDao();
        this.consumeRecordDao = new ConsumeRecordDao();

        initComponents();
        initLayout();
        loadRechargeRecords();
        loadConsumeRecords();
    }

    /**
     * 初始化所有界面组件
     * 依次创建顶部充值操作区、中间记录展示区。
     */
    private void initComponents() {
        initTopPanel();
        initCenterPanel();
    }

    /**
     * 初始化顶部充值操作区域
     * 包含用户查找行、用户信息展示行和充值操作行。
     * 使用 GridLayout 布局排列三行内容。
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("充值操作"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        /* 第一行：用户查找 */
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLabel = new JLabel("用户ID/用户名：");
        searchField = new JTextField(10);
        JButton searchButton = new JButton("查找");
        searchButton.addActionListener(e -> searchUser());
        searchRow.add(searchLabel);
        searchRow.add(searchField);
        searchRow.add(searchButton);
        topPanel.add(searchRow);

        /* 第二行：用户信息展示 */
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        userInfoLabel = new JLabel("请先查找用户");
        infoRow.add(userInfoLabel);
        topPanel.add(infoRow);

        /* 第三行：充值操作 */
        JPanel rechargeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel amountLabel = new JLabel("充值金额：");
        amountField = new JTextField(8);
        JButton rechargeButton = new JButton("确认充值");
        rechargeButton.addActionListener(e -> doRecharge());
        rechargeRow.add(amountLabel);
        rechargeRow.add(amountField);
        rechargeRow.add(rechargeButton);
        topPanel.add(rechargeRow);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 初始化中间记录展示区域
     * 上半部分为充值记录表格，下半部分为消费记录表格，
     * 使用 GridLayout(2, 1) 均分上下两部分。
     */
    private void initCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        /* 上半部分：充值记录 */
        JPanel rechargeRecordPanel = new JPanel(new BorderLayout());
        rechargeRecordPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("充值记录"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        String[] rechargeColumnNames = {"ID", "用户ID", "用户名", "充值金额", "充值时间", "操作员"};
        rechargeTableModel = new DefaultTableModel(rechargeColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rechargeTable = new JTable(rechargeTableModel);
        rechargeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        rechargeTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane rechargeScrollPane = new JScrollPane(rechargeTable);

        JPanel rechargeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton refreshRechargeButton = new JButton("刷新");
        refreshRechargeButton.addActionListener(e -> loadRechargeRecords());
        rechargeButtonPanel.add(refreshRechargeButton);

        rechargeRecordPanel.add(rechargeScrollPane, BorderLayout.CENTER);
        rechargeRecordPanel.add(rechargeButtonPanel, BorderLayout.SOUTH);

        centerPanel.add(rechargeRecordPanel);

        /* 下半部分：消费记录 */
        JPanel consumeRecordPanel = new JPanel(new BorderLayout());
        consumeRecordPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("消费记录"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        String[] consumeColumnNames = {"ID", "用户ID", "用户名", "消费金额", "消费类型", "消费时间"};
        consumeTableModel = new DefaultTableModel(consumeColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        consumeTable = new JTable(consumeTableModel);
        consumeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        consumeTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane consumeScrollPane = new JScrollPane(consumeTable);

        JPanel consumeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton refreshConsumeButton = new JButton("刷新");
        refreshConsumeButton.addActionListener(e -> loadConsumeRecords());
        consumeButtonPanel.add(refreshConsumeButton);

        consumeRecordPanel.add(consumeScrollPane, BorderLayout.CENTER);
        consumeRecordPanel.add(consumeButtonPanel, BorderLayout.SOUTH);

        centerPanel.add(consumeRecordPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * 初始化布局
     * 设置面板使用 BorderLayout 布局管理器。
     */
    private void initLayout() {
        setLayout(new BorderLayout());
    }

    /**
     * 查找用户
     * 根据输入的ID或用户名查询用户信息。
     * 支持按数字ID精确查找，或按用户名模糊查找。
     * 查找成功后更新用户信息标签显示。
     */
    private void searchUser() {
        String input = searchField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入用户ID或用户名！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = null;

        /* 尝试将输入解析为数字ID进行精确查找 */
        try {
            Integer userId = Integer.parseInt(input);
            user = userService.getUserById(userId);
        } catch (NumberFormatException e) {
            /* 输入不是数字，按用户名进行模糊查找 */
            List<User> userList = userService.searchUsers(input);
            if (userList != null && !userList.isEmpty()) {
                if (userList.size() > 1) {
                    JOptionPane.showMessageDialog(this,
                            "找到多个匹配用户，请使用ID精确查找！",
                            "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                user = userList.get(0);
            }
        }

        if (user == null) {
            currentFoundUser = null;
            userInfoLabel.setText("未找到该用户，请检查输入");
            JOptionPane.showMessageDialog(this,
                    "未找到该用户，请检查输入的用户ID或用户名！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentFoundUser = user;

        /* 构建用户信息展示文本 */
        StringBuilder info = new StringBuilder();
        info.append("用户：").append(user.getUsername());
        info.append("  当前余额：").append(user.getBalance() != null ? user.getBalance().toString() : "0.00").append("元");
        info.append("  会员等级：").append(user.getVipLevelName() != null ? user.getVipLevelName() : "普通会员");
        userInfoLabel.setText(info.toString());
    }

    /**
     * 执行充值操作
     * 校验充值金额是否大于0，调用 UserService.recharge() 完成充值，
     * 成功后弹出提示并刷新用户信息和充值记录。
     */
    private void doRecharge() {
        if (currentFoundUser == null) {
            JOptionPane.showMessageDialog(this,
                    "请先查找要充值的用户！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amountText = amountField.getText().trim();

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入充值金额！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "充值金额格式不正确，请输入有效的数字！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "充值金额必须大于0！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = userService.recharge(currentFoundUser.getId(), amount, OPERATOR_NAME);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "充值成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);

            amountField.setText("");

            /* 刷新用户信息，获取最新的余额 */
            currentFoundUser = userService.getUserById(currentFoundUser.getId());
            if (currentFoundUser != null) {
                StringBuilder info = new StringBuilder();
                info.append("用户：").append(currentFoundUser.getUsername());
                info.append("  当前余额：").append(currentFoundUser.getBalance() != null ? currentFoundUser.getBalance().toString() : "0.00").append("元");
                info.append("  会员等级：").append(currentFoundUser.getVipLevelName() != null ? currentFoundUser.getVipLevelName() : "普通会员");
                userInfoLabel.setText(info.toString());
            }

            loadRechargeRecords();
        } else {
            JOptionPane.showMessageDialog(this,
                    "充值失败，请稍后重试！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载充值记录
     * 从 RechargeRecordDao 获取所有充值记录并更新表格显示。
     * 数据按充值时间降序排列。
     */
    private void loadRechargeRecords() {
        rechargeTableModel.setRowCount(0);

        List<RechargeRecord> recordList = rechargeRecordDao.findAll();
        if (recordList == null || recordList.isEmpty()) {
            return;
        }

        for (RechargeRecord record : recordList) {
            Object[] rowData = {
                    record.getId(),
                    record.getUserId(),
                    record.getUsername() != null ? record.getUsername() : "",
                    record.getAmount() != null ? record.getAmount().toString() : "0.00",
                    record.getRechargeTime() != null ? record.getRechargeTime() : "",
                    record.getOperatorName() != null ? record.getOperatorName() : ""
            };
            rechargeTableModel.addRow(rowData);
        }
    }

    /**
     * 加载消费记录
     * 从 ConsumeRecordDao 获取所有消费记录并更新表格显示。
     * 数据按消费时间降序排列。
     */
    private void loadConsumeRecords() {
        consumeTableModel.setRowCount(0);

        List<ConsumeRecord> recordList = consumeRecordDao.findAll();
        if (recordList == null || recordList.isEmpty()) {
            return;
        }

        for (ConsumeRecord record : recordList) {
            Object[] rowData = {
                    record.getId(),
                    record.getUserId(),
                    record.getUsername() != null ? record.getUsername() : "",
                    record.getAmount() != null ? record.getAmount().toString() : "0.00",
                    record.getConsumeType() != null ? record.getConsumeType() : "",
                    record.getCreateTime() != null ? record.getCreateTime() : ""
            };
            consumeTableModel.addRow(rowData);
        }
    }
}