package software.frisby.core.concurrency.log;

/**
 * A builder for creating an instance of {@link SystemLogVerifier}.  Obtain via
 * {@link SystemLogVerifier#builder()}.
 */
public interface SystemLogVerifierBuilder {
    /**
     * Configures the logger bound to {@code clazz} to the specified {@link System.Logger.Level}
     * for the duration of the test.  The original level is restored when {@link SystemLogVerifier#close()}
     * is called.
     * <p>
     * Use {@link System.Logger.Level#OFF} to drive the {@code isLoggable()} guards inside the
     * class under test to {@code false}, enabling branch coverage of those guards.
     *
     * @param clazz The class whose logger level will be overridden.
     * @param level The level to set for the duration of the test.
     * @return The current Builder instance.
     */
    SystemLogVerifierBuilder configure(Class<?> clazz, System.Logger.Level level);

    /**
     * Registers one or more {@link LogExpectation} instances that the verifier will evaluate
     * against captured log events during the test run.
     *
     * @param expectations One or more expectations to register.
     * @return The current Builder instance.
     */
    SystemLogVerifierBuilder expect(LogExpectation... expectations);

    /**
     * Returns a new {@link SystemLogVerifier} instance configured by this builder and immediately
     * registers it as the active verifier so that log events are routed to it.
     *
     * @return A new {@link SystemLogVerifier} instance.
     */
    SystemLogVerifier build();
}

