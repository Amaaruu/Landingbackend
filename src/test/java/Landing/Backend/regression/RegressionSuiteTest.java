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
@SelectPackages("Landing.Backend")
@IncludeTags("regression")
public class RegressionSuiteTest {
}