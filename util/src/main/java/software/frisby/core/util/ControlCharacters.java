package software.frisby.core.util;

/**
 * Static utility methods for making arbitrary strings safe to write to a raw terminal
 * or log file.
 *
 * <p>Untrusted text (HTTP request/response bodies, header values, user input, etc.) can
 * contain terminal control characters — ANSI/VT100 escape sequences, bell, backspace
 * tricks, and similar — that a terminal emulator interprets as commands rather than
 * displaying as text. If such a string is ever viewed with a raw tool ({@code cat},
 * {@code tail}) rather than a viewer that renders plain text, an attacker-controlled
 * control sequence can manipulate the terminal (move the cursor, overwrite prior lines,
 * change colors, etc.). {@link #escape(String)} neutralizes this by replacing every
 * control character with a visible, unambiguous escape sequence, leaving everything
 * else untouched.
 *
 * <p>The following ranges of Unicode code points are considered control characters and
 * are escaped:
 * <ul>
 *     <li>C0 controls, {@code U+0000}–{@code U+001F}, <strong>except</strong>
 *     {@code U+0009} (tab) and {@code U+000A} (line feed), which are passed through
 *     unchanged since they are purely structural for legitimate multi-line output.</li>
 *     <li>{@code U+007F} (DEL).</li>
 *     <li>C1 controls, {@code U+0080}–{@code U+009F}.</li>
 * </ul>
 *
 * <p>{@code U+000D} (carriage return) is deliberately <strong>not</strong> exempted
 * alongside tab and line feed: unlike them it has a terminal-rendering side effect
 * (moving the cursor to column 0, enabling line-overwrite/spoofing of prior output)
 * rather than being purely structural.
 *
 * <p>Every other character — printable ASCII and all non-control Unicode — is passed
 * through completely unchanged.
 *
 * <p>Each escaped character is replaced with the four-character literal sequence
 * {@code \xHH}, where {@code HH} is the character's two-digit lowercase hexadecimal
 * code point (e.g. ESC, {@code U+001B}, becomes the literal text {@code \x1b}). Every
 * code point escaped by this class fits in two hex digits, so the format is always
 * unambiguous.
 *
 * <p>If {@code value} contains no characters requiring escaping, {@link #escape(String)}
 * returns {@code value} itself unchanged with no new allocation — the overwhelmingly
 * common case in practice. A {@code null} input returns {@code null}, which is
 * convenient for callers assembling a log message from possibly-null fields.
 *
 * <p>This transform is intentionally <strong>not</strong> losslessly reversible: a
 * string that already contains the literal four-character text {@code \x1b} is
 * indistinguishable, after escaping, from a string that contained an actual
 * {@code U+001B} character. This is an accepted trade-off for a "make this safe to
 * display" transform rather than a serialization format — {@link #escape(String)} is
 * not intended to be undone.
 */
public final class ControlCharacters {
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private ControlCharacters() {
    }

    /**
     * Replaces every control character in {@code value} with a visible {@code \xHH}
     * escape sequence, leaving every other character unchanged.
     *
     * @param value The string to escape; may be null.
     * @return {@code value} with every control character replaced by a {@code \xHH}
     * escape sequence; {@code value} itself (unchanged, no new allocation) if it
     * contains no control characters; {@code null} if {@code value} is null.
     */
    public static String escape(String value) {
        if (null == value) {
            return null;
        }

        int length = value.length();
        int firstControl = -1;

        for (int i = 0; i < length; i++) {
            if (isControl(value.charAt(i))) {
                firstControl = i;
                break;
            }
        }

        if (firstControl < 0) {
            return value;
        }

        StringBuilder escaped = new StringBuilder(length + 16);
        escaped.append(value, 0, firstControl);

        for (int i = firstControl; i < length; i++) {
            char c = value.charAt(i);

            if (isControl(c)) {
                appendEscaped(escaped, c);
            } else {
                escaped.append(c);
            }
        }

        return escaped.toString();
    }

    private static boolean isControl(char c) {
        if (c == '\t' || c == '\n') return false;
        if (c <= 0x1F) return true;
        if (c == 0x7F) return true;

        return c >= 0x80 && c <= 0x9F;
    }

    private static void appendEscaped(StringBuilder escaped, char c) {
        escaped.append('\\')
                .append('x')
                .append(HEX_DIGITS[(c >> 4) & 0xF])
                .append(HEX_DIGITS[c & 0xF]);
    }
}

