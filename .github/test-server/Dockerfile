# Use the official OpenJDK image as a base
FROM openjdk:21-jdk-alpine
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

# Run the plugin download script
RUN chmod +x download-plugin.sh
RUN sh download-plugin.sh

# Start the Spigot server
CMD ["java", "-Xmx1024M", "-Xms1024M", "-jar", "spigot.jar", "nogui"]