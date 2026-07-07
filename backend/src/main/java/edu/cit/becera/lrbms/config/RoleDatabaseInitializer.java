package edu.cit.becera.lrbms.config;

import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleDatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final MemberRepository memberRepository;

    public RoleDatabaseInitializer(JdbcTemplate jdbcTemplate, MemberRepository memberRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'MEMBER'");
        jdbcTemplate.execute("UPDATE members SET role = 'MEMBER' WHERE role IS NULL");

        ensureAccount("admin@library.test", "Admin", "User", "admin123", "ADMIN");
        ensureAccount("librarian@library.test", "Library", "Staff", "librarian123", "LIBRARIAN");
    }

    @Transactional
    private void ensureAccount(String email, String firstName, String lastName, String password, String role) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE email = ?",
                Integer.class,
                email);

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE members SET first_name = ?, last_name = ?, password = ?, role = ? WHERE email = ?",
                    firstName,
                    lastName,
                    password,
                    role,
                    email);
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO members (email, first_name, last_name, password, role) VALUES (?, ?, ?, ?, ?)",
                email,
                firstName,
                lastName,
                password,
                role);
    }
}
