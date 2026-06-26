package software.frisby.core.util;

import software.frisby.core.validation.BlankValueException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Strings;
import software.frisby.core.validation.Values;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Static utility methods for structural manipulation of {@link URI} instances.
 *
 * <p>All methods in this class are scheme-agnostic — they operate on the structural
 * components of a URI ({@code scheme}, {@code authority}, {@code path}, {@code query},
 * {@code fragment}) and make no assumptions about the protocol.  For HTTP/S-specific
 * operations such as origin extraction, effective port resolution, and scheme upgrading,
 * see {@link HttpUris}.
 *
 * <p>Methods that strip or replace path components require the URI to be hierarchical
 * (i.e., it must have an authority component introduced by a {@code //} prefix).  Opaque
 * URIs such as {@code mailto:user@example.com} are not supported.
 *
 * @see HttpUris
 * @see URI
 */
public final class Uris {
    private static final String URI_ARGUMENT_NAME = "uri";
    private static final String PATH_ARGUMENT_NAME = "path";
    private static final String SEGMENT_ARGUMENT_NAME = "segment";
    private static final String NO_AUTHORITY_MSG =
            "The 'uri' value of '%s' is invalid.  The URI must be hierarchical and have an authority component.";
    private static final String INVALID_URI_MSG =
            "The 'uri' value of '%s' is invalid.  The value is not a valid URI.";

    private Uris() {
    }

    /**
     * Returns a new {@link URI} with the path, query string, and fragment removed.
     *
     * <p>The scheme and authority (host, port, and any userinfo) are preserved exactly
     * as they appear in {@code uri}; no normalization is applied.  For HTTP/S URIs where
     * default port suppression is required, use {@link HttpUris#origin(URI)} instead.
     *
     * @param uri The URI to strip; must not be null and must have an authority component.
     * @return A new {@link URI} containing only the scheme and authority of {@code uri}.
     * @throws NullValueException       if {@code uri} is null.
     * @throws IllegalArgumentException if {@code uri} has no authority component.
     */
    public static URI withoutPath(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        throwIfNoAuthority(uri);

        return URI.create(uri.getScheme() + "://" + uri.getRawAuthority());
    }

    /**
     * Returns a new {@link URI} with the path replaced by {@code path}.
     *
     * <p>The query string and fragment are not preserved.  The returned URI contains
     * only the scheme, authority, and the supplied {@code path}.
     *
     * @param uri  The base URI; must not be null and must have an authority component.
     * @param path The replacement path; must not be null.  Use an empty string or
     *             {@code "/"} to navigate to the root.
     * @return A new {@link URI} with the same scheme and authority as {@code uri} and
     * the given {@code path}.
     * @throws NullValueException       if {@code uri} or {@code path} is null.
     * @throws IllegalArgumentException if {@code uri} has no authority component.
     */
    public static URI withPath(URI uri, String path) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        Values.notNull(PATH_ARGUMENT_NAME, path);
        throwIfNoAuthority(uri);

        return URI.create(uri.getScheme() + "://" + uri.getRawAuthority() + path);
    }

    /**
     * Returns a new {@link URI} with {@code segment} appended to the existing path.
     *
     * <p>Exactly one {@code /} separator is placed between the existing path and
     * {@code segment} regardless of whether the existing path ends with {@code /} or
     * {@code segment} starts with {@code /}.  The query string and fragment of
     * {@code uri} are not preserved.
     *
     * @param uri     The base URI; must not be null and must have an authority component.
     * @param segment The path segment to append; must not be null or blank.
     * @return A new {@link URI} with {@code segment} appended to the path of {@code uri}.
     * @throws NullValueException       if {@code uri} is null.
     * @throws NullValueException       if {@code segment} is null.
     * @throws BlankValueException      if {@code segment} is blank.
     * @throws IllegalArgumentException if {@code uri} has no authority component.
     */
    public static URI appendPath(URI uri, String segment) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        Strings.notBlank(SEGMENT_ARGUMENT_NAME, segment);
        throwIfNoAuthority(uri);

        String base = uri.getRawPath();
        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String normalizedSegment = segment.startsWith("/") ? segment : "/" + segment;

        return URI.create(
                uri.getScheme() + "://" + uri.getRawAuthority() + normalizedBase + normalizedSegment
        );
    }

    /**
     * Returns a new {@link URI} with the query string and fragment removed.
     *
     * <p>The scheme, authority, and path of {@code uri} are preserved unchanged.
     *
     * @param uri The URI to strip; must not be null and must have an authority component.
     * @return A new {@link URI} with the query string and fragment removed.
     * @throws NullValueException       if {@code uri} is null.
     * @throws IllegalArgumentException if {@code uri} has no authority component.
     */
    public static URI withoutQuery(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);
        throwIfNoAuthority(uri);

        return URI.create(uri.getScheme() + "://" + uri.getRawAuthority() + uri.getRawPath());
    }

    /**
     * Parses {@code uri} into a {@link URI} instance.
     *
     * <p>This method provides a validation-convention front-door for URI construction.
     * It is equivalent to {@code new URI(uri)} but throws project-standard exceptions
     * rather than a checked {@link URISyntaxException}.
     *
     * @param uri The URI string to parse; must not be null or blank.
     * @return A new {@link URI} parsed from {@code uri}.
     * @throws NullValueException       if {@code uri} is null.
     * @throws BlankValueException      if {@code uri} is blank.
     * @throws IllegalArgumentException if {@code uri} is not a valid URI.
     */
    public static URI parse(String uri) {
        Strings.notBlank(URI_ARGUMENT_NAME, uri);

        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    String.format(
                            INVALID_URI_MSG,
                            uri
                    ),
                    e
            );
        }
    }

    /**
     * Returns the user component of the userinfo in {@code uri}, or {@code null} if
     * the URI has no userinfo.
     *
     * <p>The returned value is percent-decoded.  If the userinfo contains no {@code :}
     * separator the entire userinfo is returned as the user.
     *
     * <p>Example:
     * <pre>
     * user(URI.create("mongodb://frank:secret@localhost/admin"))  // "frank"
     * user(URI.create("ftp://frank@files.example.com"))           // "frank"
     * user(URI.create("https://example.com/path"))                // null
     * </pre>
     *
     * @param uri The URI to inspect; must not be null.
     * @return The decoded user component, or {@code null} if the URI has no userinfo.
     * @throws NullValueException if {@code uri} is null.
     */
    public static String user(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);

        String userInfo = uri.getUserInfo();
        if (null == userInfo) {
            return null;
        }

        int colon = userInfo.indexOf(':');

        return colon == -1 ? userInfo : userInfo.substring(0, colon);
    }

    /**
     * Returns the password component of the userinfo in {@code uri}, or {@code null}
     * if the URI has no userinfo or the userinfo contains no {@code :} separator.
     *
     * <p>The returned value is percent-decoded.  If the password itself contains
     * {@code :} characters those are preserved; only the <em>first</em> {@code :} in
     * the userinfo is treated as the user/password separator.
     *
     * <p>Example:
     * <pre>
     * password(URI.create("mongodb://frank:secret@localhost/admin"))  // "secret"
     * password(URI.create("mongodb://frank:pa:ss@localhost/admin"))   // "pa:ss"
     * password(URI.create("ftp://frank@files.example.com"))           // null
     * password(URI.create("https://example.com/path"))                // null
     * </pre>
     *
     * @param uri The URI to inspect; must not be null.
     * @return The decoded password component, or {@code null} if absent.
     * @throws NullValueException if {@code uri} is null.
     */
    public static String password(URI uri) {
        Values.notNull(URI_ARGUMENT_NAME, uri);

        String userInfo = uri.getUserInfo();
        if (null == userInfo) {
            return null;
        }

        int colon = userInfo.indexOf(':');

        return colon == -1 ? null : userInfo.substring(colon + 1);
    }

    private static void throwIfNoAuthority(URI uri) {
        if (null == uri.getRawAuthority()) {
            throw new IllegalArgumentException(
                    String.format(
                            NO_AUTHORITY_MSG,
                            uri
                    )
            );
        }
    }
}
