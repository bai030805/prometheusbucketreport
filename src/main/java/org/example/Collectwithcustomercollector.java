package org.example;

import io.prometheus.client.exporter.HTTPServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Properties;

public class Collectwithcustomercollector {

    public static void main(String[] args) throws IOException {

        Properties prop=new Properties();
        String rootPath = System.getProperty("user.dir").replace("\\", "/");
        FileInputStream configfile = new FileInputStream(rootPath+"/connconfig.properties");
        prop.load(configfile);

        String ECSUser = prop.getProperty("ECSUser");
        String ECSPassword = prop.getProperty("ECSPassword");
        String ECSVDC1IP = prop.getProperty("ECSVDC1IP");
        String ECSVDC2IP = prop.getProperty("ECSVDC2IP");

        HTTPServer server = new HTTPServer(1234);
        new MyCustomCollector(ECSUser,ECSPassword,ECSVDC1IP,ECSVDC2IP).register();
    }

}
