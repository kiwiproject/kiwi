package org.kiwiproject.collect;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

@UtilityClass
public class KiwiProperties {

    public static Properties newProperties(String... items) {
        checkEvenItemCount(items);
        Properties properties = new Properties();
        for (int i = 0; i < items.length; i += 2) {
            String name = items[i];
            String value = items[i + 1];
            properties.setProperty(name, value);
        }
        return properties;
    }

    public static Properties newProperties(List<String> items) {
        checkEvenItemCount(items);
        Properties properties = new Properties();
        for (int i = 0; i < items.size(); i += 2) {
            String name = items.get(i);
            String value = items.get(i + 1);
            properties.setProperty(name, value);
        }
        return properties;
    }

    public static Properties newProperties(Map<String, String> map) {
        requireNonNull(map);
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    public static Properties newPropertiesFromStringPairs(List<List<String>> items) {
        requireNonNull(items);
        return items.stream().collect(
                Properties::new,
                KiwiProperties::checkPairAndAccumulate,
                Properties::putAll);
    }

    private static void checkPairAndAccumulate(Properties accumulator, List<String> pair) {
        checkArgument(pair.size() == 2, "Each sublist must contain exactly 2 items");
        accumulator.setProperty(first(pair), second(pair));
    }

}
