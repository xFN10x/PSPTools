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

import com.formdev.flatlaf.util.SystemFileChooser;
import fn10.psptools.psp.PSP;
import fn10.psptools.psp.reader.SFOReader.Params;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListener;
import fn10.psptools.util.ErrorShower;
import fn10.psptools.util.ImageUtilites;
import fn10.psptools.util.SavedVariables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
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

public class DatabaseTools extends JFrame implements SFOListElementListener {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private String currentMenu = "root";
    private final JTextField databaseSearch = new JTextField();
    private final JPanel databasePanel = new JPanel();
    private final JButton databaseBack = new JButton("< Back");
    private final JLabel databasePath = new JLabel(currentMenu);
    private final JPanel databaseListingInnerPanel = new JPanel();
    private final JScrollPane databaseListingPanel = new JScrollPane(databaseListingInnerPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final SpringLayout Lay = new SpringLayout();
    private final SpringLayout Lay2 = new SpringLayout();

    private Future<?> currentThread;
    private BufferedReader currentReader;
    // private volatile Future<?> currentThread;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void paintComponents(Graphics g) {
        databasePath.setText(currentMenu);
        super.paintComponents(g);
    }

    public DatabaseTools(Frame parent) {
        super("Database Tools");

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
        });

        setResizable(false);
        setLayout(Lay);
        setSize(new Dimension(450, 500));

        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        tabbedPane.addTab("Save Database", databasePanel);

        Lay.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, tabbedPane, 0, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, getContentPane());

//        Lay.putConstraint(SpringLayout.NORTH, databasePanel, 0, SpringLayout.NORTH, getContentPane());
//        Lay.putConstraint(SpringLayout.WEST, databasePanel, 0, SpringLayout.WEST, getContentPane());
//        Lay.putConstraint(SpringLayout.EAST, databasePanel, 0, SpringLayout.EAST, getContentPane());
//        Lay.putConstraint(SpringLayout.SOUTH, databasePanel, 0, SpringLayout.SOUTH, getContentPane());
        databaseListingInnerPanel.setLayout(new BoxLayout(databaseListingInnerPanel, BoxLayout.Y_AXIS));

        add(tabbedPane);

        // #region datapase panel
        databasePanel.setLayout(Lay2);

        Lay2.putConstraint(SpringLayout.NORTH, databaseSearch, 10, SpringLayout.NORTH, databasePanel);
        // Lay2.putConstraint(SpringLayout.SOUTH, databaseSearch, -40,
        // SpringLayout.SOUTH, databasePanel);
        Lay2.putConstraint(SpringLayout.EAST, databaseSearch, -10, SpringLayout.EAST, databasePanel);
        Lay2.putConstraint(SpringLayout.WEST, databaseSearch, 10, SpringLayout.WEST, databasePanel);

        Lay2.putConstraint(SpringLayout.NORTH, databasePath, 5, SpringLayout.SOUTH, databaseSearch);

        Lay2.putConstraint(SpringLayout.NORTH, databaseListingPanel, 5, SpringLayout.SOUTH, databaseSearch);
        Lay2.putConstraint(SpringLayout.SOUTH, databaseListingPanel, -40, SpringLayout.SOUTH, databasePanel);
        Lay2.putConstraint(SpringLayout.EAST, databaseListingPanel, -10, SpringLayout.EAST, databasePanel);
        Lay2.putConstraint(SpringLayout.WEST, databaseListingPanel, 10, SpringLayout.WEST, databasePanel);

        Lay2.putConstraint(SpringLayout.NORTH, databaseBack, 5, SpringLayout.SOUTH, databaseListingPanel);
        Lay2.putConstraint(SpringLayout.SOUTH, databaseBack, -5, SpringLayout.SOUTH, databasePanel);
        Lay2.putConstraint(SpringLayout.EAST, databaseBack, -10, SpringLayout.EAST, databasePanel);
        Lay2.putConstraint(SpringLayout.WEST, databaseBack, 10, SpringLayout.WEST, databasePanel);
        databaseListingPanel.getVerticalScrollBar().setUnitIncrement(18);
        databaseListingInnerPanel.add(new JLabel("Search by ID (e.g. ULUS03410) or by name (e.g. METAL GEAR SOLID)"));

        databaseSearch.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadDatabase(saved.DatabaseUrl, databaseSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadDatabase(saved.DatabaseUrl, databaseSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadDatabase(saved.DatabaseUrl, databaseSearch.getText());
            }

        });

        databaseBack.addActionListener(ac -> {
            if (!currentMenu.equals("root")) {
                currentMenu = "root";
                databaseSearch.setText("");
                reloadDatabase(saved.DatabaseUrl, "");
                databaseListingInnerPanel
                        .add(new JLabel("Search by ID (e.g. ULUS03410) or by name (e.g. METAL GEAR SOLID)"));
                databaseBack.setEnabled(false);
            }
        });

        databasePanel.add(databaseSearch);
        databasePanel.add(databaseListingPanel);
        databasePanel.add(databaseBack);
        // #endregion

        setLocation(LaunchPage.getScreenCenter(this));
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        parent.setVisible(false);

        if (saved.DatabaseUrl == null) {
            String choice = JOptionPane.showInputDialog(this, "There is no save database selected.\nPlease Select one.",
                    "Select Save Database", JOptionPane.INFORMATION_MESSAGE, null,
                    new String[] { "Apollo Save Database" }, "Apollo Save Database").toString();
            if (choice.equals("Apollo Save Database")) {
                try {
                    saved.DatabaseUrl = new URI("https://bucanero.github.io/apollo-saves/").toURL();
                    saved.Save();
                } catch (MalformedURLException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            } else {
                setVisible(false);
            }
        }
    }

    public void reloadDatabase(URL url, String searchTerm) {
        if (currentThread != null) {
            try {
                currentThread.cancel(true);
                if (currentReader != null)
                    currentReader.close();
            } catch (Exception e) {
                ErrorShower.full(this, e);
            }
        }

        databaseListingInnerPanel.removeAll();
        databaseListingInnerPanel.repaint();
        if (currentMenu.equals("root")) {
            currentThread = executorService.submit(
                    () -> {
                        Component comp = null;
                        if (searchTerm.trim().isEmpty())
                            return;
                        try (BufferedReader buff = new BufferedReader(
                                new InputStreamReader(
                                        new URI(url.toString() + "/PSP/games.txt").toURL().openStream()))) {
                            currentReader = buff;
                            String currentLine;
                            while ((currentLine = currentReader.readLine()) != null
                                    && !Thread.currentThread().isInterrupted()) {
                                String[] keyandval = currentLine.split("=");

                                if (!keyandval[1].trim().toLowerCase().contains(searchTerm.toLowerCase().trim())
                                        && !keyandval[0].trim().toLowerCase()
                                        .contains(searchTerm.toLowerCase().trim())) {
                                    continue;
                                }

                                URL GameIconUrl = new URI(url + "/PSP/" + keyandval[0] + "/ICON0.PNG")
                                        .toURL();
                                System.out.println(keyandval[0] + "=" + keyandval[1] + "\n" + searchTerm);

                                if (Thread.currentThread().isInterrupted())
                                    break;

                                comp = databaseListingInnerPanel.add(new ParamSFOListElement(
                                        keyandval[1],
                                        keyandval[0],
                                        GameIconUrl.openStream().readAllBytes(),
                                        this));
                                if (Thread.currentThread().isInterrupted())
                                    databaseListingInnerPanel.remove(comp);

                                databaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                            }
                        } catch (Exception e) {
                            if (e instanceof IOException) {
                                if (comp != null)
                                    databaseListingInnerPanel.remove(comp);
                                return;
                            }

                            ErrorShower.full(this, e);
                        }
                    });
        } else {
            currentThread = executorService.submit(
                    () -> {
                        Component comp = null;
                        try (BufferedReader buff = new BufferedReader(
                                new InputStreamReader(new URI(url.toString() + "/PSP/" + currentMenu + "/saves.txt")
                                        .toURL().openStream()))) {
                            currentReader = buff;
                            String currentLine;
                            while ((currentLine = currentReader.readLine()) != null
                                    && !Thread.currentThread().isInterrupted()) {
                                String[] keyandval = currentLine.split("=");

                                if (!keyandval[1].trim().toLowerCase().contains(searchTerm.toLowerCase().trim())
                                        && !keyandval[0].trim().toLowerCase()
                                        .contains(searchTerm.toLowerCase().trim())) {
                                    continue;
                                }

                                URL GameIconUrl = new URI(url + "/PSP/" + currentMenu + "/ICON0.PNG")
                                        .toURL();

                                if (Thread.currentThread().isInterrupted())
                                    break;

                                comp = databaseListingInnerPanel.add(new ParamSFOListElement(
                                        keyandval[1],
                                        keyandval[0],
                                        GameIconUrl.openStream().readAllBytes(),
                                        this));

                                if (Thread.currentThread().isInterrupted())
                                    databaseListingInnerPanel.remove(comp);

                                databaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                            }
                        } catch (Exception e) {
                            if (e instanceof IOException) {
                                if (comp != null)
                                    databaseListingInnerPanel.remove(comp);
                                return;
                            }
                            ErrorShower.full(this, e);
                        }
                    });
        }

    }

    @Override
    public void selected(ParamSFOListElement selectedElement) {
        if (currentMenu.equals("root")) {
            currentMenu = selectedElement.getDescription();
            databaseSearch.setText("");
            reloadDatabase(SavedVariables.Load().DatabaseUrl, databaseSearch.getText());
        } else {
            Object choice = JOptionPane.showInputDialog(this,
                    "<html>How would you like to install the save,<br><b>" + selectedElement.getTitle()
                            + "</b>, for game "
                            + currentMenu + "?</html>",
                    "Install save: " + selectedElement.getTitle(), JOptionPane.QUESTION_MESSAGE, null,
                    new String[] { "To PSP", "To Folder", "To Zip" }, "To PSP");
            if (choice == null)
                return;
            LoadingScreen loading = new LoadingScreen(this);
            SwingUtilities.invokeLater(() -> {
                loading.setVisible(true);
            });
            new Thread(() -> {
                try {
                    URL zipUrl = new URI(SavedVariables.Load().DatabaseUrl + "/PSP/" + currentMenu + "/"
                            + selectedElement.getDescription()).toURL();
                    System.out.println("Downloading save: " + zipUrl);
                    InputStream stream = zipUrl.openStream();
                    File zipFile = File.createTempFile("PSPTOOLS", "TEMPSAVE.zip");
                    zipFile.deleteOnExit();
                    ZipUnArchiver zipUnArchiver = new ZipUnArchiver(zipFile);

                    Files.write(zipFile.toPath(), stream.readAllBytes());
                    stream.close();

                    SystemFileChooser chooser = new SystemFileChooser();
                    switch (choice.toString()) {
                        case "To Zip":
                            chooser.setDialogType(SystemFileChooser.SAVE_DIALOG);
                            chooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
                            chooser.setSelectedFile(new File("test.zip"));
                            chooser.showOpenDialog(loading);

                            File file = chooser.getSelectedFile();
                            if (file == null) {
                                loading.setVisible(false);
                                return;
                            }

                            FileUtils.copyFile(zipFile, file);

                            loading.setVisible(false);
                            JOptionPane.showMessageDialog(null,
                                    "The save has been downloaded.");
                            break;
                        case "To Folder":
                            chooser.setDialogType(SystemFileChooser.SAVE_DIALOG);
                            chooser.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
                            chooser.showOpenDialog(loading);

                            File directory = chooser.getSelectedFile();
                            if (directory == null) {
                                loading.setVisible(false);
                                return;
                            }

                            zipUnArchiver.setDestDirectory(directory);
                            zipUnArchiver.extract();
                            loading.setVisible(false);
                            JOptionPane.showMessageDialog(null,
                                    "The save has been downloaded.");

                            break;
                        case "To PSP":
                            if (!PSP.getCurrentPSP().pspActive()) {
                                int option2 = JOptionPane.showConfirmDialog(loading,
                                        "No PSP is selected, but is required.\nSelect one?",
                                        "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

                                if (option2 == JOptionPane.YES_OPTION) {
                                    PSP.setCurrentPSP(PSPSelectionUI.getNewPSP(null));

                                    File tempFile = File.createTempFile("PSPTOOLS", "TEMPSAVE");
                                    zipUnArchiver.setDestFile(tempFile);
                                    zipUnArchiver.extract();
                                    PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA").addFile(tempFile);
                                    tempFile.delete();

                                    loading.setVisible(false);
                                    JOptionPane.showMessageDialog(loading,
                                            "The save has been downloaded.");

                                } else {
                                    loading.setVisible(false);
                                    return;
                                }
                            } else {

                                /*
                                 * zipUnArchiver
                                 * .setDestDirectory(.toFile());
                                 * zipUnArchiver.extract();
                                 */
                                File tempFile = File.createTempFile("PSPTOOLS", "TEMPSAVE");
                                zipUnArchiver.setDestFile(tempFile);
                                zipUnArchiver.extract();
                                PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA").addFile(tempFile);
                                tempFile.delete();

                                loading.setVisible(false);
                                JOptionPane.showMessageDialog(null,
                                        "The save has been downloaded.");

                            }
                            break;

                        default:
                            break;
                    }
                    zipFile.delete();
                } catch (Exception e) {
                    ErrorShower.full(this, e);
                    loading.setVisible(false);
                    JOptionPane.showMessageDialog(null,
                            "Failed to download save: " + e.getMessage());
                }
            }).start();
        }
    }

    @Override
    public void backup() {
    }

    @Override
    public void restore() {
    }

    @Override
    public void delete(ParamSFOListElement selectedElement) {
    }

    @Override
    public void onThreadCreate(Thread thread) {}

}
