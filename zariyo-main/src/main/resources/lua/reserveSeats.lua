local reservation_token = ARGV[1]
local ttl_seconds = tonumber(ARGV[2])
local timestamp = tonumber(ARGV[3])
local success = true

for i = 1, #KEYS do
    if redis.call('EXISTS', KEYS[i]) == 1 then
        success = false
        break
    end
end

if success then
    local seat_data = reservation_token .. ':' .. timestamp

    for i = 1, #KEYS do
        -- 좌석 선점 저장
        redis.call('SETEX', KEYS[i], ttl_seconds, seat_data)
        -- TTL 만료 감지를 위한 스케줄 등록
        local expire_time = timestamp + (ttl_seconds * 1000)
        redis.call('ZADD', 'seat:expiry:schedule', expire_time, KEYS[i])
    end
end

return success
