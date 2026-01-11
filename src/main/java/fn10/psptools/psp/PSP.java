package fn10.psptools.psp;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import fn10.psptools.util.SavedVariables;

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
                    File PSPFolder = new File(Path.of(path.toString(), "PSP").toString());
                    File ISOFolder = new File(Path.of(path.toString(), "ISO").toString());
                    File PSPGameFolder = new File(Path.of(path.toString(), "PSP", "Game").toString());
                    File PSPSaveFolder = new File(Path.of(path.toString(), "PSP", "Savedata").toString());

                    return (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists()
                            && PSPSaveFolder.exists());

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Path getFolder(String... child) {
        return path.resolve(PSPPath.of(child).toString());
    }

}
