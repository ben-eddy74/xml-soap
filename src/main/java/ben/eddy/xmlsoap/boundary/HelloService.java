package ben.eddy.xmlsoap.boundary;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(name = "HelloService", serviceName = "HelloService")
public interface HelloService {

    @WebMethod
    String hello(String text);

}