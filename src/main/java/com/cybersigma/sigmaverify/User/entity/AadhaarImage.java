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
@Table(name = "aadhaar_image")
public class AadhaarImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long aadhaarImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] aadhaarFile;
    private String aadhaarFileName;
    private String aadhaarFileType;

    @ManyToOne
    @JoinColumn(name = "aadhaarDetailsId")
    @JsonIgnore
    private AadhaarDetails aadhaarDetails;
}
