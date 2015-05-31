package eu.rapasoft.test.webservice.repository;

import eu.rapasoft.test.webservice.dto.SampleDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author rap
 */
@Service(value = "sampleRepository")
public class SampleRepository {

    public List<SampleDto> fetchAll() {
        return Arrays.asList(
                new SampleDto(1, "sample name", "sample description", new Date())
        );
    }
}
