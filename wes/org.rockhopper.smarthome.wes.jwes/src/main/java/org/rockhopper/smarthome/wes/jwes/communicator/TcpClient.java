package org.rockhopper.smarthome.wes.jwes.communicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

public class TcpClient {
    static final public long DEFAULT_SLEEP_TIME = 500L; // In milli-seconds

    private long sleepTime = DEFAULT_SLEEP_TIME;

    private String host;
    private int port;

    private Socket socket;
    PrintWriter socketOut;
    BufferedReader socketIn;
    private ScheduledExecutorService executor;
    private Semaphore tcpPortSemaphore;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;

        tcpPortSemaphore = new Semaphore(1);
        executor = Executors.newScheduledThreadPool(1);
    }

    public String call(String command) {
        String response = null;
        try {
            if (tcpPortSemaphore.tryAcquire(sleepTime, TimeUnit.MILLISECONDS)) {
                if (socket == null) {
                    try {
                        socket = new Socket(host, port);
                        socketOut = new PrintWriter(socket.getOutputStream(), true);
                        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (socket != null) {
                    try {
                        socketOut.println(command);
                        response = socketIn.readLine();
                        if ((response != null) && (!response.isEmpty()) && (command.equals(response))) {
                        	System.out.println("Response (request echoed): " + response);
                        	response = socketIn.readLine();
                        	if ((response != null) && (!response.isEmpty() && (response.startsWith("=")))) {
                            	response = StringUtils.substring(response, 1);
                            }	
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        this.close();
                    }
                }
                tcpPortSemaphore.release();
            } else {
                System.out.println("Fail to acquire socket resource!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void close() {
        executor.shutdownNow();
        try {
			executor.awaitTermination(2, TimeUnit.SECONDS);
		} 
        catch (InterruptedException ie) {
		}

        try {
            tcpPortSemaphore.acquire();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Nothing we can do!
                }
            }
            tcpPortSemaphore.release();
        } catch (InterruptedException ie) {
            // Nothing we can do!
        }
    }
}
