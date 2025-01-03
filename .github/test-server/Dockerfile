# Use the official OpenJDK image as a base
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /minecraft

# Download the Spigot server jar file
ADD https://download.getbukkit.org/spigot/spigot-1.21.jar spigot.jar
COPY download-plugin.sh download-plugin.sh

# Expose the Minecraft server port
EXPOSE 25565

# Accept the EULA
RUN echo "eula=true" > eula.txt

# Run the plugin download script
RUN chmod +x download-plugin.sh
RUN ./download-plugin.sh

# Start the Spigot server
CMD ["java", "-Xmx1024M", "-Xms1024M", "-jar", "spigot.jar", "nogui"]