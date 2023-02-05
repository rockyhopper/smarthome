FROM ghcr.io/home-assistant/amd64-base:3.15

RUN apk update 
RUN apk add openjdk8 maven

ARG wes2mqtt_version=dev-latest-SNAPSHOT

COPY .m2 /root/.m2

RUN mvn -s /root/.m2/settings.xml org.apache.maven.plugins:maven-dependency-plugin:3.5.0:copy -Dmdep.stripVersion=true -Dmdep.stripClassifier=true -Dartifact=org.rockhopper.smarthome:wes2mqtt:$wes2mqtt_version -DoutputDirectory=/

RUN apk del maven && rm /var/cache/apk/*

ENTRYPOINT ["java","-jar","/wes2mqtt.jar"]


