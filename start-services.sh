#!/bin/bash
# Launch all 9 microservices as detached java -jar processes
JAVA="C:/Program Files/Java/jdk1.8.0_202/bin/java.exe"
PROJ="C:/Users/liyuq/OneDrive/桌面/新建文件夹/xc/xuecheng-plus-project"
LOGS="C:/docker-data/xc-logs"
JVM="-Xms256m -Xmx512m -Dfile.encoding=UTF-8"

declare -A SVCS=(
  [system-api]="$PROJ/xuecheng-plus-system/xuecheng-plus-system-api/target/xuecheng-plus-system-api-0.0.1-SNAPSHOT.jar"
  [content-api]="$PROJ/xuecheng-plus-content/xuecheng-plus-content-api/target/xuecheng-plus-content-api-0.0.1-SNAPSHOT.jar"
  [media-api]="$PROJ/xuecheng-plus-media/xuecheng-plus-media-api/target/xuecheng-plus-media-api-0.0.1-SNAPSHOT.jar"
  [orders-api]="$PROJ/xuecheng-plus-orders/xuecheng-plus-orders-api/target/xuecheng-plus-orders-api-0.0.1-SNAPSHOT.jar"
  [learning-api]="$PROJ/xuecheng-plus-learning/xuecheng-plus-learning-api/target/xuecheng-plus-learning-api-0.0.1-SNAPSHOT.jar"
  [search]="$PROJ/xuecheng-plus-search/target/xuecheng-plus-search-0.0.1-SNAPSHOT.jar"
  [auth]="$PROJ/xuecheng-plus-auth/target/xuecheng-plus-auth-0.0.1-SNAPSHOT.jar"
  [checkcode]="$PROJ/xuecheng-plus-checkcode/target/xuecheng-plus-checkcode-0.0.1-SNAPSHOT.jar"
  [gateway]="$PROJ/xuecheng-plus-gateway/target/xuecheng-plus-gateway-0.0.1-SNAPSHOT.jar"
)

for name in system-api content-api media-api orders-api learning-api search auth checkcode gateway; do
  jar="${SVCS[$name]}"
  if [ ! -f "$jar" ]; then
    echo "MISSING: $name -> $jar"; continue
  fi
  echo "Starting $name ..."
  nohup "$JAVA" $JVM -jar "$jar" > "$LOGS/$name.log" 2>&1 &
  echo "  pid=$! log=$LOGS/$name.log"
  sleep 1
done
echo "===== all 9 services launched ====="
