package software.frisby.core.concurrency;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * A type token that captures a full generic type at runtime, working around Java's type erasure.
 *
 * <p>Instantiate this class as an anonymous subclass, supplying the desired type as the type
 * argument.  The constructor uses reflection to extract the actual type argument from the
 * anonymous subclass's {@code getGenericSuperclass()} metadata — information that survives
 * erasure because it is encoded in the class file, not at the call site.</p>
 *
 * <pre>{@code
 * // Capture List<Message> as a runtime type token
 * GenericType<List<Message>> type = new GenericType<>() {};
 *
 * // Use as an inference aid in fluent factory methods
 * Buffer<List<Message>> buffer = Buffer.of(new GenericType<List<Message>>() {});
 * }</pre>
 *
 * <p>Alternatively, create a named subclass for a frequently-used type:</p>
 *
 * <pre>{@code
 * public final class MessageListType extends GenericType<List<Message>> {}
 * }</pre>
 *
 * @param <T> The generic type captured by this token.
 * @implNote The super type token pattern was first described by Neal Gafter.  This
 * implementation is inspired by Jersey's {@code jakarta.ws.rs.core.GenericType}
 * (Eclipse Foundation, Apache License 2.0) and Jackson's
 * {@code com.fasterxml.jackson.core.type.TypeReference}.
 */
@SuppressWarnings("GrazieInspectionRunner")
public abstract class GenericType<T> {
    private final Type type;
    private final Class<T> rawType;

    /**
     * Captures the generic type argument supplied by the anonymous or named subclass.
     * Must be invoked from a concrete subclass that specifies the type argument — either
     * as an anonymous class ({@code new GenericType<List<Message>>() {}}) or as a named
     * subclass ({@code class MessageListType extends GenericType<List<Message>> {}}).
     *
     * @throws IllegalStateException if the subclass does not supply a concrete type
     *                               argument for {@code T}.
     */
    @SuppressWarnings("unchecked")
    protected GenericType() {
        this.type = getTypeArgument(getClass());
        this.rawType = (Class<T>) getTypeClass(type);
    }

    private static Type getTypeArgument(final Class<?> clazz) {
        Type currentType;
        Class<?> currentClass = clazz;

        do {
            currentType = currentClass.getGenericSuperclass();

            if (currentType instanceof Class<?> c) {
                currentClass = c;
            }

            if (currentType instanceof ParameterizedType p) {
                currentClass = (Class<?>) p.getRawType();
            }
        } while (!currentClass.equals(GenericType.class));

        TypeVariable<?> tv = GenericType.class.getTypeParameters()[0];

        if (currentType instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            int argIndex = Arrays.asList(rawType.getTypeParameters()).indexOf(tv);

            return parameterizedType.getActualTypeArguments()[argIndex];
        }

        throw new IllegalStateException(
                String.format(
                        "The type '%s' does not specify the type parameter T of GenericType<T>.",
                        currentType
                )
        );
    }

    // Package-private to support unit testing.  Do not make public.
    static Class<?> getTypeClass(final Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }

        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() instanceof Class<?> c) {
                return c;
            }
        }

        if (type instanceof GenericArrayType array) {
            final Class<?> componentRawType = getTypeClass(array.getGenericComponentType());

            return Array.newInstance(componentRawType, 0).getClass();
        }

        throw new IllegalArgumentException(
                String.format(
                        "Type parameter '%s' is not a class or parameterized type whose raw type is a class.",
                        type
                )
        );
    }

    /**
     * Returns the full generic type captured by this token, including any type arguments.
     * For example, for {@code new GenericType<List<String>>() {}}, this returns the
     * {@link ParameterizedType} representing {@code List<String>}.
     *
     * @return The full {@link Type} represented by this token.
     */
    public final Type getType() {
        return type;
    }

    /**
     * Returns the raw (erased) class of the captured type.  For example, for
     * {@code new GenericType<List<String>>() {}}, this returns {@code List.class}.
     *
     * @return The raw {@link Class} of the captured type.
     */
    public final Class<T> getRawType() {
        return rawType;
    }
}

