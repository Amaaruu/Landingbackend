package Landing.Backend.regression;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Suite de regresión basada en Etiquetas (@Tag).
 * Escanea todo el proyecto buscando pruebas críticas.
 */
@Suite
@SuiteDisplayName("Suite de Regresión — WLSuite Backend")
@SelectPackages("Landing.Backend") // Escanea desde la raíz de tu proyecto
@IncludeTags("regression")         // Solo atrapa lo que tenga esta etiqueta
public class RegressionSuiteTest {
    // Clase vacía de configuración
}