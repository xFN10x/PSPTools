package fn10.psptools.ui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NameNotFoundException;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import fn10.psptools.psp.sfo.ParamSFO;

public class SFOViewer extends JDialog {

    public SFOViewer(Window parent, ParamSFO sfo) {
        super(parent);
        setSize(500, 500);
        setTitle("Viewing SFO");

        List<String[]> data = new ArrayList<>();

        for (Entry<String, byte[]> entry : sfo.paramData.entrySet()) {
            try {
                String paramName = entry.getKey();
                String paramType;
                if (sfo.dataTypes.get(entry.getKey()) == ParamSFO.UTF8) {
                    paramType = "UTF8";
                } else if (sfo.dataTypes.get(entry.getKey()) == ParamSFO.UTF8_S) {
                    paramType = "UTF8 (Null Terminated)";
                } else {
                    paramType = "INT32";
                }
                String paramValue = sfo.getParam(entry.getKey()).toString();

                data.add(new String[] { paramName, paramType, paramValue });
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        JTable table = new JTable();
        table.setModel(new DefaultTableModel(data.toArray(new String[0][0]), new String[] { "Name", "Type", "Value" }) {
            @Override
            // https://stackoverflow.com/questions/1990817/how-to-make-a-jtable-non-editable
            public boolean isCellEditable(int row, int column) {
                // all cells false
                return false;
            }
        });
        add(table);
    }
}
