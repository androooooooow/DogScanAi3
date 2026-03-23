package com.dogscanai.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val accept_terms: Boolean = false,
    val policy_key: String = "registration_scan_policy",
    val policy_version: String = "2026-03-22"
)
