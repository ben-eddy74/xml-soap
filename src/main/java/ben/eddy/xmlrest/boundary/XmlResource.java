package ben.eddy.xmlrest.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("xml")
public class XmlResource {
    
    @GET
    @Produces
    public String getXml() {
        return "Hello";
    }
}
