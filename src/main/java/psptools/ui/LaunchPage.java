package psptools.ui;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.ui.FlatFileChooserUI;

import psptools.psp.PSP;
import psptools.psp.PSPSelectionUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Component;
import java.awt.Toolkit;

public class LaunchPage extends JFrame {

    public final static Dimension size = new Dimension(400, 200);

    public final JPanel ButtonsPane = new JPanel();
    public final JMenuBar MenuBar = new JMenuBar();
    public final JMenu FileMenu = new JMenu("File");
    public final JMenu ExtraMenu = new JMenu("Extra");

    public final JButton SaveEditor = new JButton("Save Manager");
    public final JButton GameEditor = new JButton("Game Manager");
    public final JButton ConvertersButton = new JButton("Converters");

    public final SpringLayout Lay = new SpringLayout();

    public static Point getScreenCenter(Component target) {
        var size = Toolkit.getDefaultToolkit().getScreenSize();
        return new Point(((int) ((size.getWidth() - target.getWidth()) * 0.5)),
                ((int) ((size.getHeight() - target.getHeight()) * 0.5)));
    }

    public LaunchPage() {
        super("PSPTools");
        setLayout(Lay);

        Lay.putConstraint(SpringLayout.EAST, ButtonsPane, -10, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, ButtonsPane, 10, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, ButtonsPane, -10, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, ButtonsPane, 10, SpringLayout.NORTH, getContentPane());

        add(ButtonsPane);

        ButtonsPane.setLayout(new GridLayout(0, 3, 10, 10));

        SaveEditor.addActionListener(action -> {
            if (!PSP.getCurrentPSP().pspActive()) {
                int option = JOptionPane.showConfirmDialog(this, "No PSP is selected, but is required.\nSelect one?",
                        "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    PSP.setCurrentPSP(PSPSelectionUI.getNewPSP(this));
                    SaveEditor.doClick();
                } else
                    return;
            } else {
                setVisible(false);
                new SFOBasedManager(this, SFOBasedManager.SAVES_MODE, "Save Manager",
                        PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA").toFile()).setVisible(true);
                ;
            }
        });

        GameEditor.addActionListener(action -> {
            if (!PSP.getCurrentPSP().pspActive()) {
                int option = JOptionPane.showConfirmDialog(this, "No PSP is selected, but is required.\nSelect one?",
                        "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    PSP.setCurrentPSP(PSPSelectionUI.getNewPSP(this));
                    SaveEditor.doClick();
                } else
                    return;
            } else {
                setVisible(false);
                new SFOBasedManager(this, SFOBasedManager.GAMES_MODE, "Save Manager",
                        PSP.getCurrentPSP().getFolder("PSP", "GAME").toFile(),
                        PSP.getCurrentPSP().getFolder("PSP", "GAME150").toFile(),
                        PSP.getCurrentPSP().getFolder("ISO").toFile(),
                        PSP.getCurrentPSP().getFolder("PSP", "GAME303").toFile()).setVisible(true);

            }
        });

        // #region we need this cause the r dont fit, wont fit
        Font GameEditorFont = GameEditor.getFont();
        GameEditor.setFont(GameEditorFont.deriveFont(GameEditorFont.getSize() - 1f));
        // #endregion

        ButtonsPane.add(SaveEditor);
        ButtonsPane.add(GameEditor);

        ButtonsPane.add(ConvertersButton);

        ExtraMenu.add("Open Single PARAM.SFO").addActionListener(ac -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("SFO Files", "sfo"));
            fileChooser.showOpenDialog(this);
            new SFOBasedManager(this, SFOBasedManager.GAMES_MODE, fileChooser.getSelectedFile().getParentFile().getName(),
                    fileChooser.getSelectedFile().getParentFile().getParentFile()).setVisible(true);

        });

        MenuBar.add(FileMenu);
        MenuBar.add(ExtraMenu);

        setJMenuBar(MenuBar);
        setResizable(false);
        setSize(size);
        setLocation(getScreenCenter(this));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
