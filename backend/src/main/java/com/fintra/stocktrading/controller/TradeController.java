package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.TradeControllerDoc;
import com.fintra.stocktrading.model.dto.response.TradeDto;
import com.fintra.stocktrading.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Slf4j
public class TradeController implements TradeControllerDoc {

    private final TradeService tradeService;

    @GetMapping("/{tradeId}")
    @Override
    public ResponseEntity<TradeDto> getTradeById(@PathVariable Integer tradeId) {
        TradeDto response = tradeService.getTradeById(tradeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<TradeDto>> getAllTrades() {
        List<TradeDto> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/order/{orderId}")
    @Override
    public ResponseEntity<List<TradeDto>> getTradesByOrderId(@PathVariable Integer orderId) {
        List<TradeDto> trades = tradeService.getTradesByOrderId(orderId);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/equity/{equityId}")
    @Override
    public ResponseEntity<List<TradeDto>> getTradesByEquityId(@PathVariable Integer equityId) {
        List<TradeDto> trades = tradeService.getTradesByEquityId(equityId);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/equity")
    @Override
    public ResponseEntity<Page<TradeDto>> getTradesByEquityWithDateRange(
            @RequestParam Long equityId,
            @RequestParam(required = false, name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false, name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @ParameterObject Pageable pageable
    ) {
        Page<TradeDto> page = tradeService.getTradesByEquityIdAndDateRange(equityId, from, to, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/settled")
    @Override
    public ResponseEntity<List<TradeDto>> getSettledTrades() {
        log.info("Request to get all settled trades");
        List<TradeDto> settledTrades = tradeService.getSettledTrades();
        log.info("Found {} settled trades", settledTrades.size());
        return ResponseEntity.ok(settledTrades);
    }
}
