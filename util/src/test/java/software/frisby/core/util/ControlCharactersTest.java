package software.frisby.core.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ControlCharactersTest {
    @Nested
    class NoControlCharacters {
        @Test
        void plainAsciiText_returnsSameInstance() {
            String value = "Hello, world! 123";

            assertSame(value, ControlCharacters.escape(value));
        }

        @Test
        void nonControlUnicodeText_returnsSameInstance() {
            String value = "café résumé 日本語 emoji 🎉";

            assertSame(value, ControlCharacters.escape(value));
        }

        @Test
        void emptyString_returnsSameInstance() {
            String value = "";

            assertSame(value, ControlCharacters.escape(value));
        }
    }

    @Nested
    class TabAndNewline {
        @Test
        void tab_passedThroughUnchanged() {
            assertEquals("a\tb", ControlCharacters.escape("a\tb"));
        }

        @Test
        void newline_passedThroughUnchanged() {
            assertEquals("a\nb", ControlCharacters.escape("a\nb"));
        }

        @Test
        void tabAndNewline_returnsSameInstance() {
            String value = "line one\nline two\ttabbed";

            assertSame(value, ControlCharacters.escape(value));
        }
    }

    @Nested
    class CarriageReturn {
        @Test
        void carriageReturn_escapedAsHexOd() {
            assertEquals("a\\x0db", ControlCharacters.escape("a\rb"));
        }

        @Test
        void carriageReturnNewline_onlyCarriageReturnEscaped() {
            assertEquals("a\\x0d\nb", ControlCharacters.escape("a\r\nb"));
        }
    }

    @Nested
    class C0Controls {
        @Test
        void nul_escapedAsHex00() {
            assertEquals("a\\x00b", ControlCharacters.escape("a\u0000b"));
        }

        @Test
        void bell_escapedAsHex07() {
            assertEquals("a\\x07b", ControlCharacters.escape("a\u0007b"));
        }

        @Test
        void backspace_escapedAsHex08() {
            assertEquals("a\\x08b", ControlCharacters.escape("a\bb"));
        }

        @Test
        void escape_escapedAsHex1b() {
            assertEquals("a\\x1bb", ControlCharacters.escape("a\u001Bb"));
        }

        @Test
        void unitSeparator_escapedAsHex1f() {
            assertEquals("a\\x1fb", ControlCharacters.escape("a\u001Fb"));
        }
    }

    @Nested
    class Del {
        @Test
        void del_escapedAsHex7f() {
            assertEquals("a\\x7fb", ControlCharacters.escape("a\u007Fb"));
        }
    }

    @Nested
    class C1Controls {
        @Test
        void nextLine_escapedAsHex85() {
            assertEquals("a\\x85b", ControlCharacters.escape("a\u0085b"));
        }

        @Test
        void characterTabulationWithJustification_escapedAsHex9b() {
            assertEquals("a\\x9bb", ControlCharacters.escape("a\u009Bb"));
        }
    }

    @Nested
    class Mixed {
        @Test
        void printableTextWithMultipleControlCharsOfDifferentKinds_escapesOnlyControlCharsInOrder() {
            String value = "start\u0000mid\u001Bdle\r\u007Fend\u009Blast";

            assertEquals(
                    "start\\x00mid\\x1bdle\\x0d\\x7fend\\x9blast",
                    ControlCharacters.escape(value)
            );
        }

        @Test
        void mixedWithTabAndNewlinePreserved_onlyTrueControlCharsEscaped() {
            String value = "a\tb\u0000c\nd\u007Fe";

            assertEquals("a\tb\\x00c\nd\\x7fe", ControlCharacters.escape(value));
        }
    }

    @Nested
    class Null {
        @Test
        void nullValue_returnsNull() {
            assertNull(ControlCharacters.escape(null));
        }
    }
}


