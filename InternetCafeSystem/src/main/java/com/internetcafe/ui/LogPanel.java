package com.internetcafe.ui;

import com.internetcafe.dao.SystemLogDao;
import com.internetcafe.entity.SystemLog;
import com.internetcafe.service.LogService;
import com.internetcafe.util.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 系统日志面板
 * 提供系统操作日志的查询、筛选、展示和清理功能。
 * 面板采用 BorderLayout 布局，顶部为多条件筛选栏，
 * 中间为日志数据表格，底部为操作按钮栏。
 * 日志数据按操作时间降序排列，最新的日志显示在最上方。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class LogPanel extends JPanel {

    /** 日志服务类，负责日志的业务逻辑操作 */
    private LogService logService;

    /** 系统日志数据访问对象，用于日志清理等直接数据库操作 */
    private SystemLogDao systemLogDao;

    /** 日志表格数据模型 */
    private DefaultTableModel logTableModel;

    /** 日志数据展示表格 */
    private JTable logTable;

    /** 操作人筛选输入框 */
    private JTextField operatorField;

    /** 操作类型筛选下拉框 */
    private JComboBox<String> typeComboBox;

    /** 开始时间输入框 */
    private JTextField startDateField;

    /** 结束时间输入框 */
    private JTextField endDateField;

    /** 缓存的全部日志列表，用于多条件组合筛选 */
    private List<SystemLog> allLogsCache;

    /**
     * 构造方法
     * 初始化服务实例、构建界面布局并加载初始日志数据。
     */
    public LogPanel() {
        this.logService = new LogService();
        this.systemLogDao = new SystemLogDao();
        this.allLogsCache = new ArrayList<>();

        initComponents();
        initLayout();
        loadLogs();
    }

    /**
     * 初始化所有界面组件
     * 分别初始化顶部筛选栏、中间日志表格和底部按钮栏。
     */
    private void initComponents() {
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
    }

    /**
     * 初始化顶部筛选栏
     * 包含操作人输入框、操作类型下拉框、开始/结束时间输入框、
     * 查询按钮和重置按钮，使用 FlowLayout 水平排列。
     * 所有筛选条件支持组合查询，筛选结果按时间降序排列。
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createTitledBorder("筛选条件")
        ));

        topPanel.add(new JLabel("操作人："));
        operatorField = new JTextField(8);
        topPanel.add(operatorField);

        topPanel.add(new JLabel("操作类型："));
        typeComboBox = new JComboBox<>(new String[]{"全部", "登录", "删除", "修改", "错误"});
        topPanel.add(typeComboBox);

        topPanel.add(new JLabel("开始时间："));
        startDateField = new JTextField(10);
        startDateField.setToolTipText("格式：yyyy-MM-dd，例如 2025-01-01");
        topPanel.add(startDateField);

        topPanel.add(new JLabel("结束时间："));
        endDateField = new JTextField(10);
        endDateField.setToolTipText("格式：yyyy-MM-dd，例如 2025-12-31");
        topPanel.add(endDateField);

        JButton searchButton = new JButton("查询");
        searchButton.addActionListener(e -> searchLogs());
        topPanel.add(searchButton);

        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> resetFilters());
        topPanel.add(resetButton);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 初始化中间区域的日志表格
     * 创建包含日志各字段的 DefaultTableModel，
     * 将 JTable 放入 JScrollPane 中，设置单行选择模式且不可编辑。
     * 表格列：ID、操作人、操作类型、操作详情、操作时间。
     */
    private void initCenterPanel() {
        String[] columnNames = {"ID", "操作人", "操作类型", "操作详情", "操作时间"};

        logTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(logTableModel);
        logTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        logTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        logTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        logTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 初始化底部操作按钮栏
     * 包含"刷新"按钮和"清理日志"按钮。
     * "刷新"用于重新从数据库加载所有日志数据，
     * "清理日志"用于删除指定日期之前的过期日志记录。
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> {
            loadLogs();
            resetFilters();
        });
        bottomPanel.add(refreshButton);

        JButton cleanButton = new JButton("清理日志");
        cleanButton.addActionListener(e -> cleanLogs());
        bottomPanel.add(cleanButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 初始化面板布局
     * 设置面板使用 BorderLayout 布局管理器。
     */
    private void initLayout() {
        setLayout(new BorderLayout());
    }

    /**
     * 加载所有日志数据
     * 从 LogService 获取全部系统日志记录，
     * 按操作时间降序排列后缓存到 allLogsCache 中，
     * 并更新表格显示。
     */
    private void loadLogs() {
        List<SystemLog> logs = logService.getAllLogs();
        if (logs == null) {
            logs = new ArrayList<>();
        }

        Collections.sort(logs, new Comparator<SystemLog>() {
            @Override
            public int compare(SystemLog o1, SystemLog o2) {
                String t1 = o1.getCreateTime();
                String t2 = o2.getCreateTime();
                if (t1 == null && t2 == null) return 0;
                if (t1 == null) return 1;
                if (t2 == null) return -1;
                return t2.compareTo(t1);
            }
        });

        allLogsCache = logs;
        updateTable(logs);
    }

    /**
     * 执行多条件组合查询
     * 根据顶部筛选栏中输入的操作人、操作类型、开始时间和结束时间，
     * 对缓存的全部日志进行内存过滤。
     * 所有条件均为可选，支持任意组合：
     * - 操作人：模糊匹配（包含关系）
     * - 操作类型：精确匹配（"全部"表示不筛选）
     * - 时间范围：按日期区间筛选
     * 筛选结果保持时间降序排列。
     */
    private void searchLogs() {
        String operator = operatorField.getText().trim();
        String type = (String) typeComboBox.getSelectedItem();
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();

        List<SystemLog> filtered = new ArrayList<>();

        for (SystemLog log : allLogsCache) {
            boolean match = true;

            if (!operator.isEmpty()) {
                String logOperator = log.getOperatorName();
                if (logOperator == null || !logOperator.contains(operator)) {
                    match = false;
                }
            }

            if (match && type != null && !"全部".equals(type)) {
                String logType = log.getOperationType();
                if (logType == null || !logType.equals(type)) {
                    match = false;
                }
            }

            if (match && !startDate.isEmpty()) {
                String logTime = log.getCreateTime();
                if (logTime == null || logTime.compareTo(startDate + " 00:00:00") < 0) {
                    match = false;
                }
            }

            if (match && !endDate.isEmpty()) {
                String logTime = log.getCreateTime();
                if (logTime == null || logTime.compareTo(endDate + " 23:59:59") > 0) {
                    match = false;
                }
            }

            if (match) {
                filtered.add(log);
            }
        }

        updateTable(filtered);
    }

    /**
     * 重置所有筛选条件
     * 清空操作人输入框、操作类型下拉框恢复为"全部"、
     * 清空开始/结束时间输入框，然后重新显示全部日志数据。
     */
    private void resetFilters() {
        operatorField.setText("");
        typeComboBox.setSelectedItem("全部");
        startDateField.setText("");
        endDateField.setText("");

        updateTable(allLogsCache);
    }

    /**
     * 清理过期日志
     * 弹出日期输入对话框让用户输入截止日期，
     * 删除该日期之前的所有日志记录。
     * 清理完成后自动刷新日志列表。
     * 清理前会弹出确认对话框，防止误操作。
     */
    private void cleanLogs() {
        String dateStr = JOptionPane.showInputDialog(this,
                "请输入截止日期（格式：yyyy-MM-dd）：\n将删除该日期之前的所有日志记录。",
                "清理日志",
                JOptionPane.QUESTION_MESSAGE);

        if (dateStr == null || dateStr.trim().isEmpty()) {
            return;
        }

        dateStr = dateStr.trim();

        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "日期格式不正确，请输入格式为 yyyy-MM-dd 的日期！\n例如：2025-01-01",
                    "格式错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmResult = JOptionPane.showConfirmDialog(this,
                "确定要删除【" + dateStr + "】之前的所有日志吗？\n此操作不可撤销！",
                "确认清理",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        int deletedCount = systemLogDao.deleteBefore(dateStr);

        JOptionPane.showMessageDialog(this,
                "日志清理完成！共删除 " + deletedCount + " 条记录。",
                "清理完成",
                JOptionPane.INFORMATION_MESSAGE);

        loadLogs();
    }

    /**
     * 更新表格数据
     * 清空表格当前所有行，然后将传入的日志列表逐行添加到表格中。
     *
     * @param logs 待显示的日志列表
     */
    private void updateTable(List<SystemLog> logs) {
        logTableModel.setRowCount(0);

        if (logs == null || logs.isEmpty()) {
            return;
        }

        for (SystemLog log : logs) {
            Object[] rowData = {
                    log.getId(),
                    log.getOperatorName() != null ? log.getOperatorName() : "",
                    log.getOperationType() != null ? log.getOperationType() : "",
                    log.getOperationContent() != null ? log.getOperationContent() : "",
                    log.getCreateTime() != null ? log.getCreateTime() : ""
            };
            logTableModel.addRow(rowData);
        }
    }
}