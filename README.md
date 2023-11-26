## Build and run

require JDK 17+ to build and run.

run by 

```shell
./gradlew bootRun
```

Goto http://localhost:8080/ to see the result.

## APIs

### Websocket (with STOMP)

#### Connection URL:

ws://localhost:8080/exchange-websocket

#### topics:

- Order book updates (both bid and ask order book) in `/topic/orderbookupdates`
- Bid order book updates in `/topic/bidorderbook`
- Ask order book updates in `/topic/askorderbook`
- Recent trade updates in `/topic/recenttrades`
- Latest 1-minute open high low close price in `/topic/ohlc`
- User trade updates in `/topic/usertrades`
- Order updates for the orders by the web user in `/topic/orderupdates`. Order status would include orderId, side, price, and size of the order, and also status, such as InsertAccepted, PartiallyFilled, FullyFilled, Cancelled, etc. Examples of order updates would be:

```json
{
  "orderId": "b407036c-8684-410b-b941-13bfc4665c57",
  "orderUpdateTime": 1700929159,
  "side": "buy",
  "price": 15.0,
  "size": 10,
  "filledPrice": "NaN",
  "filledSize": 0,
  "orderStatus": "InsertAccepted"
}
```

```json
{
  "orderId": "b407036c-8684-410b-b941-13bfc4665c57",
  "orderUpdateTime": 1700929159,
  "side": "buy",
  "price": 15.0,
  "size": 10,
  "filledPrice": 15.0,
  "filledSize": 3,
  "orderStatus": "PartiallyFilled"
}
```

- Position and profit and loss updates in `/topic/positionpnl` and examples as below:

```json
{
  "position": 10,
  "averageEntryPrice": 15.2,
  "marketPrice": 15.53,
  "unrealizedPnL": 3.3,
  "realizedPnL": 2.1,
  "totalPnL": 5.4
}
```

```json
{
  "position": -5,
  "averageEntryPrice": 14.9,
  "marketPrice": 15.56,
  "unrealizedPnL": -3.3,
  "realizedPnL": -10.2,
  "totalPnL": -13.5
}
```

### REST

- GET request to `http://localhost:8080/orderbook` for both the bid and ask order book at the moment
- GET request to `http://localhost:8080/bidorderbook` for both the bid order book at the moment
- GET request to `http://localhost:8080/askorderbook` for both the ask order book at the moment
- GET request to `http://localhost:8080/recenttrades` for all recent trades
- GET request to `http://localhost:8080/ohlc` for the 1-minute open high low close prices
- GET request to `http://localhost:8080/usertrades` for the trades for the web user
- GET request to `http://localhost:8080/positionpnl` for the current position and profit and loss for the web user
- POST request to `http://localhost:8080/orderinsert` with payload as example below for inserting limit order
```json
{
  "side": "buy",
  "price": 15.0,
  "size": 1
}
```

and will get a response like:

```json
{
  "orderId": "b407036c-8684-410b-b941-13bfc4665c57"
}
```

- POST request to `http://localhost:8080/ordercancel` with payload as example below for cancelling a limit order

```json
{
  "orderId": "b407036c-8684-410b-b941-13bfc4665c57"
}
```

If the order is successfully cancelled, the order status will be changed to Cancelled in the websocket topic `/topic/orderupdates`