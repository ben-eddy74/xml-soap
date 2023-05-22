package ben.eddy.xmlsoap.control;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

public class SecuritySoapHandlerTest implements SOAPHandler<SOAPMessageContext> {

    private final String keystore = "/keystore.jks";
    private final String keystore_alias = "webserver";
    private final String keystore_password = "changeit";
    private final String keystore_keypassword = "changeit";

    private Key key;
    private X509Certificate cert;

    public SecuritySoapHandlerTest() {

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

        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {

            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            Document document = soapPart.getOwnerDocument();

            if (!outbound) {

                System.out.println("Handling incoming SOAP message");
                
                NodeList signatures = document.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");
                System.out.printf("signatures: %d\n",signatures.getLength());

                System.out.println(document.getFirstChild().getLocalName());
                if(signatures.getLength() > 0){
                    System.out.println("Verifying signature");
                    Element signatureElement = (Element)signatures.item(0);

                    XMLSignature signature = new XMLSignature(signatureElement, "");
                    KeyInfo ki = signature.getKeyInfo();
                    Boolean valid = signature.checkSignatureValue(ki.getX509Certificate());

                    System.out.println("Signature valid: " + valid);
                }
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
