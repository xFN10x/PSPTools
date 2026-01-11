package fn10.psptools;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;

import fn10.psptools.psp.PSP;
import fn10.psptools.ui.LaunchPage;
import fn10.psptools.util.SavedVariables;

public class App {
    public static void main(String[] args) {
        if (args != null)
            System.out.println("Args: " + String.join(", ", args));

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // #region read settings
        SavedVariables saved = SavedVariables.Load();
        PSP LastPSP = saved.LastSelectedPSP;

        System.out.println(LastPSP);

        if (LastPSP != null) {
            if (LastPSP.pspActive())
                PSP.setCurrentPSP(LastPSP, false);
            else
                JOptionPane.showMessageDialog(null, "PSP was disconnected.");
        }
        // #endregion

        new LaunchPage().setVisible(true);
    }
}
