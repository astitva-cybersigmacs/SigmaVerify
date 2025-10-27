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
@Table(name = "pan_details")
public class PanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long panDetailsId;

    @Column(unique = true)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String providerResponse;

    private String sourceOfVerification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "panDetailsId")
    private List<PanImage> panImages;
}
