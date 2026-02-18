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
package fn10.psptools.ui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import com.formdev.flatlaf.util.SystemFileChooser;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.psps.real.RealPSP;
import fn10.psptools.psp.psps.real.RealPSPDirectory;
import fn10.psptools.psp.psps.real.RealPSPFile;
import fn10.psptools.psp.sfo.ParamSFO;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.util.SavedVariables;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Component;
import java.awt.Toolkit;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LaunchPage extends JFrame {

    public final static Dimension size = new Dimension(600, 300);

    public final JPanel ButtonsPane = new JPanel();
    public final JMenuBar MenuBar = new JMenuBar();
    public final JMenu FileMenu = new JMenu("File");
    public final JMenu ExtraMenu = new JMenu("Extra");

    public final JButton SaveEditor = new JButton("Save Manager");
    public final JButton SaveTools = new JButton("Save Tools");
    public final JButton GameEditor = new JButton("Game Manager");
    public final JButton GameTools = new JButton("Game Tools");
    public final JButton AutoPSX = new JButton("AutoPSX");
    public final JButton FileManager = new JButton("File Manager");

    public final SpringLayout Lay = new SpringLayout();

    public static Point getScreenCenter(Component target) {
        // fuck it, if something needs this function, it needs the icon
        if (target instanceof JFrame)
            ((JFrame) target).setIconImage(new ImageIcon(target.getClass().getResource("/icon.png")).getImage());

        var size = Toolkit.getDefaultToolkit().getScreenSize();
        return new Point(((int) ((size.getWidth() - target.getWidth()) * 0.5)),
                ((int) ((size.getHeight() - target.getHeight()) * 0.5)));

    }

    public LaunchPage() {
        super("PSPTools");
        setLayout(Lay);

        FileManager.setEnabled(false);
        AutoPSX.setEnabled(false);
        GameTools.setEnabled(false);

        Lay.putConstraint(SpringLayout.EAST, ButtonsPane, -10, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, ButtonsPane, 10, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, ButtonsPane, -10, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, ButtonsPane, 10, SpringLayout.NORTH, getContentPane());

        add(ButtonsPane);

        ButtonsPane.setLayout(new GridLayout(2, 3, 10, 10));

        SaveEditor.addActionListener(action -> {
            if (!PSP.getCurrentPSP().pspActive()) {
                int option = JOptionPane.showConfirmDialog(this, "No PSP is selected, but is required.\nSelect one?",
                        "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    PSP psp = PSPSelectionUI.getNewPSP(this);
                    if (psp == null)
                        return;
                    PSP.setCurrentPSP(psp);
                    SaveEditor.doClick();
                } else {
                }
            } else {
                setVisible(false);
                new SFOBasedManager(this, SFOBasedManager.SAVES_MODE, "Save Manager",
                        PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA")).setVisible(true);

            }
        });

        SaveTools.addActionListener(act -> {
            new SaveTools(this);
        });

        GameEditor.addActionListener(action -> {
            if (!PSP.getCurrentPSP().pspActive()) {
                int option = JOptionPane.showConfirmDialog(this, "No PSP is selected, but is required.\nSelect one?",
                        "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    PSP.setCurrentPSP(PSPSelectionUI.getNewPSP(this));
                    GameEditor.doClick();
                } else {
                }
            } else {
                setVisible(false);
                new SFOBasedManager(this, SFOBasedManager.GAMES_MODE, "Game Manager",
                        PSP.getCurrentPSP().getFolder("PSP", "GAME"),
                        PSP.getCurrentPSP().getFolder("PSP", "GAME150"),
                        PSP.getCurrentPSP().getFolder("ISO"),
                        PSP.getCurrentPSP().getFolder("PSP", "GAME303")).setVisible(true);

            }
        });
        // #endregion

        ButtonsPane.add(SaveEditor);
        ButtonsPane.add(SaveTools);
        ButtonsPane.add(GameEditor);
        ButtonsPane.add(FileManager);
        ButtonsPane.add(AutoPSX);
        ButtonsPane.add(GameTools);

        FileMenu.add("Select New PSP").addActionListener(ac -> {
            PSP selected = PSPSelectionUI.getNewPSP(this);
            if (selected != null)
                PSP.setCurrentPSP(selected);
        });

        ExtraMenu.add("Open Custom SFO Manager").addActionListener(ac -> {
            SystemFileChooser fileChooser = new SystemFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(this);
            if (fileChooser.getSelectedFile() != null)
                new SFOBasedManager(this, SFOBasedManager.SINGLE, "Single SFO",
                        new RealPSPDirectory(fileChooser.getSelectedFile()))
                        .setVisible(true);

        });

        ExtraMenu.add("View SFO").addActionListener(ac -> {
            try {
                SystemFileChooser fileChooser = new SystemFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.addChoosableFileFilter(new SystemFileChooser.FileNameExtensionFilter("Supported Files", "sfo", "iso"));
                fileChooser.addChoosableFileFilter(new SystemFileChooser.FileNameExtensionFilter("SFO Files", "sfo"));
                fileChooser.addChoosableFileFilter(new SystemFileChooser.FileNameExtensionFilter("ISO Files", "iso"));
                fileChooser.showOpenDialog(this);
                if (fileChooser.getSelectedFile() == null)
                    return;
                RealPSPFile pspdir = new RealPSPFile(fileChooser.getSelectedFile());
                if (pspdir.getExtension().equalsIgnoreCase("sfo"))
                    new SFOViewer(getOwner(), ParamSFO.ofPSPFile(pspdir)).setVisible(true);
                else if (pspdir.getExtension().equalsIgnoreCase("iso"))
                    new SFOViewer(getOwner(), ParamSFOListElement.ofIso(pspdir, null).sfo).setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ExtraMenu.add("Select Demo PSP").addActionListener(ac -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "<html>Selecting the Demo PSP will set the PSP is this session to an example one, with example games, and saves; all of which their data removed, so its only the things required for PSPTools to know what it is.<br/> <br/> <b>Save patching is not possible with these saves.</b></html>",
                    "Entering Demo Mode", JOptionPane.OK_CANCEL_OPTION);
            if (choice == JOptionPane.OK_OPTION) {
                try {
                    Path demoFolder = SavedVariables.DataFolder.resolve("demo");

                    if (!demoFolder.toFile().exists()) {
                        Files.createDirectories(demoFolder);
                        File tempZip = File.createTempFile("PSPTOOLS", "TEMPDEMOZIP.zip");
                        Files.write(tempZip.toPath(), getClass().getResourceAsStream("/DEMO.zip").readAllBytes(),
                                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.WRITE);
                        tempZip.deleteOnExit();
                        ZipUnArchiver zua = new ZipUnArchiver(tempZip);
                        zua.setDestDirectory(demoFolder.toFile());
                        zua.extract();

                    }
                    PSP.DemoMode = true;
                    PSP.setCurrentPSP(new RealPSP(demoFolder), true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Unable to enter demo mode.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
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
