package org.example;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class MyRestAPIConnect{

    String ECSUser;
    String ECSPassword;
    String VDC1IP;
    String VDC2IP;
    String VDC1Token;
    String VDC2Token;
    public static void main(String[] args) throws IOException, DocumentException {


        MyRestAPIConnect test = new MyRestAPIConnect(
                "root",
                "P@ssw0rd",
                "10.32.32.223",
                "10.32.32.42");
    }

    public MyRestAPIConnect(){

    }

    public MyRestAPIConnect(String ECSUser, String ECSPassword, String VDC1IP, String VDC2IP) throws IOException {
        this.ECSUser = ECSUser;
        this.ECSPassword = ECSPassword;
        this.VDC1IP = VDC1IP;
        this.VDC2IP = VDC2IP;
        this.VDC1Token = this.getToken(VDC1IP);
        this.VDC2Token = this.getToken(VDC2IP);
    }


    public Element getMyRootElement(String VDC1url, String VDC2url) throws IOException, DocumentException {

        HttpsURLConnection VDC1conn = this.getURLConnection(VDC1url,VDC1Token);
        HttpsURLConnection VDC2conn = this.getURLConnection(VDC2url,VDC2Token);
        SAXReader mySaxReader = new SAXReader();

        if (VDC1conn.getResponseCode() == 200) {
            InputStream myInputStream = VDC1conn.getInputStream();
            Document myDocument = mySaxReader.read(myInputStream);
            Element myRootElement = myDocument.getRootElement();
            return myRootElement;
        }else if (VDC2conn.getResponseCode() == 200){
            InputStream myInputStream = VDC2conn.getInputStream();
            Document myDocument = mySaxReader.read(myInputStream);
            Element myRootElement = myDocument.getRootElement();
            return myRootElement;
        } else {
            throw new RuntimeException("Failed : VDC1 HTTP error code : "
                    + VDC2conn.getResponseCode() +
                    "||| VDC2 HTTP error code:"+ VDC1conn.getResponseCode());
        }
    }

    public HttpsURLConnection getURLConnection(String URL, String myToken) throws IOException {
        URL myUrl = new URL(URL);
        HttpsURLConnection myConn = (HttpsURLConnection) myUrl.openConnection();
        myConn.setRequestMethod("GET");
        myConn.setRequestProperty("Accept", "application/xml");
        myConn.setRequestProperty("X-SDS-AUTH-TOKEN",myToken);
        trustAllHosts(myConn);
        myConn.setHostnameVerifier(DO_NOT_VERIFY);
        return myConn;
    }

    public String getToken(String VDCIP) throws IOException {
        String ECSLoginURL = "Https://" + VDCIP +":4443/login";
        String auth = ECSUser + ":" + ECSPassword;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeaderValue = "Basic " + new String(encodedAuth);

        URL initUrl = new URL(ECSLoginURL);
        // get token
        HttpsURLConnection initConn = (HttpsURLConnection) initUrl.openConnection();
        initConn.setRequestMethod("GET");
        initConn.setRequestProperty("Accept", "application/xml");
        initConn.setRequestProperty("Authorization", authHeaderValue);
        trustAllHosts(initConn);
        initConn.setHostnameVerifier(DO_NOT_VERIFY);

        if (initConn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + initConn.getResponseCode());
        }
        Map<String, List<String>> map = initConn.getHeaderFields();
        String myToken = map.get("X-SDS-AUTH-TOKEN").toString()
                .replaceAll("\\[","")
                .replaceAll("\\]","");
        //System.out.println(myToken);

        return myToken;
    }

    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }};

}
