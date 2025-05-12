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
@Table(name = "bank_statement_details")
public class BankStatementDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long bankStatementDetailsId;

    @Column(unique = true)
    private String bankAccountNumber;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bankStatementDetailsId")
    private List<BankStatementImage> bankStatementImages;
}
