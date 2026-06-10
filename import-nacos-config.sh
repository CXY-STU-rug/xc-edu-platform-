#!/bin/bash
# Import all yaml configs into Nacos under matching groups
NACOS=http://localhost:8848
DIR="C:/Users/liyuq/OneDrive/桌面/学成在线Java后端项目/xc/nacos-config"
ok=0; fail=0
for group_dir in "$DIR"/*/; do
  group=$(basename "$group_dir")
  for f in "$group_dir"*.yaml; do
    [ -f "$f" ] || continue
    dataId=$(basename "$f")
    code=$(curl -s -o /tmp/_nacos_resp -w "%{http_code}" \
      --data-urlencode "dataId=$dataId" \
      --data-urlencode "group=$group" \
      --data-urlencode "tenant=dev402" \
      --data-urlencode "type=yaml" \
      --data-urlencode "content@$f" \
      -X POST "$NACOS/nacos/v1/cs/configs")
    body=$(cat /tmp/_nacos_resp)
    if [ "$code" = "200" ] && [ "$body" = "true" ]; then
      ok=$((ok+1)); echo "OK   [$group] $dataId"
    else
      fail=$((fail+1)); echo "FAIL [$group] $dataId  -> code=$code body=$body"
    fi
  done
done
echo "==== ok=$ok fail=$fail ===="
