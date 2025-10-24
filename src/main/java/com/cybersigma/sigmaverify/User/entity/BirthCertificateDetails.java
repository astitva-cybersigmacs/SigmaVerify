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
@Table(name = "birth_certificate")
public class BirthCertificateDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long birthCertificateDetailsId;

    @Column(unique = true)
    private String birthCertificateNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "birthCertificateDetailsId")
    private List<BirthCertificateImage> birthCertificateImages;
}
