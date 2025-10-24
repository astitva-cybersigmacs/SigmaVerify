package com.cybersigma.sigmaverify.User.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "driving_license_details")
public class DrivingLicenseDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long drivingLicenseDetailsId;

    @Column(unique = true)
    private String drivingLicenseNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "drivingLicenseDetailsId")
    private List<DrivingLicenseImage> drivingLicenseImages;

}
