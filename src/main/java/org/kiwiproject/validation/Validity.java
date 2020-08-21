package org.kiwiproject.validation;

/**
 * An enum that is used internally by some kiwi validators that have a multi-step validation process.
 */
enum Validity {
    VALID, INVALID, CONTINUE
}
