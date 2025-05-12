package com.cybersigma.sigmaverify.User.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "aadhaar_details")
public class AadhaarDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long aadhaarDetailsId;

    @Column(unique = true)
    private String digilockerId;

    @Column(unique = true)
    private String aadhaarNumber;

    private String name;
    private String gender;
    private String dateOfBirth;
    private String maskedNumber;

    @Column(columnDefinition = "TEXT")
    private String photo;

    private String email;
    private String phone;

    // Address fields
    private String careOf;
    private String house;
    private String street;
    private String landmark;
    private String locality;
    private String vtc;
    private String subDistrict;
    private String district;
    private String state;
    private String country;
    private String pin;
    private String postOffice;

    // Verification details
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean signatureVerified;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;
    private String shareCode;
    private LocalDateTime validUntil;

    private LocalDateTime generatedAt;
    private String traceId;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "aadhaarDetailsId")
    private List<AadhaarImage> aadhaarImages;
}
