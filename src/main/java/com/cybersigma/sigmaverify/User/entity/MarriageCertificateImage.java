package com.cybersigma.sigmaverify.User.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "marriage_certificate_image")
public class MarriageCertificateImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long marriageCertificateImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] marriageCertificateImageFile;
    private String marriageCertificateImageFileName;
    private String marriageCertificateImageFileType;

    @ManyToOne
    @JoinColumn(name = "marriageCertificateDetailsId")
    @JsonIgnore
    private MarriageCertificateDetails marriageCertificateDetails;
}
