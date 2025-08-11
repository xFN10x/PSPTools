package psptools.psp;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;

import psptools.ui.LaunchPage;

public class PSPSelectionUI extends JDialog {

    public final static Dimension size = new Dimension(300, 200);
    public final SpringLayout Lay = new SpringLayout();

    public final JTabbedPane Tabbed = new JTabbedPane(JTabbedPane.TOP);

    // #region drive selection part
    public final JPanel DS = new JPanel();
    public final SpringLayout DSLay = new SpringLayout();
    public final JButton AutoButton = new JButton("Auto");
    public final JButton SelectButton = new JButton("Select");
    public final JComboBox<File> InputDriveBox = new JComboBox<File>(File.listRoots());
    // #endregion
    public final JPanel VDS = new JPanel();
    public final JPanel DIRS = new JPanel();
    public final JPanel FTPS = new JPanel();

    public File SelectedFolder = null;
    public SelectionMode SelectedMode = null;

    private PSPSelectionUI(Frame parent) {
        super(parent, "Select PSP", ModalityType.APPLICATION_MODAL);
        setLayout(Lay);

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (InputDriveBox.getItemCount() != File.listRoots().length) {
                    InputDriveBox.removeAllItems();

                    for (File f : File.listRoots()) {
                        InputDriveBox.addItem(f);
                    }
                }
            }

        }, 5, 5);

        Lay.putConstraint(SpringLayout.EAST, Tabbed, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, Tabbed, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, Tabbed, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, Tabbed, 0, SpringLayout.SOUTH, getContentPane());

        // #region Drive Selection Stuff
        DS.setLayout(DSLay);
        DSLay.putConstraint(SpringLayout.NORTH, AutoButton, 5, SpringLayout.NORTH, DS);
        DSLay.putConstraint(SpringLayout.WEST, AutoButton, 5, SpringLayout.WEST, DS);

        DSLay.putConstraint(SpringLayout.NORTH, InputDriveBox, 5, SpringLayout.NORTH, DS);
        DSLay.putConstraint(SpringLayout.WEST, InputDriveBox, 5, SpringLayout.EAST, AutoButton);
        DSLay.putConstraint(SpringLayout.EAST, InputDriveBox, -5, SpringLayout.EAST, DS);

        DSLay.putConstraint(SpringLayout.SOUTH, SelectButton, -5, SpringLayout.SOUTH, DS);
        DSLay.putConstraint(SpringLayout.EAST, SelectButton, -5, SpringLayout.EAST, DS);
        DSLay.putConstraint(SpringLayout.WEST, SelectButton, 5, SpringLayout.WEST, DS);

        AutoButton.addActionListener(action -> {
            for (File root : File.listRoots()) {
                File PSPFolder = new File(Path.of(root.getPath(), "PSP").toString());
                File ISOFolder = new File(Path.of(root.getPath(), "ISO").toString());
                File PSPGameFolder = new File(Path.of(root.getPath(), "PSP", "Game").toString());

                if (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists()) {
                    JOptionPane.showMessageDialog(parent, "Found PSP @ " + root.toString(), "PSPFound",
                            JOptionPane.INFORMATION_MESSAGE);
                    InputDriveBox.setSelectedItem(root);
                    return;

                }
            }
            JOptionPane.showMessageDialog(parent, "Did not find PSP connected to any drives.", "PSP Not Found",
                    JOptionPane.WARNING_MESSAGE);

        });

        SelectButton.addActionListener(action -> {
            File root = (File) InputDriveBox.getSelectedItem();
            File PSPFolder = new File(Path.of(root.getPath(), "PSP").toString());
            File ISOFolder = new File(Path.of(root.getPath(), "ISO").toString());
            File PSPGameFolder = new File(Path.of(root.getPath(), "PSP", "Game").toString());

            if (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists()) {
                SelectedFolder = root;
                SelectedMode = SelectionMode.PSP_DIR;
                setVisible(false);
                dispose();
            }
        });

        DS.add(AutoButton);
        DS.add(InputDriveBox);
        DS.add(SelectButton);

        // #endregion

        add(Tabbed);
        Tabbed.add(DS);
        Tabbed.add(VDS);
        Tabbed.add(DIRS);
        Tabbed.add(FTPS);

        Tabbed.addTab("Drive Selection", DS);
        Tabbed.addTab("Vita Drive Selection", VDS);
        Tabbed.addTab("Directory Selection", DIRS);
        Tabbed.addTab("FTP Selection", FTPS);
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

        PSPSelectionUI ui = new PSPSelectionUI(parent);

        ui.setVisible(true);

        switch (ui.SelectedMode) {
            case SelectionMode.PSP_DIR:

                return new PSP(SelectionMode.PSP_DIR, ui.SelectedFolder.toPath());

            default:
                return null;

        }

    }
}
