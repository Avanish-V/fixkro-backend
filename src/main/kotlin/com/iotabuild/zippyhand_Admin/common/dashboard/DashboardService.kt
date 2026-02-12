package com.iotabuild.zippyhand_Admin.common.dashboard

import com.iotabuild.zippyhand_Admin.order.OrderRepository
import com.iotabuild.zippyhand_Admin.order.OrderService
import com.iotabuild.zippyhand_Admin.order.OrderStatus
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import com.iotabuild.zippyhand_Admin.professionals.repository.ProfessionalRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

@Service
class DashboardService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val professionalRepository: ProfessionalRepository,
    private val orderService: OrderService
) {

    fun getDashboardStats(): DashboardStats {
        val allOrders = orderRepository.findAll()
        val totalOrders = allOrders.size.toLong()
        val totalRevenue = allOrders.sumOf { it.totalAmount }.toLong()
        val activeServices = productRepository.count()
        val totalProfessionals = professionalRepository.count()

        val today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        val completedToday = allOrders.count { 
            it.status == OrderStatus.COMPLETED && it.completedAt?.isAfter(today) == true 
        }.toLong()
        
        val inProgress = allOrders.count { it.status == OrderStatus.IN_PROGRESS }.toLong()
        val pending = allOrders.count { it.status == OrderStatus.CREATED || it.status == OrderStatus.ASSIGNED }.toLong()

        // Dummy data for revenue chart based on last 6 months
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

        val recentOrders = allOrders.sortedByDescending { it.createdAt }
            .take(5)
            .map { orderService.getOrder(null, it.id) }

        return DashboardStats(
            totalOrders = totalOrders,
            totalRevenue = totalRevenue,
            activeServices = activeServices,
            totalProfessionals = totalProfessionals,
            completedToday = completedToday,
            inProgress = inProgress,
            pending = pending,
            growthRate = "+12%", // In a real app, calculate this vs last month
            revenueData = revenueData,
            recentOrders = recentOrders
        )
    }
}
