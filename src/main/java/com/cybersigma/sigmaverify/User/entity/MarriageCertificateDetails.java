package com.cybersigma.sigmaverify.User.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "marriage_certificate")
public class MarriageCertificateDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long marriageCertificateDetailsId;

    @Column(unique = true)
    private String marriageCertificateNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "birthCertificateDetailsId")
    private List<MarriageCertificateImage> marriageCertificateImages;
}
