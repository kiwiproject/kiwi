package org.kiwiproject.validation;

/**
 * An enum that is used internally by some kiwi validators that have a multistep validation process.
 */
enum Validity {
    VALID, INVALID, CONTINUE
}
