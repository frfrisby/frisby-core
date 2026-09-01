package software.frisby.core.util;

import software.frisby.core.validation.DisallowedValueException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Values;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

/**
 * Static utility methods for working with HTTP and HTTPS {@link URI} instances.
 *
 * <p>Every method in this class enforces that the URI scheme is either {@code http}
 * or {@code https}.  Passing a URI with any other scheme — or a URI whose scheme is
 * absent — throws a {@link software.frisby.core.validation.DisallowedValueException}
 * or {@link software.frisby.core.validation.NullValueException} respectively.
 *
 * <p>For scheme-agnostic structural operations (path replacement, segment appending,
 * query stripping), see {@link Uris}.
 *
 * <p>Example:
 * <pre>
 * URI api = URI.create("https://example.com:8443/api/v1/users?page=2");
 *
 * HttpUris.origin(api);           // https://example.com:8443
 * HttpUris.effectivePort(api);    // 8443
 * HttpUris.isDefaultPort(api);    // false
 * HttpUris.toHttps(api);          // https://example.com:8443/api/v1/users?page=2 (unchanged)
 *
 * URI plain = URI.create("http://example.com/login");
 *
 * HttpUris.origin(plain);         // http://example.com   (port 80 suppressed)
 * HttpUris.effectivePort(plain);  // 80
 * HttpUris.isDefaultPort(plain);  // true
 * HttpUris.toHttps(plain);        // https://example.com/login
 * </pre>
 *
 * @see Uris
 * @see URI
 */
public final class HttpUris {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final Set<String> HTTP_SCHEMES = Set.of(HTTP, HTTPS);
    private static final String URI_ARGUMENT_NAME = "uri";
    private static final String SCHEME_ARGUMENT_NAME = "scheme";
    private static final String NO_HOST_MSG =
                "The 'uri' value of '%s' is invalid. The URI must have a resolvable host component.";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private HttpUris() {
    }

    /**
     * Returns the HTTP origin of {@code source}: the scheme, host, and port, with the
     * port omitted when it is the default for the scheme ({@code 80} for {@code http},
     * {@code 443} for {@code https}).
     *
     * <p>This method implements the origin concept defined in RFC 6454 and is suitable
     * for use in CORS, CSP, and cookie-domain logic.  Userinfo and path components are
     * never included in the origin.
     *
     * <p>Examples:
     * <pre>
     * origin(URI.create("https://example.com:443/path"))   // https://example.com
     * origin(URI.create("https://example.com:8443/path"))  // https://example.com:8443
     * origin(URI.create("http://example.com/path"))        // http://example.com
     * origin(URI.create("http://example.com:80/path"))     // http://example.com
     * </pre>
     *
     * @param uri The URI whose origin is to be extracted; must not be null and must
     *            use the {@code http} or {@code https} scheme.
     * @return A new {@link URI} representing the origin of {@code uri}.
     * @throws NullValueException       if {@code uri} is null.
     * @throws NullValueException       if {@code uri} has no scheme.
     * @throws DisallowedValueException if the scheme of {@code uri} is not {@code http}
     *                                  or {@code https}.
     */
    public static URI origin(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        Values.oneOf(SCHEME_ARGUMENT_NAME, scheme(uri), HTTP_SCHEMES);

        String scheme = scheme(uri);
        String host = formatHost(uri);
        int port = resolvedPort(uri);

        if (isDefaultPort(uri)) {
            return URI.create(scheme + "://" + host);
        }

        return URI.create(scheme + "://" + host + ":" + port);
    }

    /**
     * Returns the effective port of {@code uri}.
     *
     * <p>If the URI specifies an explicit port, that port is returned.  Otherwise, the
     * default port for the scheme is returned: {@code 80} for {@code http} and
     * {@code 443} for {@code https}.
     *
     * @param uri The URI whose effective port is to be resolved; must not be null and
     *            must use the {@code http} or {@code https} scheme.
     * @return The effective port number.
     * @throws NullValueException       if {@code uri} is null.
     * @throws NullValueException       if {@code uri} has no scheme.
     * @throws DisallowedValueException if the scheme of {@code uri} is not {@code http}
     *                                  or {@code https}.
     */
    public static int effectivePort(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        Values.oneOf(SCHEME_ARGUMENT_NAME, scheme(uri), HTTP_SCHEMES);

        return resolvedPort(uri);
    }

    /**
     * Returns {@code true} if the port of {@code uri} is the default for its scheme.
     *
     * <p>A URI with no explicit port (i.e., {@link URI#getPort()} returns {@code -1})
     * is considered to be on the default port.  A URI with an explicit port that equals
     * the scheme default ({@code 80} for {@code http}, {@code 443} for {@code https})
     * is also considered to be on the default port.
     *
     * @param uri The URI to evaluate; must not be null and must use the {@code http}
     *            or {@code https} scheme.
     * @return {@code true} if {@code uri} is on the default port for its scheme.
     * @throws NullValueException       if {@code uri} is null.
     * @throws NullValueException       if {@code uri} has no scheme.
     * @throws DisallowedValueException if the scheme of {@code uri} is not {@code http}
     *                                  or {@code https}.
     */
    public static boolean isDefaultPort(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        Values.oneOf(SCHEME_ARGUMENT_NAME, scheme(uri), HTTP_SCHEMES);

        int port = uri.getPort();
        if (port == -1) {
            return true;
        }

        return (HTTP.equals(scheme(uri)) && port == HTTP_DEFAULT_PORT)
                || (HTTPS.equals(scheme(uri)) && port == HTTPS_DEFAULT_PORT);
    }

    /**
     * Returns {@code true} if {@code uri} uses the {@code http} scheme.
     *
     * @param uri The URI to test; must not be null.
     * @return {@code true} if the scheme of {@code uri} is {@code http}.
     * @throws NullValueException if {@code uri} is null.
     */
    public static boolean isHttp(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);

        return HTTP.equals(scheme(uri));
    }

    /**
     * Returns {@code true} if {@code uri} uses the {@code https} scheme.
     *
     * @param uri The URI to test; must not be null.
     * @return {@code true} if the scheme of {@code uri} is {@code https}.
     * @throws NullValueException if {@code uri} is null.
     */
    public static boolean isHttps(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);

        return HTTPS.equals(scheme(uri));
    }

    /**
     * Returns a new {@link URI} with the scheme changed to {@code https}.
     *
     * <p>If {@code uri} already uses the {@code https} scheme it is returned unchanged.
     * The authority, path, query string, and fragment are preserved exactly as they
     * appear in {@code uri}.
     *
     * @param uri The URI to upgrade; must not be null and must use the {@code http}
     *            or {@code https} scheme.
     * @return A {@link URI} identical to {@code uri} except with the {@code https} scheme.
     * @throws NullValueException       if {@code uri} is null.
     * @throws NullValueException       if {@code uri} has no scheme.
     * @throws DisallowedValueException if the scheme of {@code uri} is not {@code http}
     *                                  or {@code https}.
     */
    public static URI toHttps(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        Values.oneOf(SCHEME_ARGUMENT_NAME, scheme(uri), HTTP_SCHEMES);

        if (HTTPS.equals(scheme(uri))) {
            return uri;
        }

        StringBuilder sb = new StringBuilder(HTTPS).append("://").append(uri.getRawAuthority());

        sb.append(uri.getRawPath());

        String rawQuery = uri.getRawQuery();
        if (null != rawQuery) {
            sb.append('?').append(rawQuery);
        }

        String rawFragment = uri.getRawFragment();
        if (null != rawFragment) {
            sb.append('#').append(rawFragment);
        }

        return URI.create(sb.toString());
    }

    private static int resolvedPort(URI uri) {
        int port = uri.getPort();

        if (port != -1) {
            return port;
        }

        return HTTP.equals(scheme(uri)) ? HTTP_DEFAULT_PORT : HTTPS_DEFAULT_PORT;
    }

    private static String scheme(URI uri) {
        String s = uri.getScheme();

        return null == s ? null : s.toLowerCase(Locale.ROOT);
    }

    private static String formatHost(URI uri) {
        String host = uri.getHost();

        if (null == host) {
            throw new IllegalArgumentException(
                    String.format(
                            NO_HOST_MSG,
                            uri
                    )
            );
        }

        // On Java 17+, URI.getHost() returns IPv6 addresses with the square
        // brackets already included (e.g. "[::1]"), so no reformatting is needed.
        return host;
    }
}


