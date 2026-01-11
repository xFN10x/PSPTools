package fn10.psptools.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.sfo.ParamSFO;
import fn10.psptools.ui.components.MediaPlayer;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListiener;
import fn10.psptools.util.ImageUtilites;

public class SFOBasedSelector extends JDialog implements SFOListElementListiener {

    public static final int SAVES_MODE = 0;
    public static final int GAMES_MODE = 1;
    public static final int SINGLE = 2;

    private static final Dimension Size = new Dimension(350, 392);

    protected final JPanel InnerSFOFolderViewer = new JPanel();
    protected final JButton SelectButton = new JButton("Select");
    protected final JScrollPane SFOFolderViewer = new JScrollPane(InnerSFOFolderViewer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final SpringLayout Lay = new SpringLayout();

    private ParamSFOListElement selected = null;

    private final JLabel Background = new JLabel(new ImageIcon(getClass().getResource("/bg.png")));

    public static ParamSFOListElement openSaveSelector(Frame parent) {
        SFOBasedSelector selector = new SFOBasedSelector(parent, SAVES_MODE, "Select Save...",
                PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA").toFile());

        selector.setVisible(true);

        return selector.selected;
    }

    private SFOBasedSelector(Frame parent, int mode, String title, File... targets) {
        super(parent, title);

        InnerSFOFolderViewer.setLayout(new BoxLayout(InnerSFOFolderViewer, BoxLayout.Y_AXIS));
        InnerSFOFolderViewer.setBackground(new Color(0.3f, 0.3f, 0.3f));
        SFOFolderViewer.getVerticalScrollBar().setUnitIncrement(18);
        SFOFolderViewer.setBackground(new Color(0, 0, 0));

        Lay.putConstraint(SpringLayout.WEST, SFOFolderViewer, 10, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, SFOFolderViewer, -10, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, SFOFolderViewer, 10, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, SFOFolderViewer, -3, SpringLayout.NORTH, SelectButton);

        Lay.putConstraint(SpringLayout.WEST, SelectButton, 10, SpringLayout.WEST, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, SelectButton, -10, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, SelectButton, -10, SpringLayout.SOUTH, getContentPane());

        Lay.putConstraint(SpringLayout.HORIZONTAL_CENTER, Background, 0, SpringLayout.HORIZONTAL_CENTER,
                getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, Background, 0, SpringLayout.NORTH, getContentPane());
        Lay.putConstraint(SpringLayout.SOUTH, Background, 0, SpringLayout.SOUTH, getContentPane());

        SelectButton.addActionListener(ac -> {
            setVisible(false);
        });

        add(SFOFolderViewer);
        add(SelectButton);
        // always last
        add(Background);

        setLayout(Lay);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(Size);
        setResizable(false);
        setLocation(LaunchPage.getScreenCenter(this));

        setModal(true);

        FillOutWindow(targets);
    }

    public void FillOutWindow(File... Target) {
        Thread main = new Thread(() -> {
            // InnerSFOFolderViewer.removeAll();
            ParamSFOListElement first = null;
            for (File target : List.of(Target)) { // get all target folders
                if (target.isDirectory() && target.exists())
                    for (File dir : target.listFiles()) { // get all folders (saves, games, etc)
                        // System.out.println(dir.getAbsolutePath());
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
                else {
                    // System.out.println(target.getAbsolutePath());
                }
            }
            // first.mouseClicked(null);
            System.gc();

        });
        main.start();
    }

    @Override
    public void selected(ParamSFOListElement selectedElement) {

        try {

            Background.setIcon(
                    ImageUtilites.ResizeIcon(selectedElement.getPic1(), (int) Size.getWidth(),
                            (int) Size.getHeight()));
            Background.repaint();


            this.selected = selectedElement;

        } catch (Exception e) {
            e.printStackTrace();

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
