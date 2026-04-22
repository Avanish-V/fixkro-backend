package com.iotabuild.zippyhand_Admin.order.mapper

import com.iotabuild.zippyhand_Admin.order.dto.Address
import com.iotabuild.zippyhand_Admin.order.dto.OrderResponse
import com.iotabuild.zippyhand_Admin.order.entity.OrderEntity
import com.iotabuild.zippyhand_Admin.professionals.entity.ProfessionalEntity
import org.springframework.stereotype.Component

@Component
class OrderMapper {

    fun toResponse(
        order: OrderEntity,
        technician: ProfessionalEntity? = null,
        otp: String? = null
    ): OrderResponse {
        return OrderResponse(
            id = order.id,
            orderId = order.orderId,
            customerId = order.customerId,
            productId = order.productId,
            productTitle = order.productTitle,
            productImage = order.productImage,
            applianceBrand = order.applianceBrand ?: "",
            applianceModel = order.applianceModel ?: "",
            technicianId = technician?.id,
            technicianName = technician?.name,
            technicianPhoto = technician?.photo,
            technicianUid = technician?.firebaseUid,
            technicianPhone = technician?.mobile,
            otp = otp,
            price = order.price,
            tax = order.tax,
            offerId = order.offerId,
            offerCode = order.offerCode,
            discount = order.discount ?: 0,
            totalAmount = order.totalAmount,
            status = order.status,
            paymentStatus = order.paymentStatus,
            paymentMode = order.paymentMode,
            scheduledAt = order.scheduledAt ?: java.time.LocalDateTime.now(), // Fallback or handle null
            completedAt = order.completedAt,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            cancellationReason = order.cancellationReason,
            serviceAddress = Address(
                fullName = order.fullName,
                phone = order.phone,
                addressLine1 = order.addressLine1,
                addressLine2 = order.addressLine2,
                city = order.city,
                state = order.state,
                postalCode = order.postalCode,
            )
        )
    }
}
