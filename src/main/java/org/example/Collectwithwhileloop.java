package org.example;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import org.dom4j.DocumentException;
import org.dom4j.Element;

public class Collectwithwhileloop {

    public static void main(String[] args) throws IOException, InterruptedException {

        Properties prop=new Properties();
        String rootPath = System.getProperty("user.dir").replace("\\", "/");
        FileInputStream configfile = new FileInputStream(rootPath+"/connconfig.properties");
        prop.load(configfile);

        String ECSUser = prop.getProperty("ECSUser");
        String ECSPassword = prop.getProperty("ECSPassword");
        String ECSVDC1IP = prop.getProperty("ECSVDC1IP");
        String ECSVDC2IP = prop.getProperty("ECSVDC2IP");

        String VDC1nsURL = "https://" + ECSVDC1IP +":4443/object/namespaces";
        String VDC2nsURL = "https://" + ECSVDC2IP +":4443/object/namespaces";
        String VDC1bucketBaseURL = "https://" + ECSVDC1IP + ":4443/object/bucket?namespace=";
        String VDC2bucketBaseURL = "https://" + ECSVDC2IP + ":4443/object/bucket?namespace=";
        String VDC1bucketMeterBaseURL = "https://" + ECSVDC1IP + ":4443/object/billing/buckets/";
        String VDC2bucketMeterBaseURL = "https://" + ECSVDC2IP + ":4443/object/billing/buckets/";

        MyRestAPIConnect ecsConnect = new MyRestAPIConnect(ECSUser,ECSPassword,ECSVDC1IP,ECSVDC2IP);
        Gauge bucketTotalsize =  Gauge.build().labelNames("nsname","bucketname")
                .name("bucket_totalsize").help("bucket totalsize").register();
        Gauge bucketBlocksize = Gauge.build().labelNames("nsname","bucketname")
                .name("bucket_blocksize").help("bucket blocksize").register();
        Gauge bucketNotificationsize = Gauge.build().labelNames("nsname","bucketname")
                .name("bucket_notificationsize").help("bucket notificationsize").register();

        HTTPServer server = new HTTPServer(1234);

        while(true){
            try {
                //get namespace list
                Element nsRoot = ecsConnect.getMyRootElement(VDC1nsURL,VDC2nsURL);
                List nsList = nsRoot.elements("namespace");
                for (Iterator namespaces = nsList.iterator(); namespaces.hasNext();){
                    Element nselm = (Element) namespaces.next();
                    String nsid = nselm.element("id").getTextTrim();

                    // get all buckets info from specified namespace
                    String VDC1bucketURL = VDC1bucketBaseURL + nsid;
                    String VDC2bucketURL = VDC2bucketBaseURL + nsid;
                    Element bucketRoot = ecsConnect.getMyRootElement(VDC1bucketURL,VDC2bucketURL);
                    List bucketList = bucketRoot.elements("object_bucket");
                    for (Iterator buckets = bucketList.iterator(); buckets.hasNext();){
                        Element bucketelm = (Element) buckets.next();
                        String bucketName = bucketelm.element("name").getTextTrim();
                        String blockSize = bucketelm.element("block_size").getTextTrim();
                        String notificationsize = bucketelm.element("notification_size").getTextTrim();

                        //get bucket data size
                        String VDC1bucketMeterURL = VDC1bucketMeterBaseURL + nsid + "/" + bucketName + "/info";
                        String VDC2bucketMeterURL = VDC2bucketMeterBaseURL + nsid + "/" + bucketName + "/info";
                        Element bucketMeterRoot = ecsConnect.getMyRootElement(VDC1bucketMeterURL,VDC2bucketMeterURL);
                        String bucketTotalSize = bucketMeterRoot.element("total_size").getTextTrim();

                        bucketTotalsize.labels(nsid,bucketName).set(Double.parseDouble(bucketTotalSize));
                        bucketBlocksize.labels(nsid,bucketName).set(Double.parseDouble(blockSize));
                        bucketNotificationsize.labels(nsid,bucketName).set(Double.parseDouble(notificationsize));
                    }
                }

            }  catch (IOException | DocumentException e) {
                e.printStackTrace();
            }

            Thread.sleep(10*60*1000);
        }

    }
}