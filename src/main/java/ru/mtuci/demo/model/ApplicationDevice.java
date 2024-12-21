package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "devices")
@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class ApplicationDevice {

    @Id
    @GeneratedValue
    private UUID id;

    @NonNull
    private String name;

    @NonNull
    private String macAddress;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser user;
}
