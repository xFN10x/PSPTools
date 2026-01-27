package fn10.psptools.ui.components;

import javax.naming.NameNotFoundException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import com.formdev.flatlaf.ui.FlatLineBorder;

import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.psps.RealPSPFile;
import fn10.psptools.psp.sfo.ParamSFO;
import fn10.psptools.psp.sfo.ParamSFO.Params;
import fn10.psptools.ui.interfaces.SFOListElementListener;
import fn10.psptools.util.ImageUtilites;
import fn10.psptools.util.SavedVariables;
import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.filesystems.umdiso.UmdIsoReader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ParamSFOListElement extends JPanel implements MouseListener {

    public static ParamSFOListElement makeEmpty(SFOListElementListener listiener)
            throws NameNotFoundException, IOException {
        return new ParamSFOListElement(
                ParamSFO.ofStream(ParamSFOListElement.class.getResourceAsStream("/EmptyParam/PARAM.SFO")), null,
                ParamSFOListElement.class.getResourceAsStream("/EmptyParam/ICON0.PNG").readAllBytes(),
                ParamSFOListElement.class.getResourceAsStream("/EmptyParam/PIC1.PNG").readAllBytes(), null, null,
                listiener);
    }

    private static final Dimension Size = new Dimension(290, 60);
    private static final FlatLineBorder border = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 2, 8);
    private static final FlatLineBorder selectedBorder = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 4, 8);

    public final ParamSFO sfo;
    public String videoDir = null;
    private final ImageIcon icon0;
    private final ImageIcon pic1;
    public final PSPDirectory dir;
    private final SpringLayout Lay = new SpringLayout();

    private final JLabel SFOTitle = new JLabel();
    private final JLabel SFODesc = new JLabel();
    private final JLabel Icon0 = new JLabel();
    private final JLabel HasAudio = new JLabel();
    private final JLabel HasVideo = new JLabel();
    private final JLabel BackedUp = new JLabel(new ImageIcon(getClass().getResource("/backed.png")));

    private final JPopupMenu RightClickMenu = new JPopupMenu();

    private final SFOListElementListener selectedFunc;

    public boolean backuped = false;
    public boolean hasAudio = false;

    public String getBackupName() {
        try {
            if (sfo == null)
                return "";
            switch (sfo.getParam(Params.Category).toString().trim()) {
                case "MS":

                    return (sfo.getParam(Params.SaveFolderName).toString()).replace("\u0000", "").replace(":", " ")
                            + ".zip";

                case "sd": // psvita,4,5 save data
                    if (sfo.paramData.containsKey("PARENT_DIRECTORY")) // vita
                        return sfo.getParam("PARENT_DIRECTORY").toString().replace("/", "").replace("\u0000", "")
                                .replace(":", "")
                                .replace("\n", " ").replace(" ", "-") + ".zip";
                    else
                        return sfo.getParam("TITLE_ID").toString().replace("/", "").replace("\u0000", "")
                                .replace(":", "")
                                .replace("\n", " ").replace(" ", "-") + ".zip";

                default:
                    if (sfo != null)
                        return sfo.getParam(Params.Title).toString().replace("\u0000", "").replace(":", "")
                                .replace("\n", " ").replace(" ", "-") + ".zip";
                    else
                        return dir.getName().replace(":", "")
                                .replace("\n", " ").replace(" ", "-") + ".zip";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void delete() throws IOException {
        dir.delete();
    }

    public ImageIcon getIcon0() {
        return icon0;
    }

    public ImageIcon getPic1() {
        return pic1;
    }

    public String getTitle() {
        return SFOTitle.getText();
    }

    public String getDescription() {
        return SFODesc.getText();
    }

    public static ParamSFOListElement ofIso(PSPFile iso, SFOListElementListener selectedFunction) {
        try {

            UmdIsoReader reader = new UmdIsoReader(iso);

            UmdIsoFile param = reader.getFile("PSP_GAME/PARAM.SFO");
            UmdIsoFile icon = reader.getFile("PSP_GAME/ICON0.PNG");
            UmdIsoFile bg = reader.getFile("PSP_GAME/PIC1.PNG");
            UmdIsoFile icon1;
            UmdIsoFile snd;
            try {
                snd = reader.getFile("PSP_GAME/SND0.AT3");

            } catch (Exception e) {
                snd = null;
            }
            try {
                icon1 = reader.getFile("PSP_GAME/ICON1.PMF");

            } catch (Exception e) {
                icon1 = null;
            }

            ParamSFO sfo = ParamSFO.ofStream(param);

            return new ParamSFOListElement(sfo,
                    null,
                    icon.readNBytes((int) icon.length()),
                    bg.readNBytes((int) bg.length()),
                    icon1 != null ? icon1.readAllBytes() : null,
                    snd != null ? snd.readAllBytes() : null,
                    selectedFunction);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readFileWithNameFromPSPFileButIfItDoesntExistReturnTheseBytesInstead(PSPDirectory dir,
            String name, byte[] instead) {
        PSPFile file = dir.getFileWithName(name);
        if (file == null)
            return instead;
        return file.readAll();
    }

    private File tempAudioFile = null;
    private final byte[] audioData;

    public File getTempAudioFile() throws IOException {
        if (audioData == null) return null;
        if (tempAudioFile == null) {
            tempAudioFile = File.createTempFile("PSPTOOLS", "TEMPAUDIOFILE");
            Files.write(tempAudioFile.toPath(), audioData, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return tempAudioFile;
        } else
            return tempAudioFile;
    }

    public ParamSFOListElement(ParamSFO ParamSFO, PSPDirectory dir, SFOListElementListener selectedFunction)
            throws MalformedURLException, IOException, URISyntaxException, NameNotFoundException {
        this(ParamSFO, dir,
                readFileWithNameFromPSPFileButIfItDoesntExistReturnTheseBytesInstead(dir, "icon0.png",
                        ParamSFOListElement.class.getResourceAsStream("/no_icon0.png").readAllBytes()),
                readFileWithNameFromPSPFileButIfItDoesntExistReturnTheseBytesInstead(dir, "pic1.png",
                        ParamSFOListElement.class.getResourceAsStream("/no_icon0.png").readAllBytes()),
                dir.getFileStartingWith("icon1").readAll(),
                dir.getFileWithName("snd0.at3").readAll(),
                selectedFunction);
        // if (ParamSFO != null)
        // System.out.println(ParamSFO.getParam(Params.Title));
    }

    public ParamSFOListElement(String Title, String Desc, byte[] IconData, SFOListElementListener selectedFunc)
            throws NameNotFoundException, IOException {
        this(null, null,
                IconData, IconData, null, null, selectedFunc);

        SFOTitle.setText(Title);
        SFODesc.setText(Desc);
    }

    public ParamSFOListElement(ParamSFO ParamSFO, PSPDirectory dir, byte[] icon0Data, byte[] pic1Data, byte[] icon1Data,
            byte[] snd0Data,
            SFOListElementListener selectedFunction)
            throws NameNotFoundException, IOException {
        super();
        // System.out.println("SGIMAS");
        this.sfo = ParamSFO;
        this.selectedFunc = selectedFunction;
        this.dir = dir;
        this.audioData = snd0Data;

        if (snd0Data != null)
            this.hasAudio = true;

        Path backupPath = Path.of(SavedVariables.DataFolder.toString(), "PSPSaveBackups", getBackupName());

        ImageIcon rawIcon = new ImageIcon(icon0Data);
        this.icon0 = new ImageIcon(icon0Data);
        if (icon1Data != null) {
            File icon1 = File.createTempFile("PSPTOOLS", "TEMPICON1.PMF");

            Files.write(icon1.toPath(), icon1Data, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            icon1.deleteOnExit();

            this.videoDir = icon1.getAbsolutePath();
        }

        this.pic1 = new ImageIcon(pic1Data);

        if (dir != null)
            RightClickMenu.add("Delete").addActionListener(ac -> selectedFunction.delete(this));
        if (sfo != null && sfo.getParam(Params.Category).toString().trim().replace("\u0000", "").equals("MS")) {
            RightClickMenu.add("Backup").addActionListener(ac -> selectedFunction.backup());
            if (backupPath.toFile().exists())
                RightClickMenu.add("Restore").addActionListener(ac -> selectedFunction.restore());
        }
        System.out.println(backupPath.toString());
        System.out.println(dir);

        backuped = backupPath.toFile().exists() && dir != null && sfo != null;

        Icon0.setIcon(ImageUtilites.ResizeIcon(rawIcon, 90, 50));

        SFOTitle.setFont(SFOTitle.getFont().deriveFont(Font.BOLD, 12f));

        if (sfo != null) {
            switch (sfo.getParam(Params.Category).toString().trim()) {
                case "MS": // memory stick save

                    RightClickMenu.setLabel(
                            sfo.getParam(Params.Title).toString() + " ("
                                    + (String) sfo.getParam(Params.SaveFolderName)
                                    + ")");

                    SFOTitle.setText((String) sfo.getParam(Params.SaveTitle));

                    SFODesc.setFont(SFODesc.getFont().deriveFont(10f));

                    SFODesc.setText((String) sfo.getParam(Params.Description, true));

                    setToolTipText(
                            (String) sfo.getParam(Params.Title) + " ("
                                    + (String) sfo.getParam(Params.SaveFolderName)
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

                case "DG": // PS3 disc game

                    SFOTitle.setText((String) sfo.getParam(Params.Title, true));

                    SFODesc.setFont(SFODesc.getFont().deriveFont(10f));

                    SFODesc.setText((String) sfo.getParam("TITLE_ID", true));

                    setToolTipText(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam("TITLE_ID")
                                    + ")");
                    RightClickMenu.setLabel(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam("TITLE_ID")
                                    + ")");
                    break;

                case "PT": // PSPTools
                    SFOTitle.setText((String) sfo.getParam(Params.Title, true));
                    break;

                case "sd": // PSVita, 4, 5
                    if (sfo.paramData.containsKey("PARENT_DIRECTORY")) // vita
                        SFOTitle.setText((String) sfo.getParam("PARENT_DIRECTORY", true).toString().replace("/", ""));
                    else
                        SFOTitle.setText((String) sfo.getParam("MAINTITLE", true).toString().replace("/", ""));
                    break;

                default:
                    SFOTitle.setText((String) sfo.getParam(Params.Title, true));

                    SFODesc.setFont(SFODesc.getFont().deriveFont(10f));

                    SFODesc.setText((String) sfo.getParam("TITLE_ID", true));

                    setToolTipText(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam("TITLE_ID")
                                    + ")");
                    RightClickMenu.setLabel(
                            (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam("TITLE_ID")
                                    + ")");
                    break;
            }
        } else if (dir != null) {
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

        Lay.putConstraint(SpringLayout.EAST, HasAudio, -4, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.SOUTH, HasAudio, -4, SpringLayout.SOUTH, this);

        Lay.putConstraint(SpringLayout.EAST, HasVideo, -4, SpringLayout.WEST, HasAudio);
        Lay.putConstraint(SpringLayout.SOUTH, HasVideo, -4, SpringLayout.SOUTH, this);

        Lay.putConstraint(SpringLayout.EAST, BackedUp, -4, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.VERTICAL_CENTER, BackedUp, 0, SpringLayout.VERTICAL_CENTER, this);

        Lay.putConstraint(SpringLayout.EAST, SFODesc, 0, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.WEST, SFODesc, 0, SpringLayout.WEST, SFOTitle);
        Lay.putConstraint(SpringLayout.NORTH, SFODesc, 0, SpringLayout.SOUTH, SFOTitle);
        Lay.putConstraint(SpringLayout.SOUTH, SFODesc, 0, SpringLayout.SOUTH, this);

        setComponentPopupMenu(RightClickMenu);

        HasVideo.setIcon(new ImageIcon(getClass().getResource("/hasVideo.png")));
        HasAudio.setIcon(new ImageIcon(getClass().getResource("/hasAudio.png")));

        add(SFOTitle);
        add(Icon0);
        add(SFODesc);
        if (backuped) {
            add(BackedUp);
        }
        if (icon1Data != null) {
            add(HasVideo);
        }
        if (snd0Data != null) {
            add(HasAudio);
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
