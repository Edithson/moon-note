/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

/**
 *
 * @author edithson
 */

import java.awt.*;
import java.io.IOException;
import java.util.Locale;

public class NotificationManager {

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);

    /**
     * Envoie une notification selon l'OS
     * @param title
     * @param message
     */
    public static void notify(String title, String message) {
        try {
            if (isWindows()) {
                sendWindowsNotification(title, message);
            } else if (isLinux()) {
                sendLinuxNotification(title, message);
            } else if (isMac()) {
                sendMacNotification(title, message);
            } else {
                sendSwingFallback(title, message);
            }
        } catch (Exception e) {
            // fallback si erreur
            sendSwingFallback(title, message);
        }
    }

    // -------------------------
    // OS DETECTION
    // -------------------------

    private static boolean isWindows() {
        return OS.contains("win");
    }

    private static boolean isLinux() {
        return OS.contains("linux");
    }

    private static boolean isMac() {
        return OS.contains("mac");
    }

    // -------------------------
    // WINDOWS
    // -------------------------

    private static void sendWindowsNotification(String title, String message) throws IOException {
        // Appelle PowerShell -> Windows Toast
        String command = "powershell.exe -Command \""
                + "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] > $null;"
                + "$template = [Windows.UI.Notifications.ToastTemplateType]::ToastText02;"
                + "$xml = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent($template);"
                + "$textNodes = $xml.GetElementsByTagName('text');"
                + "$textNodes.Item(0).AppendChild($xml.CreateTextNode('" + title + "')) > $null;"
                + "$textNodes.Item(1).AppendChild($xml.CreateTextNode('" + message + "')) > $null;"
                + "$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);"
                + "$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('JavaApp');"
                + "$notifier.Show($toast);\"";

        Runtime.getRuntime().exec(command);
    }

    // -------------------------
    // LINUX
    // -------------------------

    private static void sendLinuxNotification(String title, String message) {
        try {
            Runtime.getRuntime().exec(new String[]{
                    "notify-send", title, message
            });
        } catch (Exception e) {
            sendSwingFallback(title, message);
        }
    }

    // -------------------------
    // MACOS
    // -------------------------

    private static void sendMacNotification(String title, String message) {
        try {
            Runtime.getRuntime().exec(new String[]{
                    "osascript",
                    "-e",
                    "display notification \"" + message + "\" with title \"" + title + "\""
            });
        } catch (Exception e) {
            sendSwingFallback(title, message);
        }
    }

    // -------------------------
    // FALLBACK SWING
    // -------------------------

    private static void sendSwingFallback(String title, String message) {
        if (!SystemTray.isSupported()) {
            // si même le tray ne marche pas → dialog swing
            javax.swing.JOptionPane.showMessageDialog(null, message, title, javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("/icons/app_icon.png");

            TrayIcon trayIcon = new TrayIcon(image, "Java Notification");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

            // on retire pour nettoyer
            tray.remove(trayIcon);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, message, title, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
