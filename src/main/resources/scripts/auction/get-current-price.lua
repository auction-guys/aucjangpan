local value_count = redis.call("ZCARD", KEYS[1])

if value_count == 0 then
    return 0
end

local current_price = redis.call("ZREVRANGE", KEYS[1], 0, 0, "WITHSCORES")
local score = current_price[2]

return score