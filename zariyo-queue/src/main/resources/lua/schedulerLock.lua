if redis.call('EXISTS', KEYS[1]) == 0 then
    redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
    return 1
else
    return 0
end