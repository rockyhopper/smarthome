package org.rockhopper.smarthome.wes.jwes;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.rockhopper.smarthome.wes.jwes.communicator.FtpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.TcpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.WesHttpClient;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.FieldConverter;
import org.rockhopper.smarthome.wes.jwes.communicator.xml.WesRelaysCardsConverter;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.WstxDriver;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CommunicatorTest extends TestSuite {
    protected static Logger logger = LoggerFactory.getLogger(CommunicatorTest.class);

    public CommunicatorTest() {
        super(CommunicatorTest.class.getSimpleName());
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite testSuite = new TestSuite();

        testSuite.addTest(FTPClientTest.suite());
        testSuite.addTest(HttpClientTest.suite());
        testSuite.addTest(TcpClientTest.suite());
        testSuite.addTest(XmlTest.suite());
        return testSuite;
    }

    static public class FTPClientTest extends TestCase {

        public static String wesDeviceIp = "192.168.0.1";
        public static String ftpUser = "adminftp";
        public static String ftpPass = "wesftp";

        private static FtpClient ftpClient;

        public FTPClientTest() {
            super(FTPClientTest.class.getSimpleName());
        }

        @Override
        protected void setUp() throws Exception {
            ftpClient = new FtpClient(wesDeviceIp);
            boolean logged = ftpClient.login(ftpUser, ftpPass);
            if (logged) {
                logger.info("FTP login is successful on WES");

                ftpClient.changeWorkingDirectory("/JWES");
                ftpClient.deleteFiles(false);
                ftpClient.removeDirectory("/JWES");
                ftpClient.changeWorkingDirectory("/");
                ftpClient.makeDirectory("/JWES");
                ftpClient.changeWorkingDirectory("/JWES");
                ftpClient.uploadClassPathFile("LOW.CGX", "org/rockhopper/smarthome/jwes/xml/LOW.CGX");
                ftpClient.uploadClassPathFile("HIGH.CGX", "org/rockhopper/smarthome/jwes/xml/HIGH.CGX");

            } else {
                logger.error("FTP login has failed on WES");
            }
            super.setUp();
        }

        public void listFiles() {
            if (ftpClient != null) {
                try {
                    FTPFile[] ftpFiles = ftpClient.listFiles();
                    if ((ftpFiles != null) && (ftpFiles.length > 0)) {
                        for (FTPFile ftpFile : ftpFiles) {
                            if (ftpFile != null) {
                                logger.info("Found file on WES: [{}]", ftpFile.getName());
                            } else {
                                Assert.fail("*NULL* FTPFile returned by #listFiles(\"/\")");
                            }
                        }
                    } else {
                        Assert.fail("No file found on WES!");
                    }
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                Assert.fail("No FTP Client!");
            }
        }

        public void testListFilesRoot() {
            logger.info(">listFilesRoot()");
            if (ftpClient != null) {
                try {
                    if (ftpClient.changeWorkingDirectory("/")) {
                        listFiles();
                    } else {
                        logger.warn("Fail to change working directory!");
                    }

                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                Assert.fail("No FTP Client!");
            }
            logger.info("<listFilesRoot()");
        }

        public void testListFilesOpenHab() {
            logger.info(">listFilesRoot()");
            if (ftpClient != null) {
                try {
                    if (ftpClient.changeWorkingDirectory("/JWES")) {
                        listFiles();
                    } else {
                        logger.warn("Fail to change working directory!");
                    }

                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                Assert.fail("No FTP Client!");
            }
            logger.info("<listFilesRoot()");
        }

        @Override
        protected void tearDown() throws Exception {
            if (ftpClient != null) {
                ftpClient.logout();
            }

            super.tearDown();
        }

        public static Test suite() {
            return new TestSuite(FTPClientTest.class);
        }

    }

    static public class HttpClientTest extends TestCase {

        public static String wesDeviceIp = "192.168.0.1";

        private static WesHttpClient httpClient;

        public HttpClientTest() {
            super(HttpClientTest.class.getSimpleName());
        }

        @Override
        protected void setUp() throws Exception {
            httpClient = new WesHttpClient(wesDeviceIp, WesConstants.DEFAULT_HTTP_PORT);
            httpClient.login(WesConstants.DEFAULT_HTTP_USER, WesConstants.DEFAULT_HTTP_PASS);
            super.setUp();
        }

        public void testGetHIGH() {
            logger.info(">getHIGH()");
            if (httpClient != null) {
                String xmlContent = httpClient.httpGet("/JWES/HIGH.CGX");

                XStream xstream = new XStream(new WstxDriver());
                xstream.processAnnotations(WesData.class);
                xstream.registerConverter(new WesRelaysCardsConverter(xstream.getMapper(), null, false));
                xstream.registerConverter(new FieldConverter(null, false));
                xstream.fromXML(xmlContent);

            } else {
                Assert.fail("No HTTP Client!");
            }
            logger.info("<getHIGH()");
        }

        @Override
        protected void tearDown() throws Exception {
            if (httpClient != null) {
                httpClient.close();
            }
            super.tearDown();
        }

        public static Test suite() {
            return new TestSuite(HttpClientTest.class);
        }

    }

    static public class TcpClientTest extends TestCase {

        public static String wesDeviceIp = "192.168.0.1";
        public static int port = WesConstants.DEFAULT_TCP_PORT;

        private static TcpClient tcpClient;

        public TcpClientTest() {
            super(TcpClientTest.class.getSimpleName());
        }

        @Override
        protected void setUp() throws Exception {
            super.setUp();

            tcpClient = new TcpClient(wesDeviceIp, port);

        }

        public void testRun() {
            logger.info(">run()");
            if (tcpClient != null) {
                tcpClient.call("gW0001");

                try {
                    Thread.sleep(30 * 1000); // 30 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                tcpClient.close();
            } else {
                Assert.fail("No TCP Client!");
            }
            logger.info("<run()");
        }

        @Override
        protected void tearDown() throws Exception {
            if (tcpClient != null) {
                tcpClient.close();
            }
            super.tearDown();
        }

        public static Test suite() {
            return new TestSuite(TcpClientTest.class);
        }

    }

    static public class XmlTest extends TestCase {

        public XmlTest() {
            super(XmlTest.class.getSimpleName());
        }

        @Override
        protected void setUp() throws Exception {
            // WesXPathParser xmlParser = new WesXPathParser();
            // xmlParser.parse(xmlContent);
            super.setUp();
        }

        public void testParse1Wire() {
            logger.info(">parse1Wire()");

            logger.info("<parse1Wire()");
        }

        @Override
        protected void tearDown() throws Exception {

            super.tearDown();
        }

        public static Test suite() {
            return new TestSuite(XmlTest.class);
        }

    }
}
