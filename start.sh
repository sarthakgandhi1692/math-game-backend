#!/bin/bash

# Spring Boot Application Startup Script with Port Conflict Detection

# Default port
PORT=${SERVER_PORT:-8080}

# Function to check if port is in use
check_port() {
    if lsof -i :$PORT > /dev/null 2>&1; then
        echo "Port $PORT is already in use."
        echo "Checking what's running on port $PORT:"
        lsof -i :$PORT
        echo ""
        read -p "Do you want to kill the process on port $PORT? (y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            # Get the PID and kill it
            PID=$(lsof -t -i :$PORT)
            if [ ! -z "$PID" ]; then
                echo "Killing process $PID on port $PORT..."
                kill $PID
                sleep 2
                if lsof -i :$PORT > /dev/null 2>&1; then
                    echo "Process still running, force killing..."
                    kill -9 $PID
                    sleep 1
                fi
                echo "Port $PORT is now free."
            fi
        else
            # Find an alternative port
            echo "Finding alternative port..."
            for ((port=8081; port<=8090; port++)); do
                if ! lsof -i :$port > /dev/null 2>&1; then
                    echo "Using alternative port: $port"
                    export SERVER_PORT=$port
                    break
                fi
            done
        fi
    else
        echo "Port $PORT is available."
    fi
}

# Function to start the application
start_app() {
    echo "Starting Spring Boot application..."
    echo "Port: ${SERVER_PORT:-8080}"
    echo "Use Ctrl+C to stop the application gracefully."
    echo ""
    ./gradlew bootRun
}

# Main execution
echo "Spring Boot Application Launcher"
echo "================================"
check_port
start_app

