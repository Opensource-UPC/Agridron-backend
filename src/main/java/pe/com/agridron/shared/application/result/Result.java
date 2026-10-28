package pe.com.agridron.shared.application.result;

import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the result of a command or operation that can either succeed with a value or fail with an error.
 *
 * <p>This type encodes the possibility of failure in the type system, making error cases explicit and
 * composable. Application services must return {@code Result} instead of throwing exceptions for
 * expected failures.</p>
 *
 * @param <T> the type of the successful result value
 * @param <E> the type of the error/failure information
 */
@NullMarked
public sealed interface Result<T, E> {

    /**
     * Represents a successful result containing a value.
     */
    record Success<T, E>(T value) implements Result<T, E> {
    }

    /**
     * Represents a failed result containing error information.
     */
    record Failure<T, E>(E error) implements Result<T, E> {
    }

    /**
     * Creates a successful result with the given value.
     *
     * @param value the success value
     * @param <T>   the type of the value
     * @param <E>   the type of the error
     * @return a {@link Success} result
     */
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed result with the given error.
     *
     * @param error the error information
     * @param <T>   the type of the value
     * @param <E>   the type of the error
     * @return a {@link Failure} result
     */
    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    /**
     * Returns {@code true} if this result is a success.
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Returns {@code true} if this result is a failure.
     */
    default boolean isFailure() {
        return this instanceof Failure;
    }

    /**
     * Returns an {@link Optional} containing the value if this is a success, otherwise an empty optional.
     */
    default Optional<T> toOptional() {
        return switch (this) {
            case Success<T, E> success -> Optional.of(success.value());
            case Failure<T, E> failure -> Optional.empty();
        };
    }

    /**
     * Extracts the value if successful, or returns the given default value if failed.
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T, E> success -> success.value();
            case Failure<T, E> failure -> defaultValue;
        };
    }

    /**
     * Applies a function to the value if this is a success, producing a new result.
     */
    default <T2> Result<T2, E> map(Function<T, T2> mapper) {
        return switch (this) {
            case Success<T, E> success -> Result.success(mapper.apply(success.value()));
            case Failure<T, E> failure -> Result.failure(failure.error());
        };
    }

    /**
     * Applies a function that itself returns a {@code Result} if this is a success.
     */
    default <T2> Result<T2, E> flatMap(Function<T, Result<T2, E>> mapper) {
        return switch (this) {
            case Success<T, E> success -> mapper.apply(success.value());
            case Failure<T, E> failure -> Result.failure(failure.error());
        };
    }

    /**
     * Applies a function to the error if this is a failure, otherwise returns this unchanged.
     */
    default <E2> Result<T, E2> mapError(Function<E, E2> mapper) {
        return switch (this) {
            case Success<T, E> success -> Result.success(success.value());
            case Failure<T, E> failure -> Result.failure(mapper.apply(failure.error()));
        };
    }

    /**
     * Applies a recovery function to the error if this is a failure.
     */
    default Result<T, E> recover(Function<E, Result<T, E>> recovery) {
        return switch (this) {
            case Success<T, E> success -> this;
            case Failure<T, E> failure -> recovery.apply(failure.error());
        };
    }

    /**
     * Folds both branches into a single value.
     */
    default <R> R fold(Function<T, R> onSuccess, Function<E, R> onFailure) {
        return switch (this) {
            case Success<T, E> success -> onSuccess.apply(success.value());
            case Failure<T, E> failure -> onFailure.apply(failure.error());
        };
    }

    /**
     * Executes the given action when this result is a success and returns this unchanged.
     */
    default Result<T, E> peek(Consumer<T> action) {
        if (this instanceof Success<T, E> success) {
            action.accept(success.value());
        }
        return this;
    }
}
