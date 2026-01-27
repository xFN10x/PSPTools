package fn10.psptools.psp;

import java.nio.file.Path;

import javax.swing.JOptionPane;

import fn10.psptools.util.SavedVariables;

public abstract class PSP {

    public SelectionMode selectionMode;

    protected static PSP CurrentPSP;

    public PSP(SelectionMode mode) {
        this.selectionMode = mode;

        if (mode != SelectionMode.PSP_DIR)
            throw new IllegalAccessError("Wrong Method for this selection mode.");
    }

    private PSP() {
    }
    
    public static PSP getCurrentPSP() {
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

    public abstract boolean pspActive();

    public abstract PSPDirectory getFolder(String child, String... others);

}
