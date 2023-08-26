package org.kiwiproject.ansible.vault;

import org.kiwiproject.base.KiwiDeprecated;

import java.util.List;

/**
 * Interface that describes a simple contract for an operating system command.
 *
 * @deprecated replaced by {@link org.kiwiproject.base.process.OsCommand}
 */
@Deprecated(since = "3.1.0", forRemoval = true)
@KiwiDeprecated(
        replacedBy = "org.kiwiproject.base.process.OsCommand",
        reference = "https://github.com/kiwiproject/kiwi/issues/1026",
        removeAt = "4.0.0"
)
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
