package software.frisby.core.util;

import software.frisby.core.validation.EmptyValueException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Strings;
import software.frisby.core.validation.Values;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Static utility methods for converting numeric values to and from {@link BigDecimal}.
 *
 * <p>This class addresses the classic float/double precision problem: constructing a
 * {@link BigDecimal} from a {@code double} or {@code float} via
 * {@code new BigDecimal(value)} captures the exact binary floating-point
 * representation, which is rarely the decimal value intended. For example,
 * {@code new BigDecimal(0.1)} produces
 * {@code 0.1000000000000000055511151231257827021181583404541015625}.
 *
 * <p>All conversion methods in this class route through the
 * {@link BigDecimal#BigDecimal(String)} constructor by way of
 * {@link String#valueOf(double)} or {@link String#valueOf(float)}, which produces
 * the shortest decimal representation that round-trips back to the same
 * floating-point value. This avoids the precision problem entirely.
 *
 * <p>The {@code toString} methods produce plain decimal strings (no exponent notation)
 * via {@link BigDecimal#toPlainString()}, making output suitable for display, logging,
 * and persistence without further formatting.
 *
 * @see BigDecimal
 * @see RoundingMode
 */
public final class Decimals {
    private Decimals() {
    }

    /**
     * Returns the string representation of the provided {@link BigDecimal} without an
     * exponent field and with the default number of digits to the right of the decimal
     * point.
     *
     * @param value The {@link BigDecimal} to convert; must not be null.
     * @return A plain-string representation of {@code value} with trailing zeros stripped.
     * @throws NullValueException if {@code value} is null.
     */
    public static String toString(BigDecimal value) {
        Values.notNull("value", value);

        return value.stripTrailingZeros()
                .toPlainString();
    }

    /**
     * Returns the string representation of the provided {@link BigDecimal} without an
     * exponent field and with a maximum of {@code scale} digits to the right of the
     * decimal point.
     *
     * <p>This method uses {@link RoundingMode#DOWN} as the default rounding mode.
     *
     * @param value The {@link BigDecimal} to convert; must not be null.
     * @param scale The maximum number of digits to the right of the decimal point.
     * @return A plain-string representation of {@code value} rounded to {@code scale}
     *         decimal places with trailing zeros stripped.
     * @throws NullValueException if {@code value} is null.
     */
    public static String toString(BigDecimal value, int scale) {
        return toString(value, scale, RoundingMode.DOWN);
    }

    /**
     * Returns the string representation of the provided {@link BigDecimal} without an
     * exponent field and with a maximum of {@code scale} digits to the right of the
     * decimal point.
     *
     * @param value        The {@link BigDecimal} to convert; must not be null.
     * @param scale        The maximum number of digits to the right of the decimal point.
     * @param roundingMode The rounding mode to apply; must not be null.
     * @return A plain-string representation of {@code value} rounded to {@code scale}
     *         decimal places with trailing zeros stripped.
     * @throws NullValueException if {@code value} or {@code roundingMode} is null.
     */
    public static String toString(BigDecimal value, int scale, RoundingMode roundingMode) {
        Values.notNull("value", value);
        Values.notNull("roundingMode", roundingMode);

        return value.setScale(scale, roundingMode)
                .stripTrailingZeros()
                .toPlainString();
    }

    /**
     * Converts the provided string value into a {@link BigDecimal}.
     *
     * @param value The string to parse; must not be null or empty.
     * @return A new {@link BigDecimal} created from {@code value} with trailing zeros
     *         stripped.
     * @throws NullValueException  if {@code value} is null.
     * @throws EmptyValueException if {@code value} is empty.
     */
    public static BigDecimal parse(String value) {
        Strings.notEmpty("value", value);

        return convertToDecimal(value);
    }

    /**
     * Converts the provided string value into a {@link BigDecimal} with a maximum of
     * {@code scale} digits to the right of the decimal point.
     *
     * <p>This method uses {@link RoundingMode#DOWN} as the default rounding mode.
     *
     * @param value The string to parse; must not be null or empty.
     * @param scale The maximum number of digits to the right of the decimal point.
     * @return A new {@link BigDecimal} created from {@code value}, rounded to
     *         {@code scale} decimal places with trailing zeros stripped.
     * @throws NullValueException  if {@code value} is null.
     * @throws EmptyValueException if {@code value} is empty.
     */
    public static BigDecimal parse(String value, int scale) {
        return parse(value, scale, RoundingMode.DOWN);
    }

    /**
     * Converts the provided string value into a {@link BigDecimal} with a maximum of
     * {@code scale} digits to the right of the decimal point.
     *
     * @param value        The string to parse; must not be null or empty.
     * @param scale        The maximum number of digits to the right of the decimal point.
     * @param roundingMode The rounding mode to apply; must not be null.
     * @return A new {@link BigDecimal} created from {@code value}, rounded to
     *         {@code scale} decimal places with trailing zeros stripped.
     * @throws NullValueException  if {@code value} is null.
     * @throws EmptyValueException if {@code value} is empty.
     */
    public static BigDecimal parse(String value, int scale, RoundingMode roundingMode) {
        Strings.notEmpty("value", value);

        return convertToDecimal(value, scale, roundingMode);
    }

    /**
     * Converts the provided {@code long} value into a {@link BigDecimal}.
     *
     * @param value The value to convert.
     * @return A new {@link BigDecimal} representing {@code value}.
     */
    public static BigDecimal of(long value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts the provided {@code float} value into a {@link BigDecimal}.
     *
     * @param value The value to convert.
     * @return A new {@link BigDecimal} representing {@code value} with trailing zeros
     *         stripped.
     */
    public static BigDecimal of(float value) {
        return convertToDecimal(String.valueOf(value));
    }

    /**
     * Converts the provided {@code float} value into a {@link BigDecimal} with a maximum
     * of {@code scale} digits to the right of the decimal point.
     *
     * <p>This method uses {@link RoundingMode#DOWN} as the default rounding mode.
     *
     * @param value The value to convert.
     * @param scale The maximum number of digits to the right of the decimal point.
     * @return A new {@link BigDecimal} representing {@code value}, rounded to
     *         {@code scale} decimal places with trailing zeros stripped.
     */
    public static BigDecimal of(float value, int scale) {
        return of(value, scale, RoundingMode.DOWN);
    }

    /**
     * Converts the provided {@code float} value into a {@link BigDecimal} with a maximum
     * of {@code scale} digits to the right of the decimal point.
     *
     * @param value        The value to convert.
     * @param scale        The maximum number of digits to the right of the decimal point.
     * @param roundingMode The rounding mode to apply.
     * @return A new {@link BigDecimal} representing {@code value}, rounded to
     *         {@code scale} decimal places with trailing zeros stripped.
     */
    public static BigDecimal of(float value, int scale, RoundingMode roundingMode) {
        return convertToDecimal(String.valueOf(value), scale, roundingMode);
    }

    /**
     * Converts the provided {@code double} value into a {@link BigDecimal}.
     *
     * @param value The value to convert.
     * @return A new {@link BigDecimal} representing {@code value} with trailing zeros
     *         stripped.
     */
    public static BigDecimal of(double value) {
        return convertToDecimal(String.valueOf(value));
    }

    /**
     * Converts the provided {@code double} value into a {@link BigDecimal} with a maximum
     * of {@code scale} digits to the right of the decimal point.
     *
     * <p>This method uses {@link RoundingMode#DOWN} as the default rounding mode.
     *
     * @param value The value to convert.
     * @param scale The maximum number of digits to the right of the decimal point.
     * @return A new {@link BigDecimal} representing {@code value}, rounded to
     *         {@code scale} decimal places with trailing zeros stripped.
     */
    public static BigDecimal of(double value, int scale) {
        return of(value, scale, RoundingMode.DOWN);
    }

    /**
     * Converts the provided {@code double} value into a {@link BigDecimal} with a maximum
     * of {@code scale} digits to the right of the decimal point.
     *
     * @param value        The value to convert.
     * @param scale        The maximum number of digits to the right of the decimal point.
     * @param roundingMode The rounding mode to apply.
     * @return A new {@link BigDecimal} representing {@code value}, rounded to
     *         {@code scale} decimal places with trailing zeros stripped.
     */
    public static BigDecimal of(double value, int scale, RoundingMode roundingMode) {
        return convertToDecimal(String.valueOf(value), scale, roundingMode);
    }

    private static BigDecimal convertToDecimal(String value) {
        // Constructing BigDecimal directly from a double or float via new BigDecimal(value)
        // captures the exact binary floating-point representation, not the intended decimal
        // value. Routing through String.valueOf() and then BigDecimal(String) produces the
        // shortest decimal that round-trips back to the same floating-point value, avoiding
        // precision artifacts such as new BigDecimal(0.1) yielding
        // 0.1000000000000000055511151231257827021181583404541015625.
        //
        // After stripTrailingZeros(), large integer values may acquire a negative scale
        // (e.g. 5000 becomes 5E+3 internally). Adding BigDecimal.ZERO normalizes the scale
        // to zero so the returned value prints without exponent notation when the caller
        // uses toString() rather than toPlainString().
        return new BigDecimal(value)
                .stripTrailingZeros()
                .add(BigDecimal.ZERO);
    }

    private static BigDecimal convertToDecimal(String value, int scale, RoundingMode roundingMode) {
        Numbers.notNegative("scale", scale);

        // See convertToDecimal(String) for an explanation of the String-based construction
        // and the BigDecimal.ZERO addition.
        return new BigDecimal(value)
                .setScale(scale, roundingMode)
                .stripTrailingZeros()
                .add(BigDecimal.ZERO);
    }
}