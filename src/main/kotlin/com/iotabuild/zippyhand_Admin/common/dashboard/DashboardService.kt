package com.iotabuild.zippyhand_Admin.common.dashboard

import com.iotabuild.zippyhand_Admin.order.entity.OrderStatus
import com.iotabuild.zippyhand_Admin.order.service.OrderQueryService
import com.iotabuild.zippyhand_Admin.products.service.ProductQueryService
import com.iotabuild.zippyhand_Admin.professionals.service.ProfessionalQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

@Service
class DashboardService(
    private val orderQueryService: OrderQueryService,
    private val productQueryService: ProductQueryService,
    private val professionalQueryService: ProfessionalQueryService
) {

    @Transactional(readOnly = true)
    fun getDashboardStats(): DashboardStats {
        val allOrders = orderQueryService.getAllOrders()
        val totalOrders = allOrders.size.toLong()
        val totalRevenue = allOrders.sumOf { it.totalAmount }.toLong()
        val activeServices = productQueryService.getAllProducts(null).size.toLong()
        val totalProfessionals = professionalQueryService.getAllProfessionals().size.toLong()

        val today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        val completedToday = allOrders.count { 
            it.status == OrderStatus.COMPLETED && it.completedAt?.isAfter(today) == true 
        }.toLong()
        
        val inProgress = allOrders.count { it.status == OrderStatus.IN_PROGRESS }.toLong()
        val pending = allOrders.count { it.status == OrderStatus.CREATED || it.status == OrderStatus.ASSIGNED }.toLong()

        // Revenue chart based on last 6 months
        val revenueData = (0..6).reversed().map { monthsAgo ->
            val date = LocalDateTime.now().minusMonths(monthsAgo.toLong())
            val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            
            val monthOrders = allOrders.filter { 
                it.createdAt.month == date.month && it.createdAt.year == date.year 
            }
            
            ChartData(
                name = monthName,
                revenue = monthOrders.sumOf { it.totalAmount }.toLong(),
                orders = monthOrders.size.toLong()
            )
        }

        val recentOrders = allOrders.sortedByDescending { it.createdAt }.take(5)

        return DashboardStats(
            totalOrders = totalOrders,
            totalRevenue = totalRevenue,
            activeServices = activeServices,
            totalProfessionals = totalProfessionals,
            completedToday = completedToday,
            inProgress = inProgress,
            pending = pending,
            growthRate = "+12%",
            revenueData = revenueData,
            recentOrders = recentOrders
        )
    }
}
