/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import com.google.gson.Gson;
import com.mycompany.moon.DBManager;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author edithson
 */
class ExportedNote {
    int id;
    String titre;
    String contenu;
    String created_at;
    String updated_at;
    
    // Constructeur Map -> Objet pour simplifier la conversion
    public ExportedNote(Map<String, Object> data) {
        // Utiliser Integer/String directement si l'objet DBManager retourne ces types
        this.id = (Integer) data.get("id");
        this.titre = (String) data.get("titre");
        this.contenu = (String) data.get("contenu");
        this.created_at = (String) data.get("created_at");
        this.updated_at = (String) data.get("updated_at");
    }
}

class ExportedCategory {
    int id;
    String nom;
    List<ExportedNote> notes = new ArrayList<>();
    
    // Constructeur Map -> Objet
    public ExportedCategory(Map<String, Object> data) {
        this.id = (Integer) data.get("id");
        this.nom = (String) data.get("nom");
    }
}
// ------------------------------------------------------------------------

public class ExportManager {

    /**
     * Détermine le chemin sécurisé pour l'exportation (compatible OS).
     * Crée un dossier 'MoonExports' dans le répertoire utilisateur (~/MoonExports).
     */
    private static Path getExportPath() throws IOException {
        String homeDir = System.getProperty("user.home");
        // Crée un répertoire "MoonExports" dans le répertoire utilisateur
        Path exportDir = Paths.get(homeDir, "MoonExports");
        
        // Crée le dossier s'il n'existe pas
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        
        // Nom du fichier avec timestamp pour garantir un nom unique
        String fileName = "moon_export_" + System.currentTimeMillis() + ".json";
        return exportDir.resolve(fileName);
    }
    
    /**
     * Exporte la base de données complète vers un fichier JSON.
     * @return Un message indiquant le succès ou l'échec et le chemin du fichier.
     */
    public static String exportDatabase() {
        Gson gson = new Gson();
        // C'est la liste racine qui sera sérialisée en JSON
        List<ExportedCategory> allCategoriesWithNotes = new ArrayList<>();
        
        try {
            // 1. LIRE TOUTES LES CATÉGORIES
            String sqlCats = "SELECT id, nom FROM categories ORDER BY id ASC";
            List<Map<String, Object>> categoryMaps = DBManager.read(sqlCats); // Utilise la méthode read sans paramètre du DBManager

            // 2. LIRE LES NOTES POUR CHAQUE CATÉGORIE ET CONSTRUIRE LA STRUCTURE
            for (Map<String, Object> categoryMap : categoryMaps) {
                ExportedCategory exportedCat = new ExportedCategory(categoryMap);
                int catId = exportedCat.id;
                
                // Requête pour lire uniquement les notes non supprimées d'une catégorie spécifique
                String sqlNotes = "SELECT id, titre, contenu, created_at, updated_at "
                                + "FROM notes WHERE categorie_id = ? AND deleted_at IS NULL "
                                + "ORDER BY created_at DESC";
                
                String[] values = {String.valueOf(catId)};
                
                // Récupération des notes pour la catégorie courante
                List<Map<String, Object>> noteMaps = DBManager.read(sqlNotes, values); // Utilise la méthode read avec paramètres du DBManager

                // Convertir les Map de notes en objets ExportedNote
                for (Map<String, Object> noteMap : noteMaps) {
                    exportedCat.notes.add(new ExportedNote(noteMap));
                }
                
                allCategoriesWithNotes.add(exportedCat);
            }
            
            // 3. SÉRIALISER EN JSON
            String jsonOutput = gson.toJson(allCategoriesWithNotes);
            
            // 4. ÉCRIRE DANS LE FICHIER
            Path filePath = getExportPath();
            
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(jsonOutput);
            }

            return "Exportation réussie ! Fichier enregistré ici : " + filePath.toString();
            
        } catch (Exception e) {
            System.err.println("Erreur fatale lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
            return "Échec de l'exportation : " + e.getMessage();
        }
    }
}