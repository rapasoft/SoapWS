#SOAP WebServices made easily with Spring Boot

I know, SOAP-based web services are not cool, but in enterprise world they are still popular since it's well documented and robust and it's been here forever. Recently I have received an assignment to create simple SOAP endpoints. JAVA EE and JAX-WS would be a good choice, but I've decided to stick with Spring WS with Spring Boot as the super-glue. 
  
The assignment covered these topics:

* Create two endpoints that would return list of query results based on simple filtering
* Use WS-Security for basic authentication

Following [the official tutorial](https://spring.io/guides/gs/producing-web-service/), I have started with an empty Maven project adding all the dependencies and preliminary configurations (Spring Boot Application main class, WebService configuration etc.). Next task was creating XSD file with the definition of transfer objects. For brevity, let's assume the definition of transfer object looks like this:

            <xs:complexType name="sampleDto">
                <xs:sequence>
                    <xs:element minOccurs="1" maxOccurs="1" name="id" type="xs:int"/>
                    <xs:element name="sample_name" type="xs:string"/>
                    <xs:element name="description" type="xs:string"/>
                    <xs:element minOccurs="0" name="date_added" type="xs:date"/>
                </xs:sequence>
            </xs:complexType>
            	
Next topic was generating classes from the XSD file. For this [Maven JAXB2 plugin](http://mojo.codehaus.org/jaxb2-maven-plugin/) is the obvious choice. It is easy to setup and configuration options are really wide. Classes are generated using XJC generator - but the first problem that I encountered I already explained [in this previous post](http://blog.rapasoft.eu/xjc_and_jdk8). XJC needs to have special permissions so that it can work correctly, enough said.
 
Second problem with generation of classes was the `xs:date` type, which produces `XMLGregorianCalendar`-typed fields instead of more widely spread `java.util.Date`. For this, custom binding and adapter must be created to successfully convert these date types. First of all, a simple `DateAdapter` class must be made with two methods that will convert the `Date` to `String` and vice-versa:
 
             public class DateAdapter {
             
                public static Date parseDate(String s) {
                    return DatatypeConverter.parseDate(s).getTime();
                }
             
                public static String printDate(Date dt) {
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(dt);
                    return DatatypeConverter.printDate(cal).split("\\+")[0];
                }
             
             }

Then, the binding definition file (`jaxb-custom-binding.xml`) must be created so that the `xs:date` is properly converted:
  
              <bindings xmlns="http://java.sun.com/xml/ns/jaxb" version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <globalBindings>
                    <javaType name="java.util.Date" xmlType="xs:date"
                              parseMethod="com.scor.norda.webservice.util.DateAdapter.parseDate"
                              printMethod="com.scor.norda.webservice.util.DateAdapter.printDate"
                            />
                </globalBindings>
              </bindings>
  
Ok, close enough, but there is still one thing missing - to be able to use these generated classes for example as DTOs in JPA we need to have all-args constructor present for this class. This can be also setup easily with `jaxb2-value-constructor` by setting one Maven property `xjc.arguments`. An excerpt from pom.xml with relevant parts (together with jaxb2 setup) would look like this:
    
            ...
    
            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <xjc.arguments>-Xvalue-constructor</xjc.arguments>
            </properties>
            
            ...
            
            <plugin>
                  <groupId>org.codehaus.mojo</groupId>
                  <artifactId>jaxb2-maven-plugin</artifactId>
                  <version>1.6</version>
                  <executions>
                      <execution>
                          <id>xjc</id>
                          <phase>generate-sources</phase>
                          <goals>
                              <goal>xjc</goal>
                          </goals>
                      </execution>
                  </executions>
                  <configuration>
                      <schemaDirectory>${project.basedir}/src/main/resources/</schemaDirectory>
                      <outputDirectory>${project.basedir}/src/main/jaxb</outputDirectory>
                      <packageName>eu.rapasoft.webservice.dto</packageName>
                      <clearOutputDir>false</clearOutputDir>
                      <!-- TODO: IMPORTANT! You need to execute Maven goal with -Djavax.xml.accessExternalSchema=all VM arguments -->
                      <!-- SEE: http://stackoverflow.com/questions/23011547/webservice-client-generation-error-with-jdk8 -->
                      <bindingDirectory>${project.basedir}/src/main/resources</bindingDirectory>
                      <bindingFiles>jaxb-custom-binding.xml</bindingFiles>
                  </configuration>
                  <dependencies>
                      <dependency>
                          <groupId>org.jvnet.jaxb2_commons</groupId>
                          <artifactId>jaxb2-value-constructor</artifactId>
                          <version>3.0</version>
                      </dependency>
                  </dependencies>
              </plugin>
              
With all this ready, the generated classes are ready to be used directly as DTOs in JPA queries (e.g. `select new eu.rapasoft.dto.SampleDto(...) from ...`).
 
The endpoint itself is really easy. With Spring WS you just create properly annotated class:

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
            
As I wrote in the introduction, next step would be adding simple WS-Security basic authentication. A relevant part of code for this is hidden in Spring Boot configuration class (where also other WS-related settings are defined):

            @EnableWs
            @Configuration
            public class WebServiceConfiguration extends WsConfigurerAdapter {
            
                ...
                // Other WS-related settings
                ...
            
                @Bean
                public XwsSecurityInterceptor securityInterceptor() {
                    XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();
                    securityInterceptor.setCallbackHandler(callbackHandler());
                    securityInterceptor.setPolicyConfiguration(new ClassPathResource("securityPolicy.xml"));
                    return securityInterceptor;
                }
            
                @Bean
                SimplePasswordValidationCallbackHandler callbackHandler() {
                    SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
                    // TODO @rap: Use real username and passwords
                    callbackHandler.setUsersMap(Collections.singletonMap("user", "password"));
                    return callbackHandler;
                }
            
                @Override
                public void addInterceptors(List<EndpointInterceptor> interceptors) {
                    interceptors.add(securityInterceptor());
                }
                
            }
            
The `securityPolicy.xml` file (placed in resources directory) can look like this:

            <xwss:SecurityConfiguration xmlns:xwss="http://java.sun.com/xml/ns/xwss/config">
            	<xwss:RequireUsernameToken passwordDigestRequired="false" nonceRequired="false" />
            </xwss:SecurityConfiguration>
            
As you can see, no enhanced encryption, just basic authentication which combined with secure chanel (SSL) should suffice for internal use. The envelope that is being send as a request should contain headers with username/password:

            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                <soapenv:Header>
                    <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" soapenv:mustUnderstand="1">
                        <wsse:UsernameToken>
                            <wsse:Username>user</wsse:Username>
                            <wsse:Password>password</wsse:Password>
                        </wsse:UsernameToken>
                    </wsse:Security>
                </soapenv:Header>
                <soapenv:Body>
                    ...
                </soapenv:Body>
            </soapenv:Envelope>
            
And that's basically it. This is just a rough overview, more sense would make viewing the whole source code ;), therefore I have prepared a sample GitHub (without the DB connection and JPA stuff, to decrease complexity a bit).