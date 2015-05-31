package eu.rapasoft.test.webservice.repository;

import eu.rapasoft.test.webservice.Application;
import eu.rapasoft.test.webservice.dto.SampleDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rap
 */
public class SampleRepositoryTest {

    private SampleRepository sampleRepository = new SampleRepository();

    @Test
    public void testCanRetrieveData() {
        List<SampleDto> dtos = sampleRepository.fetchAll();

        assertNotNull(dtos);
        assertFalse(dtos.isEmpty());

        SampleDto sampleDto = dtos.get(0);

        assertEquals(1, sampleDto.getId());
        assertEquals("sample name", sampleDto.getSampleName());
        assertEquals("sample description", sampleDto.getDescription());
        assertNotNull(sampleDto.getDateAdded());
    }

}