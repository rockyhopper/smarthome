package org.rockhopper.smarthome.wes.jwes;

import java.util.ArrayList;
import java.util.List;

import org.rockhopper.smarthome.wes.jwes.discovery.WesServerDiscoveryService;
import org.rockhopper.smarthome.wes.jwes.discovery.WesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class DiscoveryTest extends TestSuite {
    protected static Logger logger = LoggerFactory.getLogger(DiscoveryTest.class);

    public static String wesDeviceIp = "192.168.0.1";
    public static int wesDeviceIpPort= WesConstants.DEFAULT_TCP_PORT;
    		
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DiscoveryTest() {
        super(DiscoveryTest.class.getSimpleName());
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite testSuite = new TestSuite();

        testSuite.addTest(HttpPingTest.suite());
        testSuite.addTest(GetMacAddressTest.suite());
        testSuite.addTest(WesServerDiscoveryServiceTest.suite());

        return testSuite;
    }

    static public class WesServerDiscoveryServiceTest extends TestCase {

        public WesServerDiscoveryServiceTest() {
            super(WesServerDiscoveryServiceTest.class.getSimpleName());
        }

        public static Test suite() {
            return new TestSuite(WesServerDiscoveryServiceTest.class);
        }

        public void testWesServerDiscoveryService() {
            final List<String> wesServersDetails = new ArrayList<String>(1);

            WesServerDiscoveryService discoveryService = new WesServerDiscoveryService() {
                @Override
                public void newServer(String ip, String macAddress) {
                    logger.info("Found WES with Ip={}, MacAddress={}", ip, macAddress);
                    wesServersDetails.add("ip (" + macAddress + ")");
                }
            };
            discoveryService.scan();

            assertEquals(1, wesServersDetails.size());
        }
    }

    static public class GetMacAddressTest extends TestCase {
        public GetMacAddressTest() {
            super(GetMacAddressTest.class.getSimpleName());
        }

        public static Test suite() {
            return new TestSuite(GetMacAddressTest.class);
        }

        public void testGetMac() {

            String macAddress = WesUtils.getMac(wesDeviceIp, wesDeviceIpPort);
            logger.info("Mac Address is: {}", macAddress);
            assertNotNull(macAddress);
        }
    }

    static public class HttpPingTest extends TestCase {
        public HttpPingTest() {
            super(HttpPingTest.class.getSimpleName());
        }

        public static Test suite() {
            return new TestSuite(HttpPingTest.class);
        }

        public void testHttpPing() {
            logger.info("Pinging {}:{}", wesDeviceIp, wesDeviceIpPort);
            Boolean pingResult= WesUtils.httpPing(wesDeviceIp, wesDeviceIpPort);
            Assert.assertNotNull(pingResult);
            Assert.assertTrue(pingResult);
        }
    }

    /*
     * TOFIX:
     *
     * @Test
     * public void nativePing() {
     * try {
     * Assert.assertTrue(WesUtils.nativePing(wesDeviceIp, 0, WesServerDiscoveryService.PING_TIMEOUT_IN_MS));
     * } catch (IOException e) {
     * Assert.fail(e.getMessage());
     * } catch (InterruptedException e) {
     * Assert.fail(e.getMessage());
     * }
     * }
     */
    /*
     * TOFIX:
     *
     * @Test
     * public void pingCheckVitality() {
     * try {
     * Assert.assertTrue(Ping.checkVitality(wesDeviceIp, 0, WesServerDiscoveryService.PING_TIMEOUT_IN_MS));
     * } catch (SocketTimeoutException e) {
     * Assert.fail(e.getMessage());
     * } catch (IOException e) {
     * Assert.fail(e.getMessage());
     * }
     * }
     */

}
