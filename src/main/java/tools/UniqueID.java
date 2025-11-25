/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

/**
 *
 * @author edithson
 */
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UniqueID {

    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    public static String generateUniqueCode() {

        // Date ultra précise (année-mois-jour-heure-minute-seconde-millisecondes)
        LocalDateTime now = LocalDateTime.now();
        String timeStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        // Génération de 12 caractères aléatoires
        StringBuilder randomPart = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(ALPHANUM.length());
            randomPart.append(ALPHANUM.charAt(index));
        }

        // Concaténation → id unique illisible
        return timeStamp + randomPart.toString();
    }
}
