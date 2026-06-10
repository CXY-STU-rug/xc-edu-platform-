-- 秒杀原子脚本：库存校验 + 防重复购买 + 扣减，三步在 Redis 单线程内一次完成
-- KEYS[1] = 库存 key   (seckill:stock:{activityId})
-- KEYS[2] = 已购用户集合 (seckill:users:{activityId})
-- ARGV[1] = userId
-- 返回: 1=抢购成功  0=库存不足  -1=已购买过  -2=活动未预热

-- 活动没预热（库存 key 不存在）直接拒绝，防止 decr 出负数幽灵库存
if redis.call('exists', KEYS[1]) == 0 then
    return -2
end
-- 同一用户只允许抢一次
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -1
end
-- 库存不足
if tonumber(redis.call('get', KEYS[1])) <= 0 then
    return 0
end
-- 扣库存 + 记录用户，两步同在脚本内，天然原子
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])
return 1
