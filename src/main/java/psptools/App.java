package psptools;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import psptools.psp.PSP;
import psptools.ui.LaunchPage;
import psptools.util.SavedVariables;

public class App {
    public static void main(String[] args) {
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

        if (LastPSP.pspActive())
            PSP.setCurrentPSP(LastPSP);
        else
            JOptionPane.showMessageDialog(null, "PSP was disconnected.");
        // #endregion

        new LaunchPage().setVisible(true);
    }
}
