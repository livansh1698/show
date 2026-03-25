package com.ddmo.app.service;

import com.ddmo.app.dto.LoginRequest;
import com.ddmo.app.dto.RegisterRequest;
import com.ddmo.app.security.JwtService;
import com.ddmo.app.security.TenantContext;
import com.ddmo.app.util.SnowflakeIdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtService jwtService;
    private final SnowflakeIdGenerator idGenerator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JdbcTemplate jdbcTemplate, JwtService jwtService, SnowflakeIdGenerator idGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
        this.idGenerator = idGenerator;
    }

    public Map<String, Object> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null
            || request.getUsername().isBlank() || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        String sql = "SELECT id, tenant_id, username, password_hash, nickname, status FROM t_manager WHERE username = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, request.getUsername().trim());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        Map<String, Object> row = rows.get(0);
        String status = String.valueOf(row.get("status"));
        if (!"active".equals(status)) {
            throw new IllegalArgumentException("账号已停用");
        }

        String dbHash = String.valueOf(row.get("password_hash"));
        boolean ok;
        if (dbHash.startsWith("$2a$") || dbHash.startsWith("$2b$") || dbHash.startsWith("$2y$")) {
            ok = passwordEncoder.matches(request.getPassword(), dbHash);
        } else {
            ok = request.getPassword().equals(dbHash);
            if (ok) {
                jdbcTemplate.update("UPDATE t_manager SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    passwordEncoder.encode(request.getPassword()), row.get("id"));
            }
        }
        if (!ok) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        long tenantId = ((Number) row.get("tenant_id")).longValue();
        String token = jwtService.generateToken(String.valueOf(row.get("username")), tenantId);

        Map<String, Object> user = new HashMap<>();
        user.put("username", String.valueOf(row.get("username")));
        user.put("nickname", String.valueOf(row.get("nickname")));
        user.put("avatar", "");
        user.put("role", "admin");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public Map<String, Object> me() {
        long tenantId = TenantContext.getTenantId();
        String sql = "SELECT username, nickname, status FROM t_manager WHERE tenant_id = ? LIMIT 1";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tenantId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Map<String, Object> row = rows.get(0);
        Map<String, Object> user = new HashMap<>();
        user.put("username", String.valueOf(row.get("username")));
        user.put("nickname", String.valueOf(row.get("nickname")));
        user.put("avatar", "");
        user.put("role", "admin");
        user.put("status", String.valueOf(row.get("status")));
        return user;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
            || request.getPassword() == null || request.getPassword().isBlank()
            || request.getNickname() == null || request.getNickname().isBlank()) {
            throw new IllegalArgumentException("用户名、密码、昵称不能为空");
        }
        String username = request.getUsername().trim();
        String password = request.getPassword().trim();
        String nickname = request.getNickname().trim();
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        if (nickname.length() > 6) {
            throw new IllegalArgumentException("昵称最多6个字");
        }
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM t_manager WHERE username = ?",
            Long.class,
            username
        );
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        long managerId = idGenerator.nextId();
        long tenantId = managerId;
        jdbcTemplate.update("""
                INSERT INTO t_manager(id, tenant_id, username, password_hash, nickname, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
            managerId, tenantId, username, passwordEncoder.encode(password), nickname);

        insertDefaultServiceTypes(tenantId);
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("旧密码和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }

        long tenantId = TenantContext.getTenantId();
        String sql = "SELECT id, password_hash FROM t_manager WHERE tenant_id = ? LIMIT 1";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tenantId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Map<String, Object> row = rows.get(0);
        long managerId = ((Number) row.get("id")).longValue();
        String dbHash = String.valueOf(row.get("password_hash"));

        boolean match;
        if (dbHash.startsWith("$2a$") || dbHash.startsWith("$2b$") || dbHash.startsWith("$2y$")) {
            match = passwordEncoder.matches(oldPassword, dbHash);
        } else {
            match = oldPassword.equals(dbHash);
        }
        if (!match) {
            throw new IllegalArgumentException("旧密码错误");
        }

        String newHash = passwordEncoder.encode(newPassword);
        jdbcTemplate.update("UPDATE t_manager SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", newHash, managerId);
    }

    private void insertDefaultServiceTypes(long tenantId) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM t_service_type WHERE tenant_id = ?",
            Long.class,
            tenantId
        );
        if (count != null && count > 0) {
            return;
        }

        insertService(tenantId, "洗剪吹", BigDecimal.valueOf(58));
        insertService(tenantId, "染发", BigDecimal.valueOf(188));
        insertService(tenantId, "烫发", BigDecimal.valueOf(268));
        insertService(tenantId, "护理", BigDecimal.valueOf(128));
    }

    private void insertService(long tenantId, String name, BigDecimal price) {
        jdbcTemplate.update("""
                INSERT INTO t_service_type(id, tenant_id, name, price, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
            idGenerator.nextId(), tenantId, name, price);
    }
}
