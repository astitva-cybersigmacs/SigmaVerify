package com.cybersigma.sigmaverify.User.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "passport_details")
public class PassportDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long passportDetailsId;

    @Column(unique = true)
    private String passportNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "passportDetailsId")
    private List<PassportImage> passportImages;
}
