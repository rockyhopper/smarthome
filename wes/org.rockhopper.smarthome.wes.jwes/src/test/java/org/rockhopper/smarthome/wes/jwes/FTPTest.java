package org.rockhopper.smarthome.wes.jwes;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.rockhopper.smarthome.wes.jwes.DiscoveryTest.GetMacAddressTest;
import org.rockhopper.smarthome.wes.jwes.DiscoveryTest.WesServerDiscoveryServiceTest;
import org.rockhopper.smarthome.wes.jwes.communicator.FtpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class FTPTest extends TestSuite {
    protected static Logger logger = LoggerFactory.getLogger(FTPTest.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FTPTest() {
        super(FTPTest.class.getSimpleName());
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite testSuite = new TestSuite();

        testSuite.addTest(GetMacAddressTest.suite());
        testSuite.addTest(WesServerDiscoveryServiceTest.suite());
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

                ftpClient.changeWorkingDirectory("/OPENHAB");
                ftpClient.deleteFiles(false);
                ftpClient.removeDirectory("/OPENHAB");
                ftpClient.changeWorkingDirectory("/");
                ftpClient.makeDirectory("/OPENHAB");
                ftpClient.changeWorkingDirectory("/OPENHAB");
                ftpClient.uploadClassPathFile("OHABLOW.CGX", "org/openhab/binding/wes/xml/OHABLOW.CGX");
                ftpClient.uploadClassPathFile("OHABHIGH.CGX", "org/openhab/binding/wes/xml/OHABHIGH.CGX");

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
                    if (ftpClient.changeWorkingDirectory("/OPENHAB")) {
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
}
