package fn10.psptools.psp.psps.real;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;

public class RealPSPRoot {

    private final File root;
    private final String name;

    protected RealPSPRoot(File root) {
        this.root = root;
        this.name = FileSystemView.getFileSystemView().getSystemDisplayName(root);
    }

    public static RealPSPRoot[] getRoots() {
        ArrayList<RealPSPRoot> list = new ArrayList<>();
        for (File root : File.listRoots()) {
            list.add(new RealPSPRoot(root));
        }
        return list.toArray(new RealPSPRoot[0]);
    }

    public File getFile() {
        return root;
    }

    public boolean equals(Object other) {
        if (other instanceof RealPSPRoot) {
            return ((RealPSPRoot)other).root.equals(root);
        } else
            return false;
    }

    public String toString() {
        return name;
    }
}
