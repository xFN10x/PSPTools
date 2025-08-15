package psptools.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import psptools.ui.components.ParamSFOListElement;
import psptools.util.ErrorShower;
import psptools.util.SavedVariables;

public class SaveTools extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final JTextField databaseSearch = new JTextField();
    private final JPanel databasePanel = new JPanel();
    private final JPanel databaseListingInnerPanel = new JPanel();
    private final JScrollPane databaseListingPanel = new JScrollPane(databaseListingInnerPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final SpringLayout Lay = new SpringLayout();
    private final SpringLayout Lay2 = new SpringLayout();

    private volatile Thread currentThread;

    public SaveTools(Frame parent) {
        super("Save Manager");

        SavedVariables saved = SavedVariables.Load();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                parent.setVisible(true);
                if (currentThread != null)
                    currentThread.interrupt();
                System.gc();
            }

        });

        setResizable(false);
        setLayout(Lay);
        setSize(new Dimension(350, 500));

        tabbedPane.addTab("Save Database", databasePanel);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        Lay.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, tabbedPane, 0, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, getContentPane());

        Lay.putConstraint(SpringLayout.NORTH, databasePanel, 0, SpringLayout.SOUTH, tabbedPane);
        Lay.putConstraint(SpringLayout.WEST, databasePanel, 0, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, databasePanel, 0, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, databasePanel, 0, SpringLayout.SOUTH, getContentPane());

        databaseListingInnerPanel.setLayout(new BoxLayout(databaseListingInnerPanel, BoxLayout.Y_AXIS));

        add(tabbedPane);

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

        databaseListingInnerPanel.add(new JLabel("Search by ID (e.g. ULUS03410) or by name (e.g. METAL GEAR SOLID)"));

        databaseSearch.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadDatabase(saved.DatabaseUrl);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadDatabase(saved.DatabaseUrl);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadDatabase(saved.DatabaseUrl);
            }

        });

        databasePanel.add(databaseSearch);
        databasePanel.add(databaseListingPanel);

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

    public void reloadDatabase(URL url) {
        System.gc();
        if (currentThread != null)
            currentThread.interrupt();
        Thread thread = new Thread(() -> {
            try {
                databaseListingInnerPanel.removeAll();
                databaseListingInnerPanel.repaint();
                if (databaseSearch.getText().trim().equals(""))
                    return;
                System.out.println(url.toString());
                URL GameTXTUrl = new URI(url.toString() + "/PSP/games.txt").toURL();
                BufferedReader buff = new BufferedReader(new InputStreamReader(GameTXTUrl.openStream()));
                //Map<String, String> entrys = new HashMap<String, String>();

                String currentLine;
                while ((currentLine = buff.readLine()) != null && !Thread.interrupted()) {
                    String[] keyandval = currentLine.split("=");
                    System.out.println("Loading");
                    if (!keyandval[1].trim().toLowerCase().contains(databaseSearch.getText().toLowerCase())
                            && !keyandval[0].trim().toLowerCase().contains(databaseSearch.getText().toLowerCase())) {
                        continue;
                    }
                    URL GameIconUrl = new URI(url.toString() + "/PSP/" + keyandval[0] + "/ICON0.PNG").toURL();
                    System.out.println(keyandval[0] + "=" + keyandval[1]);
                    databaseListingInnerPanel.add(new ParamSFOListElement(keyandval[1], keyandval[0],
                            GameIconUrl.openStream().readAllBytes()));
                    databaseListingInnerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
                if (Thread.interrupted()) {
                    databaseListingInnerPanel.removeAll();
                    databaseListingInnerPanel.repaint();
                }
                System.out.println("Done");
                buff.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        currentThread = thread;
        thread.start();
    }

}
