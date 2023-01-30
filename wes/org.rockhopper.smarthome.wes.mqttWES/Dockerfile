FROM ghcr.io/home-assistant/amd64-base:3.15

# add Azul's public key
RUN wget -P /etc/apk/keys/ https://cdn.azul.com/public_keys/alpine-signing@azul.com-5d5dc44c.rsa.pub 
# add the Azul APK repository
RUN echo "https://repos.azul.com/zulu/alpine" | tee -a /etc/apk/repositories
# update the list of packages
RUN apk update
RUN apk add zulu17-jdk

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
