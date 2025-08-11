package psptools.psp;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import psptools.util.SavedVariables;

public class PSP {

    public SelectionMode selectionMode;
    public Path path;

    protected static PSP CurrentPSP;

    public PSP(SelectionMode mode, Path Path) {
        this.selectionMode = mode;
        this.path = Path;

        if (mode != SelectionMode.PSP_DIR)
            throw new IllegalAccessError("Wrong Method for this selection mode.");

        // PSP.CurrentPSP = this;
    }

    public PSP() {
    }

    // public String toString() {
    // return path.toString() + ": " + selectionMode.toString();
    // }

    public static PSP getCurrentPSP() {
        if (CurrentPSP == null)
            return new PSP();
        return CurrentPSP;
    }

    public static void setCurrentPSP(PSP psp) {
        setCurrentPSP(psp, true);
    }

    public static void setCurrentPSP(PSP psp, boolean showPopup) {
        if (showPopup)
            JOptionPane.showMessageDialog(null, "Selected new PSP.");
        var saved = SavedVariables.Load();
        saved.LastSelectedPSP = psp;
        saved.Save();
        CurrentPSP = psp;
    }

    public boolean pspActive() {
        try {
            switch (selectionMode) {
                case SelectionMode.PSP_DIR:
                    return Files.exists(path);

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Path getFolder(String... child) {
        return Path.of(path.toString(), child);
    }
}
