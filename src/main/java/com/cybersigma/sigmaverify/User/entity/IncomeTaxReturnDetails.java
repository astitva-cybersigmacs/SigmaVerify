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
@Table(name = "income_tax_return_details")
public class IncomeTaxReturnDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long incomeTaxReturnDetailsId;

    @Column(unique = true)
    private String incomeTaxReturnNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "incomeTaxReturnDetailsId")
    private List<IncomeTaxReturnImage> incomeTaxReturnImages ;
}
