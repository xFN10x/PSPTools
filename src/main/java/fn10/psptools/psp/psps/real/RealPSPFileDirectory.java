package fn10.psptools.psp.psps.real;

import java.io.File;
import java.nio.file.Path;

import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.PSPFileDirectory;

public class RealPSPFileDirectory implements PSPFileDirectory {

    private final File file;

    public RealPSPFileDirectory(File file) {
        this.file = file;
    }

    public RealPSPFileDirectory(Path path) {
        this(path.toFile());
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public PSPFile getFile() {
        return new RealPSPFile(file);
    }

    @Override
    public PSPDirectory getDirectory() {
        return new RealPSPDirectory(file);
    }

    @Override
    public boolean actuallyExists() {
        return file.exists();
    }

}
