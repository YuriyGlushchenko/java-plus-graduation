package ru.practicum.ewm.user.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)  //  безопасно для @OneToMany и lazy-загрузок
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA обязан иметь конструктор без аргументов
@AllArgsConstructor  // для Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

}