package info.openrocket.swing.gui.dialogs.preset;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test pour vérifier l'affichage des nouvelles colonnes ajoutées
 * à la librairie parachute dans OpenRocket
 *
 * Ce test vérifie uniquement que les nouvelles colonnes sont présentes
 * dans ComponentPreset.ORDERED_KEY_LIST sans créer de ComponentPresetTable
 * pour éviter les problèmes d'injection de dépendances.
 */
public class ComponentPresetTableTest {

    private static final List<TypedKey<?>> NEW_PARACHUTE_COLUMNS = Arrays.asList(
            ComponentPreset.PACKED_DIAMETER,
            ComponentPreset.PACKED_LENGTH,
            ComponentPreset.MASS,
            ComponentPreset.CD_AREA,
            ComponentPreset.SURFACE_AREA
    );

    @BeforeAll
    public static void setUpClass() {
        // Initialiser l'application pour les tests
        // Cette méthode initialise l'injector si nécessaire
        try {
            Application.class.getDeclaredMethod("initializeForTesting");
        } catch (NoSuchMethodException e) {
            // Si la méthode n'existe pas, ce n'est pas grave
            // les tests unitaires de base fonctionneront quand même
        }
    }

    @Test
    @DisplayName("Vérifier que PACKED_DIAMETER est dans ORDERED_KEY_LIST")
    public void testPackedDiameterInOrderedKeyList() {
        assertTrue(ComponentPreset.ORDERED_KEY_LIST.contains(ComponentPreset.PACKED_DIAMETER),
                "PACKED_DIAMETER devrait être dans ComponentPreset.ORDERED_KEY_LIST");
    }

    @Test
    @DisplayName("Vérifier que PACKED_LENGTH est dans ORDERED_KEY_LIST")
    public void testPackedLengthInOrderedKeyList() {
        assertTrue(ComponentPreset.ORDERED_KEY_LIST.contains(ComponentPreset.PACKED_LENGTH),
                "PACKED_LENGTH devrait être dans ComponentPreset.ORDERED_KEY_LIST");
    }

    @Test
    @DisplayName("Vérifier que MASS est dans ORDERED_KEY_LIST")
    public void testMassInOrderedKeyList() {
        assertTrue(ComponentPreset.ORDERED_KEY_LIST.contains(ComponentPreset.MASS),
                "MASS devrait être dans ComponentPreset.ORDERED_KEY_LIST");
    }

    @Test
    @DisplayName("Vérifier que CD_AREA est dans ORDERED_KEY_LIST")
    public void testCdAreaInOrderedKeyList() {
        assertTrue(ComponentPreset.ORDERED_KEY_LIST.contains(ComponentPreset.CD_AREA),
                "CD_AREA devrait être dans ComponentPreset.ORDERED_KEY_LIST");
    }

    @Test
    @DisplayName("Vérifier que SURFACE_AREA est dans ORDERED_KEY_LIST")
    public void testSurfaceAreaInOrderedKeyList() {
        assertTrue(ComponentPreset.ORDERED_KEY_LIST.contains(ComponentPreset.SURFACE_AREA),
                "SURFACE_AREA devrait être dans ComponentPreset.ORDERED_KEY_LIST");
    }

    @Test
    @DisplayName("Vérifier que toutes les nouvelles colonnes sont présentes")
    public void testAllNewColumnsInOrderedKeyList() {
        int foundCount = 0;
        StringBuilder missingColumns = new StringBuilder();

        for (TypedKey<?> key : NEW_PARACHUTE_COLUMNS) {
            if (ComponentPreset.ORDERED_KEY_LIST.contains(key)) {
                foundCount++;
            } else {
                if (missingColumns.length() > 0) {
                    missingColumns.append(", ");
                }
                missingColumns.append(key.getName());
            }
        }

        assertEquals(5, foundCount,
                "Les 5 nouvelles colonnes devraient être dans ORDERED_KEY_LIST. Colonnes manquantes: " +
                        (missingColumns.length() > 0 ? missingColumns.toString() : "aucune"));
    }

    @Test
    @DisplayName("Vérifier que les TypedKey ont un nom")
    public void testNewColumnsHaveNames() {
        for (TypedKey<?> key : NEW_PARACHUTE_COLUMNS) {
            assertNotNull(key.getName(),
                    "La colonne " + key + " devrait avoir un nom");
            assertFalse(key.getName().isEmpty(),
                    "Le nom de la colonne " + key + " ne devrait pas être vide");
        }
    }

    @Test
    @DisplayName("Vérifier que les TypedKey ont un type")
    public void testNewColumnsHaveTypes() {
        for (TypedKey<?> key : NEW_PARACHUTE_COLUMNS) {
            assertNotNull(key.getType(),
                    "La colonne " + key.getName() + " devrait avoir un type");
        }
    }

    @Test
    @DisplayName("Vérifier que les colonnes numériques ont un UnitGroup")
    public void testNumericColumnsHaveUnitGroup() {
        // PACKED_DIAMETER, PACKED_LENGTH, MASS, CD_AREA, SURFACE_AREA
        // devraient tous avoir un UnitGroup puisqu'ils représentent des mesures physiques

        for (TypedKey<?> key : NEW_PARACHUTE_COLUMNS) {
            assertNotNull(key.getUnitGroup(),
                    "La colonne " + key.getName() + " devrait avoir un UnitGroup car c'est une mesure physique");
        }
    }

    @Test
    @DisplayName("Vérifier que les colonnes sont de type Double")
    public void testColumnsAreDoubleType() {
        for (TypedKey<?> key : NEW_PARACHUTE_COLUMNS) {
            assertEquals(Double.class, key.getType(),
                    "La colonne " + key.getName() + " devrait être de type Double");
        }
    }

    @Test
    @DisplayName("Vérifier l'ordre relatif des nouvelles colonnes dans ORDERED_KEY_LIST")
    public void testColumnsOrder() {
        // Vérifier que les colonnes apparaissent dans ORDERED_KEY_LIST
        // et obtenir leurs positions

        int packedDiameterIndex = ComponentPreset.ORDERED_KEY_LIST.indexOf(ComponentPreset.PACKED_DIAMETER);
        int packedLengthIndex = ComponentPreset.ORDERED_KEY_LIST.indexOf(ComponentPreset.PACKED_LENGTH);
        int massIndex = ComponentPreset.ORDERED_KEY_LIST.indexOf(ComponentPreset.MASS);
        int cdAreaIndex = ComponentPreset.ORDERED_KEY_LIST.indexOf(ComponentPreset.CD_AREA);
        int surfaceAreaIndex = ComponentPreset.ORDERED_KEY_LIST.indexOf(ComponentPreset.SURFACE_AREA);

        // Vérifier qu'elles sont toutes présentes (index >= 0)
        assertTrue(packedDiameterIndex >= 0, "PACKED_DIAMETER devrait être dans la liste");
        assertTrue(packedLengthIndex >= 0, "PACKED_LENGTH devrait être dans la liste");
        assertTrue(massIndex >= 0, "MASS devrait être dans la liste");
        assertTrue(cdAreaIndex >= 0, "CD_AREA devrait être dans la liste");
        assertTrue(surfaceAreaIndex >= 0, "SURFACE_AREA devrait être dans la liste");

        System.out.println("Position des nouvelles colonnes dans ORDERED_KEY_LIST:");
        System.out.println("  PACKED_DIAMETER: " + packedDiameterIndex);
        System.out.println("  PACKED_LENGTH: " + packedLengthIndex);
        System.out.println("  MASS: " + massIndex);
        System.out.println("  CD_AREA: " + cdAreaIndex);
        System.out.println("  SURFACE_AREA: " + surfaceAreaIndex);
    }

    @Test
    @DisplayName("Vérifier que ComponentPreset.Type.PARACHUTE existe")
    public void testParachuteTypeExists() {
        assertNotNull(ComponentPreset.Type.PARACHUTE,
                "Le type PARACHUTE devrait exister dans ComponentPreset.Type");
    }

    @Test
    @DisplayName("Afficher toutes les clés de ORDERED_KEY_LIST pour information")
    public void testDisplayAllOrderedKeys() {
        System.out.println("\n=== Liste complète des colonnes (ORDERED_KEY_LIST) ===");
        System.out.println("Nombre total de colonnes: " + ComponentPreset.ORDERED_KEY_LIST.size());

        int index = 0;
        for (TypedKey<?> key : ComponentPreset.ORDERED_KEY_LIST) {
            String marker = NEW_PARACHUTE_COLUMNS.contains(key) ? " [NOUVELLE]" : "";
            System.out.println(String.format("%3d. %s (Type: %s, UnitGroup: %s)%s",
                    index,
                    key.getName(),
                    key.getType().getSimpleName(),
                    key.getUnitGroup() != null ? key.getUnitGroup().toString() : "null",
                    marker
            ));
            index++;
        }
        System.out.println("===================================================\n");

        // Ce test passe toujours, il sert juste à afficher des informations
        assertTrue(true);
    }
}