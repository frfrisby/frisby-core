package software.frisby.core.validation;

final class Throws {
    private Throws() {
    }

    static void ifInvalidName(String name) {
        ifNull("name", name);

        if (name.isBlank()) {
            throw new NullPointerException(
                    "The 'name' value is invalid. The value must be non null and cannot contain only white space characters."
            );
        }
    }

    static void ifNull(String name, Object value) {
        if (null == value) {
            throw new NullPointerException(String.format("The '%s' value was not provided.", name));
        }
    }

    static void ifLessThanOne(String name, long value) {
        if (value < 1) {
            throw new IllegalConfigurationException(
                    String.format(
                            "The '%s' value of '%d' is invalid. The value must be greater than or equal to '1'.",
                            name,
                            value
                    )
            );
        }
    }

    static void ifLessThan(String name, long value, String otherName, long other) {
        if (value < other) {
            throw new IllegalConfigurationException(
                    String.format(
                            "The '%s' value of '%d' is invalid. The value must be greater than or equal to the '%s' value of '%d'.",
                            name,
                            value,
                            otherName,
                            other
                    )
            );
        }
    }
}
