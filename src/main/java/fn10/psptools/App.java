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

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.lang3.SystemUtils;

import com.formdev.flatlaf.FlatDarculaLaf;

import fn10.psptools.psp.PSP;
import fn10.psptools.ui.LaunchPage;
import fn10.psptools.util.SavedVariables;

public class App {

    public static final String VERSION = "1.1.0";

    static void main(String[] args) {
        if (args != null)
            if (args.length > 0)
                System.out.println("Args: " + String.join(", ", args));
        System.out.println("*********");
        System.out.println("PSPTools Version " + VERSION + ", Running on " + SystemUtils.OS_NAME + " ("
                + SystemUtils.OS_ARCH + "), " + SystemUtils.OS_VERSION + " with JDK "
                + System.getProperty("java.vm.name"));
        System.out.println("*********");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // #region read settings
        SavedVariables saved = SavedVariables.Load();
        PSP LastPSP = null;
        try {
            LastPSP = PSP.getPSPFromLastSelectedInfo(saved.LastSelectedPSP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Last PSP: " + LastPSP);

        if (LastPSP != null) {
            if (LastPSP.pspActive())
                PSP.setCurrentPSP(LastPSP, false);
            else {
                JOptionPane.showMessageDialog(null, "PSP was disconnected.");
                PSP.setCurrentPSP(null);
            }
        }
        // #endregion

        new LaunchPage().setVisible(true);
    }
}
