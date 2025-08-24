package org.rockhopper.smarthome.wes.wes2mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for monitoring and managing MQTT connection status
 */
@RestController
@RequestMapping("/api/mqtt")
public class MqttStatusController {

    @Autowired
    private MqttWesClient mqttWesClient;

    @Autowired
    private MqttPushClient mqttPushClient;

    /**
     * Get current MQTT connection status
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("connected", mqttPushClient.isConnected());
        status.put("connectionStatus", mqttPushClient.getConnectionStatus());
        status.put("isReconnecting", mqttWesClient.isReconnecting());
        status.put("reconnectionAttempts", mqttWesClient.getReconnectionAttempts());
        status.put("shutdownInProgress", mqttWesClient.isShutdownInProgress());
        
        return status;
    }

    /**
     * Manually trigger MQTT reconnection
     */
    @PostMapping("/reconnect")
    public Map<String, Object> reconnect() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (mqttWesClient.isShutdownInProgress()) {
                result.put("success", false);
                result.put("message", "Cannot reconnect: shutdown in progress");
                return result;
            }
            
            // Reset reconnection state first
            mqttWesClient.resetReconnectionState();
            
            // Attempt reconnection
            mqttPushClient.reconnectMqttPushClient();
            
            result.put("success", true);
            result.put("message", "Reconnection successful");
            result.put("connected", mqttPushClient.isConnected());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Reconnection failed: " + e.getMessage());
            result.put("connected", false);
        }
        
        return result;
    }

    /**
     * Reset reconnection state
     */
    @PostMapping("/reset")
    public Map<String, Object> resetReconnectionState() {
        Map<String, Object> result = new HashMap<>();
        
        mqttWesClient.resetReconnectionState();
        
        result.put("success", true);
        result.put("message", "Reconnection state reset");
        result.put("reconnectionAttempts", mqttWesClient.getReconnectionAttempts());
        result.put("isReconnecting", mqttWesClient.isReconnecting());
        
        return result;
    }

    /**
     * Manually trigger Home Assistant discovery republication
     */
    @PostMapping("/discovery/republish")
    public Map<String, Object> republishDiscovery() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            mqttWesClient.republishHomeAssistantDiscovery();
            
            result.put("success", true);
            result.put("message", "Home Assistant discovery messages successfully republished");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to republish discovery messages: " + e.getMessage());
        }
        
        return result;
    }
}
