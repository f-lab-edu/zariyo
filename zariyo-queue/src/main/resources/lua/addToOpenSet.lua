local threshold = tonumber(redis.call('GET', KEYS[3]) or '500')
local currentCount = tonumber(redis.call('GET', KEYS[2]) or '0')

if currentCount < threshold then
    redis.call('SET', KEYS[1], '1', 'EX', ARGV[1])
    redis.call('INCRBY', KEYS[2], 1)
    return 1
else
    return 0
end