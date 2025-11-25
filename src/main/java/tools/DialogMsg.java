/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import javax.swing.JOptionPane;

/**
 *
 * @author edithson
 */
public class DialogMsg {
    
    public static void successMsg(String msg){
        JOptionPane.showMessageDialog(null, msg, "Monn", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void errorMsg(String msg){
        JOptionPane.showMessageDialog(null, msg, "Monn", JOptionPane.ERROR_MESSAGE);
    }
    
}
