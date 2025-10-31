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
@Table(name = "crime_check_image")
public class CrimeCheckImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long crimeCheckImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] crimeCheckImageFile;
    private String crimeCheckFileName;
    private String crimeCheckFileType;

    @ManyToOne
    @JoinColumn(name = "crimeCheckDetailsId")
    @JsonIgnore
    private CrimeCheckDetails crimeCheckDetails;


}
