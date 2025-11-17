/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author edithson
 */
public class TimeConvert {
    
    public static String formatDateTime(String dateTimeString) {
        // Définir le format de sortie
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        try {
            // Convertir la chaîne en LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
            // Retourner la date formatée
            return dateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return "Format de date invalide";
        }
    }
    
}
