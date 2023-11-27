#!/bin/bash

# Define the array of values
values=("SPIGOT" "HANGAR" "CURSE_FORGE" "MODRITH")
mvn install

# Specify the path to your pom.xml file
pom_file="pom.xml"

# Extract the version using grep and sed
version=$(grep -m 1 -o '<version>[^<]*</version>' "$pom_file" | sed 's/<version>\(.*\)<\/version>/\1/')

# Print the version
echo "Creating Version: $version"

# Loop through the array
for value in "${values[@]}"; do
    #!/bin/bash

    # Replace 'OLD_WORD' with the word you want to replace
    OLD_WORD="GITHUB"

    # Specify the path to the input JAR file
    INPUT_JAR="target/playtime-$version.jar"

    # Specify the path to the output JAR file
    OUTPUT_JAR="target/playtime-$version-$value.jar"

    # Extract the contents of the JAR
    jar xf "$INPUT_JAR" plugin.yml

    # Replace the word in the plugin.yml file
    sed -i "s/$OLD_WORD/$value/g" plugin.yml

    # Package the modified files back into a JAR
    jar cf "$OUTPUT_JAR" plugin.yml

    # Clean up temporary files
    rm plugin.yml

done

