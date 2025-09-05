package com.fintra.stocktrading.service.event;

import com.fintra.stocktrading.event.TradeMatchedEvent;
import com.fintra.stocktrading.service.OrderMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeEventListener {

    private final OrderMatchingService orderMatchingService;

    @KafkaListener(
            topics = "${app.kafka.trade-matched-topic:trade-matched-events}",
            containerFactory = "tradeMatchedEventKafkaListenerContainerFactory"
    )
    public void listen(TradeMatchedEvent event) {
        log.info("KAFKA: Trade matched event: {}", event);
        tryMatch(event.getBuyOrderId(), "buy");
        tryMatch(event.getSellOrderId(), "sell");
    }

    private void tryMatch(Integer orderId, String tag) {
        if (orderId == null) { log.debug("Skip {}: null id", tag); return; }
        try {
            orderMatchingService.matchOrder(orderId);
        } catch (Exception ex) {
            log.warn("Match skipped for {}OrderId={}: {}", tag, orderId, ex.getMessage());
        }
    }
}
