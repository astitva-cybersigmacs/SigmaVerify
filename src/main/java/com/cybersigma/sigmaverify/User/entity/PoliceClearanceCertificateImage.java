package com.cybersigma.sigmaverify.User.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "police_clearance_certificate_image")
public class PoliceClearanceCertificateImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long policeClearanceCertificateImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] policeClearanceCertificateImageFile;

    private String policeClearanceCertificateImageFileName;
    private String policeClearanceCertificateImageFileType;

    @ManyToOne
    @JoinColumn(name = "policeClearanceCertificateDetailsId")
    @JsonIgnore
    private PoliceClearanceCertificateDetails policeClearanceCertificateDetails;
}

