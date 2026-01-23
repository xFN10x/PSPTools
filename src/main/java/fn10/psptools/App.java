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

    public static void main(String[] args) {
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
