package edu.cit.becera.lrbms.config;

import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleDatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public RoleDatabaseInitializer(JdbcTemplate jdbcTemplate, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'MEMBER'");
        jdbcTemplate.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS student_id VARCHAR(20)");
        jdbcTemplate.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30)");
        jdbcTemplate.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS address VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS date_registered DATE");
        jdbcTemplate.execute("UPDATE members SET role = 'MEMBER' WHERE role IS NULL");
        jdbcTemplate.execute("UPDATE members SET date_registered = CURRENT_DATE WHERE date_registered IS NULL");

        ensureAccount("admin@library.test", "Admin", "User", "admin123", "ADMIN");
        ensureAccount("librarian@library.test", "Library", "Staff", "librarian123", "LIBRARIAN");
    }

    @Transactional
    private void ensureAccount(String email, String firstName, String lastName, String password, String role) {
        String hashed = passwordEncoder.encode(password);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE email = ?",
                Integer.class,
                email);

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE members SET first_name = ?, last_name = ?, password = ?, role = ? WHERE email = ?",
                    firstName,
                    lastName,
                    hashed,
                    role,
                    email);
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO members (email, first_name, last_name, password, role, date_registered) VALUES (?, ?, ?, ?, ?, CURRENT_DATE)",
                email,
                firstName,
                lastName,
                hashed,
                role);
    }
}
