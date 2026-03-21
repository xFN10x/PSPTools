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
import fn10.psptools.PSPTools;
import fn10.psptools.psp.PSP;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListener;
import fn10.psptools.util.AtomicActionListener;
import fn10.psptools.util.ErrorShower;
import fn10.psptools.util.ImageUtilites;
import fn10.psptools.util.VimmDownloader;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatabaseTools extends JFrame implements SFOListElementListener {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    public URL DatabaseUrl = new URI("https://bucanero.github.io/apollo-saves/").toURL();

    private String currentMenu = "root";
    private final JTextField saveDatabaseSearch = new JTextField();
    private final JPanel saveDatabasePanel = new JPanel();
    private final JButton saveDatabaseBack = new JButton("< Back");
    private final JLabel saveDatabasePath = new JLabel(currentMenu) {
        @Override
        protected void paintComponent(Graphics g) {
            setText(currentMenu);
            super.paintComponent(g);
        }
    };
    private final JPanel saveDatabaseListingInnerPanel = new JPanel();
    private final JScrollPane saveDatabaseListingPanel = new JScrollPane(saveDatabaseListingInnerPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private static final JPanel gameDatabasePanel = new JPanel();

    private static final JPanel gameDatabaseInnerDetailsPanel = new JPanel();
    private static final JLabel gameDiscCoverImg = new JLabel(new ImageIcon(DatabaseTools.class.getResource("/no_cover.png")));
    private static final JLabel gameTitle = new JLabel();
    private static final JTable gameDetails = new JTable();

    private static final JButton downloadExternalButton = new JButton("Download to File");
    private static final JButton downloadButton = new JButton("Download to PSP");
    private static AtomicActionListener downloadAction = new AtomicActionListener(null);

    private static final Dimension coverSize = new Dimension(174, 300);
    private static final JTabbedPane gameDatabaseBrowserPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
    private static Map<Character, List<VimmDownloader.VimmGame>> games = null;

    private final SpringLayout GameDatabaseLay = new SpringLayout();

    private Future<?> currentThread;
    private BufferedReader currentReader;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static boolean loadedGames = false;

    public DatabaseTools(Frame parent) throws MalformedURLException, URISyntaxException {
        super("Database Tools");

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

        //setResizable(false);
        SpringLayout contentPaneLay = new SpringLayout();
        setLayout(contentPaneLay);
        setSize(new Dimension(700, 500));

        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabbedPane);

        tabbedPane.addChangeListener(_ -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                if (!loadedGames)
                    loadGameDatabase();
            }
        });

        contentPaneLay.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.NORTH, getContentPane());
        contentPaneLay.putConstraint(SpringLayout.SOUTH, tabbedPane, 0, SpringLayout.SOUTH, getContentPane());
        contentPaneLay.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, getContentPane());
        contentPaneLay.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, getContentPane());

        //#region save database
        tabbedPane.addTab("Save Database", saveDatabasePanel);

        saveDatabaseListingInnerPanel.setLayout(new BoxLayout(saveDatabaseListingInnerPanel, BoxLayout.Y_AXIS));

        SpringLayout saveDatabaseLay = new SpringLayout();
        saveDatabasePanel.setLayout(saveDatabaseLay);

        saveDatabaseLay.putConstraint(SpringLayout.NORTH, saveDatabaseSearch, 10, SpringLayout.NORTH, saveDatabasePanel);
        // Lay2.putConstraint(SpringLayout.SOUTH, databaseSearch, -40,
        // SpringLayout.SOUTH, databasePanel);
        saveDatabaseLay.putConstraint(SpringLayout.EAST, saveDatabaseSearch, -10, SpringLayout.EAST, saveDatabasePanel);
        saveDatabaseLay.putConstraint(SpringLayout.WEST, saveDatabaseSearch, 10, SpringLayout.WEST, saveDatabasePanel);

        saveDatabaseLay.putConstraint(SpringLayout.NORTH, saveDatabasePath, 5, SpringLayout.SOUTH, saveDatabaseSearch);
        saveDatabaseLay.putConstraint(SpringLayout.WEST, saveDatabasePath, 0, SpringLayout.WEST, saveDatabaseSearch);

        saveDatabaseLay.putConstraint(SpringLayout.NORTH, saveDatabaseListingPanel, 5, SpringLayout.SOUTH, saveDatabasePath);
        saveDatabaseLay.putConstraint(SpringLayout.SOUTH, saveDatabaseListingPanel, -40, SpringLayout.SOUTH, saveDatabasePanel);
        saveDatabaseLay.putConstraint(SpringLayout.EAST, saveDatabaseListingPanel, -10, SpringLayout.EAST, saveDatabasePanel);
        saveDatabaseLay.putConstraint(SpringLayout.WEST, saveDatabaseListingPanel, 10, SpringLayout.WEST, saveDatabasePanel);

        saveDatabaseLay.putConstraint(SpringLayout.NORTH, saveDatabaseBack, 5, SpringLayout.SOUTH, saveDatabaseListingPanel);
        saveDatabaseLay.putConstraint(SpringLayout.SOUTH, saveDatabaseBack, -5, SpringLayout.SOUTH, saveDatabasePanel);
        saveDatabaseLay.putConstraint(SpringLayout.EAST, saveDatabaseBack, -10, SpringLayout.EAST, saveDatabasePanel);
        saveDatabaseLay.putConstraint(SpringLayout.WEST, saveDatabaseBack, 10, SpringLayout.WEST, saveDatabasePanel);
        saveDatabaseListingPanel.getVerticalScrollBar().setUnitIncrement(18);
        saveDatabaseListingInnerPanel.add(new JLabel("Search by ID (e.g. ULUS03410) or by name (e.g. METAL GEAR SOLID)"));

        saveDatabaseSearch.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadSaveDatabase(DatabaseUrl, saveDatabaseSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadSaveDatabase(DatabaseUrl, saveDatabaseSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadSaveDatabase(DatabaseUrl, saveDatabaseSearch.getText());
            }

        });

        saveDatabaseBack.addActionListener(ac -> {
            if (!currentMenu.equals("root")) {
                currentMenu = "root";
                saveDatabaseSearch.setText("");
                reloadSaveDatabase(DatabaseUrl, "");
                saveDatabaseListingInnerPanel
                        .add(new JLabel("Search by ID (e.g. ULUS03410) or by name (e.g. METAL GEAR SOLID)"));
            }
        });

        saveDatabasePanel.add(saveDatabaseSearch);
        saveDatabasePanel.add(saveDatabaseListingPanel);
        saveDatabasePanel.add(saveDatabaseBack);
        saveDatabasePanel.add(saveDatabasePath);
        //#endregion

        //#region game database
        tabbedPane.addTab("Game Database", gameDatabasePanel);
        SpringLayout gameDatabasePanelLay = new SpringLayout();
        gameDatabasePanel.setLayout(gameDatabasePanelLay);

        gameDatabasePanelLay.putConstraint(SpringLayout.NORTH, gameDatabaseBrowserPane, 0, SpringLayout.NORTH, gameDatabasePanel);
        gameDatabasePanelLay.putConstraint(SpringLayout.SOUTH, gameDatabaseBrowserPane, 0, SpringLayout.SOUTH, gameDatabasePanel);
        gameDatabasePanelLay.putConstraint(SpringLayout.WEST, gameDatabaseBrowserPane, 0, SpringLayout.WEST, gameDatabasePanel);
        gameDatabasePanelLay.putConstraint(SpringLayout.EAST, gameDatabaseBrowserPane, -10, SpringLayout.HORIZONTAL_CENTER, gameDatabasePanel);

        gameDatabasePanelLay.putConstraint(SpringLayout.NORTH, gameDatabaseInnerDetailsPanel, 0, SpringLayout.NORTH, gameDatabasePanel);
        gameDatabasePanelLay.putConstraint(SpringLayout.SOUTH, gameDatabaseInnerDetailsPanel, 0, SpringLayout.SOUTH, gameDatabasePanel);
        gameDatabasePanelLay.putConstraint(SpringLayout.EAST, gameDatabaseInnerDetailsPanel, 0, SpringLayout.EAST, gameDatabasePanel);
        gameDatabasePanelLay.putConstraint(SpringLayout.WEST, gameDatabaseInnerDetailsPanel, 10, SpringLayout.HORIZONTAL_CENTER, gameDatabasePanel);

        SpringLayout gameInnerDatabasePanelLay = new SpringLayout();
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.WEST, gameDiscCoverImg, 5, SpringLayout.WEST, gameDatabaseInnerDetailsPanel);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.NORTH, gameDiscCoverImg, 5, SpringLayout.NORTH, gameDatabaseInnerDetailsPanel);

        gameInnerDatabasePanelLay.putConstraint(SpringLayout.WEST, gameTitle, 0, SpringLayout.WEST, gameDiscCoverImg);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.NORTH, gameTitle, 5, SpringLayout.SOUTH, gameDiscCoverImg);

        gameInnerDatabasePanelLay.putConstraint(SpringLayout.NORTH, gameDetails, 0, SpringLayout.NORTH, gameDiscCoverImg);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.WEST, gameDetails, 5, SpringLayout.EAST, gameDiscCoverImg);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.EAST, gameDetails, -5, SpringLayout.EAST, gameDatabaseInnerDetailsPanel);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.SOUTH, gameDetails, 0, SpringLayout.SOUTH, gameDiscCoverImg);

        gameInnerDatabasePanelLay.putConstraint(SpringLayout.WEST, downloadButton, 10, SpringLayout.WEST, gameDatabaseInnerDetailsPanel);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.EAST, downloadButton, -10, SpringLayout.EAST, gameDatabaseInnerDetailsPanel);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.SOUTH, downloadButton, -10, SpringLayout.SOUTH, gameDatabaseInnerDetailsPanel);

        gameInnerDatabasePanelLay.putConstraint(SpringLayout.WEST, downloadExternalButton, 0, SpringLayout.WEST, downloadButton);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.EAST, downloadExternalButton, 0, SpringLayout.EAST, downloadButton);
        gameInnerDatabasePanelLay.putConstraint(SpringLayout.SOUTH, downloadExternalButton, -5, SpringLayout.NORTH, downloadButton);

        gameDatabaseInnerDetailsPanel.setLayout(gameInnerDatabasePanelLay);
        gameDatabaseInnerDetailsPanel.add(gameDiscCoverImg);
        gameDatabaseInnerDetailsPanel.add(gameTitle);
        gameTitle.setFont(gameDiscCoverImg.getFont().deriveFont(Font.BOLD, 18));
        gameDatabaseInnerDetailsPanel.add(gameDetails);
        gameDatabaseInnerDetailsPanel.add(downloadButton);
        gameDatabaseInnerDetailsPanel.add(downloadExternalButton);

        downloadExternalButton.setActionCommand("extern");
        downloadExternalButton.addActionListener(downloadAction);
        downloadButton.addActionListener(downloadAction);

        gameDatabasePanel.add(gameDatabaseBrowserPane);
        gameDatabasePanel.add(gameDatabaseInnerDetailsPanel);
        //#endregion

        setLocation(LaunchPage.getScreenCenter(this));
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        parent.setVisible(false);
    }

    public void loadGameDatabase() {
        LoadingScreen loadingScreen = new LoadingScreen(this);
        loadingScreen.showWhenPossible();
        new Thread(() -> {
            try {
                loadingScreen.changeText("Downloading database...");
                VimmDownloader vimmDownloader = VimmDownloader.of();
                games = vimmDownloader.getAllGames(loadingScreen);
                loadedGames = true;

                char[] letters = "#ABCDEFGHIJKLMNOPQRSTUVWXZY".toCharArray();
                for (char letter : letters) {
                    JList<VimmDownloader.VimmGame> gameList = new JList<>();
                    gameList.setListData(games.get(letter).toArray(new VimmDownloader.VimmGame[0]));
                    gameList.addListSelectionListener(_ -> {
                        LoadingScreen ls = new LoadingScreen(this);
                        ls.changeText("Loading game info...");
                        ls.showWhenPossible();
                        Thread thread = new Thread(() -> {
                            try {
                                VimmDownloader.VimmGameDetails details = vimmDownloader.getDetailsFromRomID(gameList.getSelectedValue().gameID());
                                gameDiscCoverImg.setIcon(ImageUtilites.ResizeIcon(new ImageIcon(details.img()), coverSize));
                                gameTitle.setText(details.title());
                                ArrayList<String[]> list = new ArrayList<>();
                                for (Map.Entry<String, String> entry : details.details().entrySet()) {
                                    list.add(new String[]{entry.getKey(), entry.getValue()});
                                }
                                gameDetails.setModel(new DefaultTableModel(list.toArray(new String[0][0]), new String[]{"Key", "Value"}));

                                downloadAction.setListener(ac -> {
                                    new Thread(() -> {
                                        if (ac.getActionCommand().equalsIgnoreCase("extern")) {
                                            LoadingScreen ls2 = new LoadingScreen(this);
                                            try {
                                                System.out.println(details.isoFileName());
                                                SystemFileChooser chooser = new SystemFileChooser();
                                                chooser.setSelectedFile(new File("~/" + details.isoFileName()));
                                                chooser.setFileFilter(new SystemFileChooser.FileNameExtensionFilter("ISOs", "iso"));
                                                SwingUtilities.invokeAndWait(() -> {
                                                    chooser.showSaveDialog(this);
                                                });
                                                File selectedFile = chooser.getSelectedFile();
                                                if (selectedFile == null) return;
                                                try (HttpClient client = HttpClient.newHttpClient()) {
                                                    ls2.showWhenPossible();
                                                    ls2.changeText("Starting Download...");
                                                    HttpRequest req = vimmDownloader.getDownloadRequestFromRomID(gameList.getSelectedValue().gameID());
                                                    HttpResponse<InputStream> downloaded = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
                                                    if (downloaded.statusCode() != 200)
                                                        throw new HttpStatusException("Not OK", downloaded.statusCode(), req.uri().toString());

                                                    Thread thread1 = new Thread(() -> {
                                                        FileOutputStream output = null;
                                                        try (InputStream body = downloaded.body()) {
                                                            PSPTools.log.info(downloaded.request().headers());
                                                            PSPTools.log.info(downloaded.version());
                                                            PSPTools.log.info(downloaded.headers());
                                                            final int size = Integer.parseInt(downloaded.headers().firstValue("Content-Length").get());
                                                            byte[] read = {};
                                                            int i = 0;

                                                            Path file = Path.of(chooser.getSelectedFile().getAbsolutePath() + ".7z");
                                                            if (file.toFile().exists())
                                                                file.toFile().delete();
                                                            Files.createFile(file);
                                                            output = FileUtils.openOutputStream(file.toFile());

                                                            while ((read = body.readNBytes(1000000)).length != 0) {
                                                                i += read.length;
                                                                float totalPercent = (float) i / size;
                                                                PSPTools.log.info("Read {} bytes.", read.length);
                                                                output.write(read);
                                                                ls2.changeText("Downloading... (" + (totalPercent * 100) + "%)");
                                                                ls2.setProgress((int) (totalPercent * 100));
                                                            }
                                                            SevenZFile zip = SevenZFile.builder().setFile(file.toFile()).get();
                                                            ls2.MainBar.setIndeterminate(true);
                                                            ls2.changeText("Unzipping...");
                                                            SevenZArchiveEntry entry = zip.getNextEntry();
                                                            while (entry != null) {
                                                                PSPTools.log.info("Unzipping: {}...", entry.getName());
                                                                if (entry.getName().equalsIgnoreCase(details.isoFileName())) {
                                                                    byte[] read2 = new byte[1000000];
                                                                    Path file2 = chooser.getSelectedFile().toPath();
                                                                    file2.toFile().createNewFile();
                                                                    FileOutputStream fileOutputStream = FileUtils.openOutputStream(file2.toFile());
                                                                    int i2 = 0;
                                                                    while (zip.read(read2, 0, 1000000) != -1) {
                                                                        i2 += read2.length;
                                                                        float totalPercent = (float) i2 / size;
                                                                        fileOutputStream.write(read2);
                                                                        if (i2 % 5 == 0) {
                                                                            ls2.changeText("Unzipping... (" + (totalPercent * 100) + "%)");
                                                                            ls2.setProgress((int) (totalPercent * 100));
                                                                        }
                                                                    }
                                                                    fileOutputStream.close();
                                                                }
                                                                entry = zip.getNextEntry();
                                                            }
                                                        } catch (Exception e) {
                                                            ErrorShower.full(this, "Failed to download game.", e);
                                                        } finally {
                                                            ls2.hideWhenPossible();
                                                        }
                                                    });
                                                    thread1.setPriority(Thread.MAX_PRIORITY);
                                                    thread1.start();
                                                } catch (Exception e) {
                                                    ErrorShower.full(this, e);
                                                }
                                            } catch (Exception e) {
                                                ErrorShower.full(this, e);
                                            }
                                        } else {

                                        }
                                    }).start();

                                });
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                ls.hideWhenPossible();
                            }
                        });
                        thread.start();
                    });
                    JScrollPane scroll = new JScrollPane(gameList);
                    scroll.getVerticalScrollBar().setUnitIncrement(12);
                    gameDatabaseBrowserPane.addTab(String.valueOf(letter), scroll);
                    GameDatabaseLay.putConstraint(SpringLayout.WEST, gameList, 0, SpringLayout.WEST, gameDatabaseBrowserPane);
                    GameDatabaseLay.putConstraint(SpringLayout.EAST, gameList, 0, SpringLayout.EAST, gameDatabaseBrowserPane);
                    GameDatabaseLay.putConstraint(SpringLayout.NORTH, gameList, 0, SpringLayout.NORTH, gameDatabaseBrowserPane);
                    GameDatabaseLay.putConstraint(SpringLayout.SOUTH, gameList, 0, SpringLayout.SOUTH, gameDatabaseBrowserPane);
                }

            } catch (Exception e) {
                ErrorShower.full(this, "Failed to load game database", e);
            } finally {
                loadingScreen.hideWhenPossible();
            }
        }).start();
    }

    public void reloadSaveDatabase(URL url, String searchTerm) {
        if (currentThread != null) {
            try {
                currentThread.cancel(true);
                if (currentReader != null)
                    currentReader.close();
            } catch (Exception e) {
                ErrorShower.full(this, e);
            }
        }

        saveDatabaseListingInnerPanel.removeAll();
        saveDatabaseListingInnerPanel.repaint();
        saveDatabasePath.repaint();
        if (currentMenu.equals("root")) {
            saveDatabaseBack.setEnabled(false);
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

                                comp = saveDatabaseListingInnerPanel.add(new ParamSFOListElement(
                                        keyandval[1],
                                        keyandval[0],
                                        GameIconUrl.openStream().readAllBytes(),
                                        this));
                                if (Thread.currentThread().isInterrupted())
                                    saveDatabaseListingInnerPanel.remove(comp);

                                saveDatabaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                            }
                        } catch (Exception e) {
                            if (e instanceof IOException) {
                                if (comp != null)
                                    saveDatabaseListingInnerPanel.remove(comp);
                                return;
                            }

                            ErrorShower.full(this, e);
                        }
                    });
        } else {
            saveDatabaseBack.setEnabled(true);
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

                                comp = saveDatabaseListingInnerPanel.add(new ParamSFOListElement(
                                        keyandval[1],
                                        keyandval[0],
                                        GameIconUrl.openStream().readAllBytes(),
                                        this));

                                if (Thread.currentThread().isInterrupted())
                                    saveDatabaseListingInnerPanel.remove(comp);

                                saveDatabaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                            }
                        } catch (Exception e) {
                            if (e instanceof IOException) {
                                if (comp != null)
                                    saveDatabaseListingInnerPanel.remove(comp);
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
            saveDatabaseSearch.setText("");
            reloadSaveDatabase(DatabaseUrl, saveDatabaseSearch.getText());
        } else {
            Object choice = JOptionPane.showInputDialog(this,
                    "<html>How would you like to install the save,<br><b>" + selectedElement.getTitle()
                            + "</b>, for game <b>"
                            + currentMenu + "</b>?</html>",
                    "Install save: " + selectedElement.getTitle(), JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"To PSP", "To Folder", "To Zip"}, "To PSP");
            if (choice == null)
                return;
            LoadingScreen loading = new LoadingScreen(this);
            SwingUtilities.invokeLater(() -> {
                loading.setVisible(true);
            });
            new Thread(() -> {
                try {
                    URL zipUrl = new URI(DatabaseUrl + "/PSP/" + currentMenu + "/"
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
    public void onThreadCreate(Thread thread) {
    }

}
