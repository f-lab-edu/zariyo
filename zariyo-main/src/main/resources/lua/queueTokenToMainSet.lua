redis.call('SET', KEYS[1], '1', 'EX', ARGV[1])
redis.call('INCRBY', KEYS[2], 1)