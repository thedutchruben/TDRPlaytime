#!/bin/sh

# Define the array of values
values=("GITHUB" "SPIGOT" "HANGAR")

# Loop through the array
for value in "${values[@]}"; do
    # Replace the previous value in plugin.yml
    sed -i "s/GITHUB/$value/g" ../src/main/resources/plugin.yml

    # Compile the application with mvn install
    mvn install

    # Add the value to the filename
#    mv target/your-application.jar target/your-application_$value.jar

    # Reset plugin.yml to its original state for the next iteration
    git checkout -- ../src/main/resources/plugin.yml
done