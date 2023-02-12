package org.rockhopper.smarthome.wes.jwes.discovery;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.rockhopper.smarthome.wes.jwes.WesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public abstract class WesServerDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(WesServerDiscoveryService.class);

    private Integer tcpPort;
    private Integer httpPort;
    
    // public static final int SEARCH_TIME = 30;
    public static final int PING_TIMEOUT_IN_MS = 100;

    private int scanningNetworkSize = 0;

    private boolean discoveryDone = false;

    private Set<String> discoveryInterfaceIPs= null;
    
    @Nullable
    private ExecutorService executorService = null;

    /**
     * Creates a FreeboxDiscoveryService with background discovery disabled.
     */
    public WesServerDiscoveryService() {

    }

    public void scan() {

        startScan();

        awaitTermination();
    }

    public void startScan() {
        if (executorService != null) {
            interruptScan();

            discoveryDone = false;
        }
        if (tcpPort==null) {
        	tcpPort= WesConstants.DEFAULT_TCP_PORT;
        }
        if (httpPort==null) {
        	httpPort= WesConstants.DEFAULT_HTTP_PORT;
        }

        logger.debug("Starting WES Server discovery scan");
        
        TreeSet<String> interfaceIPs= WesUtils.getInterfaceIPs();
        if ((discoveryInterfaceIPs==null)&&(interfaceIPs.size()>1)) {
        	logger.info("List of available InterfacesIP... {}", String.join(",", interfaceIPs));
        	throw new UnsupportedOperationException("Please set an InterfaceIP name to limit discovery effort!");
        }
        else {
        	interfaceIPs.retainAll(discoveryInterfaceIPs);
        }
        LinkedHashSet<String> networkIPs = WesUtils.getNetworkIPs(interfaceIPs);
        scanningNetworkSize = networkIPs.size();
        logger.debug("Number of IP Addresses to scan {}", scanningNetworkSize);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
        synchronized (this) {
	        for (Iterator<String> it = networkIPs.iterator(); it.hasNext();) {
	            final String ip = it.next();
	            if ((executorService != null) && (!executorService.isShutdown())){
	            	WesPingRunnable wesPing= new WesPingRunnable(ip, tcpPort, httpPort, this);
	                executorService.execute(wesPing);                
	            }
	        }
        }
    }

    public synchronized void awaitTermination() {
        if (executorService != null) {
            synchronized (this) {
                try {
                    executorService.awaitTermination(PING_TIMEOUT_IN_MS * scanningNetworkSize, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                }
                if (executorService != null) {
                    interruptScan();
                }
            }
            logger.debug("Stopping WES Server discovery scan");
        }
    }

    public void interruptScan() {
        if (executorService != null) {
            synchronized (this) {
            	List<Runnable>  tasksAwaitingExecution= executorService.shutdownNow();
                try {
                	executorService.awaitTermination(2, TimeUnit.SECONDS);
        		} 
                catch (InterruptedException ie) {
        		}
				if ((tasksAwaitingExecution!=null)&&(!tasksAwaitingExecution.isEmpty())){
					Iterator<Runnable> tasksAwaitingExecutionIt= tasksAwaitingExecution.iterator();
					while (tasksAwaitingExecutionIt.hasNext()) {
						Runnable runnable= tasksAwaitingExecutionIt.next();
						if (runnable instanceof WesPingRunnable) {
							logger.warn("WesPing for IP#{} has not been executed", ((WesPingRunnable)runnable).getIp());
						}
						else {
							logger.warn("{} has not been executed", runnable);
						}
					}
				}
                executorService = null;
            }
            discoveryDone = true;
        }
    }

    public Boolean isTerminated() {
        return discoveryDone;
    }

    public void setTcpPort(Integer tcpPort) {
		this.tcpPort = tcpPort;
	}
    
    public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}
    
	public void setDiscoveryInterfaceIPs(Set<String> discoveryInterfaceIPs) {
	  this.discoveryInterfaceIPs = discoveryInterfaceIPs;
	}
	
	/**
	 * 
	 * @param discoveryInterfaceIPs Comma separated list of IP interfaces
	 */
	public void setDiscoveryInterfaceIPs(String discoveryInterfaceIPs) {
		if (StringUtils.isBlank(discoveryInterfaceIPs)) {
			return; 
		}
		String[] discoveryInterfaceIPsArray= StringUtils.split(discoveryInterfaceIPs, ",");
		Set<String> discoveryInterfaceIPsToSet= new HashSet<String>();
		for (String discoveryInterfaceIP: discoveryInterfaceIPsArray) {
			String currentDiscoveryInterfaceIP= discoveryInterfaceIP.trim();
			if (!StringUtils.isEmpty(currentDiscoveryInterfaceIP)) {
				discoveryInterfaceIPsToSet.add(currentDiscoveryInterfaceIP);
			}
		}
		if (discoveryInterfaceIPsToSet.size()==0) {
			logger.warn("'discoveryInterfaceIPs' is actually empty!");
		}
		setDiscoveryInterfaceIPs(discoveryInterfaceIPsToSet);
	}
	
    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link startScan}.
     *
     * @param ip The device IP
     * @param macAddress The device MAC Address
     */
    abstract public void newServer(String ip, String macAddress);
}
