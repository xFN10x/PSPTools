package fn10.psptools.psp;

import java.nio.file.Path;

import javax.swing.JOptionPane;

import fn10.psptools.psp.psps.RealPSP;
import fn10.psptools.util.SavedVariables;

public abstract class PSP {

    protected static PSP CurrentPSP = new RealPSP(Path.of(""));

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

            default:
                return null;
        }
    }

    public static void setCurrentPSP(PSP psp, boolean showPopup) {
        if (showPopup)
            JOptionPane.showMessageDialog(null, "Selected new PSP.");
        var saved = SavedVariables.Load();
        saved.LastSelectedPSP = new LastSelectedPSPInfo(psp.getSelectionMode(), psp.getSelectionData());
        saved.Save();
        CurrentPSP = psp;
    }

    public abstract boolean pspActive();

    public abstract PSPDirectory getFolder(String child, String... others);

    protected abstract SelectionMode getSelectionMode();

    protected abstract String getSelectionData();

}
