package fn10.psptools.ui;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.ui.components.PSPFileListElement;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewLaunchPage extends JFrame {

    private final SpringLayout lay = new SpringLayout();
    private final ArrayList<PSPFileListElement> selectedFilesList = new ArrayList<>();

    public NewLaunchPage() {
        super("PSPTools");
        setSize(new Dimension(669, 500));
        setLocation(LaunchPage.getScreenCenter(this));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JPanel files = new JPanel();
        JScrollPane scroll = new JScrollPane(files);
        GridLayout mgr = new GridLayout(0, 1, 0, 0);
        files.setLayout(mgr);
        int i = 1;
        ArrayList<PSPFileDirectory> all = new ArrayList<>(List.of(PSP.getCurrentPSP().getFolder("/").getAll()));
        all.sort((o1, o2) -> {
            if (o1.isDirectory() && !o2.isDirectory()) {
                return 1;
            } else if (o1.isDirectory() && o2.isDirectory()) {
                return 0;
            } else
                return -1;
        });
        for (PSPFileDirectory pfd : all) {
            PSPFileListElement comp = null;
            if (i % 2 == 0)
                comp = new PSPFileListElement(pfd, Color.darkGray.darker(), selectedFilesList);
            else
                comp = new PSPFileListElement(pfd, selectedFilesList);
            i++;
            files.add(comp);
        }

        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);
    }
}
