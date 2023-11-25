package ca.webdev.exchange.web.rest;

import ca.webdev.exchange.OpenHighLowCloseComponent;
import ca.webdev.exchange.matching.MatchingEngine;
import ca.webdev.exchange.web.model.OpenHighLowClose;
import ca.webdev.exchange.web.model.RecentTrade;
import ca.webdev.exchange.web.model.UserTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static ca.webdev.exchange.web.Constants.WEB_USER;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class TradesRestController {

    private final List<RecentTrade> recentTrades = new LinkedList<>();

    private final List<UserTrade> userTrades = new LinkedList<>();

    @Autowired
    private OpenHighLowCloseComponent openHighLowCloseComponent;

    public TradesRestController(MatchingEngine matchingEngine) {
        matchingEngine.registerMarketTradeListener((tradeId, tradeTimeInEpochMillis, price, size, buyer, seller, isTakerSideBuy) -> {
            recentTrades.add(new RecentTrade(tradeTimeInEpochMillis / 1000, price, size, isTakerSideBuy ? "B" : "S"));
        });
        matchingEngine.registerTradeListener(WEB_USER, (tradeTradeInEpochMillis, isBuyOrder, price, size, buyer, seller) -> {
            userTrades.add(new UserTrade(tradeTradeInEpochMillis / 1000, isBuyOrder ? "buy" : "sell", price, size));
        });
    }

    @GetMapping("/recenttrades")
    public List<RecentTrade> recentTrades() {
        return recentTrades.subList(Math.max(recentTrades.size() - 500, 0), recentTrades.size());
    }

    @GetMapping("/ohlc")
    public Collection<OpenHighLowClose> ohlc() {
        return openHighLowCloseComponent.getOneMinuteOhlcMap().values();
    }

    @GetMapping("/usertrades")
    public List<UserTrade> userTrades() {
        return userTrades;
    }
}
