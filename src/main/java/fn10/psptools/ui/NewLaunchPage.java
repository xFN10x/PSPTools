package fn10.psptools.ui;

import fn10.psptools.PSPTools;
import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.ui.components.PSPFileListElement;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewLaunchPage extends JFrame {

    private final SpringLayout lay = new SpringLayout();
    private final ArrayList<PSPFileListElement> selectedFilesList = new ArrayList<>();
    private final JPanel files = new JPanel();
    private final JLabel loadingText = new JLabel("Not Busy");

    public NewLaunchPage() {
        super("PSPTools");
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

        lay.putConstraint(SpringLayout.WEST, scroll, 5, SpringLayout.WEST, getContentPane());
        lay.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, getContentPane());
        lay.putConstraint(SpringLayout.SOUTH, scroll, -10, SpringLayout.NORTH, loadingText);
        lay.putConstraint(SpringLayout.NORTH, scroll, 5, SpringLayout.NORTH, getContentPane());

        lay.putConstraint(SpringLayout.WEST, loadingText, 10, SpringLayout.WEST, getContentPane());
        lay.putConstraint(SpringLayout.SOUTH, loadingText, -10, SpringLayout.SOUTH, getContentPane());

        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);
        add(loadingText);

        showDir("/");
    }

    public void showDir(String path) {
        Thread thread = new Thread(() -> {
            String print = "Listing Directory '" + path + "'...";
            PSPTools.log.info(print);
            loadingText.setText(print);
            int i = 1;
            files.removeAll();
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
                    comp = new PSPFileListElement(pfd, Color.darkGray.darker(), selectedFilesList);
                else
                    comp = new PSPFileListElement(pfd, selectedFilesList);
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
