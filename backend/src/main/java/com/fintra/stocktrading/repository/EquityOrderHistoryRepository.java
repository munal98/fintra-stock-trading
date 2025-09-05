package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse;
import com.fintra.stocktrading.model.entity.EquityOrderHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquityOrderHistoryRepository extends JpaRepository<EquityOrderHistory, Long> {

    Page<EquityOrderHistory> findAll(Pageable pageable);

    @Query("""
    select new com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse(
      h.historyId, o.orderId, a.accountId, e.equityId,
      coalesce(c.firstName, ''), coalesce(c.lastName, ''),
      h.orderStatus, h.orderSide, h.orderType,
      coalesce(h.oldOrderQuantity, o.orderQuantity),
      coalesce(h.oldPrice,        o.price),
      h.transactionTime
    )
    from EquityOrderHistory h
      join h.equityOrder o
      join o.account a
      join o.equity e
      left join a.customer c
    where o.orderId = :orderId
    order by h.transactionTime desc
    """)
    List<EquityOrderHistoryResponse> findHistoryViewByOrderId(Integer orderId);


    @Query("""
select new com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse(
  h.historyId, o.orderId, a.accountId, e.equityId,
  coalesce(c.firstName, ''), coalesce(c.lastName, ''),
  h.orderStatus, h.orderSide, h.orderType,
  coalesce(h.oldOrderQuantity, o.orderQuantity),
  coalesce(h.oldPrice,        o.price),
  h.transactionTime
)
from EquityOrderHistory h
  join h.equityOrder o
  join o.account a
  join o.equity e
  left join a.customer c
where (:accountId is null or a.accountId = :accountId)
  and (:equityId  is null or e.equityId  = :equityId)
  and h.transactionTime = (
    select max(h2.transactionTime)
    from EquityOrderHistory h2
    where h2.equityOrder.orderId = o.orderId
  )
order by h.transactionTime desc
""")
    Page<EquityOrderHistoryResponse> findLatestPerOrderView(
            @Param("accountId") Integer accountId,
            @Param("equityId")  Integer equityId,
            Pageable pageable);

    Optional<EquityOrderHistory> findTopByEquityOrder_OrderIdOrderByTransactionTimeDesc(Integer orderId);
}
