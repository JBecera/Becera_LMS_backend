package edu.cit.becera.lrbms.mobile.ui.common

/** Mirrors the backend policy in MembershipService so forms can warn before submitting. */
const val PASSWORD_HINT = "At least 8 characters, including a letter and a number."

fun passwordStrengthError(password: String?): String? {
    if (password == null || password.length < 8) return "Password must be at least 8 characters long."
    if (!password.any { it.isLetter() } || !password.any { it.isDigit() }) {
        return "Password must include at least one letter and one number."
    }
    return null
}

private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun emailFormatError(email: String?): String? {
    if (email.isNullOrBlank() || !EMAIL_PATTERN.matches(email.trim().lowercase())) {
        return "Email must be a valid email address."
    }
    return null
}
