package org.kiwiproject.base.process;

import java.util.List;

/**
 * Interface that describes a simple contract for an operating system command.
 */
public interface OsCommand {

    /**
     * Returns a list containing the command and all its arguments, which can then be used to construct
     * a {@link ProcessBuilder}.
     *
     * @return a list of command arguments
     * @see ProcessBuilder#ProcessBuilder(List)
     */
    List<String> parts();

    /**
     * Returns a string array containing the command and all its arguments, which can be used to construct
     * a {@link ProcessBuilder}.
     *
     * @return a string array of command arguments
     * @see ProcessBuilder#command(String...)
     */
    default String[] partsAsArray() {
        return parts().toArray(new String[0]);
    }
}
