package tests.java.unit.service;

import org.junit.jupiter.api.Test;
import root.service.IdGenerator;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class IdGeneratorTest {

    @Test
    void shouldGenerateUniqueId() {
        // GIVEN:
        var idGenerator = new IdGenerator();
        var setOfIds = new HashSet<String>();

        // EXPECT:
        IntStream.range(0, 20).forEach(__ -> {
            var id = idGenerator.generate();
            assertThat(id).hasSize(8);
            assertThat(setOfIds.add(id)).isTrue();
        });
    }
}
