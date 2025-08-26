FROM ghcr.io/home-assistant/amd64-base:3.22

ARG WES2MQTT_VERSION

RUN apk update 
RUN apk add openjdk21-jre

RUN curl -o /wes2mqtt.jar https://repo1.maven.org/maven2/org/rockhopper/smarthome/wes2mqtt/$WES2MQTT_VERSION/wes2mqtt-$WES2MQTT_VERSION.jar

ENTRYPOINT ["java","-jar","/wes2mqtt.jar"]


