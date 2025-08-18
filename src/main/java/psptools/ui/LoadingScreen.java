package psptools.ui;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import java.awt.*;

/**
 * derived from bedrockR's RLoadingScreen
 */
public class LoadingScreen extends JDialog {

    protected JProgressBar MainBar = new JProgressBar();
    protected JLabel MainText = new JLabel("Loading...");;

    public Integer Steps = null;

    public final SpringLayout Lay = new SpringLayout();

    public void changeText(String text) {
        try {
            SwingUtilities.invokeLater(() -> {
                MainText.setText(text);
            });
        } catch (Exception e) {
            System.err.println("Failed to change progress message.");
        }
    }

    public void increaseProgress(int increase, String TextChange) {
        changeText(TextChange);
        SwingUtilities.invokeLater(() -> {
            if (!MainBar.isIndeterminate())
                MainBar.setIndeterminate(false);
            MainBar.setValue(MainBar.getValue() + increase);
        });
    }

    public void setProgress(int progess) {
        SwingUtilities.invokeLater(() -> {
            if (MainBar.isIndeterminate())
                MainBar.setIndeterminate(false);
            MainBar.setValue(progess);
        });
    }

    // completly gets rid of window
    public void destroy() {
        dispose();
    }

    public void increaseProgressBySteps(String TextChange) throws IllegalAccessException {
        if (Steps == null)
            throw new IllegalArgumentException("Total steps not set yet.");
        if (!MainBar.isIndeterminate())
            SwingUtilities.invokeLater(() -> {
                MainBar.setIndeterminate(false);
            });

        changeText(TextChange);

        SwingUtilities.invokeLater(() -> {
            MainBar.setValue(MainBar.getValue() + 100 / Steps);
        });
    }

    public LoadingScreen(Frame Parent) {
        super(Parent, "Loading");
        // super(Parent, DO_NOTHING_ON_CLOSE, "Loading");

        setModal(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(new Dimension(300, 100));
        setResizable(false);
        setLayout(Lay);

        setLocation(LaunchPage.getScreenCenter(this));
        MainBar.setOrientation(JProgressBar.HORIZONTAL);

        MainBar.setValue(0);
        MainBar.setIndeterminate(true);

        // progress bar
        Lay.putConstraint(SpringLayout.SOUTH, MainBar, -5, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.NORTH, MainBar, -30, SpringLayout.SOUTH, getContentPane());
        Lay.putConstraint(SpringLayout.EAST, MainBar, -5, SpringLayout.EAST, getContentPane());
        Lay.putConstraint(SpringLayout.WEST, MainBar, 5, SpringLayout.WEST, getContentPane());
        // progress text
        Lay.putConstraint(SpringLayout.SOUTH, MainText, -3, SpringLayout.NORTH, MainBar);
        Lay.putConstraint(SpringLayout.WEST, MainText, 0, SpringLayout.WEST, MainBar);

        add(MainText);
        add(MainBar);
    }
}
