redis.call('RPUSH', KEYS[1], ARGV[1])
return redis.call('INCRBY', KEYS[2], 1)