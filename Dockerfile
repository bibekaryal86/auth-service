FROM eclipse-temurin:17-jre-alpine
RUN adduser --system --group springdocker
USER springdocker:springdocker
ARG JAR_FILE=app/build/libs/user-mgmt-sys.jar
COPY ${JAR_FILE} user-mgmt-sys.jar
ENTRYPOINT ["java","-jar", \
#"-DPORT=8080", \
#"-DSPRING_PROFILES_ACTIVE=docker", \
#"-DTZ=America/Denver", \
#"-DVAR1=some_var", \
#"-DVAR2=another_var", \
"/user-mgmt-sys.jar"]
# ENV variables to add in docker-compose.yml
