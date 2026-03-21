package fn10.psptools.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AtomicActionListener implements ActionListener {
    
    private ActionListener real = null;
    
    public void setListener(ActionListener set) {
        this.real = set;
    }
    
    public AtomicActionListener(ActionListener set) {
        setListener(set);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (real != null)
            real.actionPerformed(e);
    }
}
