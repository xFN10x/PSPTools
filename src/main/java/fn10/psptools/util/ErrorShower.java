package fn10.psptools.util;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import static fn10.psptools.PSPTools.log;


/**
 * this is from bedrockR
 */
public class ErrorShower {

    public static void full(Component parent, Exception ex) {
        full(parent, "No message provided.", ex);
    }

    public static void full(Component parent, String msg, Exception ex) {
        log.error(msg, ex);

        showError(parent, msg, ex);
    }

    public static void showError(Component parent, String msg, String title, Exception ex) {

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));

        String trun;
        if (sw.toString().length() >= 1000)
            trun = sw.toString().substring(0, 1000);
        else
            trun = sw.toString();

        var message = msg + "\n" + trun;

        JOptionPane.showMessageDialog(parent,
                message,
                title, JOptionPane.ERROR_MESSAGE);

    }

    public static void showError(Component parent, String msg, Exception ex) {
        showError(parent, msg, ex.getMessage(), ex);
    }
}
