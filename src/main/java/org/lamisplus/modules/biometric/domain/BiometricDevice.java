package org.lamisplus.modules.biometric.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "biometric_device")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class BiometricDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String url;
    private Boolean active = true;
}
