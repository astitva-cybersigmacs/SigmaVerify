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
@Table(name = "class_x_images")
public class ClassXImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long classXImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] classXImageFile;
    private String classXImageFileName;
    private String classXImageFileType;

    @ManyToOne
    @JoinColumn(name = "classXDetailsId")
    @JsonIgnore
    private ClassXDetails classXDetails;
}
