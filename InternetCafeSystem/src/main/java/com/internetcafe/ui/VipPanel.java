package com.internetcafe.ui;

import com.internetcafe.dao.VipLevelDao;
import com.internetcafe.entity.User;
import com.internetcafe.entity.VipLevel;
import com.internetcafe.service.UserService;
import com.internetcafe.service.VipService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员管理面板
 * 提供会员等级设置、用户VIP升级以及VIP统计功能。
 * 面板采用 BorderLayout 布局，顶部为会员等级设置表格，
 * 中间为用户VIP升级操作区，底部为VIP统计信息表格。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class VipPanel extends JPanel {

    /** 会员服务类，负责会员等级相关的业务逻辑操作 */
    private VipService vipService;

    /** 用户服务类，负责用户查询操作 */
    private UserService userService;

    /** 会员等级数据访问对象，用于修改折扣率 */
    private VipLevelDao vipLevelDao;

    /** 当前查找到的用户对象，null 表示尚未查找或未找到 */
    private User currentFoundUser;

    /** 当前加载的会员等级列表缓存 */
    private List<VipLevel> currentVipLevelList;

    /** 会员等级表格数据模型 */
    private DefaultTableModel vipLevelTableModel;

    /** 会员等级展示表格 */
    private JTable vipLevelTable;

    /** VIP统计表格数据模型 */
    private DefaultTableModel vipStatsTableModel;

    /** VIP统计展示表格 */
    private JTable vipStatsTable;

    /** 顶部搜索输入框，用于输入用户ID或用户名 */
    private JTextField searchField;

    /** 用户信息展示标签，显示查找到的用户名称、当前VIP等级、积分和余额 */
    private JLabel userInfoLabel;

    /** 会员等级下拉选择框，用于选择新的VIP等级 */
    private JComboBox<String> vipLevelComboBox;

    /**
     * 构造方法
     * 初始化服务实例和DAO对象，构建界面布局并加载初始数据。
     */
    public VipPanel() {
        this.vipService = new VipService();
        this.userService = new UserService();
        this.vipLevelDao = new VipLevelDao();

        initComponents();
        initLayout();
        loadVipLevels();
        loadVipStats();
    }

    /**
     * 初始化所有界面组件
     * 依次创建顶部会员等级设置区、中间用户VIP升级区、底部统计区。
     */
    private void initComponents() {
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
    }

    /**
     * 初始化顶部会员等级设置区域
     * 包含会员等级表格、刷新按钮和修改折扣按钮。
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("会员等级设置"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        String[] columnNames = {"ID", "等级名称", "折扣率", "描述"};
        vipLevelTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vipLevelTable = new JTable(vipLevelTableModel);
        vipLevelTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        vipLevelTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(vipLevelTable);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> loadVipLevels());
        buttonPanel.add(refreshButton);

        JButton modifyDiscountButton = new JButton("修改折扣");
        modifyDiscountButton.addActionListener(e -> modifyDiscount());
        buttonPanel.add(modifyDiscountButton);

        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 初始化中间用户VIP升级操作区域
     * 包含用户查找行、用户信息展示行、VIP等级选择行和升级按钮。
     * 使用 GridLayout 布局排列多行内容。
     */
    private void initCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("用户VIP管理"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        /* 第一行：用户查找 */
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLabel = new JLabel("搜索用户：");
        searchField = new JTextField(10);
        JButton searchButton = new JButton("查找");
        searchButton.addActionListener(e -> searchUser());
        searchRow.add(searchLabel);
        searchRow.add(searchField);
        searchRow.add(searchButton);
        centerPanel.add(searchRow);

        /* 第二行：用户信息展示 */
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        userInfoLabel = new JLabel("请先查找用户");
        infoRow.add(userInfoLabel);
        centerPanel.add(infoRow);

        /* 第三行：VIP等级选择与升级 */
        JPanel upgradeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel levelLabel = new JLabel("选择VIP等级：");
        vipLevelComboBox = new JComboBox<>();
        upgradeRow.add(levelLabel);
        upgradeRow.add(vipLevelComboBox);

        JButton upgradeButton = new JButton("升级会员");
        upgradeButton.addActionListener(e -> upgradeVip());
        upgradeRow.add(upgradeButton);
        centerPanel.add(upgradeRow);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * 初始化底部VIP统计区域
     * 包含VIP统计表格和刷新按钮。
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("VIP统计"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        String[] columnNames = {"等级名称", "用户数量", "折扣率"};
        vipStatsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vipStatsTable = new JTable(vipStatsTableModel);
        vipStatsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        vipStatsTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(vipStatsTable);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton refreshStatsButton = new JButton("刷新统计");
        refreshStatsButton.addActionListener(e -> loadVipStats());
        buttonPanel.add(refreshStatsButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 初始化布局
     * 设置面板使用 BorderLayout 布局管理器。
     */
    private void initLayout() {
        setLayout(new BorderLayout());
    }

    /**
     * 加载所有会员等级
     * 从 VipService 获取所有会员等级列表，更新会员等级表格和下拉选择框。
     * 如果加载失败则清空当前数据。
     */
    private void loadVipLevels() {
        vipLevelTableModel.setRowCount(0);
        vipLevelComboBox.removeAllItems();

        List<VipLevel> vipLevelList = vipService.getAllVipLevels();
        if (vipLevelList == null || vipLevelList.isEmpty()) {
            currentVipLevelList = null;
            return;
        }

        currentVipLevelList = vipLevelList;

        for (VipLevel vipLevel : vipLevelList) {
            Object[] rowData = {
                    vipLevel.getId(),
                    vipLevel.getLevelName() != null ? vipLevel.getLevelName() : "",
                    vipLevel.getDiscountRate() != null ? vipLevel.getDiscountRate().toString() : "1.00",
                    vipLevel.getDescription() != null ? vipLevel.getDescription() : ""
            };
            vipLevelTableModel.addRow(rowData);

            vipLevelComboBox.addItem(vipLevel.getLevelName());
        }
    }

    /**
     * 查找用户
     * 根据输入的ID或用户名查询用户信息。
     * 支持按数字ID精确查找，或按用户名模糊查找。
     * 查找成功后更新用户信息标签和VIP下拉框。
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
        info.append("用户名：").append(user.getUsername());
        info.append("  当前VIP：").append(user.getVipLevelName() != null ? user.getVipLevelName() : "普通会员");
        info.append("  积分：").append(user.getPoints() != null ? user.getPoints() : 0);
        info.append("  余额：").append(user.getBalance() != null ? user.getBalance().toString() : "0.00").append("元");
        userInfoLabel.setText(info.toString());

        /* 设置下拉框默认选中用户当前的VIP等级 */
        if (user.getVipLevelName() != null && vipLevelComboBox.getItemCount() > 0) {
            for (int i = 0; i < vipLevelComboBox.getItemCount(); i++) {
                if (vipLevelComboBox.getItemAt(i).equals(user.getVipLevelName())) {
                    vipLevelComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /**
     * 升级会员等级
     * 获取当前选中用户和下拉框中选择的新VIP等级，
     * 调用 VipService.upgradeVipLevel() 完成会员升级操作。
     * 升级成功后刷新用户信息、会员等级列表和统计数据。
     */
    private void upgradeVip() {
        if (currentFoundUser == null) {
            JOptionPane.showMessageDialog(this,
                    "请先查找要升级的用户！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentVipLevelList == null || currentVipLevelList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "暂无可用会员等级，请刷新会员等级列表！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = vipLevelComboBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentVipLevelList.size()) {
            JOptionPane.showMessageDialog(this,
                    "请选择有效的VIP等级！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer newLevelId = currentVipLevelList.get(selectedIndex).getId();
        String newLevelName = currentVipLevelList.get(selectedIndex).getLevelName();

        /* 弹出确认对话框 */
        int confirmResult = JOptionPane.showConfirmDialog(this,
                "确定要将用户【" + currentFoundUser.getUsername() + "】升级为【" + newLevelName + "】吗？",
                "确认升级",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            vipService.upgradeVipLevel(currentFoundUser.getId(), newLevelId);
            JOptionPane.showMessageDialog(this,
                    "会员升级成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);

            /* 刷新用户信息 */
            currentFoundUser = userService.getUserById(currentFoundUser.getId());
            if (currentFoundUser != null) {
                StringBuilder info = new StringBuilder();
                info.append("用户名：").append(currentFoundUser.getUsername());
                info.append("  当前VIP：").append(currentFoundUser.getVipLevelName() != null ? currentFoundUser.getVipLevelName() : "普通会员");
                info.append("  积分：").append(currentFoundUser.getPoints() != null ? currentFoundUser.getPoints() : 0);
                info.append("  余额：").append(currentFoundUser.getBalance() != null ? currentFoundUser.getBalance().toString() : "0.00").append("元");
                userInfoLabel.setText(info.toString());

                /* 更新下拉框选中项 */
                if (currentFoundUser.getVipLevelName() != null && vipLevelComboBox.getItemCount() > 0) {
                    for (int i = 0; i < vipLevelComboBox.getItemCount(); i++) {
                        if (vipLevelComboBox.getItemAt(i).equals(currentFoundUser.getVipLevelName())) {
                            vipLevelComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            loadVipLevels();
            loadVipStats();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "会员升级失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 修改会员等级折扣率
     * 获取表格中当前选中行的会员等级信息，
     * 弹出输入对话框让用户输入新的折扣率，
     * 验证通过后调用 VipLevelDao.update() 完成折扣率修改。
     */
    private void modifyDiscount() {
        int selectedRow = vipLevelTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要修改折扣率的会员等级！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer levelId = (Integer) vipLevelTableModel.getValueAt(selectedRow, 0);
        String levelName = (String) vipLevelTableModel.getValueAt(selectedRow, 1);
        String currentDiscount = (String) vipLevelTableModel.getValueAt(selectedRow, 2);

        String input = JOptionPane.showInputDialog(this,
                "请输入【" + levelName + "】的新折扣率（例如 0.80 表示8折）：",
                currentDiscount);

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        BigDecimal newDiscount;
        try {
            newDiscount = new BigDecimal(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "折扣率格式不正确，请输入有效的数字（例如 0.80）！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newDiscount.compareTo(BigDecimal.ZERO) <= 0 || newDiscount.compareTo(new BigDecimal("1.00")) > 0) {
            JOptionPane.showMessageDialog(this,
                    "折扣率必须在 0.01 到 1.00 之间！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        VipLevel vipLevel = vipService.getVipLevelById(levelId);
        if (vipLevel == null) {
            JOptionPane.showMessageDialog(this,
                    "无法获取该会员等级的详细信息！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        vipLevel.setDiscountRate(newDiscount);

        boolean success = vipLevelDao.update(vipLevel);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "折扣率修改成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
            loadVipLevels();
            loadVipStats();
        } else {
            JOptionPane.showMessageDialog(this,
                    "折扣率修改失败，请稍后重试！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载VIP统计数据
     * 统计每个会员等级下的用户数量，结合折扣率信息更新统计表格。
     * 统计逻辑：获取所有用户和所有会员等级，按会员等级分组计数。
     */
    private void loadVipStats() {
        vipStatsTableModel.setRowCount(0);

        List<VipLevel> vipLevelList = vipService.getAllVipLevels();
        if (vipLevelList == null || vipLevelList.isEmpty()) {
            return;
        }

        List<User> userList = userService.getAllUsers();
        if (userList == null) {
            userList = java.util.Collections.emptyList();
        }

        /* 统计每个VIP等级的用户数量 */
        Map<Integer, Integer> levelUserCountMap = new HashMap<>();
        for (User user : userList) {
            Integer vipLevelId = user.getVipLevel();
            if (vipLevelId != null) {
                levelUserCountMap.put(vipLevelId,
                        levelUserCountMap.getOrDefault(vipLevelId, 0) + 1);
            }
        }

        /* 填充统计表格 */
        for (VipLevel vipLevel : vipLevelList) {
            int userCount = levelUserCountMap.getOrDefault(vipLevel.getId(), 0);
            Object[] rowData = {
                    vipLevel.getLevelName() != null ? vipLevel.getLevelName() : "",
                    userCount,
                    vipLevel.getDiscountRate() != null ? vipLevel.getDiscountRate().toString() : "1.00"
            };
            vipStatsTableModel.addRow(rowData);
        }
    }
}