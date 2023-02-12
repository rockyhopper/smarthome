package org.rockhopper.smarthome.wes.jwes.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesUtils {
    private static Logger logger = LoggerFactory.getLogger(WesUtils.class);

    /**
     * Gets every IPv4 Address on each Interface except the loopback
     * The Address format is ip/subnet
     *
     * @return The collected IPv4 Addresses
     */
    public static TreeSet<String> getInterfaceIPs() {
        TreeSet<String> interfaceIPs = new TreeSet<String>();

        try {
            // For each interface ...
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                logger.debug("Found Network Interface: {}", networkInterface.getDisplayName());

                if (networkInterface.getName().startsWith("docker")) {
                    logger.debug("Docker Network Interface are skipped {}", networkInterface.getName());
                } else if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {

                    // .. and for each address ...
                    for (Iterator<InterfaceAddress> it = networkInterface.getInterfaceAddresses().iterator(); it
                            .hasNext();) {

                        // ... get IP and Subnet
                        InterfaceAddress interfaceAddress = it.next();
                        interfaceIPs.add(interfaceAddress.getAddress().getHostAddress() + "/"
                                + interfaceAddress.getNetworkPrefixLength());
                    }
                } else {
                    logger.debug("Skipping Network Interface {}", networkInterface.getName());
                }
            }
        } catch (SocketException e) {
        }

        return interfaceIPs;
    }

    /**
     * Takes the interfaceIPs and fetches every IP which can be assigned on their network
     *
     * @param interfaceIPs The IPs which are assigned to the Network Interfaces
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public static LinkedHashSet<String> getNetworkIPs(TreeSet<String> interfaceIPs) {
        LinkedHashSet<String> networkIPs = new LinkedHashSet<String>();

        for (Iterator<String> it = interfaceIPs.iterator(); it.hasNext();) {
            try {
                // gets every ip which can be assigned on the given network
                SubnetUtils utils = new SubnetUtils(it.next());
                String[] addresses = utils.getInfo().getAllAddresses();
                for (int i = 0; i < addresses.length; i++) {
                    networkIPs.add(addresses[i]);
                }

            } catch (Exception ex) {
            }
        }

        return networkIPs;
    }

    public static boolean nativePing(String hostname, int port, int timeout) throws IOException, InterruptedException {
        Process proc;
        if (SystemUtils.IS_OS_UNIX) {
            proc = new ProcessBuilder("ping", "-t", String.valueOf(timeout / 1000), "-c", "1", hostname).start();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout), "-n", "1", hostname).start();
        } else {
            throw new UnsupportedOperationException("System Ping not supported by the OS!");
        }

        int exitValue = proc.waitFor();
        if (exitValue != 0) {
            throw new IOException("Ping stopped with Error Number: " + exitValue + " on Command :" + "ping"
                    + (SystemUtils.IS_OS_UNIX ? " -t " : " -w ")
                    + (SystemUtils.IS_OS_UNIX ? String.valueOf(timeout / 1000) : String.valueOf(timeout))
                    + (SystemUtils.IS_OS_UNIX ? " -c" : " -n") + " 1 " + hostname);
        }
        return exitValue == 0;
    }

    public static Boolean httpPing(String host, int httpPort) {
        Boolean result = null;
        if (host != null) {
        	InetSocketAddress inetSocketAddress= new InetSocketAddress(host, httpPort);
        	if (inetSocketAddress.isUnresolved()) {
        		throw new RuntimeException(String.format("Unresolved InetSocketAddress %s:%4d", host, httpPort));
        	}        
            try (Socket s = new Socket()) {
	            s.setSoTimeout(2000);
	            s.connect(inetSocketAddress, 2000);
                result = false;
                try (PrintWriter pw = new PrintWriter(s.getOutputStream())) {
                    pw.println("GET / HTTP/1.1");
                    pw.println("Host: " + host);
                    pw.println();
                    pw.flush();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                        String line = br.readLine();
                        if ((line != null) && (line.indexOf("HTTP/1.1 401") == 0)) {
                            logger.trace("HTTP Response {}, line 1:[]", host, line);
                            int i = 1;
                            do {
                                line = br.readLine();
                                i++;
                                if (line != null) {
                                    logger.trace("HTTP Response {}, line {}:[{}]", host, i, line);
                                    if (line.indexOf("Basic realm") > 0 && line.indexOf("serveur WES") > 0) {
                                        result = true;
                                    }
                                }
                            } while ((line != null) && (result == false));
                        } else {
                            logger.debug("No suitable HTTP Response {}, line 1:[{}]", host, line);
                        }
                    }
                }
            } catch (NoRouteToHostException nrthe) {
                logger.trace("No device at this address '{}'", host, nrthe);
            } catch (IOException ioe) {
                logger.trace("Fail to http ping '{}'", host, ioe);
            }
        }
        return result;
    }

    public static String getMac(String host, int tcpPort) {
        String result = null;
        if (host != null) {
        	InetSocketAddress inetSocketAddress= new InetSocketAddress(host, tcpPort);
        	if (inetSocketAddress.isUnresolved()) {
        		throw new RuntimeException(String.format("Unresolved InetSocketAddress %s:%4d", host, tcpPort));
        	}
        	else {
	            try (Socket s = new Socket()) {
	            	s.setSoTimeout(2000);
	            	s.connect(inetSocketAddress, 2000);		            
	                try (PrintWriter pw = new PrintWriter(s.getOutputStream())) {
	                    pw.println("gI000");
	                    pw.flush();
	                    try (BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
	                        String line = br.readLine();
	                        if ((line != null) && (!line.isEmpty())) {
	                        	if ("gI000".equals(line)) {
	                        		System.out.println("Response (request echoed): " + line);
	                        		line = br.readLine();
	                            	if ((line != null) && (!line.isEmpty() && (line.startsWith("=")))) {
	                                	result = StringUtils.substring(line, 1);
	                                }
	                            	else {
	                            		result= line;
	                            	}
	                        	}
	                        	else {
	                        		result= line;
	                        	}
	                        }
	                    }
	                }
	            } catch (NoRouteToHostException nrthe) {
	                logger.warn("No device at this address '{}'", host, nrthe);
	            } catch (IOException ioe) {
	                logger.warn("Fail to getMac '{}'", host, ioe);
	            }
        	}
        }
        return result;
    }
    
    public static boolean isValid_WES_MACAddress(String mac){
    	if (mac==null) {
    		return false;
    	}   	
        String regex = "^([0-9A-F]{12})$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(mac);
        return m.matches();
    }
    
    /*
     * HttpClient client = new HttpClient();
     * GetMethod get = new GetMethod("http://" + ip);
     * try {
     * int status = client.executeMethod(get);
     * if (status == 403) {
     * logger.error(">>>>>>> {}" + get.getStatusLine().getReasonPhrase());
     * }
     * } catch (IOException ioe) {
     * logger.trace("Fail to http ping '{}'", ip, ioe);
     * } finally {
     * get.releaseConnection();
     * }
     * return false;
     *
     */
}
