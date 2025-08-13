package psptools.ui.components;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import org.apache.commons.io.FileUtils;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.palantir.isofilereader.isofilereader.GenericInternalIsoFile;
import com.palantir.isofilereader.isofilereader.IsoFileReader;

import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.filesystems.umdiso.UmdIsoReader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import psptools.psp.sfo.ParamSFO;
import psptools.psp.sfo.ParamSFO.Params;
import psptools.ui.interfaces.SFOListElementListiener;
import psptools.util.ImageUtilites;

public class ParamSFOListElement extends JPanel implements MouseListener {

    private static final Dimension Size = new Dimension(290, 60);
    private static final FlatLineBorder border = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 2, 8);
    private static final FlatLineBorder selectedBorder = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 4, 8);

    public final ParamSFO sfo;
    public final File dir;
    private final SpringLayout Lay = new SpringLayout();

    private final JLabel SaveTitle = new JLabel();
    private final JLabel SaveDesc = new JLabel();
    private final JLabel SaveIconTitle = new JLabel();
    private final JLabel SaveBackedup = new JLabel(new ImageIcon(getClass().getResource("/backed.png")));

    private final JPopupMenu RightClickMenu = new JPopupMenu();

    private final SFOListElementListiener selectedFunc;

    public boolean backuped = false;

    private String getBackupName() {
        if (sfo == null)
            return "";
        return (sfo.getParam(Params.SaveTitle).toString() + "-"
                + sfo.getParam(Params.SaveFolderName).toString()).replace("\u0000", "") + ".zip";
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

            ParamSFO sfo = ParamSFO.ofStream(param);

            return new ParamSFOListElement(sfo,
                    null,
                    icon.readAllBytes(), selectedFunction);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ParamSFOListElement(ParamSFO ParamSFO, File dir, SFOListElementListiener selectedFunction)
            throws MalformedURLException, IOException, URISyntaxException {
        this(ParamSFO, dir,
                Files.readAllBytes(Path.of(dir.getAbsolutePath(), "Icon0.png").toFile().exists()
                        ? Path.of(dir.getAbsolutePath(), "Icon0.png")
                        : Path.of(ParamSFOListElement.class.getResource("/bg.png").toURI())),
                selectedFunction);
                if (ParamSFO != null)
        System.out.println(ParamSFO.getParam(Params.Title));
    }

    public ParamSFOListElement(ParamSFO ParamSFO, File dir, byte[] imageData, SFOListElementListiener selectedFunction)
            throws MalformedURLException {
        super();

        this.sfo = ParamSFO;
        this.selectedFunc = selectedFunction;
        this.dir = dir;

        Path backupPath = Path.of(System.getProperty("user.home"), "PSPSaveBackups", getBackupName());

        ImageIcon rawIcon = new ImageIcon(imageData);

        if (sfo != null)
            RightClickMenu.setLabel(
                    sfo.getParam(Params.Title).toString() + " (" + (String) sfo.getParam(Params.SaveFolderName) + ")");

        RightClickMenu.add("Delete").addActionListener(ac -> selectedFunction.delete(this));
        if (sfo != null) {
            RightClickMenu.add("Backup").addActionListener(ac -> selectedFunction.backup());
            if (backupPath.toFile().exists())
                RightClickMenu.add("Restore").addActionListener(ac -> selectedFunction.restore());
        }

        SaveIconTitle.setIcon(ImageUtilites.ResizeIcon(rawIcon, 90, 50));

        SaveTitle.setFont(SaveTitle.getFont().deriveFont(11f));
        if (sfo != null)
            SaveTitle.setText((String) sfo.getParam(Params.SaveTitle));
        else
            SaveTitle.setText(dir.getName());

        SaveDesc.setFont(SaveDesc.getFont().deriveFont(10f));
        if (sfo != null)
            SaveDesc.setText((String) sfo.getParam(Params.Description, true));
        else
            SaveDesc.setText("");

        SaveDesc.setHorizontalAlignment(SwingConstants.LEFT);
        SaveDesc.setVerticalAlignment(SwingConstants.TOP);

        Lay.putConstraint(SpringLayout.WEST, SaveIconTitle, 4, SpringLayout.WEST, this);
        Lay.putConstraint(SpringLayout.VERTICAL_CENTER, SaveIconTitle, 0, SpringLayout.VERTICAL_CENTER, this);

        Lay.putConstraint(SpringLayout.WEST, SaveTitle, 4, SpringLayout.EAST, SaveIconTitle);
        Lay.putConstraint(SpringLayout.NORTH, SaveTitle, 0, SpringLayout.NORTH, this);
        Lay.putConstraint(SpringLayout.EAST, SaveTitle, -4, SpringLayout.EAST, this);

        Lay.putConstraint(SpringLayout.EAST, SaveBackedup, -4, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.VERTICAL_CENTER, SaveBackedup, 0, SpringLayout.VERTICAL_CENTER, this);

        Lay.putConstraint(SpringLayout.EAST, SaveDesc, 0, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.WEST, SaveDesc, 0, SpringLayout.WEST, SaveTitle);
        Lay.putConstraint(SpringLayout.NORTH, SaveDesc, 0, SpringLayout.SOUTH, SaveTitle);
        Lay.putConstraint(SpringLayout.SOUTH, SaveDesc, 0, SpringLayout.SOUTH, this);

        setComponentPopupMenu(RightClickMenu);

        add(SaveTitle);
        add(SaveIconTitle);
        add(SaveDesc);
        if (backupPath.toFile().exists()) {
            backuped = true;
            add(SaveBackedup);
        }
        if (sfo != null)
            setToolTipText(
                    (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam(Params.SaveFolderName) + ")");

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
