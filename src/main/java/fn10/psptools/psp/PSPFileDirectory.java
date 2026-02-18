package fn10.psptools.psp;

public interface PSPFileDirectory {

    boolean isDirectory();
    default boolean isFile() {
        return !isDirectory();
    }

    PSPFile getFile();
    PSPDirectory getDirectory();
    boolean actuallyExists();

}
