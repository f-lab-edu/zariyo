-- 토큰을 리스트에 추가
redis.call('RPUSH', KEYS[1], ARGV[1])
-- 푸시 카운트 +1
return redis.call('INCRBY', KEYS[2], 1)