package psptools.ui.components;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.ui.FlatLineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;

import psptools.psp.sfo.ParamSFO;
import psptools.psp.sfo.ParamSFO.Params;
import psptools.util.ImageUtilites;

public class ParamSFOListElement extends JPanel implements MouseListener {

    private static final Dimension Size = new Dimension(290, 60);
    private static final FlatLineBorder border = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 2, 8);
    private static final FlatLineBorder selectedBorder = new FlatLineBorder(new Insets(3, 3, 3, 3), Color.white, 4, 8);

    private final ParamSFO sfo;
    private final SpringLayout Lay = new SpringLayout();

    private final JLabel SaveTitle = new JLabel();
    private final JLabel SaveDesc = new JLabel();
    private final JLabel SaveIconTitle = new JLabel();

    private final Runnable selectedFunc;

    public ParamSFOListElement(ParamSFO ParamSFO, File dir, Runnable selectedFunction) throws MalformedURLException {
        super();

        this.sfo = ParamSFO;
        this.selectedFunc = selectedFunction;

        ImageIcon rawIcon = new ImageIcon(Path.of(dir.getAbsolutePath(), "Icon0.png").toUri().toURL());

        SaveIconTitle.setIcon(ImageUtilites.ResizeIcon(rawIcon, 90, 50));

        SaveTitle.setFont(SaveTitle.getFont().deriveFont(11f));
        SaveTitle.setText((String) sfo.getParam(Params.SaveTitle));

        SaveDesc.setFont(SaveDesc.getFont().deriveFont(10f));
        SaveDesc.setText((String) sfo.getParam(Params.Description, true));
        SaveDesc.setHorizontalAlignment(SwingConstants.LEFT);
        SaveDesc.setVerticalAlignment(SwingConstants.TOP);

        Lay.putConstraint(SpringLayout.WEST, SaveIconTitle, 4, SpringLayout.WEST, this);
        Lay.putConstraint(SpringLayout.VERTICAL_CENTER, SaveIconTitle, 0, SpringLayout.VERTICAL_CENTER, this);

        Lay.putConstraint(SpringLayout.WEST, SaveTitle, 4, SpringLayout.EAST, SaveIconTitle);
        Lay.putConstraint(SpringLayout.NORTH, SaveTitle, 0, SpringLayout.NORTH, this);
        Lay.putConstraint(SpringLayout.EAST, SaveTitle, -4, SpringLayout.EAST, this);

        Lay.putConstraint(SpringLayout.EAST, SaveDesc, 0, SpringLayout.EAST, this);
        Lay.putConstraint(SpringLayout.WEST, SaveDesc, 0, SpringLayout.WEST, SaveTitle);
        Lay.putConstraint(SpringLayout.NORTH, SaveDesc, 0, SpringLayout.SOUTH, SaveTitle);
        Lay.putConstraint(SpringLayout.SOUTH, SaveDesc, 0, SpringLayout.SOUTH, this);

        add(SaveTitle);
        add(SaveIconTitle);
        add(SaveDesc);

        setToolTipText(
                (String) sfo.getParam(Params.Title) + " (" + (String) sfo.getParam(Params.Save_Folder_Name) + ")");

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
        selectedFunc.run();
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
