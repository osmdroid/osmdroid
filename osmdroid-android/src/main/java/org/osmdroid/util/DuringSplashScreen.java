package org.osmdroid.util;

import org.osmdroid.tileprovider.modules.SqlTileWriter;

/**
 * Put there everything that could be done during a splash screen
 * @since 6.0.2
 * @author Fabrice Fontaine
 */
public class DuringSplashScreen implements SplashScreenable{

    @Override
    public void runDuringSplashScreen() {
        final SqlTileWriter sqlTileWriter = new SqlTileWriter();
        sqlTileWriter.runDuringSplashScreen();
    }
}
