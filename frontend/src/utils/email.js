// Mirrors the backend policy in MembershipService.validateEmailFormat so forms can warn
// before submitting. The backend remains the source of truth.
const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function emailFormatError(email) {
  if (!email || !EMAIL_PATTERN.test(email.trim().toLowerCase())) {
    return "Email must be a valid email address.";
  }
  return "";
}
