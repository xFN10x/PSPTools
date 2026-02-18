/*
    PSPTools - Management Utility for your PSP.
    Copyright (C) 2026 xFN10x (https://github.com/xFN10x)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package fn10.psptools.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListener;
import fn10.psptools.util.ImageUtilites;

public class SFOBasedSelector extends JDialog implements SFOListElementListener {

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
                PSP.getCurrentPSP().getFolder("PSP", "SAVEDATA"));

        selector.setVisible(true);

        return selector.selected;
    }

    private SFOBasedSelector(Frame parent, int mode, String title, PSPDirectory... targets) {
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

        SFOBasedManager.FillOutWindow(InnerSFOFolderViewer, this, targets);
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

    @Override
    public void onThreadCreate(Thread thread) {}
}
