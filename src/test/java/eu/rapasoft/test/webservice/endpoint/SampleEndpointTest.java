package eu.rapasoft.test.webservice.endpoint;

import eu.rapasoft.test.webservice.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ws.test.server.MockWebServiceClient;

import java.io.IOException;

import static org.springframework.ws.test.server.RequestCreators.withSoapEnvelope;
import static org.springframework.ws.test.server.ResponseMatchers.validPayload;

/**
 * User: U000538
 * Date: 14.05.2015
 * Time: 13:46
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = Application.class)
public class SampleEndpointTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient client;

    @Before
    public void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    public void testServiceCall() throws IOException {
        final Resource soapEnvelope = new ClassPathResource("/sampleTest/test-request.xml");
        final Resource schema = new ClassPathResource("/test-ws.xsd");

        client.sendRequest(withSoapEnvelope(soapEnvelope)).
                andExpect(validPayload(schema));
    }

}