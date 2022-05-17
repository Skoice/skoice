package net.clementraynaud.skoice.commands.skoice.arguments;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ArgumentName {
    CONFIGURE, TOKEN, LINK, UNLINK;

    public static ArgumentName get(String option) {
        return Stream.of(ArgumentName.values())
                .filter(value -> value.toString().equalsIgnoreCase(option))
                .findFirst()
                .orElse(null);
    }

    public static Collection<String> getList() {
        return Stream.of(ArgumentName.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
