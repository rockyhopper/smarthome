# MQTT Reconnection Improvements for wes2mqtt

## Overview

This implementation provides a robust MQTT reconnection mechanism for the wes2mqtt project, making it more resilient when disconnected from the MQTT broker.

## Key Features

### 1. Exponential Backoff Reconnection
- **Initial delay**: 1 second
- **Maximum delay**: 60 seconds
- **Backoff multiplier**: 2.0
- **Maximum attempts**: 10

### 2. Automatic Recovery
- Automatically detects connection loss
- Schedules reconnection attempts with increasing delays
- Resets counters upon successful reconnection
- **Re-publishes Home Assistant discovery messages** after successful reconnection
- Restarts WES polling if needed after reconnection

### 3. Improved Error Handling
- Proper exception propagation from MQTT operations
- Better logging with different log levels
- Graceful handling of shutdown scenarios

### 4. Connection Monitoring
- Real-time connection status checking
- Reconnection attempt tracking
- Management API endpoints for monitoring and control

## Implementation Details

### MqttWesClient Improvements

#### New Fields
```java
private final ScheduledExecutorService reconnectExecutor;
private ScheduledFuture<?> reconnectTask;
private final AtomicInteger reconnectAttempts;
private final AtomicBoolean isReconnecting;
```

#### Key Methods
- `scheduleReconnect()`: Schedules reconnection with exponential backoff
- `attemptReconnect()`: Performs actual reconnection attempt
- `resetReconnectionState()`: Resets reconnection counters
- `republishHomeAssistantDiscovery()`: Re-publishes HA discovery messages
- `isReconnecting()`: Returns current reconnection status
- `getReconnectionAttempts()`: Returns current attempt count

### MqttPushClient Improvements

#### Enhanced Methods
- `connect()`: Better error handling and connection validation
- `publishToSubTopic()`: Throws exceptions instead of silently failing
- `reconnectMqttPushClient()`: Improved cleanup and validation
- `isConnected()`: Connection status checking
- `getConnectionStatus()`: Human-readable status

## API Endpoints

### GET /api/mqtt/status
Returns current MQTT connection status:
```json
{
  "connected": true,
  "connectionStatus": "Connected",
  "isReconnecting": false,
  "reconnectionAttempts": 0,
  "shutdownInProgress": false
}
```

### POST /api/mqtt/reconnect
Manually triggers MQTT reconnection:
```json
{
  "success": true,
  "message": "Reconnection successful",
  "connected": true
}
```

### POST /api/mqtt/reset
Resets reconnection state:
```json
{
  "success": true,
  "message": "Reconnection state reset",
  "reconnectionAttempts": 0,
  "isReconnecting": false
}
```

### POST /api/mqtt/discovery/republish
Manually triggers Home Assistant discovery republication:
```json
{
  "success": true,
  "message": "Home Assistant discovery messages successfully republished"
}
```

## Configuration

### Reconnection Parameters
Located in `MqttWesClient`:
```java
private static final int MAX_RECONNECT_ATTEMPTS = 10;
private static final long INITIAL_RECONNECT_DELAY_MS = 1000; // 1 second
private static final long MAX_RECONNECT_DELAY_MS = 60000; // 1 minute
private static final double BACKOFF_MULTIPLIER = 2.0;
```

These can be made configurable by moving them to `MqttConfig` if needed.

## Usage Examples

### Monitoring Connection Status
```bash
curl http://localhost:8080/api/mqtt/status
```

### Manual Reconnection
```bash
curl -X POST http://localhost:8080/api/mqtt/reconnect
```

### Reset Reconnection State
```bash
curl -X POST http://localhost:8080/api/mqtt/reset
```

### Republish Home Assistant Discovery
```bash
curl -X POST http://localhost:8080/api/mqtt/discovery/republish
```

## Logging

The implementation uses different log levels for better monitoring:
- **INFO**: Connection events, successful operations
- **WARN**: Reconnection failures, minor issues
- **ERROR**: Critical failures, exceeded retry limits
- **DEBUG**: Detailed connection parameters (when debug logging is enabled)

## Thread Safety

All reconnection logic is thread-safe using:
- `AtomicInteger` for reconnection attempts
- `AtomicBoolean` for reconnection state
- `ScheduledExecutorService` for delayed execution
- Proper synchronization in critical sections

## Error Scenarios Handled

1. **Network connectivity loss**: Automatic reconnection with backoff
2. **MQTT broker restart**: Reconnection with subscription restoration and HA discovery republication
3. **Authentication failures**: Proper error reporting and retry logic
4. **Application shutdown**: Graceful cleanup of reconnection threads
5. **WES server connection issues**: Automatic restart of polling after MQTT reconnection
6. **Home Assistant discovery loss**: Automatic republication of discovery messages after reconnection

## Best Practices Implemented

1. **Exponential backoff**: Reduces load on broker during outages
2. **Maximum retry limits**: Prevents infinite reconnection loops
3. **Proper resource cleanup**: Prevents memory/thread leaks
4. **Exception propagation**: Enables proper error handling upstream
5. **Monitoring endpoints**: Facilitates operational monitoring
6. **Thread naming**: Easier debugging and monitoring

## Migration Notes

### Breaking Changes
- `publishToSubTopic()` now throws `MqttException` instead of silently failing
- `reconnectMqttPushClient()` now throws `MqttException`

### Recommended Actions
1. Update error handling in calling code to catch `MqttException`
2. Monitor the new API endpoints for connection health
3. Consider adjusting reconnection parameters based on your environment
4. Review logs for new error patterns during deployment

## Testing

### Simulating Connection Loss
```bash
# Block MQTT broker port
sudo iptables -A OUTPUT -p tcp --dport 1883 -j DROP

# Monitor reconnection behavior
curl http://localhost:8080/api/mqtt/status

# Restore connection
sudo iptables -D OUTPUT -p tcp --dport 1883 -j DROP
```

### Load Testing
- Monitor reconnection behavior under high message loads
- Verify proper cleanup during rapid connect/disconnect cycles
- Test maximum retry scenarios

## Future Enhancements

1. **Configurable parameters**: Move hardcoded values to configuration
2. **Metrics collection**: Add Micrometer metrics for monitoring
3. **Circuit breaker**: Implement circuit breaker pattern for broker failures
4. **Health checks**: Spring Boot Actuator health indicators
5. **Notification system**: Alert on prolonged disconnections
