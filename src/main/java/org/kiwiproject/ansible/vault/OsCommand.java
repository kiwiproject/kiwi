package org.kiwiproject.ansible.vault;

import java.util.List;

// TODO Should this be moved to the base.process package?

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
    List<String> getCommandParts();
}
