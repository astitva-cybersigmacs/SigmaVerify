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
@Table(name = "class_xii_docs")
public class ClassXIIDocs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long classXIIDocId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] classXIIImageFile;
    private String classXIImageFileName;
    private String classXIImageFileType;

    @ManyToOne
    @JoinColumn(name = "classXIIDetailsId")
    @JsonIgnore
    private ClassXIIDetails classXIIDetails;
}
