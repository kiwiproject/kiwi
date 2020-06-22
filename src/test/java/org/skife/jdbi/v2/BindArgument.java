package org.skife.jdbi.v2;

import static com.google.common.base.Preconditions.checkState;

import lombok.Value;
import org.skife.jdbi.v2.tweak.Argument;

/**
 * Represents an argument bound to a JDBI2 {@link SQLStatement}, for verification during tests.
 *
 * @see Argument
 * @see ObjectArgument
 */
@Value
public class BindArgument {

    String name;
    Argument argument;

    public Object getObjectArgumentValue() {
        checkState(isObjectArgument(), "The argument is not an ObjectArgument; it is a %s", argument.getClass());

        return ((ObjectArgument) argument).getValue();
    }

    private boolean isObjectArgument() {
        return argument instanceof ObjectArgument;
    }
}

