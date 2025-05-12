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
@Table(name = "passport_image")
public class PassportImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long passportImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] passportFile;
    private String passportFileName;
    private String passportFileType;

    @ManyToOne
    @JoinColumn(name = "passportDetailsId")
    @JsonIgnore
    private PassportDetails passportDetails;

}
