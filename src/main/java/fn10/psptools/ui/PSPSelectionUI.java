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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileSystemView;

import fn10.psptools.PSPTools;
import fn10.psptools.psp.psps.real.RealPSPRoot;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.ftp.FTPClient;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.psps.ftp.FTPPSP;
import fn10.psptools.psp.psps.real.RealPSP;

public class PSPSelectionUI extends JDialog {

    private final static Dimension size = new Dimension(400, 200);
    private final SpringLayout Lay = new SpringLayout();

    private final JTabbedPane Tabbed = new JTabbedPane(JTabbedPane.TOP);

    // #region drive selection part
    private final JPanel DS = new JPanel();
    private final SpringLayout DSLay = new SpringLayout();
    private final JButton AutoButton = new JButton("Auto");
    private final JButton RefreshButton = new JButton("Refresh");
    private final JButton AutoVitaButton = new JButton("Auto (PSV)");
    private final JCheckBox VitaCheck = new JCheckBox("Is this a vita?");
    private final JButton SelectButton = new JButton("Select");
    private final JComboBox<RealPSPRoot> InputDriveBox = new JComboBox<>(RealPSPRoot.getRoots());
    // #endregion
    // #region folder selection part
    private final JPanel DIRS = new JPanel();
    private final SpringLayout DirSLay = new SpringLayout();
    private final JTextField FolderSelection = new JTextField("(No folder selected.)");
    private final JButton FolderButton = new JButton("...");
    private final JButton DirSelectButton = new JButton("Select");
    // #endregion
    // #region ftp selection part
    private final SpringLayout FtpSLay = new SpringLayout();
    private final JPanel FTPS = new JPanel(FtpSLay);
    private final JTextField HostName = new JTextField("192.168.0.1");
    private final JSpinner Port = new JSpinner(new SpinnerNumberModel(21, 0, 65535, 1));
    private final JTextField Username = new JTextField("PSPTools");
    private final JTextField Password = new JTextField("");
    private final JButton FtpSelectButton = new JButton("Connect...");
    // #endregion
    private PSP SelectedPSP = null;

    private PSPSelectionUI(Frame parent) {
        super(parent, "Select PSP", ModalityType.APPLICATION_MODAL);
        setLayout(Lay);

        Timer timer = new Timer();

        Runnable onUpdateDrives = () -> {
            InputDriveBox.removeAllItems();
            for (RealPSPRoot f : RealPSPRoot.getRoots()) {
                InputDriveBox.addItem(f);
            }
        };

        Lay.putConstraint(SpringLayout.EAST, Tabbed, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, Tabbed, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, Tabbed, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, Tabbed, 0, SpringLayout.SOUTH, getContentPane());

        // #region Drive Selection Stuff
        DS.setLayout(DSLay);
        DSLay.putConstraint(SpringLayout.NORTH, AutoButton, 5, SpringLayout.NORTH, DS);
        DSLay.putConstraint(SpringLayout.WEST, AutoButton, 5, SpringLayout.WEST, DS);

        DSLay.putConstraint(SpringLayout.NORTH, AutoVitaButton, 3, SpringLayout.SOUTH, AutoButton);
        DSLay.putConstraint(SpringLayout.WEST, AutoVitaButton, 5, SpringLayout.WEST, DS);

        DSLay.putConstraint(SpringLayout.VERTICAL_CENTER, VitaCheck, 0, SpringLayout.VERTICAL_CENTER, AutoVitaButton);
        DSLay.putConstraint(SpringLayout.WEST, VitaCheck, 3, SpringLayout.EAST, AutoVitaButton);

        DSLay.putConstraint(SpringLayout.NORTH, InputDriveBox, 5, SpringLayout.NORTH, DS);
        DSLay.putConstraint(SpringLayout.WEST, InputDriveBox, 5, SpringLayout.EAST, AutoButton);
        DSLay.putConstraint(SpringLayout.EAST, InputDriveBox, -5, SpringLayout.WEST, RefreshButton);

        DSLay.putConstraint(SpringLayout.NORTH, RefreshButton, 0, SpringLayout.NORTH, InputDriveBox);
        DSLay.putConstraint(SpringLayout.SOUTH, RefreshButton, 0, SpringLayout.SOUTH, InputDriveBox);
        DSLay.putConstraint(SpringLayout.EAST, RefreshButton, -5, SpringLayout.EAST, DS);

        DSLay.putConstraint(SpringLayout.SOUTH, SelectButton, -5, SpringLayout.SOUTH, DS);
        DSLay.putConstraint(SpringLayout.EAST, SelectButton, -5, SpringLayout.EAST, DS);
        DSLay.putConstraint(SpringLayout.WEST, SelectButton, 5, SpringLayout.WEST, DS);

        RefreshButton.addActionListener(_ -> onUpdateDrives.run());

        VitaCheck.addItemListener(state -> {
            AutoVitaButton.setEnabled(state.getStateChange() == ItemEvent.SELECTED);
            AutoButton.setEnabled(state.getStateChange() != ItemEvent.SELECTED);
        });

        AutoVitaButton.setEnabled(false);

        AutoButton.addActionListener(action -> {
            ArrayList<RealPSPRoot> found = new ArrayList<>();
            for (RealPSPRoot root : RealPSPRoot.getRoots()) {
                Path rootPath = root.getFile().toPath();
                File PSPFolder = rootPath.resolve("PSP").toFile();
//                File ISOFolder = rootPath.resolve("ISO").toFile();
//                File PSPGameFolder = rootPath.resolve("PSP", "GAME").toFile();

                if (PSPFolder.exists()) {
                    found.add(root);
                }
            }
            if (found.size() == 1) {
                RealPSPRoot root = found.getFirst();
                JOptionPane.showMessageDialog(parent, "Found PSP at " + root, "PSP Found",
                        JOptionPane.INFORMATION_MESSAGE);
                InputDriveBox.setSelectedItem(root);
            } else if (found.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Did not find PSP connected to any drives.", "PSP Not Found",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                int selected = JOptionPane.showOptionDialog(parent, "Multiple PSPs were found, which drive do you want to use?", "Select PSP", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, found.toArray(), found.getFirst());
                InputDriveBox.setSelectedItem(found.get(selected));
            }
        });

        AutoVitaButton.addActionListener(_ -> {
            ArrayList<RealPSPRoot> found = new ArrayList<>();
            for (RealPSPRoot root : RealPSPRoot.getRoots()) {
                String path = root.getFile().getPath();
                File PSPFolder = Path.of(path, "pspemu", "PSP").toFile();
//                File ISOFolder = Path.of(path, "pspemu", "ISO").toFile();
//                File PSPGameFolder = Path.of(path, "pspemu", "PSP", "Game").toFile();
//                File PSPSaveFolder = Path.of(path, "pspemu", "PSP", "Savedata").toFile();

                if (PSPFolder.exists()) {
                    found.add(root);
                }
            }
            if (found.size() == 1) {
                RealPSPRoot root = found.getFirst();
                JOptionPane.showMessageDialog(parent, "Found Vita at " + root, "Vita Found",
                        JOptionPane.INFORMATION_MESSAGE);
                InputDriveBox.setSelectedItem(root);
            } else if (found.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Did not find a Vita with Adrenaline connected to any drives.", "Vita Not Found",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                int selected = JOptionPane.showOptionDialog(parent, "Multiple Vitas were found, which drive do you want to use?", "Select Vita", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, found.toArray(), found.getFirst());
                InputDriveBox.setSelectedItem(found.get(selected));
            }
        });

        SelectButton.addActionListener(_ -> {
            String root = ((RealPSPRoot) InputDriveBox.getSelectedItem()).getFile().getPath();
            if (VitaCheck.isSelected())
                root += "pspemu";
            File PSPFolder = new File(Path.of(root, "PSP").toString());
            File PSPGameFolder = new File(Path.of(root, "PSP", "Game").toString());

            if (PSPFolder.exists() && PSPGameFolder.exists()) {
                SelectedPSP = new RealPSP(Path.of(root));
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Selected drive is not a PSP.");
            }
        });

        DS.add(AutoButton);
        DS.add(AutoVitaButton);
        DS.add(VitaCheck);
        DS.add(InputDriveBox);
        DS.add(SelectButton);
        DS.add(RefreshButton);

        // #endregion
        // #region Dir Selection Stuff
        DIRS.setLayout(DirSLay);
        DirSLay.putConstraint(SpringLayout.NORTH, FolderSelection, 5, SpringLayout.NORTH, DIRS);
        DirSLay.putConstraint(SpringLayout.WEST, FolderSelection, 5, SpringLayout.WEST, DIRS);
        DirSLay.putConstraint(SpringLayout.EAST, FolderSelection, -3, SpringLayout.WEST, FolderButton);

        DirSLay.putConstraint(SpringLayout.EAST, FolderButton, -5, SpringLayout.EAST, DIRS);
        DirSLay.putConstraint(SpringLayout.NORTH, FolderButton, 0, SpringLayout.NORTH, FolderSelection);
        DirSLay.putConstraint(SpringLayout.SOUTH, FolderButton, 0, SpringLayout.SOUTH, FolderSelection);

        DirSLay.putConstraint(SpringLayout.SOUTH, DirSelectButton, -5, SpringLayout.SOUTH, DIRS);
        DirSLay.putConstraint(SpringLayout.EAST, DirSelectButton, -5, SpringLayout.EAST, DIRS);
        DirSLay.putConstraint(SpringLayout.WEST, DirSelectButton, 5, SpringLayout.WEST, DIRS);

        FolderButton.addActionListener(ac -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.showOpenDialog(this);

            if (chooser.getSelectedFile() == null)
                return;

            FolderSelection.setText(chooser.getSelectedFile().getAbsolutePath());
        });

        DirSelectButton.addActionListener(action -> {
            Path root = Path.of(FolderSelection.getText());
            File PSPFolder = root.resolve("PSP").toFile();
            File ISOFolder = root.resolve("ISO").toFile();
            File PSPGameFolder = root.resolve("PSP", "GAME").toFile();

            if (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists()) {
                SelectedPSP = new RealPSP(root);

                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Selected folder doesnt have:\n\"/PSP/\", or  \"/ISO/\", \"/PSP/GAME/\"",
                        "Folder is not PSP", JOptionPane.ERROR_MESSAGE);
            }
        });

        DIRS.add(FolderSelection);
        DIRS.add(FolderButton);
        DIRS.add(DirSelectButton);
        // #endregion
        // #region FTP Selection Stuff

        final JLabel portLabel = new JLabel("Port:");
        final JLabel un = new JLabel("Username");
        final JLabel pw = new JLabel("Password");

        FtpSLay.putConstraint(SpringLayout.WEST, HostName, 3, SpringLayout.WEST, FTPS);
        FtpSLay.putConstraint(SpringLayout.NORTH, HostName, 3, SpringLayout.NORTH, FTPS);
        HostName.setSize(250, 0);
        FtpSLay.putConstraint(SpringLayout.WEST, portLabel, 1, SpringLayout.EAST, HostName);

        FtpSLay.putConstraint(SpringLayout.WEST, Port, 3, SpringLayout.EAST, portLabel);
        FtpSLay.putConstraint(SpringLayout.EAST, Port, 1, SpringLayout.EAST, FTPS);
        FtpSLay.putConstraint(SpringLayout.NORTH, Port, 0, SpringLayout.NORTH, HostName);
        FtpSLay.putConstraint(SpringLayout.SOUTH, Port, 0, SpringLayout.SOUTH, HostName);

        FtpSLay.putConstraint(SpringLayout.VERTICAL_CENTER, portLabel, 0, SpringLayout.VERTICAL_CENTER, HostName);

        FtpSLay.putConstraint(SpringLayout.WEST, FtpSelectButton, 3, SpringLayout.WEST, FTPS);
        FtpSLay.putConstraint(SpringLayout.EAST, FtpSelectButton, -3, SpringLayout.EAST, FTPS);
        FtpSLay.putConstraint(SpringLayout.SOUTH, FtpSelectButton, -3, SpringLayout.SOUTH, FTPS);

        FtpSLay.putConstraint(SpringLayout.NORTH, Username, 3, SpringLayout.SOUTH, HostName);
        FtpSLay.putConstraint(SpringLayout.WEST, Username, 3, SpringLayout.EAST, un);
        FtpSLay.putConstraint(SpringLayout.SOUTH, un, 0, SpringLayout.SOUTH, Username);
        FtpSLay.putConstraint(SpringLayout.WEST, un, 3, SpringLayout.WEST, FTPS);
        FtpSLay.putConstraint(SpringLayout.NORTH, un, 0, SpringLayout.NORTH, Username);

        FtpSLay.putConstraint(SpringLayout.NORTH, Password, 3, SpringLayout.SOUTH, Username);
        FtpSLay.putConstraint(SpringLayout.WEST, Password, 3, SpringLayout.EAST, un);
        FtpSLay.putConstraint(SpringLayout.SOUTH, pw, 0, SpringLayout.SOUTH, Password);
        FtpSLay.putConstraint(SpringLayout.WEST, pw, 3, SpringLayout.WEST, FTPS);
        FtpSLay.putConstraint(SpringLayout.NORTH, pw, 0, SpringLayout.NORTH, Password);

        FTPS.add(HostName);
        FTPS.add(pw);
        FTPS.add(un);
        FTPS.add(Username);
        FTPS.add(Password);
        FTPS.add(Port);
        FTPS.add(portLabel);

        FTPS.add(FtpSelectButton);

        FtpSelectButton.addActionListener(_ -> {
            new Thread(() -> {
                String host = HostName.getText();
                FTPClient client = new FTPClient();
                client.setConnectTimeout(10000);
                FTPPSP ftppsp = new FTPPSP(client, host, ((int) Port.getValue()), Username.getText(), Password.getText());
                if (ftppsp.pspActive()) {
                    SelectedPSP = ftppsp;
                    setVisible(false);
                    dispose();
                }
            }).start();
        });

        // #endregion

        add(Tabbed);
        Tabbed.add(DS);
        Tabbed.add(DIRS);
        Tabbed.add(FTPS);

        Tabbed.addTab("Drive", DS);
        Tabbed.addTab("Directory", DIRS);
        Tabbed.addTab("FTP", FTPS);
        Tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timer.cancel();
            }
        });

        setSize(size);
        setResizable(false);
        setLocation(LaunchPage.getScreenCenter(this));
    }

    public static PSP getNewPSP(Frame parent) {
        if (PSP.DemoMode) {
            JOptionPane.showMessageDialog(parent, "Cannot change PSP in demo mode, please restart PSPTools.");
            return null;
        }
        PSPSelectionUI ui = new PSPSelectionUI(parent);

        ui.setVisible(true);

        if (ui.SelectedPSP != null) {
            PSPTools.log.info("New PSP Selected: {}", ui.SelectedPSP.getClass().getSimpleName());
        }

        return ui.SelectedPSP;
    }
}
