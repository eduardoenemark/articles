#!/bin/bash

# Log file name with timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="parallel_requests_${TIMESTAMP}.log"

# Clear log file or create it
> "$LOG_FILE"

echo "Starting 500 requests (30 at a time)..." | tee -a "$LOG_FILE"
echo "Log file: $LOG_FILE" | tee -a "$LOG_FILE"
echo "===============================" | tee -a "$LOG_FILE"

# Function to execute a single request and log it
execute_request() {
    local request_type=$1
    local iteration=$2

    TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")

    if [ "$request_type" = "GET" ]; then
        echo "[$TIMESTAMP] [$iteration] GET http://localhost:8080/products/count" | tee -a "$LOG_FILE"
        RESPONSE=$(curl -s -w "%{http_code}" -X 'GET' 'http://localhost:8080/products/count' 2>/dev/null)
        STATUS_CODE=${RESPONSE: -3}
        OUTPUT=${RESPONSE%???}

        echo "Status Code: $STATUS_CODE" | tee -a "$LOG_FILE"
        if [ -n "$OUTPUT" ]; then
            echo "Response Body:" | tee -a "$LOG_FILE"
            echo "$OUTPUT" | tee -a "$LOG_FILE"
        fi
        echo "[$TIMESTAMP] [GET completed with status $STATUS_CODE]" | tee -a "$LOG_FILE"
    else
        echo "[$TIMESTAMP] [$iteration] POST http://localhost:8080/product/generate-and-save" | tee -a "$LOG_FILE"
        RESPONSE=$(curl -s -w "%{http_code}" -X 'POST' 'http://localhost:8080/product/generate-and-save' -d '' 2>/dev/null)
        STATUS_CODE=${RESPONSE: -3}
        OUTPUT=${RESPONSE%???}

        echo "Status Code: $STATUS_CODE" | tee -a "$LOG_FILE"
        if [ -n "$OUTPUT" ]; then
            echo "Response Body:" | tee -a "$LOG_FILE"
            echo "$OUTPUT" | tee -a "$LOG_FILE"
        fi
        echo "[$TIMESTAMP] [POST completed with status $STATUS_CODE]" | tee -a "$LOG_FILE"
    fi
}

# Main execution loop
for ((i=1; i<=500; i++)); do
    # Determine if this is a GET or POST request (alternating)
    if ((i % 100 == 0)); then
        REQUEST_TYPE="GET"
    else
        REQUEST_TYPE="POST"
    fi

    # Execute the request in background
    execute_request "$REQUEST_TYPE" "$i" &

    # Wait for every 30th request to complete before starting next batch
    if ((i % 30 == 0)); then
        #wait  # Wait for all background processes to finish
        sleep 2s
        echo "Completed batch $((i/30)) of 30 requests" | tee -a "$LOG_FILE"
    fi
done

# Wait for any remaining background processes
wait

echo "===============================" | tee -a "$LOG_FILE"
echo "Completed 500 requests (30 at a time)" | tee -a "$LOG_FILE"
echo "Log file saved to: $LOG_FILE" | tee -a "$LOG_FILE"