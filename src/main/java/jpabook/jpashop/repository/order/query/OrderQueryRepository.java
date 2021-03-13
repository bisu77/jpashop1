package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.repository.order.simplequery.OrderSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(){
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class
        ).getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId",
                OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));//컬렉션 1쿼리 조회 후 Map 형태 반환

        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));//루프 돌면서 컬렉션 세팅

        return result;
    }

    //1) IN쿼리 수행 후,
    //2) Map<OrderItem.Order.Id, List<OrderItemQueryDto>> 형태<key,List> 반환
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItemQueryDto = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id IN :orderIds"
                , OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        return orderItemQueryDto.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }

    //List<OrderItemDto> -> List<OrderItemDto.OrderId>
    private List<Long> toOrderIds(List<OrderQueryDto> orders) {
        return orders.stream()
                    .map(o -> o.getOrderId())
                    .collect(Collectors.toList());
    }
}
