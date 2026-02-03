package com.iotabuild.zippyhand_Admin.user.repository

import com.iotabuild.zippyhand_Admin.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByFirebaseUid(firebaseUid: String): Optional<User>
}
