package net.clementraynaud.skoice.commands.skoice.arguments;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ArgumentName {
    CONFIGURE(false, true),
    TOKEN(true, true),
    LINK(false, false),
    UNLINK(false, false);

    private final boolean allowedInConsole;
    private final boolean restrictedToOperators;

    ArgumentName(boolean allowedInConsole, boolean restrictedToOperators) {
        this.allowedInConsole = allowedInConsole;
        this.restrictedToOperators = restrictedToOperators;
    }

    public boolean isAllowedInConsole() {
        return this.allowedInConsole;
    }

    public boolean isRestrictedToOperators() {
        return this.restrictedToOperators;
    }

    public static ArgumentName get(String option) {
        return Stream.of(ArgumentName.values())
                .filter(value -> value.toString().equalsIgnoreCase(option))
                .findFirst()
                .orElse(null);
    }

    public static Collection<String> getList(boolean restrictedToOperators) {
        return Stream.of(ArgumentName.values())
                .filter(arg -> arg.restrictedToOperators == restrictedToOperators || restrictedToOperators)
                .map(Enum::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
