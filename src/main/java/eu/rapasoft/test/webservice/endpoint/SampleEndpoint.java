package eu.rapasoft.test.webservice.endpoint;

import eu.rapasoft.test.webservice.dto.SampleDto;
import eu.rapasoft.test.webservice.dto.SampleRequest;
import eu.rapasoft.test.webservice.dto.SampleResponse;
import eu.rapasoft.test.webservice.repository.SampleRepository;
import eu.rapasoft.test.webservice.util.NamespaceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;

/**
 * @author rap
 */
@Endpoint(value = "sample")
public class SampleEndpoint {

    @Autowired
    private SampleRepository sampleRepository;

    @PayloadRoot(namespace = NamespaceConstants.NAMESPACE_URI, localPart = "sampleRequest")
    @ResponsePayload
    public SampleResponse getDtos(@RequestPayload SampleRequest sampleRequest) {
        List<SampleDto> legalEntityOmegaMappingDtos = sampleRepository.fetchAll();

        return new SampleResponse(legalEntityOmegaMappingDtos);
    }

}
