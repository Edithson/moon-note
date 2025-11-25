/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

/**
 *
 * @author edithson
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mycompany.moon.model.CategoryDAO;
import com.mycompany.moon.model.NoteDAO;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère l'importation des notes et catégories à partir d'un fichier JSON.
 */
public class ImportManager {
    
    // --- Classes internes pour la désérialisation ---
    
    // Note : Gson utilise ces classes pour mapper le JSON. Les champs doivent correspondre au JSON exporté.
    
    // Structure d'une note dans le fichier JSON
    /**
     * Importe les notes et catégories depuis un fichier JSON.
     * @param filePath Le chemin complet du fichier JSON à importer.
     * @return Un rapport des opérations effectuées.
     */
    public static String importDatabase(String filePath) {
        Gson gson = new Gson();
        int importedCategories = 0;
        int restoredCategories = 0;
        int skippedCategories = 0;
        int importedNotes = 0;
        int restoredNotes = 0;
        int skippedNotes = 0;
        
        // Map pour lier l'ID unique (TEXT) de la catégorie exportée à son ID final dans la DB locale.
        Map<String, String> exportedIdToLocalIdMap = new HashMap<>(); 
        
        CategoryDAO catDAO = new CategoryDAO();

        try (FileReader reader = new FileReader(filePath)) {
            
            // 1. DÉSÉRIALISATION du JSON
            Type listType = new TypeToken<List<ExportedCategory>>() {}.getType();
            List<ExportedCategory> importedData = gson.fromJson(reader, listType);

            if (importedData == null || importedData.isEmpty()) {
                return "Importation annulée : Le fichier JSON est vide ou mal formaté.";
            }

            // 2. IMPORTATION DES CATÉGORIES
            for (ExportedCategory exportedCat : importedData) {
                String finalCatId = exportedCat.id;
                
                // Tentative 1: Vérification du doublon de NOM (Active uniquement)
                String existingLocalIdByName = catDAO.getIdByName(exportedCat.nom); 
                
                if (existingLocalIdByName != null) {
                    // Catégorie ACTVE trouvée par nom -> Ignorer l'insertion, utiliser l'ID local existant.
                    finalCatId = existingLocalIdByName;
                    skippedCategories++;
                } else {
                    // Nom unique ou Catégorie soft-deleted -> Tenter l'UPSERT par ID.
                    String resultId = catDAO.upsertForImport(exportedCat.id, exportedCat.nom);
                    
                    if (resultId != null) {
                        finalCatId = resultId;
                        
                        // Déterminer le statut pour le rapport
                        Map<String, Object> finalCatData = catDAO.getCategoryById(finalCatId);
                        
                        if (finalCatData != null) {
                            String deletedAt = (String) finalCatData.get("deleted_at");
                            // Si l'ID local existe et était supprimé (deleted_at est maintenant NULL)
                            if (finalCatId.equals(exportedCat.id) && deletedAt == null && catDAO.getCategoryById(exportedCat.id) != null) {
                                // Vérification plus robuste pour savoir si c'était une restauration
                                if (catDAO.getCategoryById(exportedCat.id).containsKey("deleted_at") && catDAO.getCategoryById(exportedCat.id).get("deleted_at") == null) {
                                    restoredCategories++;
                                } else {
                                    importedCategories++;
                                }
                            } else {
                                // L'ID existait déjà, était actif ou n'a pas pu être restauré/inséré
                                skippedCategories++;
                            }
                        } else {
                            skippedCategories++;
                        }
                    } else {
                        // Erreur fatale lors de l'upsert
                        skippedCategories++;
                    }
                }
                
                // On mappe l'ID unique exporté à l'ID local final (même s'il est ignoré, pour lier les notes)
                exportedIdToLocalIdMap.put(exportedCat.id, finalCatId);
            }

            // 3. IMPORTATION DES NOTES
            for (ExportedCategory exportedCat : importedData) {
                String localCatId = exportedIdToLocalIdMap.get(exportedCat.id);
                
                if (localCatId == null) {
                     continue; // Catégorie non trouvée/traitée
                }
                
                for (ExportedNote exportedNote : exportedCat.notes) {
                    
                    // Utilisation de la méthode upsertNoteForImport
                    int status = NoteDAO.upsertNoteForImport(
                        exportedNote.id,
                        localCatId, 
                        exportedNote.titre,
                        exportedNote.contenu,
                        exportedNote.created_at,
                        exportedNote.updated_at
                    );
                    
                    if (status == 1) {
                        importedNotes++; // Insérée
                    } else if (status == 2) {
                        restoredNotes++; // Restaurée
                    } else if (status == 0) {
                        skippedNotes++; // Doublon actif
                    } 
                    // else : Erreur fatale
                }
            }
            
            // 4. RAPPORT FINAL
            return String.format(
                "Importation terminée avec succès !\n\n" +
                "Catégories :\n" +
                "  - %d importées,\n" +
                "  - %d restaurées (étaient supprimées ou ID déjà présent),\n" +
                "  - %d doublons ignorés (catégorie active déjà présente par nom).\n\n" +
                "Notes :\n" +
                "  - %d importées,\n" +
                "  - %d restaurées (étaient supprimées),\n" +
                "  - %d doublons ignorées (note active déjà présente par ID unique).",
                importedCategories, restoredCategories, skippedCategories, 
                importedNotes, restoredNotes, skippedNotes
            );

        } catch (IOException e) {
            return "Échec de l'importation : Erreur de lecture du fichier à l'emplacement " + filePath + ".\n" + e.getMessage();
        } catch (Exception e) {
            System.err.println("Erreur fatale lors de l'importation : " + e.getLocalizedMessage());
            e.printStackTrace();
            return "Échec de l'importation : Une erreur inattendue est survenue.\n" + e.getLocalizedMessage();
        }
    }
}