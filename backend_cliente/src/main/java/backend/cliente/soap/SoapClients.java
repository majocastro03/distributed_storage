package backend.cliente.soap;

import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import servidor.aplicacion.soap.interfaces.AuthInterfaceSOAP;
import servidor.aplicacion.soap.interfaces.FileInterfaceSOAP;

/**
 * Helper to create SOAP ports for Auth and File services using a WSDL URL from configuration.
 * It will try common service QName variations so it works with the simple Endpoint.publish used
 * in the existing `servidor_aplicacion`.
 */
public class SoapClients {
    private static final Logger logger = Logger.getLogger(SoapClients.class.getName());

    private final String authWsdlUrl;
    private final String fileWsdlUrl;

    public SoapClients(String authWsdlUrl, String fileWsdlUrl) {
        this.authWsdlUrl = authWsdlUrl;
        this.fileWsdlUrl = fileWsdlUrl;
    }

    public AuthInterfaceSOAP createAuthPort() throws Exception {
        return createPort(authWsdlUrl, "http://services.soap.aplicacion.servidor/", "AuthServiceSOAPService", AuthInterfaceSOAP.class);
    }

    public FileInterfaceSOAP createFilePort() throws Exception {
        return createPort(fileWsdlUrl, "http://services.soap.aplicacion.servidor/", "FileServiceSOAPService", FileInterfaceSOAP.class);
    }

    private <T> T createPort(String wsdlUrl, String namespace, String serviceName, Class<T> portClass) throws Exception {
        URL url = new URL(wsdlUrl);

        // If insecure SSL testing is enabled, install an all-trusting SSL context (ONLY FOR LOCAL TESTS)
        if ("true".equalsIgnoreCase(System.getProperty("insecure.ssl", "false"))) {
            try {
                TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                } };
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAll, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
                logger.warning("insecure.ssl=true -> trusting all HTTPS certificates (LOCAL TEST ONLY)");
            } catch (Exception e) {
                logger.severe("Failed to enable insecure SSL: " + e.getMessage());
            }
        }

        try {
            QName serviceQName = new QName(namespace, serviceName);
            Service service = Service.create(url, serviceQName);
            T port = service.getPort(portClass);
            logger.info("Created SOAP port for " + portClass.getSimpleName() + " using service QName=" + serviceName);
            return port;
        } catch (Exception e) {
            throw new Exception("Unable to create SOAP port for " + portClass.getSimpleName() + 
                                " with service {" + namespace + "}" + serviceName, e);
        }
    }
}