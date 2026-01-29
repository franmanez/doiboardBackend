# Etapa de compilación
#FROM gradle:8.10-jdk21 as build
FROM gradle:9.2.1-jdk21 as build

# Copia los archivos de configuración de Gradle y código fuente
COPY build.gradle.kts settings.gradle.kts /home/app/src/
COPY src /home/app/src/src

# Establece el directorio de trabajo
WORKDIR /home/app/src

# Ejecuta Gradle bootJar para generar el archivo jar
RUN gradle bootJar --no-daemon

# Etapa de ejecución
# FROM openjdk:21
FROM ibm-semeru-runtimes:open-17-jdk

# Variables de entorno para la JVM
#ENV JAVA_TOOL_OPTIONS="-XX:+IgnoreUnrecognizedVMOptions -XX:+UseContainerSupport -XX:+IdleTuningCompactOnIdle -XX:+IdleTuningGcOnIdle -XX:MaxRAMPercentage=96"
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:+IdleTuningCompactOnIdle -XX:+IdleTuningGcOnIdle -XX:MaxRAMPercentage=96 -Xms6g -Xmx7g"

# Crea un usuario no root y configura el entorno de trabajo
#RUN adduser appuser
#RUN useradd -m ubuntu
WORKDIR /home/ubuntu

# Cambia el usuario actual a appuser (no root)
USER ubuntu

# Copia el jar desde la etapa de compilación
COPY --from=build /home/app/src/build/libs/doiboardbackend.jar /home/ubuntu/doiboardbackend.jar

EXPOSE 8006

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/home/ubuntu/doiboardbackend.jar"]
