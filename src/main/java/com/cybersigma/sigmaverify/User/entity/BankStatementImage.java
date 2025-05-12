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
@Table(name = "bank_statement_images")
public class BankStatementImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long bankStatementImageId;

    @Enumerated(EnumType.STRING)
    private ImageSide imageSide;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] bankStatementFile;
    private String bankStatementFileName;
    private String bankStatementFileType;

    @ManyToOne
    @JoinColumn(name = "bankStatementDetailsId")
    @JsonIgnore
    private BankStatementDetails bankStatementDetails;

}
