package edu.cit.becera.lrbms.security;

public record CurrentUser(Long id, String role) {

    public boolean isStaff() {
        return "ADMIN".equalsIgnoreCase(role) || "LIBRARIAN".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean owns(Long memberId) {
        return id != null && id.equals(memberId);
    }
}
