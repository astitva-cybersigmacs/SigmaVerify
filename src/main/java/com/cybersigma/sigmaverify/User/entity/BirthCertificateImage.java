package com.cybersigma.sigmaverify.User.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "birth_certificate_image")
public class BirthCertificateImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long birthCertificateImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] birthCertificateImageFile;
    private String birthCertificateImageFileName;
    private String birthCertificateImageFileType;

    @ManyToOne
    @JoinColumn(name = "birthCertificateDetailsId")
    @JsonIgnore
    private BirthCertificateDetails birthCertificateDetails;
}
