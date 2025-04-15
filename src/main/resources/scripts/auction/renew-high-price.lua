-- arguments
local bidder_id = ARGV[1]
local bid_price = tonumber(ARGV[2])
local timestamp = ARGV[3]
local retain_rank_size = tonumber(ARGV[4])

-- check if bid_price already exists in Redis
local existing = redis.call("ZRANGEBYSCORE", KEYS[1], bid_price, bid_price)
if next(existing) == nil then
    local bidder_with_timestamp = bidder_id .. "_" .. timestamp
    redis.call("ZADD", KEYS[1], bid_price, bidder_with_timestamp)
end

local value_count = redis.call("ZCARD", KEYS[1])
if value_count > retain_rank_size then
    local remove_count = value_count - retain_rank_size
    redis.call("ZREMRANGEBYRANK", KEYS[1], 0, remove_count - 1)
end


