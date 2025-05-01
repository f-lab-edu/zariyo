for i = 1, #KEYS - 1 do
  redis.call('SET', KEYS[i], '1', 'EX', 10)
end

redis.call('INCRBY', KEYS[#KEYS], ARGV[1])

return 1