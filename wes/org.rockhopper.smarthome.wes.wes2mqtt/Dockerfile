FROM ghcr.io/home-assistant/amd64-base:3.15

RUN apk update 
RUN apk add openjdk8

RUN echo "export WES2MQTT_VERSION='$(curl https://s01.oss.sonatype.org/content/repositories/snapshots/org/rockhopper/smarthome/wes2mqtt/dev-latest-SNAPSHOT/maven-metadata.xml \
| grep '<value>.*</value>' \
| tr -s " " \
| cut -d ">" -f2 \
| cut -d "<" -f1 \
| sort -V -r \
| head -1)'" >> /envfile

RUN . /envfile; curl -o /wes2mqtt.jar https://s01.oss.sonatype.org/content/repositories/snapshots/org/rockhopper/smarthome/wes2mqtt/dev-latest-SNAPSHOT/wes2mqtt-$WES2MQTT_VERSION.jar

ENTRYPOINT ["java","-jar","/wes2mqtt.jar"]


