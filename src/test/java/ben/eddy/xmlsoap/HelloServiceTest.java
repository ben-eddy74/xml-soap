package ben.eddy.xmlsoap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import ben.eddy.xmlsoap.boundary.HelloService;
import ben.eddy.xmlsoap.boundary.HelloServiceImpl;

import ben.eddy.xmlsoap.control.SecuritySoapHandlerTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Test Hello Webservice implementation")
public class HelloServiceTest {
    
    String urlPath = "http://localhost:8080{SOAPPATH}";

    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    HelloServiceImpl service = new HelloServiceImpl();
    SecuritySoapHandlerTest handler = new SecuritySoapHandlerTest();

    @ConfigProperty(name = "quarkus.cxf.path")
    String soapPath;

    @BeforeAll
    void init(){
        factory.setServiceClass(HelloService.class);
        urlPath = urlPath.replace("{SOAPPATH}", soapPath);

        factory.getHandlers().add(handler);
    }

    
    @DisplayName("Test Hello Webservice implementation")
    void hello(){
        String text = "World";

        factory.setAddress(String.format("%s/hello", urlPath));
        
        HelloService helloService = (HelloService) factory.create();
        String result = helloService.hello(text);

        assertEquals(result, service.hello(text));
    }

    @Test
    @DisplayName("Test Hello Webservice implementation with signature")
    void helloSecure(){
        String text = "World";

        factory.setAddress(String.format("%s/secure/hello", urlPath));
        
        HelloService helloService = (HelloService) factory.create();
        String result = helloService.hello(text);

        assertEquals(result, service.hello(text));
    }
}
