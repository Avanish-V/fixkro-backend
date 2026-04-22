package com.iotabuild.zippyhand_Admin.jobs.repository

import com.iotabuild.zippyhand_Admin.jobs.entity.JobAssignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobAssignmentRepository : JpaRepository<JobAssignment, Long> {
    fun findByUid(uid: String): List<JobAssignment>
    fun findByUidAndStatus(uid: String, status: String): List<JobAssignment>
    fun findByOrderId(orderId: String): JobAssignment?
    fun findAllByOrderId(orderId: String): List<JobAssignment>
}
