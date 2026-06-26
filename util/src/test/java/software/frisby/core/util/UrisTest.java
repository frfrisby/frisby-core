package software.frisby.core.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.BlankValueException;
import software.frisby.core.validation.NullValueException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class UrisTest {
    private static final String OPAQUE_NO_AUTHORITY_MSG =
            "The 'uri' value of 'mailto:user@example.com' is invalid.  The URI must be hierarchical and have an authority component.";

    @Nested
    class WithoutPath {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.withoutPath(null)
            );
        }

        @Test
        void opaqueUri_throwsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Uris.withoutPath(URI.create("mailto:user@example.com"))
            );

            assertEquals(OPAQUE_NO_AUTHORITY_MSG, ex.getMessage());
        }

        @Test
        void uriWithPath_returnsSchemeAndAuthority() {
            URI result = Uris.withoutPath(URI.create("https://example.com/api/v1/users"));

            assertEquals(URI.create("https://example.com"), result);
        }

        @Test
        void uriWithPathAndQuery_returnsSchemeAndAuthority() {
            URI result = Uris.withoutPath(URI.create("https://example.com/api?page=1"));

            assertEquals(URI.create("https://example.com"), result);
        }

        @Test
        void uriWithExplicitPort_preservesPort() {
            URI result = Uris.withoutPath(URI.create("https://example.com:443/api"));

            assertEquals(URI.create("https://example.com:443"), result);
        }

        @Test
        void uriWithNoPath_returnsSchemeAndAuthority() {
            URI result = Uris.withoutPath(URI.create("ftp://files.example.com"));

            assertEquals(URI.create("ftp://files.example.com"), result);
        }
    }

    @Nested
    class WithPath {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.withPath(null, "/new/path")
            );
        }

        @Test
        void nullPath_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.withPath(URI.create("https://example.com/old"), null)
            );
        }

        @Test
        void uriWithExistingPath_replacesPath() {
            URI result = Uris.withPath(URI.create("https://example.com/old/path"), "/new/path");

            assertEquals(URI.create("https://example.com/new/path"), result);
        }

        @Test
        void uriWithQueryAndFragment_queryAndFragmentNotPreserved() {
            URI result = Uris.withPath(URI.create("https://example.com/old?key=val#section"), "/new");

            assertEquals(URI.create("https://example.com/new"), result);
        }

        @Test
        void emptyPath_returnsSchemeAndAuthority() {
            URI result = Uris.withPath(URI.create("https://example.com/old"), "");

            assertEquals(URI.create("https://example.com"), result);
        }

        @Test
        void uriWithExplicitPort_preservesPort() {
            URI result = Uris.withPath(URI.create("https://example.com:8443/old"), "/new");

            assertEquals(URI.create("https://example.com:8443/new"), result);
        }
    }

    @Nested
    class AppendPath {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.appendPath(null, "users")
            );
        }

        @Test
        void nullSegment_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.appendPath(URI.create("https://example.com/api"), null)
            );
        }

        @Test
        void blankSegment_throwsBlankValueException() {
            assertThrows(
                    BlankValueException.class,
                    () -> Uris.appendPath(URI.create("https://example.com/api"), "   ")
            );
        }

        @Test
        void uriWithPath_appendsSegment() {
            URI result = Uris.appendPath(URI.create("https://example.com/api/v1"), "users");

            assertEquals(URI.create("https://example.com/api/v1/users"), result);
        }

        @Test
        void uriWithTrailingSlash_appendsSegmentWithoutDoubleSlash() {
            URI result = Uris.appendPath(URI.create("https://example.com/api/v1/"), "users");

            assertEquals(URI.create("https://example.com/api/v1/users"), result);
        }

        @Test
        void segmentWithLeadingSlash_appendsSegmentWithoutDoubleSlash() {
            URI result = Uris.appendPath(URI.create("https://example.com/api/v1"), "/users");

            assertEquals(URI.create("https://example.com/api/v1/users"), result);
        }

        @Test
        void uriWithQueryAndFragment_queryAndFragmentNotPreserved() {
            URI result = Uris.appendPath(URI.create("https://example.com/api?key=val#top"), "users");

            assertEquals(URI.create("https://example.com/api/users"), result);
        }

        @Test
        void uriWithNoPath_appendsSegmentAtRoot() {
            URI result = Uris.appendPath(URI.create("https://example.com"), "api");

            assertEquals(URI.create("https://example.com/api"), result);
        }
    }

    @Nested
    class WithoutQuery {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.withoutQuery(null)
            );
        }

        @Test
        void uriWithQuery_removesQuery() {
            URI result = Uris.withoutQuery(URI.create("https://example.com/api/users?page=1&size=20"));

            assertEquals(URI.create("https://example.com/api/users"), result);
        }

        @Test
        void uriWithQueryAndFragment_removesQueryAndFragment() {
            URI result = Uris.withoutQuery(URI.create("https://example.com/page?q=test#section"));

            assertEquals(URI.create("https://example.com/page"), result);
        }

        @Test
        void uriWithoutQuery_preservesPathAndAuthority() {
            URI result = Uris.withoutQuery(URI.create("https://example.com/api/users"));

            assertEquals(URI.create("https://example.com/api/users"), result);
        }

        @Test
        void uriWithExplicitPort_preservesPort() {
            URI result = Uris.withoutQuery(URI.create("https://example.com:8443/api?key=val"));

            assertEquals(URI.create("https://example.com:8443/api"), result);
        }
    }

    @Nested
    class Parse {
        private static final String INVALID_URI_MSG =
                "The 'uri' value of 'http://[invalid' is invalid.  The value is not a valid URI.";

        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.parse(null)
            );
        }

        @Test
        void blankUri_throwsBlankValueException() {
            assertThrows(
                    BlankValueException.class,
                    () -> Uris.parse("   ")
            );
        }

        @Test
        void invalidUri_throwsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Uris.parse("http://[invalid")
            );

            assertEquals(INVALID_URI_MSG, ex.getMessage());
        }

        @Test
        void validUri_returnsUri() {
            URI result = Uris.parse("https://example.com/path");

            assertEquals(URI.create("https://example.com/path"), result);
        }

        @Test
        void validUriWithAllComponents_returnsUri() {
            URI result = Uris.parse("mongodb://user:password@localhost:27017/admin?socketTimeoutMS=60000");

            assertEquals(
                    URI.create("mongodb://user:password@localhost:27017/admin?socketTimeoutMS=60000"),
                    result
            );
        }
    }

    @Nested
    class User {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.user(null)
            );
        }

        @Test
        void uriWithoutUserInfo_returnsNull() {
            assertNull(Uris.user(URI.create("https://example.com/path")));
        }

        @Test
        void uriWithUserAndPassword_returnsUser() {
            assertEquals("frank", Uris.user(URI.create("mongodb://frank:secret@localhost/admin")));
        }

        @Test
        void uriWithUserOnly_returnsUser() {
            assertEquals("frank", Uris.user(URI.create("ftp://frank@files.example.com")));
        }

        @Test
        void uriWithEncodedCharInUser_returnsDecodedUser() {
            assertEquals("frank@work", Uris.user(URI.create("ftp://frank%40work:secret@files.example.com")));
        }
    }

    @Nested
    class Password {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> Uris.password(null)
            );
        }

        @Test
        void uriWithoutUserInfo_returnsNull() {
            assertNull(Uris.password(URI.create("https://example.com/path")));
        }

        @Test
        void uriWithUserAndPassword_returnsPassword() {
            assertEquals("secret", Uris.password(URI.create("mongodb://frank:secret@localhost/admin")));
        }

        @Test
        void uriWithUserOnly_returnsNull() {
            assertNull(Uris.password(URI.create("ftp://frank@files.example.com")));
        }

        @Test
        void uriWithPasswordContainingColon_returnsFullPassword() {
            assertEquals("pa:ss", Uris.password(URI.create("mongodb://frank:pa:ss@localhost/admin")));
        }

        @Test
        void uriWithEncodedCharInPassword_returnsDecodedPassword() {
            assertEquals("p@ss", Uris.password(URI.create("mongodb://frank:p%40ss@localhost/admin")));
        }
    }
}

