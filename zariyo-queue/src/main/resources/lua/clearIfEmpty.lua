local size = redis.call('LLEN', KEYS[1])
if size == 0 then
    redis.call('SET', KEYS[2], 0)
    redis.call('SET', KEYS[3], 0)
    return true
end
return false