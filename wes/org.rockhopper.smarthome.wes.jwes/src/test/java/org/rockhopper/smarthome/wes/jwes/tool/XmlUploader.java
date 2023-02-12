package org.rockhopper.smarthome.wes.jwes.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.WesConstants;
import org.rockhopper.smarthome.wes.jwes.communicator.FtpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.WesHttpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.FieldConverter;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.WesRelaysCardsConverter;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.XmlHandler;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.XmlProtocol;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCards;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesLabelsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.WstxDriver;

public class XmlUploader {
    protected static Logger logger = LoggerFactory.getLogger(XmlUploader.class);
    
    private String wesDeviceIp;
    
    public static String ftpUser = "adminftp";
    public static String ftpPass = "wesftp";

    public static XmlProtocol xmlProtocol;

    public static void main(String[] args) {
		System.out.println(String.format("Command-line Args[WesDeviceIp]: %s", Arrays.toString(args)));
        new XmlUploader(args[0]).run();
    }

    public XmlUploader(String wesDeviceIp) {
		this.wesDeviceIp= wesDeviceIp;
	}
    
    public void run() {

        try {
            buildXml();

            convertXMLToCGX();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        FtpClient ftpClient = null;
        try {
            ftpClient = new FtpClient(wesDeviceIp);

            boolean logged = ftpClient.login(ftpUser, ftpPass);
            if (logged) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("TEST.CGX"))) {

                    logger.info("FTP login is successful on WES");

                    ftpClient.changeWorkingDirectory("/JWES");
                    ftpClient.uploadFile("TEST.CGX", bis);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftpClient != null) {
                ftpClient.logout();
            }
        }

        WesHttpClient httpClient = new WesHttpClient(wesDeviceIp, WesConstants.DEFAULT_HTTP_PORT);
        httpClient.login(WesConstants.DEFAULT_HTTP_USER, WesConstants.DEFAULT_HTTP_PASS);
        String xmlContent = httpClient.httpGet("/JWES/TEST.CGX");

        // Object wesData = parseXml(xmlContent);
               
        XmlHandler xmlHandler= new XmlHandler(WesData.getInstance(WesSensor.MAX_SENSORS, WesRelaysCards.MAX_RELAYSCARD));
        WesData wesData = (WesData) xmlHandler.deserialize(xmlContent);
        
        ((WesData)wesData).truncate();
        xStream(wesData);
    }

    public void buildXml() throws IOException {
    	
    	WesData wesData= WesData.getInstance(3, 2);
    	
    	WesLabelsHelper.label(wesData);
    	
        xmlProtocol = new XmlProtocol(wesData);

        XStream xstream = new XStream(new WstxDriver());
        xstream.processAnnotations(WesData.class);
        xstream.registerConverter(new WesRelaysCardsConverter(xstream.getMapper(), xmlProtocol, true));
        xstream.registerConverter(new FieldConverter(xmlProtocol, true));

        BufferedOutputStream stdout = new BufferedOutputStream(System.out);
        xstream.marshal(wesData, new PrettyPrintWriter(new OutputStreamWriter(stdout)));

        try {
            Files.delete(Paths.get("TEST.XML"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("TEST.XML", true));
        xstream.marshal(wesData, new PrettyPrintWriter(writer));
        writer.close();
    }

    public Object parseXml(String xml) {
        XStream xstream = new XStream(new WstxDriver());
        xstream.processAnnotations(WesData.class);
        xstream.registerConverter(new WesRelaysCardsConverter(xstream.getMapper(), xmlProtocol, false));
        xstream.registerConverter(new FieldConverter(xmlProtocol, false));

        /*
         * WesData wesData = new WesData();
         * for (int i = 0; i < 3; i++) {
         * wesData.addSensor(new WesSensor((byte) i));
         * }
         * wesData.setRelaysCards(new WesRelaysCards((byte) 2));
         *
         * WesLabels.label(WesLabels.LABEL_DATA_PREFIX, wesData);
         */
        return xstream.fromXML(xml);
    }

    public void xStream(Object object) {
        XStream xstream = new XStream(new WstxDriver());
        xstream.processAnnotations(WesData.class);
        xstream.registerConverter(new WesRelaysCardsConverter(xstream.getMapper(), xmlProtocol, false));
        xstream.registerConverter(new FieldConverter(xmlProtocol, false));

        BufferedOutputStream stdout = new BufferedOutputStream(System.out);
        xstream.marshal(object, new PrettyPrintWriter(new OutputStreamWriter(stdout)));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("OUT.XML", true))) {
            xstream.marshal(object, new PrettyPrintWriter(writer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void convertXMLToCGX() throws IOException {
        String pattern = " command=\\\"([^\\\"]*)\\\""; // The pattern is 'command=\"([^\"]*)\"'

        Pattern p = Pattern.compile(pattern);
        try (PrintWriter pw = new PrintWriter(new FileWriter("TEST.CGX"))) {
            List<String> allLines = Files.readAllLines(Paths.get("TEST.XML"));
            for (String line : allLines) {
                Matcher matcher = p.matcher(line);
                String command = "";
                if (matcher.find()) {
                    line = matcher.replaceFirst("");
                    command = matcher.group(1);
                    if (StringUtils.contains(line, "%xml")) {
                        int idxStart = line.indexOf("<");
                        int idxStop = line.indexOf(">", idxStart);
                        String startTag = line.substring(idxStart + 1, idxStop);
                        int idx2Start = line.indexOf("</");
                        int idx2Stop = line.indexOf(">", idx2Start);
                        String endTag = line.substring(idx2Start + 1, idx2Stop);
                        pw.write("t <" + startTag + ">" + "\n");
                        pw.write("c " + command + "\n");
                        pw.write("t <" + endTag + ">" + "\n");
                    } else {
                        pw.write("c " + command + line + "\n");
                    }
                } else {
                    pw.write("t " + line + "\n");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
