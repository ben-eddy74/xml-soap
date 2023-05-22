package ben.eddy.xmlsoap.boundary;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(serviceName = "HelloService")
public class HelloServiceImpl implements HelloService {

    @WebMethod
    @Override
    public String hello(String text) {
        return "Hello " + text + "!";
    }

}