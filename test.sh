#!/bin/sh
clear

waitForAppToStart() {
    isAlive=false

    status=`curl "http://localhost:8081/actuator/health" -s -X GET`
#    echo $status

    if [[ "$status" = "{\"status\":\"UP\"}" ]]; then
      isAlive=true
    fi

    while [[ "$isAlive" == false ]]; do
        waitForAppToStart
    done
}

invokeEndpoint() {
    # The maximum number of concurrent requests is bounded by the Tomcat's container
    # max threads, and Hystrix's max concurrent requests (depends you use thread or semaphore)
    for i in {1..1000}
    do
      echo "Looping ... number $i"
      curl -i -X POST "http://localhost:8081/rx/demoEntity" -H "Content-Type: application/json" -d "{\"data\":\"a$i\"}" &
      sleep 0.001 # delay before firing next request
    done

    # fire 200 curl requests in batch with batch size equals 10
#    seq 1 200 | xargs -n1 -P10 curl -i -X POST "http://localhost:8081/rx/demoEntity" &
}

shutdownApp() {
    # Shutdown via actuator endpoint
    # curl "http://localhost:8080/actuator/shutdown" -X POST

    # Shutdown via kill command
    pids=`ps -ef | grep java | awk '{print $2}'`
    for pid in $pids
    do
         echo "java process: $pid"
         kill $pid
    done
}


clear
#./mvnw spring-boot:run &

#echo "waiting app to start ..."
#waitForAppToStart

for i in {1..100}
do
  invokeEndpoint
  sleep 0.010
done
sleep 5

# check any outstanding curl request:
`ps -ef | grep curl | awk '{print $2}'`

#shutdownApp