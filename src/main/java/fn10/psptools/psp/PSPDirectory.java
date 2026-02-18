/*
    PSPTools - Management Utility for your PSP.
    Copyright (C) 2026 xFN10x (https://github.com/xFN10x)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package fn10.psptools.psp;

import java.io.File;

public interface PSPDirectory {

    /**
     * Gets all the file, not directorys, in this directory
     * @return An array of PSPFiles
     */
    PSPFile[] getFiles();
    /**
     * Returns all of the files/directorys in this directory.
     * @return An array of PSPFileDirectory
     */
    PSPFileDirectory[] getAll();
    /**
     * Returns the first file found with the name
     * @param name The name to use
     * @return The file
     */
    PSPFile getFileWithName(String name);
    /**
     * Returns the first file found that starts with the given prefix
     * @param prefix The prefix to use
     * @return The first file found with the prefix.
     */
    PSPFile getFileStartingWith(String prefix);
    /**
     * Removes this directory from wherever it is
     */
    void delete();
    /**
     * Gets the name of this directory
     * @return the name
     */
    String getName();
    /**
     * Create a new PSPFileDirectory from this current one, into these children.
     * @param first The first folder to go into
     * @param children The folders to go into
     * @return A PSPFileDirectory 
     */
    PSPFileDirectory resolve(String first, String... children);
    /**
     * Create a new PSPFileDirectory from this current one, into these children.
     * @param first The folder to go into
     * @return A PSPFileDirectory 
     */
    PSPFileDirectory resolve(String first);

    /**
     * Adds the given file to this directory
     * @param file the file to add
     */
    void addFile(File file);
}
