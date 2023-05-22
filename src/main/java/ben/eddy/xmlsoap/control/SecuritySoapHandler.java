package ben.eddy.xmlsoap.control;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

public class SecuritySoapHandler implements SOAPHandler<SOAPMessageContext> {

    private final String keystore = "/keystore.jks";
    private final String keystore_alias = "webserver";
    private final String keystore_password = "changeit";
    private final String keystore_keypassword = "changeit";

    private Key key;
    private X509Certificate cert;

    public SecuritySoapHandler() {

        getKeyAndCertificate();
        
    }

    private void getKeyAndCertificate() {
        
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");

            keyStore.load(
                this.getClass().getClassLoader().getResourceAsStream(keystore),
                keystore_password.toCharArray());

            key = keyStore.getKey(keystore_alias, keystore_keypassword.toCharArray());
            cert = (X509Certificate) keyStore.getCertificate(keystore_alias);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void close(MessageContext mc) {

    }

    @Override
    public boolean handleFault(SOAPMessageContext arg0) {

        return false;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {

        if(key == null) {getKeyAndCertificate();}

        SOAPMessage message = smc.getMessage();
        SOAPPart soapPart = message.getSOAPPart();

        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {

            Document document = soapPart.getOwnerDocument();

            if (outbound) {
                System.out.println("Handling outgoing SOAP message");

                System.out.printf("Signing outbound message with %s\n", cert.getSubjectX500Principal().getName());

                // Get or create header
                SOAPHeader header = message.getSOAPHeader();
                if(header == null){
                    header = message.getSOAPPart().getEnvelope().addHeader();                    
                }

                // Add SOAP Signature header
                Node soapSignature = header.addChildElement(new QName("http://schemas.xmlsoap.org/soap/security/2000-12", "Signature", "SOAP-SEC"));
                // Add Body attribute
                message.getSOAPBody().setAttribute("id", "Body");

                message.saveChanges();

                // Sign using DOM
                org.apache.xml.security.Init.init();
                XMLSignature sig = new XMLSignature(message.getSOAPBody(), "", "http://www.w3.org/2000/09/xmldsig#rsa-sha1",
                        "http://www.w3.org/2001/10/xml-exc-c14n#");

                soapSignature.appendChild(sig.getElement());

                Transforms transforms = new Transforms(document);
                transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
                transforms.addTransform("http://www.w3.org/2001/10/xml-exc-c14n#");

                sig.addDocument("#Body", transforms, "http://www.w3.org/2000/09/xmldsig#sha1");

                sig.sign(key);
                sig.addKeyInfo(cert);

                message.saveChanges();

            } 

            message.saveChanges();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;

        }
    }

    @Override
    public Set<QName> getHeaders() {

        return Collections.emptySet();
    }

}
