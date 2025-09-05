package com.fintra.stocktrading.service.event;

import com.fintra.stocktrading.event.TradeMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeEventPublisher {

    private final KafkaTemplate<String, TradeMatchedEvent> kafkaTemplate;

    @Value("${app.kafka.trade-matched-topic:trade-matched-events}")
    private String tradeMatchedTopic;

    public void publishTradeMatchedEvent(TradeMatchedEvent event) {
        log.info("Sending TradeMatchedEvent to Kafka: {}", event);

        kafkaTemplate.send(tradeMatchedTopic, event)
                .whenComplete((SendResult<String, TradeMatchedEvent> result, Throwable ex) -> {
                    if (ex == null && result != null && result.getRecordMetadata() != null) {
                        var md = result.getRecordMetadata();
                        log.info("Successfully sent to topic='{}' (partition={}, offset={})",
                                tradeMatchedTopic, md.partition(), md.offset());
                    } else {
                        log.error("Failed to send TradeMatchedEvent to Kafka", ex);
                    }
                });
    }
}
