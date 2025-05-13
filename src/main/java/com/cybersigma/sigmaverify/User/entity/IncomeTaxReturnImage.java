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
@Table(name = "income_tax_return_image")
public class IncomeTaxReturnImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long incomeTaxReturnImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] incomeTaxReturnImageFile;
    private String incomeTaxReturnImageFileName;
    private String incomeTaxReturnImageFileType;

    @ManyToOne
    @JoinColumn(name = "incomeTaxReturnDetailsId")
    @JsonIgnore
    private IncomeTaxReturnDetails incomeTaxReturnDetails;
}
