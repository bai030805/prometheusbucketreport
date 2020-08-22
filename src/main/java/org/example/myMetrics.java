package org.example;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

public class myMetrics {

    public static void main(String[] args) throws IOException {

        Gauge bucketTotalsize = Gauge.build().labelNames("nsname/bucketname")
                .name("bucket_totalsize").help("bucket totalsize").register();
        Gauge bucketBlocksize = Gauge.build().labelNames("nsname/bucketname")
                .name("bucket_blocksize").help("bucket blocksize").register();
        Gauge bucketNotificationsize = Gauge.build().labelNames("nsname/bucketname")
                .name("bucket_notificationsize").help("bucket notificationsize").register();

        HTTPServer server = new HTTPServer(1234);

        bucketTotalsize.labels("ns1/bucketName").set(100);
        bucketBlocksize.labels("ns1/bucketName").set(100);
        bucketNotificationsize.labels("ns1/bucketName").set(100);

    }
}
