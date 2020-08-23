package org.example;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class BucketUtiCollector extends Collector {
    String ECSUser;
    String ECSPassword;
    String VDC1IP;
    String VDC2IP;
    MyRestAPIConnect myConnect;

    public BucketUtiCollector(String ECSUser, String ECSPassword, String VDC1IP, String VDC2IP) throws IOException {
        this.ECSUser = ECSUser;
        this.ECSPassword = ECSPassword;
        this.VDC1IP = VDC1IP;
        this.VDC2IP = VDC2IP;
        this.myConnect = new MyRestAPIConnect(ECSUser,ECSPassword,VDC1IP,VDC2IP);

    }


    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();

        GaugeMetricFamily bucketTotalsize = new GaugeMetricFamily(
                "bucket_totalsize",
                "bucket totalsize",
                Arrays.asList("nsname","bucketname"));
        GaugeMetricFamily bucketBlocksize = new GaugeMetricFamily(
                "bucket_blocksize",
                "bucket blocksize",
                Arrays.asList("nsname","bucketname"));
        GaugeMetricFamily bucketNotificationsize = new GaugeMetricFamily(
                "bucket_notificationsize",
                "bucket notificationsize",
                Arrays.asList("nsname","bucketname"));

        String VDC1nsURL = "https://" + VDC1IP +":4443/object/namespaces";
        String VDC2nsURL = "https://" + VDC2IP +":4443/object/namespaces";
        String VDC1bucketBaseURL = "https://" + VDC1IP + ":4443/object/bucket?namespace=";
        String VDC2bucketBaseURL = "https://" + VDC2IP + ":4443/object/bucket?namespace=";
        String VDC1bucketMeterBaseURL = "https://" + VDC1IP + ":4443/object/billing/buckets/";
        String VDC2bucketMeterBaseURL = "https://" + VDC2IP + ":4443/object/billing/buckets/";

        //MyRestAPIConnect ecsConnect = new MyRestAPIConnect(ECSUser,ECSPassword,ECSVDC1IP,ECSVDC2IP);
        try {
            //get namespace list
            Element nsRoot = myConnect.getMyRootElement(VDC1nsURL,VDC2nsURL);
            List nsList = nsRoot.elements("namespace");
            for (Iterator namespaces = nsList.iterator(); namespaces.hasNext();){
                Element nselm = (Element) namespaces.next();
                String nsid = nselm.element("id").getTextTrim();

                // get all buckets info from specified namespace
                String VDC1bucketURL = VDC1bucketBaseURL + nsid;
                String VDC2bucketURL = VDC2bucketBaseURL + nsid;
                Element bucketRoot = myConnect.getMyRootElement(VDC1bucketURL,VDC2bucketURL);
                List bucketList = bucketRoot.elements("object_bucket");
                for (Iterator buckets = bucketList.iterator(); buckets.hasNext();){
                    Element bucketelm = (Element) buckets.next();
                    String bucketName = bucketelm.element("name").getTextTrim();
                    String blockSize = bucketelm.element("block_size").getTextTrim();
                    String notificationsize = bucketelm.element("notification_size").getTextTrim();

                    //get bucket data size
                    String VDC1bucketMeterURL = VDC1bucketMeterBaseURL + nsid + "/" + bucketName + "/info";
                    String VDC2bucketMeterURL = VDC2bucketMeterBaseURL + nsid + "/" + bucketName + "/info";
                    Element bucketMeterRoot = myConnect.getMyRootElement(VDC1bucketMeterURL,VDC2bucketMeterURL);
                    String bucketTotalSize = bucketMeterRoot.element("total_size").getTextTrim();

                    bucketTotalsize.addMetric(Arrays.asList(nsid,bucketName), Double.parseDouble(bucketTotalSize));
                    bucketBlocksize.addMetric(Arrays.asList(nsid,bucketName), Double.parseDouble(blockSize));
                    bucketNotificationsize.addMetric(Arrays.asList(nsid,bucketName), Double.parseDouble(notificationsize));
                }
            }
        }  catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
        mfs.add(bucketTotalsize);
        mfs.add(bucketBlocksize);
        mfs.add(bucketNotificationsize);
        return mfs;
    }
}