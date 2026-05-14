package com.internetcafe.ui;

import com.internetcafe.entity.User;
import com.internetcafe.entity.VipLevel;
import com.internetcafe.service.LogService;
import com.internetcafe.service.UserService;
import com.internetcafe.service.VipService;
import com.internetcafe.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * 用户管理面板
 * 提供用户的增删改查、模糊搜索和分页浏览功能。
 * 面板采用 BorderLayout 布局，顶部为搜索和操作工具栏，
 * 中间为 JTable 数据表格，底部为分页导航栏。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class UserPanel extends JPanel {

    /** 用户服务类，负责用户的业务逻辑操作 */
    private UserService userService;

    /** 日志服务类，负责记录用户删除等操作日志 */
    private LogService logService;

    /** 会员服务类，负责获取会员等级列表供下拉框使用 */
    private VipService vipService;

    /** 表格数据模型，存储表格中的行列数据 */
    private DefaultTableModel tableModel;

    /** 用户数据展示表格 */
    private JTable table;

    /** 当前页码，从 1 开始 */
    private int currentPage = 1;

    /** 每页显示的记录数 */
    private int pageSize = 15;

    /** 当前搜索关键词，空字符串表示无搜索条件 */
    private String currentKeyword = "";

    /** 搜索输入框 */
    private JTextField searchField;

    /** 分页信息标签：显示"第 X 页 / 共 Y 页" */
    private JLabel pageInfoLabel;

    /** 总记录数标签：显示"共 N 条记录" */
    private JLabel totalCountLabel;

    /** 上一页按钮 */
    private JButton prevButton;

    /** 下一页按钮 */
    private JButton nextButton;

    /** 当前操作员名称，用于日志记录 */
    private static final String OPERATOR_NAME = "管理员";

    /**
     * 构造方法
     * 初始化服务实例、构建界面布局、加载初始数据。
     */
    public UserPanel() {
        this.userService = new UserService();
        this.logService = new LogService();
        this.vipService = new VipService();

        initComponents();
        initLayout();
        loadData();
    }

    /**
     * 初始化所有界面组件
     * 包括搜索工具栏、数据表格和分页导航栏的创建与配置。
     */
    private void initComponents() {
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
    }

    /**
     * 初始化顶部搜索和操作工具栏
     * 包含搜索标签、搜索输入框、搜索按钮以及增删改操作按钮。
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel searchLabel = new JLabel("用户名/姓名/手机号：");
        topPanel.add(searchLabel);

        searchField = new JTextField(15);
        topPanel.add(searchField);

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchUsers());
        topPanel.add(searchButton);

        JButton addButton = new JButton("添加用户");
        addButton.addActionListener(e -> addUser());
        topPanel.add(addButton);

        JButton editButton = new JButton("修改用户");
        editButton.addActionListener(e -> editUser());
        topPanel.add(editButton);

        JButton deleteButton = new JButton("删除用户");
        deleteButton.addActionListener(e -> deleteUser());
        topPanel.add(deleteButton);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 初始化中间区域的数据表格
     * 创建包含所有用户信息列的 DefaultTableModel，
     * 将 JTable 放入 JScrollPane 中，设置单行选择模式且不可编辑。
     */
    private void initCenterPanel() {
        String[] columnNames = {"ID", "用户名", "真实姓名", "身份证号", "手机号", "余额",
                "会员等级", "积分", "状态", "创建时间"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 400));

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 初始化底部分页导航栏
     * 包含上一页/下一页按钮、页码信息标签和总记录数标签。
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        prevButton = new JButton("上一页");
        prevButton.addActionListener(e -> previousPage());
        bottomPanel.add(prevButton);

        pageInfoLabel = new JLabel("第 1 页 / 共 1 页");
        bottomPanel.add(pageInfoLabel);

        nextButton = new JButton("下一页");
        nextButton.addActionListener(e -> nextPage());
        bottomPanel.add(nextButton);

        totalCountLabel = new JLabel("共 0 条记录");
        bottomPanel.add(totalCountLabel);

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
     * 加载用户数据
     * 从 UserService 获取当前页的用户列表和总记录数，
     * 更新表格内容和分页导航信息。
     */
    private void loadData() {
        List<User> userList = userService.getUsersByPage(currentPage, pageSize, currentKeyword);
        int totalCount = userService.getTotalCount(currentKeyword);

        updateTable(userList);
        updatePagination(totalCount);
    }

    /**
     * 更新表格数据
     * 清空表格当前所有行，然后将传入的用户列表逐行添加到表格中。
     * 对于状态字段，将数值转换为可读的中文描述（0-禁用，1-正常）。
     *
     * @param userList 待显示的用户列表
     */
    private void updateTable(List<User> userList) {
        tableModel.setRowCount(0);

        if (userList == null || userList.isEmpty()) {
            return;
        }

        for (User user : userList) {
            Object[] rowData = {
                    user.getId(),
                    user.getUsername(),
                    user.getRealName() != null ? user.getRealName() : "",
                    user.getIdCard() != null ? user.getIdCard() : "",
                    user.getPhone() != null ? user.getPhone() : "",
                    user.getBalance() != null ? user.getBalance().toString() : "0.00",
                    user.getVipLevelName() != null ? user.getVipLevelName() : "普通会员",
                    user.getPoints() != null ? user.getPoints() : 0,
                    user.getStatus() != null && user.getStatus() == 1 ? "正常" : "禁用",
                    user.getCreateTime() != null ? user.getCreateTime() : ""
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * 更新分页导航信息
     * 根据总记录数计算总页数，更新页码标签、总记录数标签以及按钮的启用状态。
     *
     * @param totalCount 符合条件的用户总记录数
     */
    private void updatePagination(int totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }

        pageInfoLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");
        totalCountLabel.setText("共 " + totalCount + " 条记录");

        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    /**
     * 刷新表格数据
     * 保持当前搜索条件，重新加载数据并更新表格显示。
     */
    private void refreshTable() {
        loadData();
    }

    /**
     * 翻到上一页
     * 如果当前页大于第 1 页，则页码减 1 并重新加载数据。
     */
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }

    /**
     * 翻到下一页
     * 获取总记录数计算总页数，如果当前页小于总页数则页码加 1 并重新加载数据。
     */
    private void nextPage() {
        int totalCount = userService.getTotalCount(currentKeyword);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }

        if (currentPage < totalPages) {
            currentPage++;
            loadData();
        }
    }

    /**
     * 执行模糊搜索
     * 获取搜索输入框中的关键词，重置页码为第 1 页，
     * 保存搜索关键词并重新加载数据。
     */
    private void searchUsers() {
        currentKeyword = searchField.getText().trim();
        currentPage = 1;
        loadData();
    }

    /**
     * 添加用户
     * 打开新增用户对话框，用户在对话框中填写信息后，
     * 调用 UserService.addUser() 完成添加操作。
     */
    private void addUser() {
        showAddEditDialog(null, false);
    }

    /**
     * 修改用户
     * 获取表格中当前选中行的用户数据，打开编辑对话框，
     * 预填充用户已有信息，修改后调用 UserService.updateUser() 完成更新。
     * 如果未选中任何行，则弹出提示信息。
     */
    private void editUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要修改的用户！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "无法获取该用户的详细信息，请刷新后重试！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        showAddEditDialog(user, true);
    }

    /**
     * 删除用户
     * 获取表格中当前选中行的用户信息，弹出确认对话框，
     * 用户确认后调用 UserService.deleteUser() 执行删除操作，
     * 并记录删除日志。
     * 如果未选中任何行，则弹出提示信息。
     */
    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要删除的用户！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        int confirmResult = JOptionPane.showConfirmDialog(this,
                "确定要删除用户【" + username + "】（ID：" + userId + "）吗？\n此操作不可撤销！",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = userService.deleteUser(userId);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "用户【" + username + "】删除成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);

            logService.addDeleteLog(OPERATOR_NAME,
                    "删除了用户：【" + username + "】（ID：" + userId + "）");

            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "用户【" + username + "】删除失败，请稍后重试！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示新增/编辑用户对话框
     * 根据 isEdit 参数决定对话框标题和表单的初始值。
     * 新增模式下所有字段为空，编辑模式下预填充用户已有信息。
     * 表单包含用户名、密码、真实姓名、身份证号、手机号和会员等级字段。
     * 提交时会使用 ValidationUtil 对所有必填字段进行校验。
     *
     * @param user   待编辑的用户对象，新增模式下传入 null
     * @param isEdit true 表示编辑模式，false 表示新增模式
     */
    private void showAddEditDialog(User user, boolean isEdit) {
        String title = isEdit ? "修改用户" : "添加用户";

        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel usernameLabel = new JLabel("用户名：");
        JTextField usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("密码：");
        JTextField passwordField = new JTextField(15);

        JLabel realNameLabel = new JLabel("真实姓名：");
        JTextField realNameField = new JTextField(15);

        JLabel idCardLabel = new JLabel("身份证号：");
        JTextField idCardField = new JTextField(15);

        JLabel phoneLabel = new JLabel("手机号：");
        JTextField phoneField = new JTextField(15);

        JLabel vipLevelLabel = new JLabel("会员等级：");
        JComboBox<String> vipLevelComboBox = new JComboBox<>();

        List<VipLevel> vipLevelList = vipService.getAllVipLevels();
        if (vipLevelList != null && !vipLevelList.isEmpty()) {
            for (VipLevel vipLevel : vipLevelList) {
                vipLevelComboBox.addItem(vipLevel.getLevelName());
            }
        } else {
            vipLevelComboBox.addItem("普通会员");
        }

        if (isEdit && user != null) {
            usernameField.setText(user.getUsername() != null ? user.getUsername() : "");
            passwordField.setText("");
            realNameField.setText(user.getRealName() != null ? user.getRealName() : "");
            idCardField.setText(user.getIdCard() != null ? user.getIdCard() : "");
            phoneField.setText(user.getPhone() != null ? user.getPhone() : "");

            if (user.getVipLevelName() != null) {
                for (int i = 0; i < vipLevelComboBox.getItemCount(); i++) {
                    if (vipLevelComboBox.getItemAt(i).equals(user.getVipLevelName())) {
                        vipLevelComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(realNameLabel);
        formPanel.add(realNameField);
        formPanel.add(idCardLabel);
        formPanel.add(idCardField);
        formPanel.add(phoneLabel);
        formPanel.add(phoneField);
        formPanel.add(vipLevelLabel);
        formPanel.add(vipLevelComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String realName = realNameField.getText().trim();
            String idCard = idCardField.getText().trim();
            String phone = phoneField.getText().trim();
            String vipLevelName = (String) vipLevelComboBox.getSelectedItem();

            if (ValidationUtil.isEmpty(username)) {
                JOptionPane.showMessageDialog(dialog,
                        "用户名不能为空！",
                        "校验失败",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!isEdit && ValidationUtil.isEmpty(password)) {
                JOptionPane.showMessageDialog(dialog,
                        "密码不能为空！",
                        "校验失败",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ValidationUtil.isEmpty(realName)) {
                JOptionPane.showMessageDialog(dialog,
                        "真实姓名不能为空！",
                        "校验失败",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ValidationUtil.isNotEmpty(phone) && !ValidationUtil.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(dialog,
                        "手机号格式不正确，请输入有效的11位手机号码！",
                        "校验失败",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ValidationUtil.isNotEmpty(idCard) && !ValidationUtil.isValidIdCard(idCard)) {
                JOptionPane.showMessageDialog(dialog,
                        "身份证号格式不正确，请输入有效的18位身份证号码！",
                        "校验失败",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            User saveUser;
            if (isEdit && user != null) {
                saveUser = user;
                saveUser.setUsername(username);
                if (ValidationUtil.isNotEmpty(password)) {
                    saveUser.setPassword(password);
                }
                saveUser.setRealName(realName);
                saveUser.setIdCard(idCard);
                saveUser.setPhone(phone);
            } else {
                saveUser = new User();
                saveUser.setUsername(username);
                saveUser.setPassword(password);
                saveUser.setRealName(realName);
                saveUser.setIdCard(idCard);
                saveUser.setPhone(phone);
            }

            int vipLevelId = 1;
            if (vipLevelList != null && !vipLevelList.isEmpty()) {
                int selectedIndex = vipLevelComboBox.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < vipLevelList.size()) {
                    vipLevelId = vipLevelList.get(selectedIndex).getId();
                }
            }
            saveUser.setVipLevel(vipLevelId);

            String result;
            if (isEdit) {
                result = userService.updateUser(saveUser);
            } else {
                result = userService.addUser(saveUser);
            }

            if (result != null && (result.contains("成功"))) {
                JOptionPane.showMessageDialog(dialog,
                        result,
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        result != null ? result : "操作失败，请稍后重试！",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}