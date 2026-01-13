package fn10.psptools.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import javax.naming.NameNotFoundException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import com.formdev.flatlaf.ui.FlatLineBorder;

import fn10.psptools.psp.sfo.ParamSFO;
import fn10.psptools.psp.sfo.ParamSFO.Params;
import fn10.psptools.ui.components.MediaPlayer;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListener;
import fn10.psptools.ui.interfaces.VideoPlayingListener;
import fn10.psptools.util.ErrorShower;
import fn10.psptools.util.ImageUtilites;
import fn10.psptools.util.SavedVariables;

public class SFOBasedManager extends JFrame implements SFOListElementListener, VideoPlayingListener {

    public static final int SAVES_MODE = 0;
    public static final int GAMES_MODE = 1;
    public static final int SINGLE = 2;

    private static final Dimension Size = new Dimension(706, 392);

    protected final JPanel InnerSFOFolderViewer = new JPanel();
    protected final JScrollPane SFOFolderViewer = new JScrollPane(InnerSFOFolderViewer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final JPanel ViewBG = new JPanel();
    private final JLabel ViewingIcon = new JLabel();
    private final JLabel ViewingName = new JLabel("Save Title");
    private final JLabel ViewingSubDesc = new JLabel("Save Game");
    private final JLabel ViewingCategory = new JLabel("MS");
    private final JLabel ViewingDesc = new JLabel("Save Description");
    private final JButton OpenFolderButton = new JButton("Open in Explorer");
    private final JButton BackupButton = new JButton("Backup...");
    private final JButton RestoreButton = new JButton("Restore...");

    private final SpringLayout Lay = new SpringLayout();
    private final SpringLayout Lay2 = new SpringLayout();

    private ParamSFOListElement selected;
    private MediaPlayer selectedVideoProcess;
    private final Path[] targets;

    private final JLabel Background = new JLabel(new ImageIcon(getClass().getResource("/bg.png")));

    public SFOBasedManager(Frame parent, int mode, String title, Path... targets) {
        super(title);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (parent instanceof LaunchPage)
                    parent.setVisible(true);
                MediaPlayer.stopAllAudio();
                if (selectedVideoProcess != null)
                    selectedVideoProcess.stop();
                System.gc();
            }

        });

        this.targets = targets;

        InnerSFOFolderViewer.setLayout(new BoxLayout(InnerSFOFolderViewer, BoxLayout.Y_AXIS));
        InnerSFOFolderViewer.setBackground(new Color(0.3f, 0.3f, 0.3f));
        SFOFolderViewer.getVerticalScrollBar().setUnitIncrement(18);
        SFOFolderViewer.setBackground(new Color(0, 0, 0));

        Lay.putConstraint(SpringLayout.WEST, SFOFolderViewer, 10, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, SFOFolderViewer, 320, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, SFOFolderViewer, 10, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, SFOFolderViewer, -10, SpringLayout.SOUTH, getContentPane());

        Lay.putConstraint(SpringLayout.WEST, Background, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, Background, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, Background, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, Background, 0, SpringLayout.SOUTH, getContentPane());

        Lay.putConstraint(SpringLayout.WEST, ViewBG, 50, SpringLayout.EAST, SFOFolderViewer);
        Lay.putConstraint(SpringLayout.EAST, ViewBG, -10, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, ViewBG, 10, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, ViewBG, -10, SpringLayout.SOUTH, getContentPane());

        ViewBG.setBorder(new FlatLineBorder(new Insets(5, 5, 5, 5), Color.white, 3, 16));
        ViewBG.setLayout(Lay2);

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);

        Lay2.putConstraint(SpringLayout.HORIZONTAL_CENTER, ViewingIcon, 0, SpringLayout.HORIZONTAL_CENTER, ViewBG);

        Lay2.putConstraint(SpringLayout.EAST, sep, 0, SpringLayout.EAST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.WEST, sep, 0, SpringLayout.WEST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.NORTH, sep, 5, SpringLayout.SOUTH, ViewingIcon);

        Lay2.putConstraint(SpringLayout.EAST, ViewingName, 0, SpringLayout.EAST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.WEST, ViewingName, 0, SpringLayout.WEST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.NORTH, ViewingName, 5, SpringLayout.SOUTH, sep);

        Lay2.putConstraint(SpringLayout.EAST, ViewingCategory, -5, SpringLayout.EAST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.SOUTH, ViewingCategory, 0, SpringLayout.SOUTH, ViewingName);
        Lay2.putConstraint(SpringLayout.NORTH, ViewingCategory, 0, SpringLayout.NORTH, ViewingName);

        Lay2.putConstraint(SpringLayout.EAST, ViewingSubDesc, 3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.WEST, ViewingSubDesc, 3, SpringLayout.WEST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, ViewingSubDesc, -3, SpringLayout.SOUTH, ViewBG);

        Lay2.putConstraint(SpringLayout.EAST, OpenFolderButton, -3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, OpenFolderButton, -3, SpringLayout.SOUTH, ViewBG);

        Lay2.putConstraint(SpringLayout.EAST, RestoreButton, -3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, RestoreButton, -3, SpringLayout.NORTH, OpenFolderButton);

        Lay2.putConstraint(SpringLayout.EAST, BackupButton, -3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, BackupButton, -3, SpringLayout.NORTH, RestoreButton);

        Lay2.putConstraint(SpringLayout.EAST, ViewingDesc, 0, SpringLayout.EAST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.WEST, ViewingDesc, 0, SpringLayout.WEST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.NORTH, ViewingDesc, 5, SpringLayout.SOUTH, ViewingName);

        ViewingName.setFont(ViewingName.getFont().deriveFont(14f));
        ViewingCategory.setFont(ViewingCategory.getFont().deriveFont(8f));
        ViewingSubDesc.setFont(ViewingSubDesc.getFont().deriveFont(10f));
        ViewingSubDesc.setForeground(new Color(0.8f, 0.8f, 0.8f));
        ViewingCategory.setForeground(new Color(0.8f, 0.8f, 0.8f));
        ViewingCategory.setAlignmentY(SwingConstants.BOTTOM);
        ViewingCategory.setAlignmentX(SwingConstants.RIGHT);

        OpenFolderButton.addActionListener(ac -> {
            if (selected.dir != null) {
                try {
                    Desktop.getDesktop().open(selected.dir);
                } catch (Exception e) {
                    e.printStackTrace();
                    OpenFolderButton.setEnabled(false);
                }
            }
        });

        RestoreButton.addActionListener(ac -> restore());

        BackupButton.addActionListener(ac -> backup());

        BackupButton.setEnabled(false);

        ViewBG.add(ViewingIcon);
        ViewBG.add(sep);
        ViewBG.add(ViewingName);
        ViewBG.add(ViewingSubDesc);
        ViewBG.add(ViewingDesc);
        ViewBG.add(OpenFolderButton);
        ViewBG.add(ViewingCategory);
        if (mode == SAVES_MODE) {
            ViewBG.add(BackupButton);
            ViewBG.add(RestoreButton);
        }

        add(SFOFolderViewer);
        add(ViewBG);
        // always last
        add(Background);

        setLayout(Lay);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(Size);
        setResizable(false);
        setLocation(LaunchPage.getScreenCenter(this));

        FillOutWindow(targets);

        try {
            selected(ParamSFOListElement.makeEmpty(this));
        } catch (NameNotFoundException | IOException e1) {
            e1.printStackTrace();
        }
    }

    public void FillOutWindow(Path... Target) {
        Thread main = new Thread(() -> {
            InnerSFOFolderViewer.removeAll();
            ParamSFOListElement first = null;
            for (Path target : Target) { // get all target folders
                if (target.toFile().isDirectory() && target.toFile().exists())
                    for (File dir : target.toFile().listFiles()) { // get all folders (saves, games, etc)
                        if (dir.isDirectory())
                            try { // try to get param.sfo
                                Boolean valid = false;
                                for (File file : dir.listFiles()) {
                                    if (file.getName().endsWith("PBP") || file.getName().endsWith("SFO"))
                                        valid = true;
                                }
                                if (!valid)
                                    continue;

                                ParamSFO sfo = ParamSFO.ofFile(Path.of(dir.toPath().toString(), "PARAM.SFO").toFile());
                                ParamSFOListElement ToAdd = new ParamSFOListElement(sfo, dir, this);
                                InnerSFOFolderViewer.add(Box.createRigidArea(new Dimension(0, 10)));
                                InnerSFOFolderViewer.add(ToAdd);
                                if (first == null)
                                    first = ToAdd;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        else if (dir.getName().endsWith("iso"))
                            try { // try to get param.sfo
                                ParamSFOListElement ToAdd = ParamSFOListElement.ofIso(dir, this);
                                // System.out.println(ToAdd);
                                InnerSFOFolderViewer.add(Box.createRigidArea(new Dimension(0, 10)));
                                InnerSFOFolderViewer.add(ToAdd);
                                if (first == null)
                                    first = ToAdd;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        revalidate();
                        repaint();
                    }

            }

            System.gc();

        });
        main.start();
    }

    @Override
    public void selected(ParamSFOListElement selectedElement) {
        try {

            MediaPlayer.stopAllAudio();
            if (selectedVideoProcess != null)
                selectedVideoProcess.stop();
            selectedVideoProcess = null;

            ViewingIcon.setIcon(ImageUtilites.ResizeIcon(selectedElement.getIcon0(), 300, 166));

            Background.setIcon(
                    ImageUtilites.ResizeIcon(selectedElement.getPic1(), (int) Size.getWidth(),
                            (int) Size.getHeight()));
            Background.repaint();

            ViewingCategory.setText(
                    ParamSFO.tryToGetCategoryName(selectedElement.sfo.getParam(Params.Category).toString().trim()));

            switch (selectedElement.sfo.getParam(Params.Category).toString().trim()) {
                case "MS":
                    ViewingName.setText(selectedElement.sfo.getParam(Params.SaveTitle, false).toString());
                    ViewingDesc.setText(selectedElement.sfo.getParam(Params.Description, true).toString());
                    ViewingSubDesc.setText(selectedElement.sfo.getParam(Params.Title, true).toString());
                    if (selectedElement.videoDir != null) {
                        selectedVideoProcess = new MediaPlayer(new File(selectedElement.videoDir),
                                selectedElement.sfo.getParam(Params.SaveFolderName).toString().trim());

                        selectedVideoProcess.start(this);
                    }
                    break;

                case "UG":
                    ViewingName.setText(selectedElement.sfo.getParam(Params.Title, false).toString());
                    ViewingDesc.setText("UMD Game: " + selectedElement.sfo.getParam(Params.DiscID, false).toString());
                    ViewingSubDesc.setText("");
                    if (selectedElement.videoDir != null) {
                        selectedVideoProcess = new MediaPlayer(new File(selectedElement.videoDir),
                                selectedElement.sfo.getParam(Params.DiscID).toString().trim());

                        selectedVideoProcess.start(this);
                    }
                    break;

                case "DG": // ps3 disc game
                    ViewingName.setText(selectedElement.sfo.getParam(Params.Title, true).toString());
                    ViewingDesc.setText("PS3 Disc Game: " + selectedElement.sfo.getParam("TITLE_ID", false).toString());
                    ViewingSubDesc.setText("");
                    if (selectedElement.videoDir != null) {
                        selectedVideoProcess = new MediaPlayer(new File(selectedElement.videoDir),
                                selectedElement.sfo.getParam("TITLE_ID").toString().trim());

                        selectedVideoProcess.start(this);
                    }
                    break;

                case "PT": // psptools
                    ViewingName.setText(selectedElement.sfo.getParam(Params.Title, true).toString());
                    ViewingDesc.setText("");
                    ViewingSubDesc.setText("");
                    break;

                default:
                    ViewingName.setText(selectedElement.sfo.getParam(Params.Title, true).toString());
                    ViewingDesc.setText("ID: " + selectedElement.sfo.getParam("TITLE_ID", false).toString());
                    ViewingSubDesc.setText("");
                    System.err.println("Need handling for " + selectedElement.sfo.getParam(Params.Category).toString());
                    if (selectedElement.videoDir != null) {
                        selectedVideoProcess = new MediaPlayer(new File(selectedElement.videoDir),
                                selectedElement.sfo.getParam(Params.SaveFolderName).toString().trim());

                        selectedVideoProcess.start(this);
                    }
                    break;
            }

            if (!BackupButton.isEnabled())
                BackupButton.setEnabled(true);

            if (selectedElement.backuped)
                RestoreButton.setEnabled(true);
            else
                RestoreButton.setEnabled(false);

            if (selectedElement.dir == null)
                OpenFolderButton.setEnabled(false);
            else
                OpenFolderButton.setEnabled(true);

            if (selectedElement.audioDir != null)
                MediaPlayer.playAudio(selectedElement.audioDir);

            this.selected = selectedElement;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backup() {
        Path backupPath = Path.of(SavedVariables.DataFolder.toString(), "PSPSaveBackups", selected.getBackupName());
        int option;
        if (!backupPath.toFile().exists())
            option = JOptionPane.showConfirmDialog(this,
                    "Backup this save?\nIt will be backed up at " + backupPath.toString(), "Backup save?",
                    JOptionPane.YES_NO_OPTION);
        else {
            option = JOptionPane.showConfirmDialog(this,
                    "Override this backup?\nThe last backup was made " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                            .format(new Date(backupPath.toFile().lastModified())),
                    "Backup save?",
                    JOptionPane.YES_NO_OPTION);
        }
        if (option == JOptionPane.YES_OPTION) {
            try {
                org.apache.commons.io.FileUtils.createParentDirectories(backupPath.toFile());
                if (!backupPath.toFile().exists())
                    backupPath.toFile().createNewFile();

                // backup save
                ZipArchiver zip = new ZipArchiver();
                zip.setDestFile(backupPath.toFile());

                LoadingScreen loading = new LoadingScreen(this);
                SwingUtilities.invokeLater(() -> {
                    loading.setVisible(true);
                });

                zip.addDirectory(selected.dir);
                zip.createArchive();

                SwingUtilities.invokeLater(() -> {
                    loading.setVisible(false);

                    System.gc();
                    FillOutWindow(targets);
                });

            } catch (Exception e) {
                e.printStackTrace();
                ErrorShower.showError(this, "Failed to backup save.", e.getMessage(), e);
            }
        }
    }

    @Override
    public void restore() {
        Path backupPath = Path.of(SavedVariables.DataFolder.toString(), "PSPSaveBackups", selected.getBackupName());
        int option = JOptionPane.showConfirmDialog(this,
                "Restore & override this save?\nThe backup was made at "
                        + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                .format(new Date(backupPath.toFile().lastModified())),
                "Restore save?",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            try {
                ZipUnArchiver zip = new ZipUnArchiver(backupPath.toFile());

                LoadingScreen loading = new LoadingScreen(this);
                SwingUtilities.invokeLater(() -> {
                    loading.setVisible(true);
                });

                Path dest = Path.of(targets[0].toString(),
                        backupPath.toFile().getName().replace(".zip", ""));

                FileUtils.deleteDirectory(dest.toFile());
                Files.createDirectory(dest);

                zip.setDestDirectory(dest.toFile());
                zip.extract();

                SwingUtilities.invokeLater(() -> {
                    int Option = JOptionPane.showConfirmDialog(null,
                            "Would you like to delete this backup?",
                            "Delete?",
                            JOptionPane.YES_NO_OPTION);
                    try {
                        if (Option == JOptionPane.YES_OPTION)
                            Files.delete(backupPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    loading.setVisible(false);

                    System.gc();
                    FillOutWindow(targets);
                });
            } catch (Exception e) {
                e.printStackTrace();
                ErrorShower.showError(this, "Failed to restore save.", e.getMessage(), e);
            }
        }
    }

    @Override
    public void delete(ParamSFOListElement selectedElement) {
        try {
            // Path backupPath = Path.of(System.getProperty("user.home"), "PSPSaveBackups",
            // getBackupName());
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this save? (it will be gone for a LONG time)",
                    "DELETE SAVE?",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                backup();

                FileUtils.deleteDirectory(selectedElement.dir);
                SwingUtilities.invokeLater(() -> {
                    try {
                        JOptionPane.showMessageDialog(this,
                                "<html>Deleted save " + selectedElement.sfo.getParam(Params.SaveTitle, true).toString()
                                        .replace("<html>", ""));
                    } catch (HeadlessException | NameNotFoundException e) {
                        e.printStackTrace();
                    }

                });
                FillOutWindow(targets);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void frameStepped(BufferedImage frame) {
        if (frame == null)
            return;
        SwingUtilities.invokeLater(() -> {
            ViewingIcon.getGraphics().drawImage(
                    ImageUtilites.ResizeImage(frame, 300, 166), 0, 0, null);
        });
    }

}
