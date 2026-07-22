// Mirrors the backend policy in MembershipService.validatePasswordStrength so forms can warn
// before submitting. The backend remains the source of truth.
export const PASSWORD_HINT = "At least 8 characters, including a letter and a number.";

export function passwordStrengthError(password) {
  if (!password || password.length < 8) {
    return "Password must be at least 8 characters long.";
  }
  if (!/[a-zA-Z]/.test(password) || !/[0-9]/.test(password)) {
    return "Password must include at least one letter and one number.";
  }
  return "";
}
