package com.cybersigma.sigmaverify.User.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "police_clearance_certificate")
public class PoliceClearanceCertificateDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long policeClearanceCertificateDetailsId;

    @Column(unique = true)
    private String policeClearanceCertificateNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "policeClearanceCertificateDetailsId") // FK in image table
    private List<PoliceClearanceCertificateImage> policeClearanceCertificateImages;
}

