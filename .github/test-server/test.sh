#!/bin/bash
set -e

# Start Minecraft server in the background
echo "Starting Minecraft server..."
java -Xms${MEMORY} -Xmx${MEMORY} -jar paper.jar nogui &
SERVER_PID=$!

# Function to check logs for a specific message
check_logs() {
    local pattern=$1
    local timeout=$2
    local start_time=$(date +%s)
    local current_time

    echo "Waiting for pattern: $pattern"

    while true; do
        if grep -q "$pattern" logs/latest.log 2>/dev/null; then
            echo "Found pattern: $pattern"
            return 0
        fi

        current_time=$(date +%s)
        if [ $((current_time - start_time)) -gt "$timeout" ]; then
            echo "Timed out waiting for pattern: $pattern"
            return 1
        fi

        sleep 1
    done
}

# Wait for server to start
if ! check_logs "Done" 120; then
    echo "Server failed to start"
    kill $SERVER_PID
    exit 1
fi

# Check if our plugin loaded
if ! check_logs "TDRPlaytime" 10; then
    echo "Plugin failed to load"
    kill $SERVER_PID
    exit 1
fi

echo "Plugin loaded successfully"

# Run additional tests here
# For example, you can use RCON to execute commands and verify plugin behavior

# For this example, we'll consider the test successful if the plugin loads
echo "Tests completed successfully"

# Clean shutdown
kill $SERVER_PID
wait $SERVER_PID || true

exit 0