package fn10.psptools.psp;

public interface PSPFileDirectory {

    public boolean isDirectory();
    default boolean isFile() {
        return !isDirectory();
    }

    public PSPFile getFile();
    public PSPDirectory getDirectory();
    public boolean actuallyExists();

}
