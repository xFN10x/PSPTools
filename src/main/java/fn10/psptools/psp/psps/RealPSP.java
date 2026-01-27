package fn10.psptools.psp.psps;

import java.io.File;
import java.nio.file.Path;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.SelectionMode;

public class RealPSP extends PSP {

    private Path path;

    public RealPSP(Path path) {
        super(SelectionMode.PSP_DIR);
        this.path = path;
    }

    @Override
    public boolean pspActive() {
        try {
            switch (selectionMode) {
                case SelectionMode.PSP_DIR:
                    File PSPFolder = new File(Path.of(path.toString(), "PSP").toString());
                    File ISOFolder = new File(Path.of(path.toString(), "ISO").toString());
                    File PSPGameFolder = new File(Path.of(path.toString(), "PSP", "Game").toString());
                    File PSPSaveFolder = new File(Path.of(path.toString(), "PSP", "Savedata").toString());

                    return (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists()
                            && PSPSaveFolder.exists());

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public PSPDirectory getFolder(String child, String... others) {
        return new RealPSPDirectory(path.resolve(child, others).toFile());
    }

}
