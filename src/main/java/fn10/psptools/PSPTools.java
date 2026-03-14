/*
    PSPTools - Management Utility for your PSP.
    Copyright (C) 2026 xFN10x (https://github.com/xFN10x)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package fn10.psptools;

import com.formdev.flatlaf.FlatDarculaLaf;
import fn10.psptools.psp.PSP;
import fn10.psptools.ui.LaunchPage;
import fn10.psptools.util.SavedVariables;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class PSPTools {

    public static final String VERSION = "1.1.1";
    private static final Logger log = LogManager.getLogger(PSPTools.class);

    static void main(String[] args) {

        PSPTools.log.info("*********");
        if (args != null && args.length > 0)
            PSPTools.log.info("Args: {}", String.join(", ", args));
        PSPTools.log.info("PSPTools Version {}, Running on {} ({}), {} with JDK {}",
                VERSION,
                SystemUtils.OS_NAME,
                SystemUtils.OS_ARCH,
                SystemUtils.OS_VERSION,
                System.getProperty("java.vm.name"));
        PSPTools.log.info("*********");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e) {
            log.error("Failed to load theme", e);
        }

        // #region read settings
        SavedVariables saved = SavedVariables.Load();
        PSP LastPSP = PSP.getPSPFromLastSelectedInfo(saved.LastSelectedPSP);

        if (LastPSP != null && LastPSP.pspActive()) {
            PSP.setCurrentPSP(LastPSP, false);
        }
        // #endregion

        LaunchPage.showLaunchPage();
    }
}
