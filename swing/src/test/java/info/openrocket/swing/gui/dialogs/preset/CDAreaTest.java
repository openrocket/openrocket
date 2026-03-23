package info.openrocket.swing.gui.dialogs.preset;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.unit.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour la colonne CD_AREA dans ComponentPresetTable
 * Vérifie le calcul et l'affichage du CD_AREA (coefficient de traînée × surface)
 */
@DisplayName("Tests de la colonne CD_AREA")
public class CDAreaTest {

    private ComponentPresetTableColumn.DoubleWithUnit cdAreaColumn;
    private Set<String> emptyFavorites;

    @BeforeEach
    public void setUp() {
        cdAreaColumn = new ComponentPresetTableColumn.DoubleWithUnit(ComponentPreset.CD_AREA, 0);
        emptyFavorites = new HashSet<>();
    }

    @Nested
    @DisplayName("Calcul du CD_AREA")
    class CalculationTests {

        @Test
        @DisplayName("Calculer CD_AREA à partir de SURFACE_AREA et CD")
        public void testCalculateCDArea_fromSurfaceAreaAndCd() {
            // Arrange
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(2.0);
            when(preset.get(ComponentPreset.CD)).thenReturn(0.5);

            // Act
            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            // Assert
            assertNotNull(result, "Le résultat ne devrait pas être null");
            assertTrue(result instanceof Value, "Le résultat devrait être de type Value");

            Value value = (Value) result;
            assertEquals(1.0, value.getValue(), 1e-9,
                    "CD_AREA devrait être égal à SURFACE_AREA × CD (2.0 × 0.5 = 1.0)");
        }

        @Test
        @DisplayName("Calculer CD_AREA avec des valeurs décimales")
        public void testCalculateCDArea_withDecimalValues() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(3.14159);
            when(preset.get(ComponentPreset.CD)).thenReturn(0.75);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNotNull(result);
            Value value = (Value) result;
            assertEquals(3.14159 * 0.75, value.getValue(), 1e-9,
                    "CD_AREA devrait être calculé correctement avec des décimales");
        }

        @Test
        @DisplayName("Calculer CD_AREA avec CD = 1.0")
        public void testCalculateCDArea_withCdEqualsOne() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(5.0);
            when(preset.get(ComponentPreset.CD)).thenReturn(1.0);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            Value value = (Value) result;
            assertEquals(5.0, value.getValue(), 1e-9,
                    "Quand CD = 1.0, CD_AREA devrait égaler SURFACE_AREA");
        }

        @Test
        @DisplayName("Calculer CD_AREA avec CD = 0.0")
        public void testCalculateCDArea_withCdEqualsZero() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(5.0);
            when(preset.get(ComponentPreset.CD)).thenReturn(0.0);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            Value value = (Value) result;
            assertEquals(0.0, value.getValue(), 1e-9,
                    "Quand CD = 0.0, CD_AREA devrait être 0.0");
        }
    }

    @Nested
    @DisplayName("Valeur directe de CD_AREA")
    class DirectValueTests {

        @Test
        @DisplayName("Utiliser CD_AREA directement quand il est disponible")
        public void testUseCDAreaDirectly_whenAvailable() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(true);
            when(preset.get(ComponentPreset.CD_AREA)).thenReturn(1.5);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNotNull(result);
            Value value = (Value) result;
            assertEquals(1.5, value.getValue(), 1e-9,
                    "CD_AREA devrait utiliser la valeur directe quand elle est disponible");

            // Vérifier que SURFACE_AREA et CD n'ont pas été appelés
            verify(preset, never()).get(ComponentPreset.SURFACE_AREA);
            verify(preset, never()).get(ComponentPreset.CD);
        }

        @Test
        @DisplayName("Préférer CD_AREA direct même si SURFACE_AREA et CD sont disponibles")
        public void testPreferDirectCDArea_overCalculation() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.CD_AREA)).thenReturn(2.5);
            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(5.0);
            when(preset.get(ComponentPreset.CD)).thenReturn(0.5);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            Value value = (Value) result;
            assertEquals(2.5, value.getValue(), 1e-9,
                    "Devrait utiliser la valeur directe de CD_AREA (2.5) et non le calcul (5.0 × 0.5 = 2.5)");
        }
    }

    @Nested
    @DisplayName("Cas limites et erreurs")
    class EdgeCasesTests {

        @Test
        @DisplayName("Retourner null quand CD_AREA n'est pas disponible et ne peut pas être calculé")
        public void testReturnNull_whenNoCDAreaAndCannotCalculate() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.CD)).thenReturn(false);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNull(result,
                    "Devrait retourner null quand CD_AREA n'est pas disponible et ne peut pas être calculé");
        }

        @Test
        @DisplayName("Retourner null quand seul SURFACE_AREA est disponible")
        public void testReturnNull_whenOnlySurfaceAreaAvailable() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(false);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(5.0);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNull(result,
                    "Devrait retourner null quand seul SURFACE_AREA est disponible (CD manquant)");
        }

        @Test
        @DisplayName("Retourner null quand seul CD est disponible")
        public void testReturnNull_whenOnlyCdAvailable() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.CD)).thenReturn(0.5);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNull(result,
                    "Devrait retourner null quand seul CD est disponible (SURFACE_AREA manquant)");
        }

        @Test
        @DisplayName("Gérer les valeurs très petites")
        public void testHandleVerySmallValues() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(0.00001);
            when(preset.get(ComponentPreset.CD)).thenReturn(0.0001);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNotNull(result);
            Value value = (Value) result;
            assertEquals(0.00001 * 0.0001, value.getValue(), 1e-15,
                    "Devrait gérer correctement les très petites valeurs");
        }

        @Test
        @DisplayName("Gérer les valeurs très grandes")
        public void testHandleVeryLargeValues() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(10000.0);
            when(preset.get(ComponentPreset.CD)).thenReturn(2.5);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNotNull(result);
            Value value = (Value) result;
            assertEquals(25000.0, value.getValue(), 1e-9,
                    "Devrait gérer correctement les grandes valeurs");
        }
    }

    @Nested
    @DisplayName("Tests de l'unité de mesure")
    class UnitTests {

        @Test
        @DisplayName("Vérifier que la colonne a un UnitGroup")
        public void testColumnHasUnitGroup() {
            assertNotNull(cdAreaColumn.unitGroup,
                    "La colonne CD_AREA devrait avoir un UnitGroup");
        }

        @Test
        @DisplayName("Vérifier que le TypedKey CD_AREA a un UnitGroup")
        public void testTypedKeyHasUnitGroup() {
            assertNotNull(ComponentPreset.CD_AREA.getUnitGroup(),
                    "ComponentPreset.CD_AREA devrait avoir un UnitGroup défini");
        }

        @Test
        @DisplayName("Vérifier que la Value retournée utilise la bonne unité")
        public void testValueHasCorrectUnit() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(true);
            when(preset.get(ComponentPreset.CD_AREA)).thenReturn(1.5);

            Object result = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertNotNull(result);
            Value value = (Value) result;
            assertNotNull(value.getUnit(),
                    "La Value devrait avoir une unité définie");
        }
    }

    @Nested
    @DisplayName("Tests d'intégration")
    class IntegrationTests {

        @Test
        @DisplayName("Vérifier le comportement avec favorites")
        public void testWithFavorites() {
            ComponentPreset preset = mock(ComponentPreset.class);
            Set<String> favorites = Set.of("favorite1", "favorite2");

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(true);
            when(preset.get(ComponentPreset.CD_AREA)).thenReturn(1.0);

            Object result = cdAreaColumn.getValueFromPreset(favorites, preset);

            assertNotNull(result,
                    "Le résultat devrait être le même peu importe le set de favorites");
            Value value = (Value) result;
            assertEquals(1.0, value.getValue(), 1e-9);
        }

        @Test
        @DisplayName("Vérifier que l'index de la colonne est correct")
        public void testColumnIndex() {
            ComponentPresetTableColumn.DoubleWithUnit column =
                    new ComponentPresetTableColumn.DoubleWithUnit(ComponentPreset.CD_AREA, 5);

            assertEquals(5, column.getModelIndex(),
                    "L'index de la colonne devrait être celui passé au constructeur");
        }
    }

    @Nested
    @DisplayName("Tests de validation des données")
    class ValidationTests {

        @Test
        @DisplayName("Le calcul devrait être cohérent")
        public void testCalculationConsistency() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);

            double surfaceArea = 4.0;
            double cd = 0.6;

            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(surfaceArea);
            when(preset.get(ComponentPreset.CD)).thenReturn(cd);

            Object result1 = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);
            Object result2 = cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            assertEquals(((Value)result1).getValue(), ((Value)result2).getValue(), 1e-9,
                    "Deux appels consécutifs avec les mêmes données devraient retourner la même valeur");
        }

        @Test
        @DisplayName("Vérifier les appels aux méthodes du preset")
        public void testPresetMethodCalls() {
            ComponentPreset preset = mock(ComponentPreset.class);

            when(preset.has(ComponentPreset.CD_AREA)).thenReturn(false);
            when(preset.has(ComponentPreset.SURFACE_AREA)).thenReturn(true);
            when(preset.has(ComponentPreset.CD)).thenReturn(true);
            when(preset.get(ComponentPreset.SURFACE_AREA)).thenReturn(2.0);
            when(preset.get(ComponentPreset.CD)).thenReturn(0.5);

            cdAreaColumn.getValueFromPreset(emptyFavorites, preset);

            // Vérifier l'ordre et le nombre d'appels
            verify(preset, times(1)).has(ComponentPreset.CD_AREA);
            verify(preset, times(1)).has(ComponentPreset.SURFACE_AREA);
            verify(preset, times(1)).has(ComponentPreset.CD);
            verify(preset, times(1)).get(ComponentPreset.SURFACE_AREA);
            verify(preset, times(1)).get(ComponentPreset.CD);
        }
    }
}