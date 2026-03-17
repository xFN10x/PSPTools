package fn10.psptools.ui.components;

import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.ui.NewLaunchPage;
import fn10.psptools.ui.SFOBasedManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PSPFileListElement extends JPanel implements MouseListener {

    private final SpringLayout lay = new SpringLayout();
    private final JLabel nameLabel = new JLabel();
    private final JButton specialButton = new JButton();

    public final PSPFileDirectory fileDir;

    private final Color hovered;
    private final Color unhovered;
    private boolean selected = false;
    private final ArrayList<PSPFileListElement> selectedList;

    public PSPFileListElement(NewLaunchPage lp, PSPFileDirectory pfd, ArrayList<PSPFileListElement> selectedList) {
        this(lp,pfd, null, selectedList);
    }

    private void setSpecialButton(String text, ActionListener onClick) {
        specialButton.setText(text);
        specialButton.setVisible(true);
        specialButton.addActionListener(onClick);
    }

    public PSPFileListElement(NewLaunchPage lp, PSPFileDirectory pfd, Color bg, ArrayList<PSPFileListElement> selectedList) {
        setLayout(lay);
        Dimension dimension = new Dimension(0, 20);
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
        addMouseListener(this);
        specialButton.setVisible(false);
        if (bg != null)
            setBackground(bg);
        this.fileDir = pfd;
        this.selectedList = selectedList;

        unhovered = getBackground();
        hovered = Color.lightGray.darker();

        String name = pfd.getDirectory().getName();
        if (pfd.isDirectory() && (name.equalsIgnoreCase("ISO") || name.equalsIgnoreCase("GAME"))) {
            nameLabel.setIcon(new ImageIcon(getClass().getResource("/fileIcons/iso.png")));
            nameLabel.setText(name);
            setSpecialButton("Open Manager", _ -> {
                new SFOBasedManager(lp, SFOBasedManager.GAMES_MODE, "SFO Manager: " + name, pfd.getDirectory()).setVisible(true);
            });
        } else if (pfd.isDirectory()) {
            nameLabel.setIcon(new ImageIcon(getClass().getResource("/fileIcons/generic-folder.png")));
            nameLabel.setText(name);
        } else if (pfd.isFile()) {
            nameLabel.setIcon(new ImageIcon(getClass().getResource("/fileIcons/generic-icon.png")));
            nameLabel.setText(pfd.getFile().getName());
        } else {
            nameLabel.setIcon(new ImageIcon(getClass().getResource("/fileIcons/generic-icon.png")));
            nameLabel.setText("???");
        }

        lay.putConstraint(SpringLayout.WEST, nameLabel, 5, SpringLayout.WEST, this);
        lay.putConstraint(SpringLayout.VERTICAL_CENTER, nameLabel, 0, SpringLayout.VERTICAL_CENTER, this);

        lay.putConstraint(SpringLayout.VERTICAL_CENTER, specialButton, 0, SpringLayout.VERTICAL_CENTER, this);
        lay.putConstraint(SpringLayout.EAST, specialButton, -5, SpringLayout.EAST, this);

        add(nameLabel);
        add(specialButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int red = getBackground().getRed();
        int green = getBackground().getGreen();
        int blue = getBackground().getBlue();
        int avg = (red + blue + green) / 3;
        Color inverted = new Color(
                255 - red,
                255 - green,
                255 - blue
        );
        if (avg >= 190) {
            nameLabel.setForeground(inverted.darker());
        } else
            nameLabel.setForeground(Color.WHITE);
    }


    public void unselect() {
        selected = false;
        mouseExited(null);
        selectedList.remove(this);
    }

    public void select() {
        selected = true;
        mouseExited(null);
        selectedList.add(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public String getFileName() {
        return (fileDir.isDirectory() ? fileDir.getDirectory().getName() : fileDir.getFile().getName());
    }

    public void rightClickCheck(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) return;

        JPopupMenu jPopupMenu = new JPopupMenu();
        if (selectedList.size() == 1) {
            jPopupMenu.setBorder(new TitledBorder("Manage " + (fileDir.isDirectory() ? "Folder" : "File") + ": " + getFileName()));
            if (fileDir.isDirectory()) {
                jPopupMenu.add("Go into...").addActionListener(_ -> {
                    NewLaunchPage.current.showDir(fileDir.getDirectory().getPath());
                });
                jPopupMenu.addSeparator();
            }
            jPopupMenu.add("Delete").addActionListener(_ -> {
                int option = JOptionPane.showConfirmDialog(getParent(), "Are you sure you want delete " + getFileName() + "?");
                if (option == JOptionPane.YES_OPTION) {
                    if (fileDir.isDirectory()) {
                        fileDir.getDirectory().delete();
                    } else {
                        fileDir.getFile().delete();
                    }
                    getParent().remove(this);
                }
            });
        } else {

        }
        jPopupMenu.show(this, e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {

        Component[] components = getParent().getComponents();
        //System.out.println(List.of(components).indexOf(this));
        if (e.isShiftDown() && !e.isPopupTrigger()) {
            select();
            List<Component> list = List.of(components);
            ArrayList<PSPFileListElement> copy = new ArrayList<>(selectedList);
            copy.sort(Comparator.comparingInt(list::indexOf));
            int lowest = list.indexOf(copy.getFirst());
            int highest = list.indexOf(copy.getLast());
            for (Component component : list) {
                int i = list.indexOf(component);
                if (component instanceof PSPFileListElement && i > lowest && i < highest) {
                    ((PSPFileListElement) component).select();
                }
            }
            rightClickCheck(e);
            return;
        }
        if (!e.isControlDown() && !e.isPopupTrigger()) {
            for (Component component : components) {
                if (component instanceof PSPFileListElement) {
                    ((PSPFileListElement) component).unselect();
                }
            }
        }

        select();
        rightClickCheck(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (selected)
            setBackground(hovered.brighter().brighter());
        else
            setBackground(hovered);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (selected)
            setBackground(hovered.brighter());
        else
            setBackground(unhovered);
    }
}
