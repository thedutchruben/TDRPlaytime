# Use the official OpenJDK image as a base
FROM openjdk:25-jdk-slim
# Install curl and jq
RUN apk add --no-cache curl jq
# Set the working directory
WORKDIR /minecraft

# Download the Spigot server jar file
RUN wget https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/72/downloads/paper-1.21.4-72.jar -O spigot.jar
COPY download-plugin.sh download-plugin.sh

# Expose the Minecraft server port
EXPOSE 25565

# Accept the EULA
RUN echo "eula=true" > eula.txt

RUN mkidr plugins
COPY TDRPlaytime.jar plugins/TDRPlaytime.jar

# Start the Spigot server
CMD ["java", "-Xmx1024M", "-Xms1024M", "-jar", "spigot.jar", "nogui"]