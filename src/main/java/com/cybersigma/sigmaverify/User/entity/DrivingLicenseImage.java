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
@Table(name = "driver_license_image")
public class DrivingLicenseImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long drivingLicenseImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] drivingLicenseFile;
    private String drivingLicenseFileName;
    private String drivingLicenseFileType;

    @ManyToOne
    @JoinColumn(name = "drivingLicenseDetailsId")
    @JsonIgnore
    private DrivingLicenseDetails drivingLicenseDetails;
}
