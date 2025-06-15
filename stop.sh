#!/bin/bash

# Spring Boot Application Stop Script

echo "Stopping Spring Boot application..."

# Find and stop Spring Boot processes
PIDS=$(pgrep -f "com.mathGame.app.SupabaseAuthDemoApplicationKt")

if [ -z "$PIDS" ]; then
    echo "No Spring Boot application found running."
    
    # Check if anything is running on port 8080-8090
    echo "Checking for processes on common Spring Boot ports..."
    for port in {8080..8090}; do
        PID=$(lsof -t -i :$port 2>/dev/null)
        if [ ! -z "$PID" ]; then
            echo "Found process $PID on port $port"
            ps aux | grep $PID | grep -v grep
        fi
    done
else
    echo "Found Spring Boot application(s) with PID(s): $PIDS"
    
    for PID in $PIDS; do
        echo "Attempting graceful shutdown of PID $PID..."
        kill $PID
        
        # Wait up to 10 seconds for graceful shutdown
        for i in {1..10}; do
            if ! kill -0 $PID 2>/dev/null; then
                echo "Process $PID stopped gracefully."
                break
            fi
            echo "Waiting for graceful shutdown... ($i/10)"
            sleep 1
        done
        
        # Force kill if still running
        if kill -0 $PID 2>/dev/null; then
            echo "Process $PID did not stop gracefully, force killing..."
            kill -9 $PID
            sleep 1
            if kill -0 $PID 2>/dev/null; then
                echo "Failed to stop process $PID"
            else
                echo "Process $PID force stopped."
            fi
        fi
    done
fi

echo "Done."

