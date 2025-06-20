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
    private String aadhaarNumber;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "aadhaarDetailsId")
    private List<AadhaarImage> aadhaarImages;
}
