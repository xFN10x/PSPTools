package fn10.psptools.psp;

import java.io.File;

public interface PSPDirectory {

    /**
     * Gets all the file, not directorys, in this directory
     * @return An array of PSPFiles
     */
    public PSPFile[] getFiles();
    /**
     * Returns all of the files/directorys in this directory.
     * @return An array of PSPFileDirectory
     */
    public PSPFileDirectory[] getAll();
    /**
     * Returns the first file found with the name
     * @param name The name to use
     * @return The file
     */
    public PSPFile getFileWithName(String name);
    /**
     * Returns the first file found that starts with the given prefix
     * @param prefix The prefix to use
     * @return The first file found with the prefix.
     */
    public PSPFile getFileStartingWith(String prefix);
    /**
     * Removes this directory from wherever it is
     */
    public void delete();
    /**
     * Gets the name of this directory
     * @return the name
     */
    public String getName();
    /**
     * Create a new PSPFileDirectory from this current one, into these children.
     * @param first The first folder to go into
     * @param children The folders to go into
     * @return A PSPFileDirectory 
     */
    public PSPFileDirectory resolve(String first, String... children);
    /**
     * Create a new PSPFileDirectory from this current one, into these children.
     * @param first The folder to go into
     * @return A PSPFileDirectory 
     */
    public PSPFileDirectory resolve(String first);

    /**
     * Adds the given file to this directory
     * @param file the file to add
     */
    public void addFile(File file);
}
