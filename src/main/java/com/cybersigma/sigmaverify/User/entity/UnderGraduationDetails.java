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
@Table(name = "under_graduation_details")
public class UnderGraduationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long underGraduationDetailsId;

    @Column(unique = true)
    private String underGraduationRollNo;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "underGraduationDetailsId")
    private List<UnderGraduationImage> underGraduationImages;
}
