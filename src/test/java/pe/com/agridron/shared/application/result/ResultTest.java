package pe.com.agridron.shared.application.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void successExposesValueAndIsSuccess() {
        var result = Result.<String, String>success("ok");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.toOptional()).contains("ok");
        assertThat(result.getOrElse("fallback")).isEqualTo("ok");
    }

    @Test
    void failureExposesErrorAndIsFailure() {
        var result = Result.<String, String>failure("boom");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.toOptional()).isEmpty();
        assertThat(result.getOrElse("fallback")).isEqualTo("fallback");
    }

    @Test
    void mapTransformsSuccessAndKeepsFailure() {
        assertThat(Result.<Integer, String>success(2).map(value -> value * 3).getOrElse(-1))
                .isEqualTo(6);
        assertThat(Result.<Integer, String>failure("boom").map(value -> value * 3).getOrElse(-1))
                .isEqualTo(-1);
    }

    @Test
    void flatMapChainsResultsAndPropagatesFailure() {
        var success = Result.<Integer, String>success(2)
                .flatMap(value -> Result.success(value + 1));
        var failure = Result.<Integer, String>failure("boom")
                .flatMap(value -> Result.<Integer, String>success(value + 1));

        assertThat(success.getOrElse(-1)).isEqualTo(3);
        assertThat(failure.isFailure()).isTrue();
    }

    @Test
    void mapErrorTransformsOnlyFailures() {
        assertThat(Result.<Integer, String>success(1).mapError(String::length).isSuccess()).isTrue();
        assertThat(Result.<Integer, String>failure("boom").mapError(String::length).toOptional()).isEmpty();
    }

    @Test
    void recoverCanReplaceAFailure() {
        var recovered = Result.<Integer, String>failure("boom")
                .recover(error -> Result.success(42));

        assertThat(recovered.getOrElse(-1)).isEqualTo(42);
    }

    @Test
    void foldBranchesIntoSingleValue() {
        var success = Result.<Integer, String>success(10)
                .fold(value -> "value=" + value, error -> "error=" + error);
        var failure = Result.<Integer, String>failure("boom")
                .fold(value -> "value=" + value, error -> "error=" + error);

        assertThat(success).isEqualTo("value=10");
        assertThat(failure).isEqualTo("error=boom");
    }

    @Test
    void peekRunsOnlyOnSuccess() {
        var counter = new int[1];

        Result.<Integer, String>success(1).peek(value -> counter[0] += value);
        Result.<Integer, String>failure("boom").peek(value -> counter[0] += value);

        assertThat(counter[0]).isEqualTo(1);
    }
}
