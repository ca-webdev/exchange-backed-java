package ca.webdev.exchange.web.rest;

import ca.webdev.exchange.matching.MatchingEngine;
import ca.webdev.exchange.matching.Order;
import ca.webdev.exchange.web.UserOrderAndTradeComponent;
import ca.webdev.exchange.web.model.OrderBlockingInfo;
import ca.webdev.exchange.web.model.OrderCancelRequest;
import ca.webdev.exchange.web.model.OrderCancelResponse;
import ca.webdev.exchange.web.model.OrderInsertRequest;
import ca.webdev.exchange.web.model.OrderInsertResponse;
import ca.webdev.exchange.web.model.OrderUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static ca.webdev.exchange.web.Constants.WEB_USER;

@CrossOrigin
@RestController
public class OrderRestController {

    @Autowired
    private MatchingEngine matchingEngine;

    @Autowired
    private UserOrderAndTradeComponent userOrderAndTradeComponent;

    @PostMapping(value = "/orderinsert")
    public ResponseEntity<OrderInsertResponse> insertOrder(@RequestBody OrderInsertRequest orderInsertRequest) {
        if ("buy".equalsIgnoreCase(orderInsertRequest.getSide())) {
            if (userOrderAndTradeComponent.isCheckBalanceOnOrderInsert()) {
                Optional<String> errorMessage = userOrderAndTradeComponent.validateBuyOrder(orderInsertRequest.getPrice(), orderInsertRequest.getSize());
                if (errorMessage.isPresent()) {
                    return ResponseEntity.ok(new OrderInsertResponse("", errorMessage.get()));
                }

                userOrderAndTradeComponent.freezeBalance(orderInsertRequest.getPrice(), orderInsertRequest.getSize());
            }

            UUID orderId = matchingEngine.insertBuyLimitOrder(WEB_USER, orderInsertRequest.getPrice(), orderInsertRequest.getSize());
            return ResponseEntity.ok(new OrderInsertResponse(orderId.toString(), getInsertMessage(orderInsertRequest)));
        } else {
            if (userOrderAndTradeComponent.isCheckBalanceOnOrderInsert()) {
                Optional<String> errorMessage = userOrderAndTradeComponent.validateSellOrder(orderInsertRequest.getSize());
                if (errorMessage.isPresent()) {
                    return ResponseEntity.ok(new OrderInsertResponse("", errorMessage.get()));
                }

                userOrderAndTradeComponent.freezePosition(orderInsertRequest.getSize());
            }

            UUID orderId = matchingEngine.insertSellLimitOrder(WEB_USER, orderInsertRequest.getPrice(), orderInsertRequest.getSize());
            return ResponseEntity.ok(new OrderInsertResponse(orderId.toString(), getInsertMessage(orderInsertRequest)));
        }
    }

    private static String getInsertMessage(OrderInsertRequest orderInsertRequest) {
        return String.format("%s order (price %.2f and size %d) is accepted.", StringUtils.capitalize(orderInsertRequest.getSide()), orderInsertRequest.getPrice(), orderInsertRequest.getSize());
    }

    @PostMapping(value = "/ordercancel")
    public ResponseEntity<OrderCancelResponse> cancelOder(@RequestBody OrderCancelRequest orderCancelRequest) throws ExecutionException, InterruptedException {
        UUID orderId = UUID.fromString(orderCancelRequest.getOrderId());
        if (userOrderAndTradeComponent.isCheckBalanceOnOrderInsert()) {
            Order order = matchingEngine.lookUpOrder(orderId).get();
            if (order != null) {
                int remainingSize = order.getRemainingSize();
                double orderPrice = order.getOrderPrice();
                if (order.isBuyOrder()) {
                    userOrderAndTradeComponent.unfreezeBalance(orderPrice * remainingSize);
                } else {
                    userOrderAndTradeComponent.unfreezePosition(remainingSize);
                }
            }
        }
        CompletableFuture<String> message = matchingEngine.cancelOrder(orderId);
        return ResponseEntity.ok(new OrderCancelResponse(orderCancelRequest.getOrderId(), message.get()));
    }

    @GetMapping("/orderupdates")
    public Map<UUID, List<OrderUpdate>> getOrderUpdates() {
        return userOrderAndTradeComponent.getCachedOrderUpdates();
    }

    @PostMapping("/orderadmin")
    public ResponseEntity<String> setCheckBalanceOnOrderInsert(boolean isCheckBalanceOnOrderInsert) {
        userOrderAndTradeComponent.setCheckBalanceOnOrderInsert(isCheckBalanceOnOrderInsert);
        return ResponseEntity.ok("isCheckBalanceOnOrderInsert set to " + userOrderAndTradeComponent.isCheckBalanceOnOrderInsert());
    }

    @GetMapping("/orderadmin")
    public ResponseEntity<String> isCheckBalanceOnOrderInsert() {
        return ResponseEntity.ok("isCheckBalanceOnOrderInsert " + userOrderAndTradeComponent.isCheckBalanceOnOrderInsert());
    }

    @GetMapping("/orderblockinginfo")
    public ResponseEntity<OrderBlockingInfo> getOrderBlockingInfo() {
        return ResponseEntity.ok(new OrderBlockingInfo(userOrderAndTradeComponent.getCurrentBalance(), userOrderAndTradeComponent.getFreezingBalance(), userOrderAndTradeComponent.getCurrentPosition(), userOrderAndTradeComponent.getFreezingPosition()));
    }
}
