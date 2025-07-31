package com.pet.taskflow.security.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(
    val id: Long,
    val usernameVal: String,
    val passwordVal: String,
    val role: Collection<GrantedAuthority>,
    private val isEnabled: Boolean = true
) : UserDetails {

    override fun getAuthorities() = role

    override fun getPassword() = passwordVal

    override fun getUsername() = usernameVal

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}