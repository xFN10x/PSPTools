package psptools.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.progress.ProgressMonitor.State;
import psptools.psp.PSP;
import psptools.psp.PSPSelectionUI;
import psptools.ui.components.ParamSFOListElement;
import psptools.ui.interfaces.SFOListElementListiener;
import psptools.util.SavedVariables;

public class SaveTools extends JFrame implements SFOListElementListiener {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final JTextField databaseSearch = new JTextField();
    private final JPanel databasePanel = new JPanel();
    private final JButton databaseBack = new JButton("< Back");
    private final JPanel databaseListingInnerPanel = new JPanel();
    private final JScrollPane databaseListingPanel = new JScrollPane(databaseListingInnerPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final JPanel PatchPanel = new JPanel();
    private final JTabbedPane PatchTabs = new JTabbedPane();

    private final JPanel SetupPanel = new JPanel();
    private final JButton downloadButton = new JButton("Download patches...");
    private final JLabel lastDownloadedStatus = new JLabel("Last updated: Never");
    private final JButton setupButton = new JButton("Setup");

    private final JPanel PSPPatchPanel = new JPanel();
    private final JButton SelectPSPSave = new JButton("Select...");
    private final JLabel SaveIcon = new JLabel();
    private final JButton SaveName = new JButton("Save Name");
    private final JButton SaveGameName = new JButton("Game Name");

    private final SpringLayout Lay = new SpringLayout();
    private final SpringLayout Lay2 = new SpringLayout();
    private final SpringLayout Lay3 = new SpringLayout();
    private final BoxLayout Lay4 = new BoxLayout(SetupPanel, BoxLayout.Y_AXIS);
    private final SpringLayout Lay5 = new SpringLayout();

    private Future<?> currentThread;
    private BufferedReader currentReader;
    private String currentMenu = "root";
    // private volatile Future<?> currentThread;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SaveTools(Frame parent) {
        super("Save Manager");

        SavedVariables saved = SavedVariables.Load();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                parent.setVisible(true);
                if (currentThread != null)
                    currentThread.cancel(true);
                System.gc();
            }

        });

        setResizable(false);
        setLayout(Lay);
        setSize(new Dimension(350, 500));

        tabbedPane.addTab("Save Database", databasePanel);
        tabbedPane.addTab("Save Patching", PatchPanel);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        Lay.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, tabbedPane, 0, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, getContentPane());

        Lay.putConstraint(SpringLayout.NORTH, databasePanel, 0, SpringLayout.SOUTH, tabbedPane);
        Lay.putConstraint(SpringLayout.WEST, databasePanel, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, databasePanel, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, databasePanel, 0, SpringLayout.SOUTH, getContentPane());

        Lay.putConstraint(SpringLayout.NORTH, PatchPanel, 0, SpringLayout.SOUTH, tabbedPane);
        Lay.putConstraint(SpringLayout.WEST, PatchPanel, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, PatchPanel, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, PatchPanel, 0, SpringLayout.SOUTH, getContentPane());

        databaseListingInnerPanel.setLayout(new BoxLayout(databaseListingInnerPanel, BoxLayout.Y_AXIS));

        add(tabbedPane);

        // #region datapase panel
        databasePanel.setLayout(Lay2);

        Lay2.putConstraint(SpringLayout.NORTH, databaseSearch, 10, SpringLayout.NORTH, databasePanel);
        // Lay2.putConstraint(SpringLayout.SOUTH, databaseSearch, -40,
        // SpringLayout.SOUTH, databasePanel);
        Lay2.putConstraint(SpringLayout.EAST, databaseSearch, -10, SpringLayout.EAST, databasePanel);
        Lay2.putConstraint(SpringLayout.WEST, databaseSearch, 10, SpringLayout.WEST, databasePanel);

        Lay2.putConstraint(SpringLayout.NORTH, databaseListingPanel, 5, SpringLayout.SOUTH, databaseSearch);
        Lay2.putConstraint(SpringLayout.SOUTH, databaseListingPanel, -40, SpringLayout.SOUTH, databasePanel);
        Lay2.putConstraint(SpringLayout.EAST, databaseListingPanel, -10, SpringLayout.EAST, databasePanel);
        Lay2.putConstraint(SpringLayout.WEST, databaseListingPanel, 10, SpringLayout.WEST, databasePanel);

        Lay2.putConstraint(SpringLayout.NORTH, databaseBack, 5, SpringLayout.SOUTH, databaseListingPanel);
        Lay2.putConstraint(SpringLayout.SOUTH, databaseBack, -5, SpringLayout.SOUTH, databasePanel);
        Lay2.putConstraint(SpringLayout.EAST, databaseBack, -10, SpringLayout.EAST, databasePanel);
        Lay2.putConstraint(SpringLayout.WEST, databaseBack, 10, SpringLayout.WEST, databasePanel);

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
                new Thread(() -> {
                    loading.setVisible(true);
                }).start();

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
                ZipFile zip = new ZipFile(tempFile);
                ProgressMonitor promon = zip.getProgressMonitor();
                zip.setRunInThread(true);
                new Thread(() -> {
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        private boolean canCancel = false;
                        private boolean Option = false;

                        @Override
                        public void run() {
                            if (promon.getState() == State.BUSY) {
                                canCancel = true;
                            }
                            if (promon.getState() == State.READY && canCancel) {

                                try {
                                    if (option.toString().equals("PSP + PS3") && !Option) {
                                        Option = true;
                                        zip.extractFile("apollo-patches-main/PS3/",
                                                Path.of(SavedVariables.DataFolder.toString(), "Patches").toString(),
                                                "PS3");
                                        return;
                                    }

                                    loading.setVisible(false);
                                    timer.cancel();
                                    zip.close();
                                    System.gc();
                                    SavedVariables vars = SavedVariables.Load();
                                    vars.SinceLastPatchUpdate = Calendar.getInstance().getTime();
                                    vars.Save();

                                    tempFile.delete();

                                    JOptionPane.showMessageDialog(null, "Patchs have been downloaded.");
                                    return;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            SwingUtilities.invokeLater(() -> {
                                loading.setProgress(promon.getPercentDone());
                            });
                        }

                    }, 5, 1);
                }).start();
                System.out.println(Path.of(SavedVariables.DataFolder.toString(), "Patches", "PSP").toString());
                switch (option.toString()) {
                    case "PSP":
                        try {
                            zip.extractFile("apollo-patches-main/PSP/",
                                    Path.of(SavedVariables.DataFolder.toString(), "Patches").toString(), "PSP");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "PS3":
                        try {
                            zip.extractFile("apollo-patches-main/PS3/",
                                    Path.of(SavedVariables.DataFolder.toString(), "Patches").toString(), "PSP");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    default:
                        try {
                            zip.extractFile("apollo-patches-main/PSP/",
                                    Path.of(SavedVariables.DataFolder.toString(), "Patches").toString(), "PSP");

                            // download psp in the other thread
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                }

            } catch (Exception e) {
                e.printStackTrace();
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
        // #endregion
        // #region setup PSP patching tab
        PSPPatchPanel.setLayout(Lay5);

        Lay5.putConstraint(SpringLayout.NORTH, SelectPSPSave, 5, SpringLayout.NORTH, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.EAST, SelectPSPSave, -5, SpringLayout.EAST, PSPPatchPanel);
        Lay5.putConstraint(SpringLayout.WEST, SelectPSPSave, 5, SpringLayout.WEST, PSPPatchPanel);

        PSPPatchPanel.add(SelectPSPSave);
        // #endregion
        Lay4.layoutContainer(SetupPanel);

        PatchTabs.setTabPlacement(JTabbedPane.LEFT);
        PatchTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        PatchTabs.addTab("Setup", SetupPanel);
        PatchTabs.addTab("PSP", PSPPatchPanel);

        PatchPanel.setLayout(Lay3);

        Lay3.putConstraint(SpringLayout.WEST, PatchTabs, 0, SpringLayout.WEST, PatchPanel);
        Lay3.putConstraint(SpringLayout.SOUTH, PatchTabs, 0, SpringLayout.SOUTH, PatchPanel);
        Lay3.putConstraint(SpringLayout.NORTH, PatchTabs, 0, SpringLayout.NORTH, PatchPanel);
        Lay3.putConstraint(SpringLayout.EAST, PatchTabs, 0, SpringLayout.EAST, PatchPanel);

        PatchPanel.add(PatchTabs);
         // #endregion

        setLocation(LaunchPage.getScreenCenter(this));
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        parent.setVisible(false);

        if (saved.DatabaseUrl == null) {
            String choice = JOptionPane.showInputDialog(this, "There is no save database selected.\nPlease Select one.",
                    "Select Save Database", JOptionPane.INFORMATION_MESSAGE, null,
                    new String[] { "Apollo Save Database" }, "Apollo Save Database").toString();
            switch (choice) {
                case "Apollo Save Database":
                    try {
                        saved.DatabaseUrl = new URI("https://bucanero.github.io/apollo-saves/").toURL();
                        saved.Save();
                    } catch (MalformedURLException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                    break;

                default:
                    setVisible(false);
                    return;
            }
        }
    }

    public void reloadDatabase(URL url, String searchTerm) {
        System.gc();

        if (currentThread != null) {
            System.out.println(currentThread.cancel(true));
            try {
                currentReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        databaseListingInnerPanel.removeAll();
        databaseListingInnerPanel.repaint();

        switch (currentMenu) {
            case "root":
                currentThread = executorService.submit(
                        () -> {
                            if (searchTerm.trim().equals(""))
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

                                    URL GameIconUrl = new URI(url.toString() + "/PSP/" + keyandval[0] + "/ICON0.PNG")
                                            .toURL();
                                    System.out.println(keyandval[0] + "=" + keyandval[1] + "\n" + searchTerm);

                                    if (Thread.currentThread().isInterrupted())
                                        break;

                                    Component comp = databaseListingInnerPanel.add(new ParamSFOListElement(
                                            keyandval[1],
                                            keyandval[0],
                                            GameIconUrl.openStream().readAllBytes(),
                                            this));
                                    if (Thread.currentThread().isInterrupted())
                                        databaseListingInnerPanel.remove(comp);

                                    databaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                                }
                            } catch (Exception e) {
                                if (e instanceof IOException)
                                    return;

                                e.printStackTrace();
                            }
                        });
                break;

            default:
                currentThread = executorService.submit(
                        () -> {
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

                                    URL GameIconUrl = new URI(url.toString() + "/PSP/" + currentMenu + "/ICON0.PNG")
                                            .toURL();

                                    if (Thread.currentThread().isInterrupted())
                                        break;

                                    databaseListingInnerPanel.add(new ParamSFOListElement(
                                            keyandval[1],
                                            keyandval[0],
                                            GameIconUrl.openStream().readAllBytes(),
                                            this));

                                    databaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                                }
                            } catch (Exception e) {
                                if (e instanceof IOException)
                                    return;
                                e.printStackTrace();
                            }
                        });
                break;
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
                    "Install save: " + selectedElement.getTitle(), JOptionPane.INFORMATION_MESSAGE, null,
                    new String[] { "To PSP", "To Folder", "Select..." }, "To PSP");
            if (choice == null)
                return;
            switch (choice.toString()) {
                case "To PSP":
                    if (!PSP.getCurrentPSP().pspActive()) {
                        int option2 = JOptionPane.showConfirmDialog(this,
                                "No PSP is selected, but is required.\nSelect one?",
                                "PSP Selection Confirm", JOptionPane.YES_NO_OPTION);

                        if (option2 == JOptionPane.YES_OPTION) {
                            PSP.setCurrentPSP(PSPSelectionUI.getNewPSP(this));
                            try {
                                URL zipUrl = new URI(SavedVariables.Load().DatabaseUrl + "/PSP/" + currentMenu + "/"
                                        + selectedElement.getDescription()).toURL();
                                System.out.println(zipUrl);
                                InputStream stream = zipUrl.openStream();
                                File zipFile = File.createTempFile("PSPTOOLS", "TEMPSAVE.zip");
                                Files.write(zipFile.toPath(), stream.readAllBytes());
                                stream.close();

                                ZipFile save = new ZipFile(zipFile);
                                ProgressMonitor promon = save.getProgressMonitor();
                                LoadingScreen loading = new LoadingScreen(this);
                                SwingUtilities.invokeLater(() -> {
                                    loading.setVisible(true);
                                });
                                save.setRunInThread(true);
                                new Thread(() -> {
                                    Timer timer = new Timer();
                                    timer.scheduleAtFixedRate(new TimerTask() {
                                        private boolean canCancel = false;

                                        @Override
                                        public void run() {
                                            if (promon.getState() == State.BUSY) {
                                                canCancel = true;
                                            }
                                            if (promon.getState() == State.READY && canCancel) {
                                                try {
                                                    loading.setVisible(false);
                                                    timer.cancel();
                                                    save.close();
                                                    System.gc();
                                                    JOptionPane.showMessageDialog(null,
                                                            "The save has been downloaded.");
                                                    return;
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            SwingUtilities.invokeLater(() -> {
                                                loading.setProgress(promon.getPercentDone());
                                            });
                                        }

                                    }, 5, 1);
                                }).start();

                                save.extractAll(PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA").toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            return;
                    } else {
                        try {
                            URL zipUrl = new URI(SavedVariables.Load().DatabaseUrl + "/PSP/" + currentMenu + "/"
                                    + selectedElement.getDescription()).toURL();
                            System.out.println(zipUrl);
                            InputStream stream = zipUrl.openStream();
                            File zipFile = File.createTempFile("PSPTOOLS", "TEMPSAVE.zip");
                            Files.write(zipFile.toPath(), stream.readAllBytes());
                            stream.close();

                            ZipFile save = new ZipFile(zipFile);
                            ProgressMonitor promon = save.getProgressMonitor();
                            LoadingScreen loading = new LoadingScreen(this);
                            SwingUtilities.invokeLater(() -> {
                                loading.setVisible(true);
                            });
                            save.setRunInThread(true);
                            new Thread(() -> {
                                Timer timer = new Timer();
                                timer.scheduleAtFixedRate(new TimerTask() {
                                    private boolean canCancel = false;

                                    @Override
                                    public void run() {
                                        if (promon.getState() == State.BUSY) {
                                            canCancel = true;
                                        }
                                        if (promon.getState() == State.READY && canCancel) {
                                            try {
                                                loading.setVisible(false);
                                                timer.cancel();
                                                save.close();
                                                System.gc();
                                                JOptionPane.showMessageDialog(null, "The save has been downloaded.");
                                                return;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        SwingUtilities.invokeLater(() -> {
                                            loading.setProgress(promon.getPercentDone());
                                        });
                                    }

                                }, 5, 1);
                            }).start();

                            save.extractAll(PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                default:
                    break;
            }
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

}
