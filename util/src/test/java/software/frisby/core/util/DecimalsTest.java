package software.frisby.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DecimalsTest {
    private static final String NULL_VALUE_MSG  = "The 'value' value is invalid. The value must not be null.";
    private static final String EMPTY_VALUE_MSG = "The 'value' value is invalid. The value must not be empty.";
    @ParameterizedTest
    @ArgumentsSource(StringConvertProvider.class)
    void parse(String actual, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.parse(actual),
                "Incorrect parse() return value. Actual='" + actual + "', Expected='" + expected.toPlainString() + "'"
        );
    }

    @ParameterizedTest
    @ArgumentsSource(StringConvertWithScaleProvider.class)
    void parse(String actual, int scale, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.parse(actual, scale),
                "Incorrect parse() return value. Value='" + actual + "', Scale=" + scale + ", Expected='" + expected.toPlainString() + "'"
        );
    }

    @Test
    void parse_nullValue_throwsNullValueException() {
        var ex = assertThrows(
                software.frisby.core.validation.NullValueException.class,
                () -> Decimals.parse(null)
        );
        assertEquals(NULL_VALUE_MSG, ex.getMessage());
    }

    @Test
    void parse_emptyValue_throwsEmptyValueException() {
        var ex = assertThrows(
                software.frisby.core.validation.EmptyValueException.class,
                () -> Decimals.parse("")
        );
        assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
    }

    @ParameterizedTest
    @ArgumentsSource(StringConvertProvider.class)
    void toString(String actual, BigDecimal expected) {
        assertEquals(
                expected.toPlainString(),
                Decimals.toString(expected),
                "Incorrect toString() return value. Actual='" + actual + "', Expected='" + expected.toPlainString() + "'"
        );
    }

    @ParameterizedTest
    @ArgumentsSource(StringConvertWithScaleProvider.class)
    void toString(String actual, int scale, BigDecimal expected) {
        assertEquals(
                expected.toPlainString(),
                Decimals.toString(Decimals.parse(actual), scale),
                "Incorrect toString() return value. Value='" + actual + "', Scale=" + scale + ", Expected='" + expected.toPlainString() + "'"
        );
    }

    @ParameterizedTest
    @ArgumentsSource(IntegerConvertProvider.class)
    void of(int actual, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.of(actual),
                "Incorrect of() return value. Actual=" + actual + ", Expected=" + expected.toPlainString()
        );
    }

    @ParameterizedTest
    @ArgumentsSource(LongConvertProvider.class)
    void of(long actual, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.of(actual),
                "Incorrect of() return value. Actual=" + actual + ", Expected=" + expected.toPlainString()
        );
    }

    @ParameterizedTest
    @ArgumentsSource(FloatConvertProvider.class)
    void of(float actual, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.of(actual),
                "Incorrect of() return value. Actual=" + actual + "f, Expected=" + expected.toPlainString()
        );
    }

    @ParameterizedTest
    @ArgumentsSource(FloatConvertWithScaleProvider.class)
    void of(float actual, int scale, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.of(actual, scale),
                "Incorrect toBigDecimal() return value. Actual=" + actual + "f, Scale=" + scale + ", Expected=" + expected.toPlainString()
        );
    }

    @ParameterizedTest
    @ArgumentsSource(DoubleConvertProvider.class)
    void of(double actual, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.of(actual),
                "Incorrect of() return value. Actual=" + actual + "d, Expected=" + expected.toPlainString()
        );
    }

    @ParameterizedTest
    @ArgumentsSource(DoubleConvertWithScaleProvider.class)
    void of(double actual, int scale, BigDecimal expected) {
        assertEquals(
                expected,
                Decimals.of(actual, scale),
                "Incorrect of() return value. Actual=" + actual + "d, Scale=" + scale + ", Expected=" + expected.toPlainString()
        );
    }

    private static class StringConvertProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(String.valueOf(Long.MIN_VALUE), new BigDecimal("-9223372036854775808")),
                    arguments(String.valueOf(Integer.MIN_VALUE), new BigDecimal("-2147483648")),
                    arguments("-180", new BigDecimal("-180")),
                    arguments("-180.0", new BigDecimal("-180")),
                    arguments("-180.00000000000000001", new BigDecimal("-180.00000000000000001")),
                    arguments("-179.99999999", new BigDecimal("-179.99999999")),
                    arguments("-179.123456", new BigDecimal("-179.123456")),
                    arguments("-1", new BigDecimal("-1")),
                    arguments("-0.12345678912345678", new BigDecimal("-0.12345678912345678")),
                    arguments("-0.123456", new BigDecimal("-0.123456")),
                    arguments("-0.1000", new BigDecimal("-0.1")),
                    arguments("0", new BigDecimal("0")),
                    arguments("0.0", new BigDecimal("0")),
                    arguments("0.1000", new BigDecimal("0.1")),
                    arguments("0.123456", new BigDecimal("0.123456")),
                    arguments("0.123456789", new BigDecimal("0.123456789")),
                    arguments("1", new BigDecimal("1")),
                    arguments("179.123456", new BigDecimal("179.123456")),
                    arguments("179.999999999", new BigDecimal("179.999999999")),
                    arguments("5E+3", new BigDecimal("5000")),
                    arguments("5E-3", new BigDecimal("0.005")),
                    arguments("180.0", new BigDecimal("180")),
                    arguments("180", new BigDecimal("180")),
                    arguments(String.valueOf(Integer.MAX_VALUE), new BigDecimal("2147483647")),
                    arguments(String.valueOf(Long.MAX_VALUE), new BigDecimal("9223372036854775807"))
            );
        }
    }

    private static class StringConvertWithScaleProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(String.valueOf(Long.MIN_VALUE), 16, new BigDecimal("-9223372036854775808")),
                    arguments(String.valueOf(Integer.MIN_VALUE), 16, new BigDecimal("-2147483648")),
                    arguments("-180", 16, new BigDecimal("-180")),
                    arguments("-180.0", 16, new BigDecimal("-180")),
                    arguments("-180.00000000000000001", 16, new BigDecimal("-180")),
                    arguments("-179.99999999", 16, new BigDecimal("-179.99999999")),
                    arguments("-179.123456", 16, new BigDecimal("-179.123456")),
                    arguments("-1", 16, new BigDecimal("-1")),
                    arguments("-0.12345678912345678", 16, new BigDecimal("-0.1234567891234567")),
                    arguments("-0.123456", 16, new BigDecimal("-0.123456")),
                    arguments("-0.1000", 16, new BigDecimal("-0.1")),
                    arguments("0", 16, new BigDecimal("0")),
                    arguments("0.0", 16, new BigDecimal("0")),
                    arguments("0.1000", 16, new BigDecimal("0.1")),
                    arguments("0.123456", 16, new BigDecimal("0.123456")),
                    arguments("0.123456789", 16, new BigDecimal("0.123456789")),
                    arguments("1", 16, new BigDecimal("1")),
                    arguments("179.123456", 16, new BigDecimal("179.123456")),
                    arguments("179.999999999", 16, new BigDecimal("179.999999999")),
                    arguments("5E+3", 16, new BigDecimal("5000")),
                    arguments("5E-3", 16, new BigDecimal("0.005")),
                    arguments("180.0", 16, new BigDecimal("180")),
                    arguments("180", 16, new BigDecimal("180")),
                    arguments(String.valueOf(Integer.MAX_VALUE), 16, new BigDecimal("2147483647")),
                    arguments(String.valueOf(Long.MAX_VALUE), 16, new BigDecimal("9223372036854775807"))
            );
        }
    }

    private static class IntegerConvertProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(Integer.MIN_VALUE, new BigDecimal("-2147483648")),
                    arguments(-180, new BigDecimal("-180")),
                    arguments(-1, new BigDecimal("-1")),
                    arguments(0, new BigDecimal("0")),
                    arguments(1, new BigDecimal("1")),
                    arguments(180, new BigDecimal("180")),
                    arguments(Integer.MAX_VALUE, new BigDecimal("2147483647"))
            );
        }
    }

    private static class LongConvertProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(Long.MIN_VALUE, new BigDecimal("-9223372036854775808")),
                    arguments(-180, new BigDecimal("-180")),
                    arguments(-1, new BigDecimal("-1")),
                    arguments(0, new BigDecimal("0")),
                    arguments(1, new BigDecimal("1")),
                    arguments(180, new BigDecimal("180")),
                    arguments(Long.MAX_VALUE, new BigDecimal("9223372036854775807"))
            );
        }
    }

    private static class FloatConvertProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(Float.MIN_VALUE, new BigDecimal("1.4E-45")),
                    arguments(-180.0f, new BigDecimal("-180")),
                    arguments(-180.0000001f, new BigDecimal("-180")),
                    arguments(-179.9999999f, new BigDecimal("-180")),
                    arguments(-179.123456f, new BigDecimal("-179.12346")),
                    arguments(-0.123456789f, new BigDecimal("-0.12345679")),
                    arguments(-0.123456f, new BigDecimal("-0.123456")),
                    arguments(0f, new BigDecimal("0")),
                    arguments(0.123456f, new BigDecimal("0.123456")),
                    arguments(0.123456789f, new BigDecimal("0.12345679")),
                    arguments(179.123456f, new BigDecimal("179.12346")),
                    arguments(179.9999999f, new BigDecimal("180")),
                    arguments(180.0f, new BigDecimal("180")),
                    arguments(Float.MAX_VALUE, new BigDecimal("3.4028235e+38").add(new BigDecimal("0")))
            );
        }
    }

    private static class FloatConvertWithScaleProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(Float.MIN_VALUE, 16, new BigDecimal("0")),
                    arguments(-180.0f, 16, new BigDecimal("-180")),
                    arguments(-180.0000001f, 16, new BigDecimal("-180")),
                    arguments(-179.9999999f, 16, new BigDecimal("-180")),
                    arguments(-179.123456f, 16, new BigDecimal("-179.12346")),
                    arguments(-0.123456789f, 16, new BigDecimal("-0.12345679")),
                    arguments(-0.123456f, 16, new BigDecimal("-0.123456")),
                    arguments(0f, 16, new BigDecimal("0")),
                    arguments(0.123456f, 16, new BigDecimal("0.123456")),
                    arguments(0.123456789f, 16, new BigDecimal("0.12345679")),
                    arguments(179.123456f, 16, new BigDecimal("179.12346")),
                    arguments(179.9999999f, 16, new BigDecimal("180")),
                    arguments(180.0f, 16, new BigDecimal("180")),
                    arguments(Float.MAX_VALUE, 16, new BigDecimal("3.4028235e+38").add(new BigDecimal("0")))
            );
        }
    }

    private static class DoubleConvertProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(Double.MIN_VALUE, new BigDecimal("4.9E-324")),
                    arguments(-180.0d, new BigDecimal("-180")),
                    arguments(-180.00000000000001d, new BigDecimal("-180")),
                    arguments(-179.9999999999999d, new BigDecimal("-179.9999999999999")),
                    arguments(-179.123456d, new BigDecimal("-179.123456")),
                    arguments(-0.12345678912345678d, new BigDecimal("-0.12345678912345678")),
                    arguments(-0.123456d, new BigDecimal("-0.123456")),
                    arguments(0d, new BigDecimal("0")),
                    arguments(0.123456d, new BigDecimal("0.123456")),
                    arguments(0.12345678912345678d, new BigDecimal("0.12345678912345678")),
                    arguments(179.123456d, new BigDecimal("179.123456")),
                    arguments(179.9999999999999d, new BigDecimal("179.9999999999999")),
                    arguments(180.0d, new BigDecimal("180")),
                    arguments(Double.MAX_VALUE, new BigDecimal("1.7976931348623157e+308").add(new BigDecimal("0"))),
                    arguments(1d, new BigDecimal("1")),
                    arguments(10d, new BigDecimal("10")),
                    arguments(100d, new BigDecimal("100")),
                    arguments(1_000d, new BigDecimal("1000")),
                    arguments(10_000d, new BigDecimal("10000")),
                    arguments(100_000d, new BigDecimal("100000")),
                    arguments(1_000_000d, new BigDecimal("1000000")),
                    arguments(1_000_000_000d, new BigDecimal("1000000000")),
                    arguments(10_000_000_000d, new BigDecimal("10000000000")),
                    arguments(100_000_000_000d, new BigDecimal("100000000000")),
                    arguments(1_000_000_000_000d, new BigDecimal("1000000000000")),
                    arguments(.1d, new BigDecimal(".1")),
                    arguments(.01d, new BigDecimal(".01")),
                    arguments(.001d, new BigDecimal(".001")),
                    arguments(.0001d, new BigDecimal(".0001")),
                    arguments(.00001d, new BigDecimal(".00001")),
                    arguments(.000001d, new BigDecimal(".000001")),
                    arguments(.000000001d, new BigDecimal(".000000001")),
                    arguments(.0000000001d, new BigDecimal(".0000000001")),
                    arguments(.00000000001d, new BigDecimal(".00000000001")),
                    arguments(.000000000001d, new BigDecimal(".000000000001"))
            );
        }
    }

    private static class DoubleConvertWithScaleProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    arguments(Double.MIN_VALUE, 16, new BigDecimal("0")),
                    arguments(-180.0d, 16, new BigDecimal("-180")),
                    arguments(-180.00000000000001d, 16, new BigDecimal("-180")),
                    arguments(-179.9999999999999d, 16, new BigDecimal("-179.9999999999999")),
                    arguments(-179.123456d, 16, new BigDecimal("-179.123456")),
                    arguments(-0.12345678912345679d, 16, new BigDecimal("-0.1234567891234567")),
                    arguments(-0.123456d, 16, new BigDecimal("-0.123456")),
                    arguments(0d, 16, new BigDecimal("0")),
                    arguments(0.123456d, 16, new BigDecimal("0.123456")),
                    arguments(0.12345678912345679d, 16, new BigDecimal("0.1234567891234567")),
                    arguments(179.123456d, 16, new BigDecimal("179.123456")),
                    arguments(179.9999999999999d, 16, new BigDecimal("179.9999999999999")),
                    arguments(180.0d, 16, new BigDecimal("180")),
                    arguments(Double.MAX_VALUE, 16, new BigDecimal("1.7976931348623157e+308").add(new BigDecimal("0"))),
                    arguments(1d, 16, new BigDecimal("1")),
                    arguments(10d, 16, new BigDecimal("10")),
                    arguments(100d, 16, new BigDecimal("100")),
                    arguments(1_000d, 16, new BigDecimal("1000")),
                    arguments(10_000d, 16, new BigDecimal("10000")),
                    arguments(100_000d, 16, new BigDecimal("100000")),
                    arguments(1_000_000d, 16, new BigDecimal("1000000")),
                    arguments(1_000_000_000d, 16, new BigDecimal("1000000000")),
                    arguments(.1d, 16, new BigDecimal(".1")),
                    arguments(.01d, 16, new BigDecimal(".01")),
                    arguments(.001d, 16, new BigDecimal(".001")),
                    arguments(.0001d, 16, new BigDecimal(".0001")),
                    arguments(.00001d, 16, new BigDecimal(".00001")),
                    arguments(.000001d, 16, new BigDecimal(".000001")),
                    arguments(.000000001d, 16, new BigDecimal(".000000001"))
            );
        }
    }
}
