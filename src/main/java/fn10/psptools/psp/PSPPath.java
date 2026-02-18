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

/**
 * An abstract path used to get files off of a PSP. This path doesn't need to exist, and its not absolute
 */
public class PSPPath {

    private final String path;

    protected PSPPath(String relativePath) {
        this.path = relativePath;
    }

    /**
     * Creates a PSPPath from the path specified.
     * 
     * @param path A reletive path to the file. For example, a save data could be
     *             {@code PSP/SAVEDATA/ULUS00000}
     * @return The created path.
     */
    public static PSPPath of(String path) {
        return new PSPPath(path);
    }

    /**
     * Creates a PSPPath from the folders specified.
     * 
     * @param files An array of strings being the folders. For example, a save data could be
     *             {@code PSP, SAVEDATA, ULUS00000}
     * @return The created path.
     */
    public static PSPPath of(String... files) {
        return of(String.join(File.separator, files));
    }

    /**
     * Checks to see if this path points to a file or directory.
     * @return a boolean specifing if this is a directory or not.
     */
    public boolean isDirectory() {
        return path.split("\\.").length == 0;
    }

    /**
     * Split this path into its parts
     * @return an array of strings being the folders in this path. e.g. "path/to/folder" would be "path,to,folder"
     */
    public String[] split() {
        return path.split(File.separator);
    }
    
    public String toString() {
        return path;
    }
}
