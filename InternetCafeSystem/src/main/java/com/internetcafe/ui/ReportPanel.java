package com.internetcafe.ui;

import com.internetcafe.service.ReportService;
import com.internetcafe.util.ExportUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 报表统计面板
 * 提供网吧运营数据的统计展示功能，包括今日/本月营收、充值统计、上机次数、
 * 用户消费排行和月度营收统计等核心报表。
 * 面板采用 BorderLayout 布局，顶部为概览统计卡片，
 * 中间为消费排行和月度营收统计的分割面板，底部为操作按钮栏。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class ReportPanel extends JPanel {

    /** 报表服务类，负责所有统计数据的业务逻辑 */
    private ReportService reportService;

    /** 用户消费排行表格数据模型 */
    private DefaultTableModel rankingModel;

    /** 月度营收统计表格数据模型 */
    private DefaultTableModel statsModel;

    /** 用户消费排行表格 */
    private JTable rankingTable;

    /** 月度营收统计表格 */
    private JTable statsTable;

    /** 年度选择下拉框，用于切换不同年份的月度营收统计 */
    private JComboBox<Integer> yearComboBox;

    /** 今日营业额标签 */
    private JLabel todayRevenueLabel;

    /** 本月营业额标签 */
    private JLabel monthRevenueLabel;

    /** 今日充值标签 */
    private JLabel todayRechargeLabel;

    /** 本月充值标签 */
    private JLabel monthRechargeLabel;

    /** 今日上机次数标签 */
    private JLabel todayOnlineCountLabel;

    /**
     * 构造方法
     * 初始化报表服务实例、构建界面布局并加载初始统计数据。
     */
    public ReportPanel() {
        this.reportService = new ReportService();

        initComponents();
        initLayout();
        refreshAll();
    }

    /**
     * 初始化所有界面组件
     * 分别初始化顶部概览卡片、中间分割面板和底部按钮栏。
     */
    private void initComponents() {
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
    }

    /**
     * 初始化顶部概览统计卡片区域
     * 使用 GridLayout(1, 5) 布局，展示五个核心统计指标卡片：
     * 今日营业额、本月营业额、今日充值、本月充值和今日上机次数。
     * 每张卡片包含标题和数值两部分，数值使用较大字体突出显示。
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(createSummaryCard("今日营业额", todayRevenueLabel = new JLabel("0.00", SwingConstants.CENTER)));
        topPanel.add(createSummaryCard("本月营业额", monthRevenueLabel = new JLabel("0.00", SwingConstants.CENTER)));
        topPanel.add(createSummaryCard("今日充值", todayRechargeLabel = new JLabel("0.00", SwingConstants.CENTER)));
        topPanel.add(createSummaryCard("本月充值", monthRechargeLabel = new JLabel("0.00", SwingConstants.CENTER)));
        topPanel.add(createSummaryCard("今日上机次数", todayOnlineCountLabel = new JLabel("0", SwingConstants.CENTER)));

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 创建单个统计概览卡片
     * 每张卡片为一个带边框和背景色的 JPanel，内部使用 BorderLayout，
     * 上方为标题标签（灰色小字），下方为数值标签（蓝色大字体）。
     *
     * @param title     卡片标题，如"今日营业额"
     * @param valueLabel 显示数值的 JLabel，由外部传入以支持动态更新
     * @return 配置好的卡片面板
     */
    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(250, 250, 250));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(120, 120, 120));

        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        valueLabel.setForeground(new Color(51, 122, 183));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * 初始化中间区域的分割面板
     * 使用垂直方向的 JSplitPane，上半部分为用户消费排行表格，
     * 下半部分为月度营收统计表格。
     */
    private void initCenterPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        splitPane.setTopComponent(createRankingPanel());
        splitPane.setBottomComponent(createMonthlyStatsPanel());

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * 创建用户消费排行面板
     * 包含标题标签、排行表格和导出按钮。
     * 排行表格列：排名、用户名、消费总额、消费次数。
     *
     * @return 用户消费排行面板
     */
    private JPanel createRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel titleLabel = new JLabel("用户消费排行");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JButton exportRankingButton = new JButton("导出Excel");
        exportRankingButton.addActionListener(e -> exportTableToExcel(rankingTable, "用户消费排行"));
        titlePanel.add(exportRankingButton, BorderLayout.EAST);

        panel.add(titlePanel, BorderLayout.NORTH);

        String[] rankingColumns = {"排名", "用户名", "消费总额", "消费次数"};
        rankingModel = new DefaultTableModel(rankingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rankingTable = new JTable(rankingModel);
        rankingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        rankingTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < rankingTable.getColumnCount(); i++) {
            rankingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(rankingTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建月度营收统计面板
     * 包含标题标签、年度选择下拉框和统计表格。
     * 统计表格列：月份、营收金额。
     *
     * @return 月度营收统计面板
     */
    private JPanel createMonthlyStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel titleLabel = new JLabel("月度营收统计");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JPanel yearPanel = new JPanel();
        yearPanel.add(new JLabel("选择年份："));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearComboBox = new JComboBox<>();
        for (int y = currentYear - 5; y <= currentYear; y++) {
            yearComboBox.addItem(y);
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.addActionListener(e -> {
            Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
            if (selectedYear != null) {
                loadMonthlyStats(selectedYear);
            }
        });
        yearPanel.add(yearComboBox);

        titlePanel.add(yearPanel, BorderLayout.EAST);
        panel.add(titlePanel, BorderLayout.NORTH);

        String[] statsColumns = {"月份", "营收金额"};
        statsModel = new DefaultTableModel(statsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        statsTable = new JTable(statsModel);
        statsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        statsTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer statsCenterRenderer = new DefaultTableCellRenderer();
        statsCenterRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < statsTable.getColumnCount(); i++) {
            statsTable.getColumnModel().getColumn(i).setCellRenderer(statsCenterRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(statsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 初始化底部操作按钮栏
     * 包含"刷新数据"按钮和"导出Excel"按钮。
     * "刷新数据"用于重新加载所有统计数据，
     * "导出Excel"用于将当前选中的表格导出为 Excel 文件。
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton refreshButton = new JButton("刷新数据");
        refreshButton.addActionListener(e -> refreshAll());
        bottomPanel.add(refreshButton);

        JButton exportButton = new JButton("导出Excel");
        exportButton.addActionListener(e -> {
            JTable activeTable = getActiveTable();
            if (activeTable != null) {
                exportTableToExcel(activeTable, "营收报表");
            }
        });
        bottomPanel.add(exportButton);

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
     * 加载概览统计数据
     * 从 ReportService 获取今日/本月营业额、充值金额和上机次数，
     * 更新顶部五个统计卡片中的数值标签。
     */
    private void loadSummary() {
        BigDecimal todayRevenue = reportService.getTodayRevenue();
        BigDecimal monthRevenue = reportService.getMonthRevenue();
        BigDecimal todayRecharge = reportService.getTodayRecharge();
        BigDecimal monthRecharge = reportService.getMonthRecharge();
        int todayOnlineCount = reportService.getTodayOnlineCount();

        todayRevenueLabel.setText(todayRevenue != null ? todayRevenue.toString() : "0.00");
        monthRevenueLabel.setText(monthRevenue != null ? monthRevenue.toString() : "0.00");
        todayRechargeLabel.setText(todayRecharge != null ? todayRecharge.toString() : "0.00");
        monthRechargeLabel.setText(monthRecharge != null ? monthRecharge.toString() : "0.00");
        todayOnlineCountLabel.setText(String.valueOf(todayOnlineCount));
    }

    /**
     * 加载用户消费排行数据
     * 从 ReportService 获取用户消费排行榜列表，
     * 按消费总额降序将数据填充到排行表格中。
     * 排名从 1 开始递增，数据来源于 getUserConsumeRanking() 返回的 Map 列表。
     */
    private void loadRanking() {
        rankingModel.setRowCount(0);

        List<Map<String, Object>> rankingList = reportService.getUserConsumeRanking();
        if (rankingList == null || rankingList.isEmpty()) {
            return;
        }

        int rank = 1;
        for (Map<String, Object> item : rankingList) {
            String username = (String) item.get("username");
            BigDecimal totalAmount = (BigDecimal) item.get("totalAmount");
            Object consumeCountObj = item.get("consumeCount");
            String consumeCount = consumeCountObj != null ? String.valueOf(consumeCountObj) : "0";

            Object[] rowData = {
                    rank++,
                    username != null ? username : "",
                    totalAmount != null ? totalAmount.toString() : "0.00",
                    consumeCount
            };
            rankingModel.addRow(rowData);
        }
    }

    /**
     * 加载指定年份的月度营收统计数据
     * 从 ReportService 获取指定年份 1 月至 12 月的营收数据，
     * 将数据填充到月度统计表格中。
     *
     * @param year 要统计的年份，例如 2025
     */
    private void loadMonthlyStats(int year) {
        statsModel.setRowCount(0);

        List<Map<String, Object>> monthlyList = reportService.getMonthlyStats(year);
        if (monthlyList == null || monthlyList.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : monthlyList) {
            Object monthObj = item.get("month");
            String monthStr = monthObj != null ? monthObj + "月" : "";
            BigDecimal amount = (BigDecimal) item.get("amount");

            Object[] rowData = {
                    monthStr,
                    amount != null ? amount.toString() : "0.00"
            };
            statsModel.addRow(rowData);
        }
    }

    /**
     * 刷新所有统计数据
     * 同时重新加载概览统计、消费排行和当前选中年份的月度营收统计。
     * 这是面板的核心刷新方法，外部调用此方法即可更新所有数据展示。
     */
    private void refreshAll() {
        loadSummary();
        loadRanking();

        Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
        if (selectedYear != null) {
            loadMonthlyStats(selectedYear);
        }
    }

    /**
     * 获取当前活跃的表格（有数据的表格）
     * 优先返回排行表格（如果有数据），否则返回统计表格。
     * 用于"导出Excel"按钮判断应该导出哪个表格。
     *
     * @return 当前有数据的 JTable，如果两个表格都没有数据则返回排行表格
     */
    private JTable getActiveTable() {
        if (rankingModel.getRowCount() > 0) {
            return rankingTable;
        }
        return statsTable;
    }

    /**
     * 将指定的 JTable 数据导出为 Excel 文件
     * 弹出文件保存对话框让用户选择保存路径和文件名，
     * 然后调用 ExportUtil.exportTableToExcel() 完成导出。
     *
     * @param table    要导出的 JTable
     * @param baseName 导出文件的默认基础名称
     */
    private void exportTableToExcel(JTable table, String baseName) {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "当前表格没有数据，无法导出！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出Excel文件");
        fileChooser.setSelectedFile(new java.io.File(baseName + ".xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }
            ExportUtil.exportTableToExcel(table, filePath);
        }
    }
}