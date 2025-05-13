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
@Table(name = "under_graduation_image")
public class UnderGraduationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long underGraduationImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] underGraduationImageFile;
    private String underGraduationImageFileName;
    private String underGraduationImageFileType;

    @ManyToOne
    @JoinColumn(name = "underGraduationDetailsId")
    @JsonIgnore
    private UnderGraduationDetails underGraduationDetails;
}
