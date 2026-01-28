# Etapa de compilación
#FROM gradle:8.10-jdk21 as build
FROM gradle:8.10-jdk17 as build

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

# Crear directorio y asignarle permisos a appuser
RUN mkdir -p /home/ubuntu/jsonfiles && chown -R ubuntu:ubuntu /home/ubuntu/jsonfiles && chmod -R 755 /home/ubuntu/jsonfiles

# Cambia el usuario actual a appuser (no root)
USER ubuntu

# Copia el jar desde la etapa de compilación
COPY --from=build /home/app/src/build/libs/aprenBackend.jar /home/ubuntu/aprenBackend.jar
#COPY keystore /home/appuser/keystore

EXPOSE 8004

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/home/ubuntu/aprenBackend.jar"]
