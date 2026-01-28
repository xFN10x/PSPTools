package fn10.psptools.psp.psps.real;

import java.io.File;
import java.nio.file.Path;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.SelectionMode;

public class RealPSP extends PSP {

    private Path path;

    public RealPSP(Path path) {
        super();
        this.path = path;
    }

    @Override
    public boolean pspActive() {
        try {
            File PSPFolder = new File(Path.of(path.toString(), "PSP").toString());
            File ISOFolder = new File(Path.of(path.toString(), "ISO").toString());
            File PSPGameFolder = new File(Path.of(path.toString(), "PSP", "Game").toString());

            return (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public PSPDirectory getFolder(String child, String... others) {
        return new RealPSPDirectory(path.resolve(child, others).toFile());
    }

    @Override
    protected SelectionMode getSelectionMode() {
        return SelectionMode.PSP_DIR;
    }

    @Override
    protected String getSelectionData() {
        return path.toString();
    }

}
