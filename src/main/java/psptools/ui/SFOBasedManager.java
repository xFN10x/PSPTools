package psptools.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.FileUtils;

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

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.google.gson.GsonBuilder;

import psptools.psp.sfo.ParamSFO;
import psptools.psp.sfo.ParamSFO.Params;
import psptools.ui.components.ParamSFOListElement;
import psptools.util.ErrorShower;
import psptools.util.ImageUtilites;

public class SFOBasedManager extends JFrame {

    public static final int SAVES_MODE = 0;
    public static final int GAMES_MODE = 1;
    public static final int SINGLE = 2;

    public static final Dimension Size = new Dimension(706, 392);

    public final JPanel InnerSFOFolderViewer = new JPanel();
    public final JScrollPane SFOFolderViewer = new JScrollPane(InnerSFOFolderViewer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    public final JPanel ViewBG = new JPanel();
    public final JLabel ViewingIcon = new JLabel();
    public final JLabel ViewingName = new JLabel("Save Title");
    public final JLabel ViewingGameName = new JLabel("Save Game");
    public final JLabel ViewingDesc = new JLabel("Save Description");
    public final JButton DebugButton = new JButton("Debug");
    public final JButton BackupButton = new JButton("Backup...");
    public final JButton RestoreButton = new JButton("Restore...");

    public final SpringLayout Lay = new SpringLayout();
    public final SpringLayout Lay2 = new SpringLayout();

    public ParamSFO selectedSFO = null;
    public File selectedDir = null;

    public final JLabel Background = new JLabel(new ImageIcon(getClass().getResource("/bg.png")));

    public SFOBasedManager(LaunchPage parent, int mode, String title, File target) {
        super(title);

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        InnerSFOFolderViewer.setLayout(new BoxLayout(InnerSFOFolderViewer, BoxLayout.Y_AXIS));
        InnerSFOFolderViewer.setBackground(new Color(0.3f, 0.3f, 0.3f));
        SFOFolderViewer.getVerticalScrollBar().setUnitIncrement(18);
        SFOFolderViewer.setBackground(new Color(0, 0, 0));
        // InnerSFOFolderViewer.setSize(new Dimension(300,0));

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

        Lay2.putConstraint(SpringLayout.EAST, ViewingGameName, 3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.WEST, ViewingGameName, 3, SpringLayout.WEST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, ViewingGameName, -3, SpringLayout.SOUTH, ViewBG);

        Lay2.putConstraint(SpringLayout.EAST, DebugButton, -3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, DebugButton, -3, SpringLayout.SOUTH, ViewBG);

        Lay2.putConstraint(SpringLayout.EAST, RestoreButton, -3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, RestoreButton, -3, SpringLayout.NORTH, DebugButton);

        Lay2.putConstraint(SpringLayout.EAST, BackupButton, -3, SpringLayout.EAST, ViewBG);
        Lay2.putConstraint(SpringLayout.SOUTH, BackupButton, -3, SpringLayout.NORTH, RestoreButton);

        Lay2.putConstraint(SpringLayout.EAST, ViewingDesc, 0, SpringLayout.EAST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.WEST, ViewingDesc, 0, SpringLayout.WEST, ViewingIcon);
        Lay2.putConstraint(SpringLayout.NORTH, ViewingDesc, 5, SpringLayout.SOUTH, ViewingName);

        ViewingName.setFont(ViewingName.getFont().deriveFont(14f));
        ViewingGameName.setFont(ViewingGameName.getFont().deriveFont(10f));
        ViewingGameName.setForeground(new Color(0.8f, 0.8f, 0.8f));

        DebugButton.addActionListener(ac -> {
            if (selectedSFO != null) {
                String json = new GsonBuilder().setPrettyPrinting().create().toJson(selectedSFO);
                System.out.println(json);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(json), null);
            }
        });

        BackupButton.addActionListener(ac -> {
            Path backupPath = Path.of(System.getProperty("user.home"), "PSPSaveBackups",
                    selectedSFO.getParam(Params.SaveTitle).toString().replace("\u0000", "") + ".zip");
            int option = JOptionPane.showConfirmDialog(this,
                    "Backup this save?\nIt will be backed up at " + backupPath.toString(), "Backup save?",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try {
                    org.apache.commons.io.FileUtils.createParentDirectories(backupPath.toFile());
                    if (!backupPath.toFile().exists())
                    backupPath.toFile().createNewFile();
                    ZipFile zip = new ZipFile(backupPath.toFile());
                    ProgressMonitor promon = zip.getProgressMonitor();
                    Timer timer = new Timer();
                    zip.setRunInThread(true);
                    zip.addFolder(selectedDir);
                    
                    timer.scheduleAtFixedRate(new TimerTask() {

                        @Override
                        public void run() {
                            System.out.println(promon.getPercentDone());
                        }
                        
                    }, 5, 5);
                    
                    zip.close();
                    timer.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorShower.showError(this, "Failed to backup save.", e.getMessage(), e);
                }
            }
        });

        BackupButton.setEnabled(false);

        ViewBG.add(ViewingIcon);
        ViewBG.add(sep);
        ViewBG.add(ViewingName);
        ViewBG.add(ViewingGameName);
        ViewBG.add(ViewingDesc);
        ViewBG.add(DebugButton);
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

        FillOutWindow(target);
    }

    public void FillOutWindow(File target) {
        Thread main = new Thread(() -> {
            ParamSFOListElement first = null;
            for (File dir : target.listFiles()) {
                try {
                    ParamSFO sfo = ParamSFO.ofFile(Path.of(dir.toPath().toString(), "PARAM.SFO").toFile());
                    ParamSFOListElement ToAdd = new ParamSFOListElement(sfo, dir, () -> {
                        try {
                            ImageIcon rawIcon = new ImageIcon(
                                    Path.of(dir.getAbsolutePath(), "Icon0.png").toUri().toURL());
                            ViewingIcon.setIcon(ImageUtilites.ResizeIcon(rawIcon, 300, 166));

                            ImageIcon rawIcon2 = new ImageIcon(
                                    Path.of(dir.getAbsolutePath(), "pic1.png").toUri().toURL());
                            Background.setIcon(
                                    ImageUtilites.ResizeIcon(rawIcon2, (int) Size.getWidth(), (int) Size.getHeight()));
                            Background.repaint();

                            ViewingName.setText(sfo.getParam(Params.SaveTitle, false).toString());
                            ViewingDesc.setText(sfo.getParam(Params.Description, true).toString());
                            ViewingGameName.setText(sfo.getParam(Params.Title, true).toString());
                            selectedSFO = sfo;
                            selectedDir = dir;
                            if (!BackupButton.isEnabled())
                                BackupButton.setEnabled(true);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    InnerSFOFolderViewer.add(Box.createRigidArea(new Dimension(0, 10)));
                    InnerSFOFolderViewer.add(ToAdd);
                    if (first == null)
                        first = ToAdd;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // first.mouseClicked(null);
            System.gc();
        });
        main.start();
    }

}
