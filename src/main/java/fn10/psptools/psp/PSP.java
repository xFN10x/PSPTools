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
package fn10.psptools.psp;

import java.nio.file.Path;

import javax.swing.JOptionPane;

import org.apache.commons.net.ftp.FTPClient;

import com.google.gson.Gson;

import fn10.psptools.psp.psps.ftp.FTPPSP;
import fn10.psptools.psp.psps.ftp.FTPSelectionData;
import fn10.psptools.psp.psps.real.RealPSP;
import fn10.psptools.util.SavedVariables;

public abstract class PSP {

    private static final PSP NULL_PSP = new RealPSP(Path.of(""));
    public static boolean DemoMode = false;

    protected static PSP CurrentPSP = NULL_PSP;

    protected PSP() {
    }

    public static PSP getCurrentPSP() {
        return CurrentPSP;
    }

    public static void setCurrentPSP(PSP psp) {
        setCurrentPSP(psp, true);
    }

    public static PSP getPSPFromLastSelectedInfo(LastSelectedPSPInfo info) {
        switch (info.mode()) {
            case SelectionMode.PSP_DIR:
                return new RealPSP(Path.of(info.data()));

            case SelectionMode.FTP:
                String dataString = info.data();
                @SuppressWarnings("null") FTPSelectionData data = new Gson().fromJson(dataString, FTPSelectionData.class);
                return new FTPPSP(new FTPClient(), data.host(), data.port(), data.username(), data.password());
            default:
                return null;
        }
    }

    public static void setCurrentPSP(PSP psp, boolean showPopup) {
        setCurrentPSP(psp, showPopup, true);
    }

    public static void setCurrentPSP(PSP psp, boolean showPopup, boolean savePSP) {
        if (showPopup && psp != null)
            JOptionPane.showMessageDialog(null, "Selected new PSP.");
        var saved = SavedVariables.Load();
        if (psp != null) {
            saved.LastSelectedPSP = new LastSelectedPSPInfo(psp.getSelectionMode(), psp.getSelectionData());
            CurrentPSP = psp;
        } else {
            saved.LastSelectedPSP = null;
            CurrentPSP = NULL_PSP;
        }
        if (savePSP) {
            saved.Save();
        }
    }

    public abstract boolean pspActive();

    public abstract PSPDirectory getFolder(String child, String... others);

    protected abstract SelectionMode getSelectionMode();

    protected abstract String getSelectionData();

}
