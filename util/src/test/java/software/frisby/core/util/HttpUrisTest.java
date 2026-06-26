package software.frisby.core.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.DisallowedValueException;
import software.frisby.core.validation.NullValueException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class HttpUrisTest {
    @Nested
    class Origin {
        @Test
        void nullSource_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.origin(null)
            );
        }

        @Test
        void nonHttpScheme_throwsDisallowedValueException() {
            assertThrows(
                    DisallowedValueException.class,
                    () -> HttpUris.origin(URI.create("ftp://example.com/file.txt"))
            );
        }

        @Test
        void noScheme_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.origin(URI.create("/api/v1/users"))
            );
        }

        @Test
        void httpsWithNoPort_returnsOrigin() {
            URI result = HttpUris.origin(URI.create("https://example.com/api/v1/users"));

            assertEquals(URI.create("https://example.com"), result);
        }

        @Test
        void httpWithNoPort_returnsOrigin() {
            URI result = HttpUris.origin(URI.create("http://example.com/api/v1/users"));

            assertEquals(URI.create("http://example.com"), result);
        }

        @Test
        void httpsWithDefaultPort_suppressesPort() {
            URI result = HttpUris.origin(URI.create("https://example.com:443/api/v1/users"));

            assertEquals(URI.create("https://example.com"), result);
        }

        @Test
        void httpWithDefaultPort_suppressesPort() {
            URI result = HttpUris.origin(URI.create("http://example.com:80/api/v1/users"));

            assertEquals(URI.create("http://example.com"), result);
        }

        @Test
        void httpsWithNonDefaultPort_includesPort() {
            URI result = HttpUris.origin(URI.create("https://example.com:8443/api/v1/users"));

            assertEquals(URI.create("https://example.com:8443"), result);
        }

        @Test
        void httpWithNonDefaultPort_includesPort() {
            URI result = HttpUris.origin(URI.create("http://example.com:8080/api/v1/users"));

            assertEquals(URI.create("http://example.com:8080"), result);
        }

        @Test
        void ipv6Host_includesBrackets() {
            URI result = HttpUris.origin(URI.create("https://[::1]:8443/api"));

            assertEquals(URI.create("https://[::1]:8443"), result);
        }

        @Test
        void ipv6HostWithDefaultPort_suppressesPort() {
            URI result = HttpUris.origin(URI.create("https://[::1]:443/api"));

            assertEquals(URI.create("https://[::1]"), result);
        }

        @Test
        void ipv4Host_returnsOrigin() {
            URI result = HttpUris.origin(URI.create("https://192.168.1.1:8443/api"));

            assertEquals(URI.create("https://192.168.1.1:8443"), result);
        }

        @Test
        void registryBasedAuthority_throwsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> HttpUris.origin(URI.create("http://host_name/path"))
            );

            assertEquals(
                    "The 'uri' value of 'http://host_name/path' is invalid.  The URI must have a resolvable host component.",
                    ex.getMessage()
            );
        }

        @Test
        void uppercaseScheme_returnsOrigin() {
            URI result = HttpUris.origin(URI.create("HTTPS://example.com/api"));

            assertEquals(URI.create("https://example.com"), result);
        }
    }

    @Nested
    class EffectivePort {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.effectivePort(null)
            );
        }

        @Test
        void nonHttpScheme_throwsDisallowedValueException() {
            assertThrows(
                    DisallowedValueException.class,
                    () -> HttpUris.effectivePort(URI.create("ftp://example.com/file.txt"))
            );
        }

        @Test
        void noScheme_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.effectivePort(URI.create("/api/v1/users"))
            );
        }

        @Test
        void httpWithNoPort_returnsEighty() {
            assertEquals(80, HttpUris.effectivePort(URI.create("http://example.com/path")));
        }

        @Test
        void httpsWithNoPort_returnsFourFortyThree() {
            assertEquals(443, HttpUris.effectivePort(URI.create("https://example.com/path")));
        }

        @Test
        void httpWithExplicitPort_returnsExplicitPort() {
            assertEquals(8080, HttpUris.effectivePort(URI.create("http://example.com:8080/path")));
        }

        @Test
        void httpsWithExplicitPort_returnsExplicitPort() {
            assertEquals(8443, HttpUris.effectivePort(URI.create("https://example.com:8443/path")));
        }

        @Test
        void httpWithDefaultPortExplicit_returnsEighty() {
            assertEquals(80, HttpUris.effectivePort(URI.create("http://example.com:80/path")));
        }

        @Test
        void httpsWithDefaultPortExplicit_returnsFourFortyThree() {
            assertEquals(443, HttpUris.effectivePort(URI.create("https://example.com:443/path")));
        }

        @Test
        void ipv6HostWithExplicitPort_returnsExplicitPort() {
            assertEquals(8443, HttpUris.effectivePort(URI.create("https://[::1]:8443/path")));
        }

        @Test
        void ipv6HostWithNoPort_returnsFourFortyThree() {
            assertEquals(443, HttpUris.effectivePort(URI.create("https://[::1]/path")));
        }
    }

    @Nested
    class IsDefaultPort {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.isDefaultPort(null)
            );
        }

        @Test
        void nonHttpScheme_throwsDisallowedValueException() {
            assertThrows(
                    DisallowedValueException.class,
                    () -> HttpUris.isDefaultPort(URI.create("ftp://example.com/file.txt"))
            );
        }

        @Test
        void noScheme_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.isDefaultPort(URI.create("/api/v1/users"))
            );
        }

        @Test
        void httpWithNoPort_returnsTrue() {
            assertTrue(HttpUris.isDefaultPort(URI.create("http://example.com/path")));
        }

        @Test
        void httpsWithNoPort_returnsTrue() {
            assertTrue(HttpUris.isDefaultPort(URI.create("https://example.com/path")));
        }

        @Test
        void httpWithExplicitDefaultPort_returnsTrue() {
            assertTrue(HttpUris.isDefaultPort(URI.create("http://example.com:80/path")));
        }

        @Test
        void httpsWithExplicitDefaultPort_returnsTrue() {
            assertTrue(HttpUris.isDefaultPort(URI.create("https://example.com:443/path")));
        }

        @Test
        void httpWithNonDefaultPort_returnsFalse() {
            assertFalse(HttpUris.isDefaultPort(URI.create("http://example.com:8080/path")));
        }

        @Test
        void httpsWithNonDefaultPort_returnsFalse() {
            assertFalse(HttpUris.isDefaultPort(URI.create("https://example.com:8443/path")));
        }

        @Test
        void ipv6HostWithNoPort_returnsTrue() {
            assertTrue(HttpUris.isDefaultPort(URI.create("https://[::1]/path")));
        }

        @Test
        void ipv6HostWithDefaultPort_returnsTrue() {
            assertTrue(HttpUris.isDefaultPort(URI.create("https://[::1]:443/path")));
        }

        @Test
        void ipv6HostWithNonDefaultPort_returnsFalse() {
            assertFalse(HttpUris.isDefaultPort(URI.create("https://[::1]:8443/path")));
        }
    }

    @Nested
    class IsHttp {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.isHttp(null)
            );
        }

        @Test
        void httpsUri_returnsFalse() {
            assertFalse(HttpUris.isHttp(URI.create("https://example.com")));
        }

        @Test
        void httpUri_returnsTrue() {
            assertTrue(HttpUris.isHttp(URI.create("http://example.com")));
        }

        @Test
        void ftpUri_returnsFalse() {
            assertFalse(HttpUris.isHttp(URI.create("ftp://example.com")));
        }

        @Test
        void uppercaseScheme_returnsTrue() {
            assertTrue(HttpUris.isHttp(URI.create("HTTP://example.com")));
        }
    }

    @Nested
    class IsHttps {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.isHttps(null)
            );
        }

        @Test
        void httpUri_returnsFalse() {
            assertFalse(HttpUris.isHttps(URI.create("http://example.com")));
        }

        @Test
        void httpsUri_returnsTrue() {
            assertTrue(HttpUris.isHttps(URI.create("https://example.com")));
        }

        @Test
        void ftpUri_returnsFalse() {
            assertFalse(HttpUris.isHttps(URI.create("ftp://example.com")));
        }

        @Test
        void uppercaseScheme_returnsTrue() {
            assertTrue(HttpUris.isHttps(URI.create("HTTPS://example.com")));
        }
    }

    @Nested
    class ToHttps {
        @Test
        void nullUri_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.toHttps(null)
            );
        }

        @Test
        void nonHttpScheme_throwsDisallowedValueException() {
            assertThrows(
                    DisallowedValueException.class,
                    () -> HttpUris.toHttps(URI.create("ftp://example.com/file.txt"))
            );
        }

        @Test
        void noScheme_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> HttpUris.toHttps(URI.create("/api/v1/users"))
            );
        }

        @Test
        void httpsUri_returnsSameInstance() {
            URI uri = URI.create("https://example.com/path");

            assertSame(uri, HttpUris.toHttps(uri));
        }

        @Test
        void httpUri_upgradesScheme() {
            URI result = HttpUris.toHttps(URI.create("http://example.com/path"));

            assertEquals(URI.create("https://example.com/path"), result);
        }

        @Test
        void httpUriWithExplicitPort_preservesPort() {
            URI result = HttpUris.toHttps(URI.create("http://example.com:8080/path"));

            assertEquals(URI.create("https://example.com:8080/path"), result);
        }

        @Test
        void httpUriWithQuery_preservesQuery() {
            URI result = HttpUris.toHttps(URI.create("http://example.com/path?key=val"));

            assertEquals(URI.create("https://example.com/path?key=val"), result);
        }

        @Test
        void httpUriWithFragment_preservesFragment() {
            URI result = HttpUris.toHttps(URI.create("http://example.com/page#section"));

            assertEquals(URI.create("https://example.com/page#section"), result);
        }

        @Test
        void httpUriWithNoPath_upgradesScheme() {
            URI result = HttpUris.toHttps(URI.create("http://example.com"));

            assertEquals(URI.create("https://example.com"), result);
        }

        @Test
        void httpUriWithQueryAndFragment_preservesAll() {
            URI result = HttpUris.toHttps(URI.create("http://example.com/page?q=test#section"));

            assertEquals(URI.create("https://example.com/page?q=test#section"), result);
        }

        @Test
        void ipv6Host_upgradesScheme() {
            URI result = HttpUris.toHttps(URI.create("http://[::1]:8080/path"));

            assertEquals(URI.create("https://[::1]:8080/path"), result);
        }
    }
}






