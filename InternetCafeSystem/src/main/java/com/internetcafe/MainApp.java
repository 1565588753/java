package com.internetcafe;

import com.internetcafe.ui.LoginFrame;
import com.internetcafe.util.DBUtil;

import javax.swing.*;

/**
 * 网吧计费管理系统 - 主启动类
 *
 * 系统入口，负责初始化数据库连接池并启动登录界面
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class MainApp {

    /**
     * 程序主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置Swing外观为系统原生样式
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("设置系统外观失败，使用默认外观: " + e.getMessage());
        }

        // 初始化数据库连接池
        System.out.println("正在初始化数据库连接池...");
        if (!DBUtil.isInitialized()) {
            System.err.println("数据库连接池初始化失败！请检查数据库配置。");
            JOptionPane.showMessageDialog(null,
                    "数据库连接失败！\n请检查MySQL服务是否启动，以及application.properties配置是否正确。",
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 测试数据库连接
        System.out.println("正在测试数据库连接...");
        if (!DBUtil.testConnection()) {
            System.err.println("数据库连接测试失败！");
            JOptionPane.showMessageDialog(null,
                    "数据库连接测试失败！\n请确保数据库 internet_cafe_db 已创建并导入了初始化SQL脚本。",
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        System.out.println("数据库连接成功！");

        // 在事件分发线程中启动登录界面
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                System.out.println("网吧计费管理系统启动成功！");
            }
        });

        // 注册JVM关闭钩子，确保程序退出时释放数据库连接池资源
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("系统正在关闭，释放数据库连接池资源...");
                DBUtil.shutdown();
                System.out.println("系统已安全退出。");
            }
        }));
    }
}