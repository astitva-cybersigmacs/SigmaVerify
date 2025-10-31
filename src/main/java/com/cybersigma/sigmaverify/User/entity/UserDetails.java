package com.cybersigma.sigmaverify.User.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    private String name;

    @NotBlank(message = "Username should not be null")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Email should not be null")
    @Email(message = "Email is not valid")
    @Column(unique = true)
    private String emailId;

    private String contactNumber;

    @Column(name = "is_validated", nullable = false)
    private boolean isValidated = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "aadhaarDetailsId")
    private AadhaarDetails aadhaarDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "panDetailsId")
    private PanDetails panDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "drivingLicenseDetailsId")
    private DrivingLicenseDetails drivingLicenseDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "passportDetailsId")
    private PassportDetails passportDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bankStatementDetailsId")
    private BankStatementDetails bankStatementDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "classXDetailsId")
    private ClassXDetails classXDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "classXIIDetailsId")
    private ClassXIIDetails classXIIDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "underGraduationDetailsId")
    private UnderGraduationDetails underGraduationDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "birthCertificateDetailsId")
    private BirthCertificateDetails birthCertificateDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "marriageCertificateDetailsId")
    private MarriageCertificateDetails marriageCertificateDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "incomeTaxReturnDetailsId")
    private IncomeTaxReturnDetails incomeTaxReturnDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "policeClearanceCertificateDetailsId")
    private PoliceClearanceCertificateDetails policeClearanceCertificateDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "crimeCheckDetailsId")
    private CrimeCheckDetails crimeCheckDetails;

}
