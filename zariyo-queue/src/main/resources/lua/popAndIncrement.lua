local listKey = KEYS[1]
local exitKey = KEYS[2]
local count = tonumber(ARGV[1])

local tokens = {}

for i = 1, count do
    local token = redis.call('LPOP', listKey)
    if token then
        table.insert(tokens, token)
        redis.call('INCR', exitKey)
    else
        break
    end
end

return tokens
