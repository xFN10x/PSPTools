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

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fn10.psptools.util.ErrorShower;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import com.formdev.flatlaf.util.SystemFileChooser;
import fn10.psptools.psp.PSP;
import fn10.psptools.psp.reader.SFOReader;
import fn10.psptools.psp.reader.SFOReader.Params;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListener;
import fn10.psptools.util.ImageUtilites;
import fn10.psptools.util.SavedVariables;
import org.jspecify.annotations.NonNull;

public class SavePatching extends JFrame {

    // #region patching
    private final JTabbedPane PatchTabs = new JTabbedPane();

    private final JPanel SetupPanel = new JPanel();
    private final JButton downloadButton = new JButton("Download patches...");
    private final JLabel lastDownloadedStatus = new JLabel("Last updated: Never");
    private final JButton setupButton = new JButton("Setup");

    private final JPanel PSPPatchPanel = new JPanel();
    private final JButton SelectPSPSave = new JButton("Select...");
    private final JLabel SaveIcon = new JLabel();
    private final JLabel SaveName = new JLabel("Save Name");
    private final JLabel SaveGameName = new JLabel("Game Name");
    private final JPanel PSPPatchSeletingPanel = new JPanel();
    private final JScrollPane PSPPatchSeletingScroll = new JScrollPane(PSPPatchSeletingPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final JButton SelectAllPSPPatches = new JButton("Select All");
    private final JButton SelectNonePSPPatches = new JButton("Select None");
    private final JButton PatchPSPSave = new JButton("Patch");
    // #endregion
    private final SpringLayout Lay = new SpringLayout();
    private final SpringLayout Lay2 = new SpringLayout();
    private final SpringLayout Lay3 = new SpringLayout();
    private final BoxLayout Lay4 = new BoxLayout(SetupPanel, BoxLayout.Y_AXIS);
    private final SpringLayout Lay5 = new SpringLayout();
    private final BoxLayout Lay6 = new BoxLayout(PSPPatchSeletingPanel, BoxLayout.Y_AXIS);

    private Future<?> currentThread;
    private BufferedReader currentReader;
    private String currentMenu = "root";
    // private volatile Future<?> currentThread;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private File currentSavePatch;
    private ParamSFOListElement currentSfoListElement;
    private String currentPatchFile;

    public final static int T_PSP = 0;
    public final static int T_PS3 = 1;

    public boolean canPatch(int mode) {
        if (!PSP.getCurrentPSP().pspActive()) {
            int option = JOptionPane.showConfirmDialog(this, "No PSP is selected, but is required.\nSelect one?",
                    "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                PSP.setCurrentPSP(PSPSelectionUI.getNewPSP(this));
                return canPatch(mode);
            } else
                return false;
        } else if (!SavedVariables.hasApolloToolsInstalled()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Apollo tools are not installed. These are required for patching.\nInstall Apollo tools?",
                    "Install Apollo CLI Tools", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                SavedVariables.installApolloTools(this);
                return canPatch(mode);
            } else
                return false;
        }

        switch (mode) {
            case 1:
                if (!Path.of(SavedVariables.DataFolder.toString(), "Patches", "PS3").toFile().exists()) {
                    JOptionPane.showMessageDialog(this, "You need to download patches first.", "No Patches Found",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;

            default:
                if (!Path.of(SavedVariables.DataFolder.toString(), "Patches", "PSP").toFile().exists()) {
                    JOptionPane.showMessageDialog(this, "You need to download patches first.", "No Patches Found",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
        }
        return true;
    }

    public SavePatching(Frame parent) {
        super("Save Patching");

        SavedVariables saved = SavedVariables.Load();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                parent.setVisible(true);
                if (currentThread != null) {
                    // System.out.println(currentThread.cancel(true));
                    try {
                        currentReader.close();
                    } catch (Exception e1) {
                        ErrorShower.full(parent, e1);
                    }
                }
            }

            public void windowOpened(WindowEvent e) {
                setupButton.setEnabled(!SavedVariables.hasApolloToolsInstalled());
            }

        });

        setResizable(false);
        setLayout(Lay);
        setSize(new Dimension(450, 500));
        // #region patch panel

        // #region setup setup tab

        SetupPanel.setLayout(Lay4);

        SetupPanel.add(Box.createVerticalStrut(180));

        SetupPanel.add(downloadButton);
        downloadButton.setAlignmentX(0.5f);
        downloadButton.setAlignmentY(0.5f);

        downloadButton.addActionListener(ac -> {
            try {
                Object option = JOptionPane.showInputDialog(this, "What patches would you like to download",
                        "Patch Downloader", JOptionPane.QUESTION_MESSAGE, null,
                        new String[] { "PSP + PS3", "PSP", "PS3" },
                        "PSP + PS3");

                if (option == null)
                    return;

                LoadingScreen loading = new LoadingScreen(this);

                SwingUtilities.invokeLater(() -> {
                    loading.setVisible(true);
                });
                new Thread(() -> {
                    try {
                        loading.changeText("Opening connection...");

                        URL patchZip = new URI("https://github.com/bucanero/apollo-patches/archive/refs/heads/main.zip")
                                .toURL();

                        InputStream stream = patchZip.openStream();
                        File tempFile = File.createTempFile("PSPTOOLS", "TEMPPATCHS.zip");

                        FileOutputStream output = new FileOutputStream(tempFile);

                        loading.changeText("Writing to temporary file...");
                        IOUtils.copy(stream, output);

                        loading.changeText("Deleting old patches...");
                        FileUtils.deleteDirectory(Path.of(SavedVariables.DataFolder.toString(), "Patches").toFile()); // remove
                                                                                                                      // old
                                                                                                                      // patches

                        // tempFile.deleteOnExit();
                        System.out.println(tempFile.getPath());
                        stream.close();

                        loading.changeText("Download patches...");
                        ZipUnArchiver zip = new ZipUnArchiver(tempFile);

                        // System.out.println(Path.of(SavedVariables.DataFolder.toString(), "Patches",
                        // "PSP").toString());
                        Path path = Path.of(SavedVariables.DataFolder.toString());
                        switch (option.toString()) {
                            case "PSP":
                                try {
                                    zip.extract("apollo-patches-main/PSP/",
                                            path.toFile());
                                } catch (Exception e) {

                                }
                                break;
                            case "PS3":
                                try {
                                    zip.extract("apollo-patches-main/PS3/",
                                            path.toFile());
                                } catch (Exception e) {
                                    ErrorShower.full(this, e);
                                }
                                break;

                            default:
                                try {
                                    zip.extract("apollo-patches-main/PSP/",
                                            path.toFile());

                                    zip.extract("apollo-patches-main/PS3/",
                                            path.toFile());
                                } catch (Exception e) {
                                    ErrorShower.full(this, e);
                                }
                                break;

                        }

                        Path.of(SavedVariables.DataFolder.toString(), "apollo-patches-main").toFile()
                                .renameTo(Path.of(SavedVariables.DataFolder.toString(), "Patches").toFile());

                        loading.setVisible(false);
                        SavedVariables vars = SavedVariables.Load();
                        vars.SinceLastPatchUpdate = Calendar.getInstance().getTime();
                        vars.Save();

                        tempFile.delete();

                        SwingUtilities.invokeLater(() -> {
                            loading.setVisible(false);
                        });

                        JOptionPane.showMessageDialog(null, "Patchs have been downloaded.");
                    } catch (Exception e) {
                        ErrorShower.full(this, e);
                    }
                }).start();
            } catch (Exception e) {
                ErrorShower.full(this, e);
            }
        });

        SetupPanel.add(lastDownloadedStatus);
        lastDownloadedStatus.setAlignmentX(0.5f);
        lastDownloadedStatus.setAlignmentY(0.5f);

        Date lastUpdated = SavedVariables.Load().SinceLastPatchUpdate;
        if (lastUpdated != null)
            lastDownloadedStatus.setText("Last Updated: "
                    + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(SavedVariables.Load().SinceLastPatchUpdate));

        SetupPanel.add(setupButton);
        setupButton.setAlignmentX(0.5f);
        setupButton.setAlignmentY(0.5f);

        setupButton.addActionListener(ac -> {
            if (!SavedVariables.hasApolloToolsInstalled()) {
                SavedVariables.installApolloTools(this);
                setupButton.setEnabled(false);
            } else {
                setupButton.setEnabled(false);
            }
        });

        // #endregion
        // #region setup PSP patching tab
        PSPPatchPanel.setLayout(Lay5);
        PSPPatchSeletingPanel.setLayout(Lay6);

        PSPPatchSeletingScroll.getVerticalScrollBar().setUnitIncrement(18);

        Lay5.putConstraint(SpringLayout.NORTH, SelectPSPSave, 5, SpringLayout.NORTH, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.EAST, SelectPSPSave, -5, SpringLayout.EAST, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.WEST, SelectPSPSave, 5, SpringLayout.WEST, PSPPatchPanel);

        Lay5.putConstraint(SpringLayout.SOUTH, PatchPSPSave, -5, SpringLayout.SOUTH, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.EAST, PatchPSPSave, -5, SpringLayout.EAST, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.WEST, PatchPSPSave, 5, SpringLayout.WEST, PSPPatchPanel);

        Lay5.putConstraint(SpringLayout.SOUTH, SelectAllPSPPatches, -5, SpringLayout.NORTH, PatchPSPSave);
        Lay5.putConstraint(SpringLayout.EAST, SelectAllPSPPatches, -5, SpringLayout.HORIZONTAL_CENTER, PatchPSPSave);
        Lay5.putConstraint(SpringLayout.WEST, SelectAllPSPPatches, 5, SpringLayout.WEST, PSPPatchPanel);

        Lay5.putConstraint(SpringLayout.SOUTH, SelectNonePSPPatches, -5, SpringLayout.NORTH, PatchPSPSave);
        Lay5.putConstraint(SpringLayout.WEST, SelectNonePSPPatches, -5, SpringLayout.HORIZONTAL_CENTER, PatchPSPSave);
        Lay5.putConstraint(SpringLayout.EAST, SelectNonePSPPatches, -5, SpringLayout.EAST, PSPPatchPanel);

        Lay5.putConstraint(SpringLayout.NORTH, PSPPatchSeletingScroll, 5, SpringLayout.SOUTH, SaveIcon);
        Lay5.putConstraint(SpringLayout.SOUTH, PSPPatchSeletingScroll, -5, SpringLayout.NORTH, SelectAllPSPPatches);
        Lay5.putConstraint(SpringLayout.EAST, PSPPatchSeletingScroll, -5, SpringLayout.EAST, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.WEST, PSPPatchSeletingScroll, 5, SpringLayout.WEST, PSPPatchPanel);

        Lay5.putConstraint(SpringLayout.WEST, SaveIcon, 0, SpringLayout.WEST, SelectPSPSave);
        Lay5.putConstraint(SpringLayout.NORTH, SaveIcon, 5, SpringLayout.SOUTH, SelectPSPSave);

        Lay5.putConstraint(SpringLayout.WEST, SaveName, 3, SpringLayout.EAST, SaveIcon);
        Lay5.putConstraint(SpringLayout.NORTH, SaveName, 0, SpringLayout.NORTH, SaveIcon);

        Lay5.putConstraint(SpringLayout.WEST, SaveGameName, 3, SpringLayout.EAST, SaveIcon);
        Lay5.putConstraint(SpringLayout.SOUTH, SaveGameName, 0, SpringLayout.SOUTH, SaveIcon);

        SaveIcon.setIcon(new ImageIcon(getClass().getResource("/no_icon0.png")));

        SelectAllPSPPatches.addActionListener(ac -> {
            for (Component component : PSPPatchSeletingPanel.getComponents()) {
                if (component instanceof JCheckBox) {
                    if (component.isEnabled())
                        ((JCheckBox) component).setSelected(true);
                }
            }
        });

        SelectNonePSPPatches.addActionListener(ac -> {
            for (Component component : PSPPatchSeletingPanel.getComponents()) {
                if (component instanceof JCheckBox) {
                    if (component.isEnabled())
                        ((JCheckBox) component).setSelected(false);
                }
            }
        });

        SelectPSPSave.addActionListener(ac -> {
            if (canPatch(T_PSP)) {
                ParamSFOListElement selected = SFOBasedSelector.openSaveSelector(this);
                currentSfoListElement = selected;

                SaveIcon.setIcon(ImageUtilites.ResizeIcon(selected.getIcon0(), 117, 65));
                try {
                    SaveName.setText(selected.sfo.getParam(SFOReader.Params.SaveTitle, true).toString());
                    SaveGameName.setText(selected.sfo.getParam(SFOReader.Params.Title, true).toString());

                    File PSPPatchDir = Path.of(SavedVariables.DataFolder.toString(), "Patches", "PSP").toFile();
                    boolean foundPatch = false;
                    System.out.println(PSPPatchDir.getAbsolutePath());
                    for (File file : PSPPatchDir.listFiles()) {
                        if (!file.getName().endsWith(".savepatch"))
                            continue;
                        // if the name of the savepatch is somewhere in the save
                        // BEGIN PARSE!!!!
                        if (selected.sfo.getParam(Params.SaveFolderName).toString()
                                .contains(file.getName().substring(0, file.getName().indexOf(".")))) {
                            foundPatch = true;
                            currentSavePatch = file;
                            BufferedReader stream = new BufferedReader(new FileReader(file));
                            // first 4 lines are info about patch
                            // System.out.println(stream.readLine());
                            // System.out.println(stream.readLine());
                            // System.out.println(stream.readLine());
                            // System.out.println(stream.readLine());

                            PSPPatchSeletingPanel.removeAll();

                            int i = 1;
                            String currentFile = "*";

                            while (true) { // read each line
                                String line = stream.readLine();
                                if (line == null) {
                                    break;
                                }

                                if (line.startsWith(":")) { // change file
                                    currentFile = line.replace(":", "").trim();
                                    JLabel toAdd = new JLabel(currentFile);

                                    // make text darker if the save doesnt have that file
                                    if (!selected.dir.resolve(currentFile).actuallyExists())
                                        toAdd.setForeground(new Color(0.5f, 0.5f, 0.5f));
                                    else // if it does have the file, (it should only have one) set the current file
                                        currentPatchFile = currentFile;

                                    PSPPatchSeletingPanel.add(toAdd);
                                }
                                if (line.startsWith("[")) { // its a patch
                                    String patchName = line.replace("[", "").replace("]", "");
                                    System.out.println("(" + currentFile + ") Patch " + i + ": "
                                            + patchName);

                                    JCheckBox check = getJCheckBox(patchName, i);

                                    PSPPatchSeletingPanel.add(check);
                                    i++;
                                }

                            }

                            stream.close();
                        }
                    }
                    if (!foundPatch)
                        JOptionPane.showMessageDialog(this, "No patches were found for this save.", "No Patches found",
                                JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    ErrorShower.full(this, e);
                }
            }
        });

        PatchPSPSave.addActionListener(ac -> {
            List<String> patchNames = new ArrayList<String>();

            for (Component comp : PSPPatchSeletingPanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    if (((JCheckBox) comp).isSelected()) {
                        patchNames.add(((JCheckBox) comp).getText());
                    }
                }
            }

            int option = JOptionPane.showConfirmDialog(this,
                    "<html>Apply the current patches?<br><b>" + String.join("<br>", patchNames) + "</html>", "Apply?",
                    JOptionPane.YES_NO_OPTION);

            if (option != JOptionPane.YES_OPTION)
                return;

            try {
                LoadingScreen loading = new LoadingScreen(this);

                SwingUtilities.invokeLater(() -> {
                    loading.setVisible(true);
                });

                List<String> patchNumbers = new ArrayList<String>();

                for (Component comp : PSPPatchSeletingPanel.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        if (((JCheckBox) comp).isSelected()) {
                            patchNumbers.add(comp.getName());
                        }
                    }
                }

                ProcessBuilder procBuilder = new ProcessBuilder(
                        Path.of(SavedVariables.DataFolder.toString(), "tools", "patcher").toString(),
                        currentSavePatch.getAbsolutePath(), String.join(", ", patchNumbers),
                        Path.of(currentSfoListElement.dir.toString(), currentPatchFile).toString());
                File file = File.createTempFile("PSPTOOLS", "TEMPTEST.log");
                procBuilder.redirectOutput(file);

                Process proc = procBuilder.start();
                new Thread(() -> {
                    try {
                        proc.waitFor();
                        loading.setVisible(false);

                        int Option = JOptionPane.showConfirmDialog(this, "The save has been patched. View log?",
                                "Save Patched", JOptionPane.YES_NO_OPTION);

                        if (Option == JOptionPane.YES_OPTION)
                            Desktop.getDesktop().open(file);
                        else
                            file.delete();

                    } catch (Exception e) {
                        ErrorShower.full(this, e);
                    }
                }).start();
            } catch (Exception e) {
                ErrorShower.full(this, e);
            }
        });

        PSPPatchPanel.add(SelectPSPSave);
        PSPPatchPanel.add(SaveIcon);
        PSPPatchPanel.add(SaveName);
        PSPPatchPanel.add(SaveGameName);
        PSPPatchPanel.add(PSPPatchSeletingScroll);
        PSPPatchPanel.add(SelectAllPSPPatches);
        PSPPatchPanel.add(SelectNonePSPPatches);
        PSPPatchPanel.add(PatchPSPSave);
        // #endregion
        Lay4.layoutContainer(SetupPanel);

        PatchTabs.setTabPlacement(JTabbedPane.LEFT);
        PatchTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        PatchTabs.addTab("Setup", SetupPanel);
        PatchTabs.addTab("PSP", PSPPatchPanel);

        setLayout(Lay3);

        Lay3.putConstraint(SpringLayout.WEST, PatchTabs, 0, SpringLayout.WEST, getContentPane());
        Lay3.putConstraint(SpringLayout.SOUTH, PatchTabs, 0, SpringLayout.SOUTH, getContentPane());
        Lay3.putConstraint(SpringLayout.NORTH, PatchTabs, 0, SpringLayout.NORTH, getContentPane());
        Lay3.putConstraint(SpringLayout.EAST, PatchTabs, 0, SpringLayout.EAST, getContentPane());

        add(PatchTabs);
        // #endregion

        setLocation(LaunchPage.getScreenCenter(this));
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        parent.setVisible(false);
    }

    private static @NonNull JCheckBox getJCheckBox(String patchName, int i) {
        JCheckBox check = new JCheckBox(patchName);
        check.setName(String.valueOf(i));

        // if the save doesnt have the file specified, it must be
        // for another save type from the game
        // if (!Path.of(selected.dir.getAbsolutePath(), currentFile).toFile().exists())
        // {
        // check.setEnabled(false);
        // }
        // commented the rest out cause the middle was already out

        if (patchName.contains("Required")) {
            check.setSelected(true);
            check.setEnabled(false);
        }
        return check;
    }

}
