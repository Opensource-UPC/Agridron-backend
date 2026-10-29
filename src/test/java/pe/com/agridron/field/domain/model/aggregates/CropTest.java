package pe.com.agridron.field.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CropTest {

    @Test
    void registersCropWithNormalizedText() {
        var crop = Crop.register("  Café  ", "  Catimor  ");

        assertThat(crop.getId()).isNull();
        assertThat(crop.getName()).isEqualTo("Café");
        assertThat(crop.getVariety()).isEqualTo("Catimor");
    }

    @Test
    void reconstitutesAnExistingCrop() {
        var crop = Crop.reconstitute(5L, "Papa", "Única");

        assertThat(crop.getId()).isEqualTo(5L);
        assertThat(crop.getName()).isEqualTo("Papa");
        assertThat(crop.getVariety()).isEqualTo("Única");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Crop.register(" ", "Catimor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsBlankVariety() {
        assertThatThrownBy(() -> Crop.register("Café", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("variety");
    }

    @Test
    void rejectsTooLongText() {
        assertThatThrownBy(() -> Crop.register("a".repeat(121), "Catimor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("120");
    }
}
