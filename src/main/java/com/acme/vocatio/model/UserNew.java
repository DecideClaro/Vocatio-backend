package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad Usuario compatible con el nuevo esquema de BD migrada.
 * Mantiene compatibilidad con la tabla 'users' existente pero preparada para migración.
 */
@Entity
@Table(name = "users") // Por ahora mantiene el nombre original
@Getter
@Setter
@NoArgsConstructor
public class UserNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Mapea tanto 'id' como 'id_usuario'
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Nuevos campos para migración gradual
    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    @Column(name = "token_expiracion")
    private Instant tokenExpiracion;

    @Column(name = "estado_cuenta", length = 50)
    private String estadoCuenta = "activo";

    // Relaciones
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Profile profile;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.estadoCuenta == null) {
            this.estadoCuenta = "activo";
        }
    }
}