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
@Table(name = "crime_check_details")
public class CrimeCheckDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long crimeCheckDetailsId;

    @Column(unique = true)
    private String crimeCheckId;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "crimeCheckDetails")
    private List<CrimeCheckImage> crimeCheckImages;
}
