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
@Table(name = "pan_image")
public class PanImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long panImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] panFile;
    private String panFileName;
    private String panFileType;

    @ManyToOne
    @JoinColumn(name = "panDetailsId")
    @JsonIgnore
    private PanDetails panDetails;
}
