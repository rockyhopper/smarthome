FROM ghcr.io/home-assistant/amd64-base:3.15

# add Azul's public key
RUN wget -P /etc/apk/keys/ https://cdn.azul.com/public_keys/alpine-signing@azul.com-5d5dc44c.rsa.pub 
# add the Azul APK repository
RUN echo "https://repos.azul.com/zulu/alpine" | tee -a /etc/apk/repositories
# update the list of packages
RUN apk update 
RUN apk add zulu17-jdk maven

ARG wes2mqtt_version=0.0.1-dev-latest
ARG GITHUB_USERNAME
ARG GITHUB_PASSWORD

COPY .m2 /root/.m2

RUN export GITHUB_USERNAME=$GITHUB_USERNAME & export GITHUB_PASSWORD=$GITHUB_PASSWORD

RUN mvn -s /root/.m2/settings.xml org.apache.maven.plugins:maven-dependency-plugin:3.5.0:copy -Dmdep.stripVersion=true -Dmdep.stripClassifier=true -Dartifact=org.rockhopper.smarthome:wes2mqtt:$wes2mqtt_version -DoutputDirectory=/

RUN unset GITHUB_USERNAME & unset GITHUB_PASSWORD

RUN apk del maven && rm /var/cache/apk/*

ENTRYPOINT ["java","-jar","/wes2mqtt.jar"]


