package com.iotabuild.zippyhand_Admin.order

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.exception.OrderNotFoundException
import com.iotabuild.zippyhand_Admin.payment.generateOrderId
import org.hibernate.validator.constraints.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun createOrder(token: FirebaseToken, request: OrderRequest): CreateOrderResponse {

        val customerUid = token.uid
        val orderId = "order_${generateOrderId()}_${System.currentTimeMillis()}"

        val order = Order(
            customerId = customerUid,
            orderId = orderId,
            applianceBrand = request.serviceProduct.applianceBrand,
            applianceModel = request.serviceProduct.applianceModel,
            totalAmount = request.priceDetail.totalAmount,
            productId = request.serviceProduct.productId.toLong(),
            technicianId = request.technicianId?.toLong(),
            discount = request.priceDetail.discount,
            paymentStatus = PaymentStatus.PAID,
            paymentMode = PaymentMode.UPI,
            scheduledAt = request.serviceSchedule,
            price = request.priceDetail.price,
            tax = request.priceDetail.tax,
            fullName = request.serviceAddress.fullName,
            phone = request.serviceAddress.phone,
            addressLine1 = request.serviceAddress.addressLine1,
            city = request.serviceAddress.city,
            state = request.serviceAddress.state,
            postalCode =request.serviceAddress.postalCode,
        )

        val savedOrder = orderRepository.save(order)

        return CreateOrderResponse(
            orderId = savedOrder.id,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount
        )
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        return OrderMapper.entityToResponse(order)
    }

//    fun getOrdersForUser(userID: java.util.UUID): List<OrderResponse>{
//        val ordersForUser = orderRepository.getAllOrdersWhereUserId(userID)
//       return ordersForUser.map {
//           OrderMapper.entityToResponse(it,"","")
//        }\n//    }

    @Transactional
    fun updateOrderPaymentStatus(orderId: String, paymentStatus: PaymentStatus) {
//        val order = orderRepository.findById(orderId)
//            .orElseThrow { OrderNotFoundException(orderId) }
//
//        order.paymentStatus = paymentStatus
//        orderRepository.save(order)
    }

}

object OrderMapper{
    fun entityToResponse(order: Order) : OrderResponse = OrderResponse(
        id = order.id,
        orderId = order.orderId,
        customerId = order.customerId,
        productId = order.productId,
        applianceBrand = order.applianceBrand,
        applianceModel = order.applianceModel,
        technicianId = order.technicianId,
        price = order.price,
        tax = order.tax,
        offerId = order.offerId,
        discount = order.discount,
        totalAmount = order.totalAmount,
        status = order.status,
        paymentStatus = order.paymentStatus,
        paymentMode = order.paymentMode,
        scheduledAt = order.scheduledAt,
        completedAt = order.completedAt,
        createdAt = order.createdAt,
        updatedAt = order.updatedAt,
        cancellationReason = order.cancellationReason,
        serviceAddress = Address(
            fullName = order.fullName,
            phone = order.phone,
            addressLine1 = order.addressLine1,
            city = order.city,
            state = order.state,
            postalCode = order.postalCode,
        )
    )
}

