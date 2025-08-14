package psptools.ui.components;

import javax.naming.NameNotFoundException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import org.apache.commons.io.FileUtils;

import com.formdev.flatlaf.ui.FlatLineBorder;

import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.filesystems.umdiso.UmdIsoReader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import psptools.psp.sfo.ParamSFO;
import psptools.psp.sfo.ParamSFO.Params;
import psptools.ui.interfaces.SFOListElementListiener;
import psptools.util.AudioStreamPlayer;
import psptools.util.ImageUtilites;

public class ParamSFOListElement extends JPanel implements MouseListener {

    private static final Dimension Size = new Dimension(290, 60);
    private static final FlatLineBorder border = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 2, 8);
    private static final FlatLineBorder selectedBorder = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 4, 8);

    public final ParamSFO sfo;
    public final ProcessBuilder playAudioProcess;
    private final ImageIcon icon0;
    public final File dir;
    private final SpringLayout Lay = new SpringLayout();

    private final JLabel SFOTitle = new JLabel();
    private final JLabel SFODesc = new JLabel();
    private final JLabel Icon0 = new JLabel();
    private final JLabel BackedUp = new JLabel(new ImageIcon(getClass().getResource("/backed.png")));

    private final JPopupMenu RightClickMenu = new JPopupMenu();

    private final SFOListElementListiener selectedFunc;

    public boolean backuped = false;

    private String getBackupName() {
        try {
            if (sfo == null)
                return "";
            switch (sfo.getParam(Params.Category).toString()) {
                case "MS":

                    return (sfo.getParam(Params.SaveTitle).toString() + "-"
                            + sfo.getParam(Params.SaveFolderName).toString()).replace("\u0000", "").replace(":", "")
                            + ".zip";

                default:
                    return sfo.getParam(Params.Title).toString().replace("\u0000", "").replace(":", "").replace("\n", " ").replace(" ", "-") + ".zip";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void delete() throws IOException {
        FileUtils.deleteDirectory(dir);
    }

    public static ParamSFOListElement ofIso(File iso, SFOListElementListiener selectedFunction) {
        // try (IsoFileReader reader = new IsoFileReader(iso)) {
        // GenericInternalIsoFile[] files = reader.getAllFiles();
        // Optional<GenericInternalIsoFile> param = reader.getSpecificFileByName(files,
        // "/PSP_GAME/PARAM.SFO");
        // Optional<GenericInternalIsoFile> icon = reader.getSpecificFileByName(files,
        // "/PSP_GAME/ICON0.PNG");
        //
        // return new
        // ParamSFOListElement(ParamSFO.ofStream(reader.getFileStream(param.get())),
        // null,
        // reader.getFileBytes(icon.get()), selectedFunction);
        // } catch (Exception e) {
        // e.printStackTrace();
        // return null;
        // }
        try {

            UmdIsoReader reader = new UmdIsoReader(iso.getAbsolutePath());
            UmdIsoFile param = reader.getFile("PSP_GAME/PARAM.SFO");
            UmdIsoFile icon = reader.getFile("PSP_GAME/ICON0.PNG");
            UmdIsoFile snd;
            try {
                snd = reader.getFile("PSP_GAME/SND0.AT3");

            } catch (Exception e) {
                snd = null;
            }

            ParamSFO sfo = ParamSFO.ofStream(param);
            if (snd != null) {
                File tempAudioFile = File.createTempFile("PSPTOOLS", "TEMP.at3");
                tempAudioFile.deleteOnExit();

                Files.write(tempAudioFile.toPath(), snd.readNBytes((int) snd.length()), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                return new ParamSFOListElement(sfo,
                        null,
                        icon.readNBytes((int) icon.length()),
                        // new ProcessBuilder(
                        // "C:\\Users\\mathd\\Downloads\\ffplay.exe -i " +
                        // tempAudioFile.getAbsolutePath()
                        // + " -nodisp -loop 0"),
                        new ProcessBuilder(
                                "C:\\Users\\mathd\\Downloads\\ffplay.exe",
                                "-i",
                                tempAudioFile.getAbsolutePath(),
                                "-nodisp",
                                "-loop",
                                "0")
                                .redirectErrorStream(true)
                                .directory(new File(System.getProperty("user.dir"))),
                        selectedFunction);
            } else {
                return new ParamSFOListElement(sfo,
                        null,
                        icon.readNBytes((int) icon.length()),
                        null,
                        selectedFunction);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ImageIcon getIcon0() {
        return icon0;
    }

    public ImageIcon getPic1() {
        return icon0;
    }

    public ParamSFOListElement(ParamSFO ParamSFO, File dir, SFOListElementListiener selectedFunction)
            throws MalformedURLException, IOException, URISyntaxException, NameNotFoundException {
        this(ParamSFO, dir,
                Files.readAllBytes(Path.of(dir.getAbsolutePath(), "Icon0.png").toFile().exists()
                        ? Path.of(dir.getAbsolutePath(), "Icon0.png")
                        : Path.of(ParamSFOListElement.class.getResource("/no_icon0.png").toURI())),
                null,
                selectedFunction);
        // if (ParamSFO != null)
        // System.out.println(ParamSFO.getParam(Params.Title));
    }

    public ParamSFOListElement(ParamSFO ParamSFO, File dir, byte[] icon0Data, ProcessBuilder playAudio,
            SFOListElementListiener selectedFunction)
            throws MalformedURLException, NameNotFoundException {
        super();
        // System.out.println("SGIMAS");
        this.sfo = ParamSFO;
        this.selectedFunc = selectedFunction;
        this.dir = dir;
        this.playAudioProcess = playAudio;

        Path backupPath = Path.of(System.getProperty("user.home"), "PSPSaveBackups", getBackupName());

        ImageIcon rawIcon = new ImageIcon(icon0Data);
        this.icon0 = new ImageIcon(icon0Data);

        RightClickMenu.add("Delete").addActionListener(ac -> selectedFunction.delete(this));
        if (sfo != null) {
            RightClickMenu.add("Backup").addActionListener(ac -> selectedFunction.backup());
            if (backupPath.toFile().exists())
                RightClickMenu.add("Restore").addActionListener(ac -> selectedFunction.restore());
        }

        Icon0.setIcon(ImageUtilites.ResizeIcon(rawIcon, 90, 50));

        SFOTitle.setFont(SFOTitle.getFont().deriveFont(Font.BOLD, 12f));

        if (sfo != null) {
            System.out.println(sfo.getParam(Params.Category).toString());
            switch (sfo.getParam(Params.Category).toString().trim()) {
                case "MS": // memory stick save

                    RightClickMenu.setLabel(
                            sfo.getParam(Params.Title).toString() + " (" + (String) sfo.getParam(Params.SaveFolderName)
                                    + ")");

                    SFOTitle.setText((String) sfo.getParam(Params.SaveTitle));

                    SFODesc.setFont(SFODesc.getFont().deriveFont(10f));

                    SFODesc.setText((String) sfo.getParam(Params.Description, true));

                    setToolTipText(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam(Params.SaveFolderName)
                                    + ")");
                    break;

                case "UG": // umd game

                    SFOTitle.setText((String) sfo.getParam(Params.Title));

                    SFODesc.setFont(SFODesc.getFont().deriveFont(10f));

                    SFODesc.setText((String) sfo.getParam(Params.DiscVersion, true));

                    setToolTipText(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam(Params.DiscID)
                                    + ")");
                    RightClickMenu.setLabel(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam(Params.DiscID)
                                    + ")");
                    break;

                default:
                    break;
            }
        } else {
            SFOTitle.setText(dir.getName());
            SFODesc.setText("");
        }

        SFODesc.setHorizontalAlignment(SwingConstants.LEFT);
        SFODesc.setVerticalAlignment(SwingConstants.TOP);

        Lay.putConstraint(SpringLayout.WEST, Icon0, 4, SpringLayout.WEST, this);
        Lay.putConstraint(SpringLayout.VERTICAL_CENTER, Icon0, 0, SpringLayout.VERTICAL_CENTER, this);

        Lay.putConstraint(SpringLayout.WEST, SFOTitle, 4, SpringLayout.EAST, Icon0);
        Lay.putConstraint(SpringLayout.NORTH, SFOTitle, 0, SpringLayout.NORTH, this);
        Lay.putConstraint(SpringLayout.EAST, SFOTitle, -4, SpringLayout.EAST, this);

        Lay.putConstraint(SpringLayout.EAST, BackedUp, -4, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.VERTICAL_CENTER, BackedUp, 0, SpringLayout.VERTICAL_CENTER, this);

        Lay.putConstraint(SpringLayout.EAST, SFODesc, 0, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.WEST, SFODesc, 0, SpringLayout.WEST, SFOTitle);
        Lay.putConstraint(SpringLayout.NORTH, SFODesc, 0, SpringLayout.SOUTH, SFOTitle);
        Lay.putConstraint(SpringLayout.SOUTH, SFODesc, 0, SpringLayout.SOUTH, this);

        setComponentPopupMenu(RightClickMenu);

        add(SFOTitle);
        add(Icon0);
        add(SFODesc);
        if (backupPath.toFile().exists()) {
            backuped = true;
            add(BackedUp);
        }

        setLayout(Lay);
        setBorder(border);
        setMinimumSize(Size);
        setMaximumSize(Size);
        setPreferredSize(Size);
        addMouseListener(this);
        setBackground(new Color(0.3f, 0.3f, 0.3f, 1));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON2)
            selectedFunc.selected(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setBorder(selectedBorder);
        getParent().repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setBorder(border);
        getParent().repaint();
    }
}
