package fn10.psptools.ui;

import fn10.psptools.PSPTools;
import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.ui.components.PSPFileListElement;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class NewLaunchPage extends JFrame {

    private final SpringLayout lay = new SpringLayout();
    private final ArrayList<PSPFileListElement> selectedFilesList = new ArrayList<>();
    private final JPanel files = new JPanel();
    private final JLabel loadingText = new JLabel("Not Busy");
    private final JLabel dirText = new JLabel("/");

    public static NewLaunchPage current;

    public NewLaunchPage() {
        super("PSPTools");
        current = this;
        setSize(new Dimension(669, 500));
        setLocation(LaunchPage.getScreenCenter(this));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(lay);

        JMenuBar menu = new JMenuBar();
        JMenu pspMenu = new JMenu("PSP");
        pspMenu.add("Select PSP").addActionListener(ac -> {
            PSP selected = PSPSelectionUI.getNewPSP(this);
            if (selected != null) {
                PSP.setCurrentPSP(selected);
                showDir("/");
            }
        });

        menu.add(pspMenu);
        setJMenuBar(menu);

        JScrollPane scroll = new JScrollPane(files);
        GridLayout mgr = new GridLayout(0, 1, 0, 0);
        files.setLayout(mgr);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));

        JButton gmButton = new JButton("Game Manager");
        gmButton.addActionListener(_ -> {
            new SFOBasedManager(this, SFOBasedManager.GAMES_MODE, "Game Manager",
                    PSP.getCurrentPSP().getFolder("PSP", "GAME"),
                    PSP.getCurrentPSP().getFolder("PSP", "GAME150"),
                    PSP.getCurrentPSP().getFolder("ISO"),
                    PSP.getCurrentPSP().getFolder("PSP", "GAME303")).setVisible(true);
        });
        JButton smButton = new JButton("Save Manager");
        smButton.addActionListener(_ -> {
            new SFOBasedManager(this, SFOBasedManager.GAMES_MODE, "Game Manager",
                    PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA")).setVisible(true);
        });
        JButton npButton = new JButton("Save Patching");
        npButton.addActionListener(_ -> {
            new SavePatching(this).setVisible(true);
        });
        JButton dtButton = new JButton("Database Tools");
        dtButton.addActionListener(_ -> {
            try {
                new DatabaseTools(this).setVisible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        JButton pmButton = new JButton("Plugin Manager");
        buttons.add(gmButton);
        buttons.add(smButton);
        buttons.add(npButton);
        buttons.add(dtButton);
        buttons.add(pmButton);

        lay.putConstraint(SpringLayout.WEST, scroll, 5, SpringLayout.WEST, getContentPane());
        lay.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, getContentPane());
        lay.putConstraint(SpringLayout.SOUTH, scroll, -10, SpringLayout.NORTH, buttons);
        lay.putConstraint(SpringLayout.NORTH, scroll, 5, SpringLayout.SOUTH, dirText);

        lay.putConstraint(SpringLayout.NORTH, dirText, 5, SpringLayout.NORTH, getContentPane());
        lay.putConstraint(SpringLayout.WEST, dirText, 10, SpringLayout.WEST, getContentPane());

        lay.putConstraint(SpringLayout.WEST, loadingText, 10, SpringLayout.WEST, getContentPane());
        lay.putConstraint(SpringLayout.SOUTH, loadingText, -10, SpringLayout.SOUTH, getContentPane());

        lay.putConstraint(SpringLayout.SOUTH, buttons, -5, SpringLayout.NORTH, loadingText);
        lay.putConstraint(SpringLayout.EAST, buttons, 0, SpringLayout.EAST, getContentPane());
        lay.putConstraint(SpringLayout.WEST, buttons, 0, SpringLayout.WEST, getContentPane());

        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);
        add(loadingText);
        add(dirText);
        add(buttons);

        showDir("/");
    }

    public static boolean isRoot(String path) {
        String serilized = path.replace("\\", "/");
        return serilized.lastIndexOf("/") == path.indexOf("/");
    }

    /**
     * Returns the parent of the path
     * @param path the path to get the parent from
     * @return the directory one level above
     */
    public static String top(String path) {
        String serilized = path.replace("\\", "/");
        String fin;
        if (serilized.lastIndexOf("/") == serilized.length() - 1) {
            fin = serilized.substring(0, serilized.length() - 2);
        } else fin = serilized;

        return fin.substring(0, fin.lastIndexOf("/") + 1);
    }

    public void showDir(String path) {
        Thread thread = new Thread(() -> {
            selectedFilesList.clear();
            dirText.setText(path);
            String print = "Listing Directory '" + path + "'...";
            PSPTools.log.info(print);
            loadingText.setText(print);
            int i = 1;
            files.removeAll();

            if (!isRoot(path)) {
                JButton backButton = new JButton("< Back");
                backButton.addActionListener(_ -> {
                    showDir(top(path));
                });
                files.add(backButton);
            }

            PSP currentPSP = PSP.getCurrentPSP();
            if (!currentPSP.pspActive()) {
                String log = "No PSP active.";
                loadingText.setText(log);
                PSPTools.log.warn(log);

                JPanel panel = getErrorPanel("No PSP is selected.", "Select PSP", () -> {
                    PSP selected = PSPSelectionUI.getNewPSP(this);
                    if (selected != null) {
                        PSP.setCurrentPSP(selected);
                        showDir("/");
                    }
                });
                files.add(panel);

                return;
            }
            ArrayList<PSPFileDirectory> all = new ArrayList<>(List.of(currentPSP.getFolder(path).getAll()));
            all.sort((o1, o2) -> {
                if (o1.isDirectory() && !o2.isDirectory()) {
                    return 1;
                } else if (o1.isDirectory() && o2.isDirectory()) {
                    return 0;
                } else
                    return -1;
            });
            for (PSPFileDirectory pfd : all) {
                PSPFileListElement comp;
                if (i % 2 == 0)
                    comp = new PSPFileListElement(this, pfd, Color.darkGray.darker(), selectedFilesList);
                else
                    comp = new PSPFileListElement(this, pfd, selectedFilesList);
                i++;
                files.add(comp);
            }
            loadingText.setText("Not Busy");
        });

        thread.setUncaughtExceptionHandler((t,e) -> {
            PSPTools.log.error("Uncaught error whilst showing a directory.", e);
            files.removeAll();
            files.add(getErrorPanel("Failed to show directory. " + e.getMessage(), "Return", () -> {
                showDir("/");
            }));
        });

        thread.start();
    }

    private @NonNull JPanel getErrorPanel(String message, String button, Runnable whenButtonClicked) {
        JPanel panel = new JPanel();
        JLabel comp = new JLabel(message);
        comp.setHorizontalAlignment(SwingConstants.CENTER);
        JButton comp1 = new JButton(button);
        comp1.addActionListener(_ -> {
            whenButtonClicked.run();
        });
        panel.add(comp);
        panel.add(comp1);
        return panel;
    }
}
