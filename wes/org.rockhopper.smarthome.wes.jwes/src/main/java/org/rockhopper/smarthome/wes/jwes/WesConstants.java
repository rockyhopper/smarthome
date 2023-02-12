/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.rockhopper.smarthome.wes.jwes;

/**
 * This class defines common constants, which are used across the whole WES library.
 *
 * @author jdupuis - Initial contribution
 */
public class WesConstants {
	
    public static final Integer DEFAULT_HTTP_PORT = 80; // Cannot be modify then that's not a config. parameter!
    public static final Integer DEFAULT_TCP_PORT = 1500;

    public static final String DEFAULT_HTTP_USER = "admin"; //$NON-NLS-1$
    public static final String DEFAULT_HTTP_PASS = "wes"; //$NON-NLS-1$

    public static final String DEFAULT_FTP_USER = "adminftp"; //$NON-NLS-1$
    public static final String DEFAULT_FTP_PASS = "wesftp"; //$NON-NLS-1$
	
/*
    // WES SYSTEM
    public static final String SYSTEM_TAG = "wes";

    //
    // List of all WES components
    //
    public final static String SERVER_TAG = "server";
    // TeleInfo (up to 2)
    public final static String TELEINFO_TAG = "teleinfo";
    // Pulse Counter (up to 4)
    public final static String PULSECOUNTER_TAG = "pulseCounter";
    // Temperature/Humidity (up to 30)
    public final static String ONEWIRE_TEMP_DS18B20_TAG = "DS18B20";
    public final static String ONEWIRE_3RELAYS_TAG = "3relays";
    public final static String ONEWIRE_8RELAYS_TAG = "8relays";

    // List of bridge ("server") parameters names
    public final static String PARAMETER_HOST = "ipAddress";
    public final static String PARAMETER_TCP_PORT = "tcpPort";
    public final static String PARAMETER_HTTP_USER = "httpUser";
    public final static String PARAMETER_HTTP_PASS = "httpPass";
    public final static String PARAMETER_FTP_USER = "ftpUser";
    public final static String PARAMETER_FTP_PASS = "ftpPass";

    // 1-wire parameters names
    public static final String PARAMETER_NUMBER = "number";
    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_ADDRESS = "address";

    // List of all Channel ids
    public final static String CHANNEL_1WIRE_TEMPERATURE = "temperature";
    public final static String CHANNEL_1WIRE_RELAY1 = "relay1";
    public final static String CHANNEL_1WIRE_RELAY2 = "relay2";
    public final static String CHANNEL_1WIRE_RELAY3 = "relay3";
    public final static String CHANNEL_1WIRE_RELAY4 = "relay4";
    public final static String CHANNEL_1WIRE_RELAY5 = "relay5";
    public final static String CHANNEL_1WIRE_RELAY6 = "relay6";
    public final static String CHANNEL_1WIRE_RELAY7 = "relay7";
    public final static String CHANNEL_1WIRE_RELAY8 = "relay8";

    public final static String CHANNEL_CPT1_NBADCO = "CPT1#nbADCO";
    public final static String CHANNEL_CPT1_APPARENTPOWER = "CPT1#apparentPower";
    public final static String CHANNEL_CPT1_BASE = "CPT1#BASE";
*/
}
