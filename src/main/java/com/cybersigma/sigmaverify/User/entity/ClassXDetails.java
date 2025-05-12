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
@Table(name = "class_x_details")
public class ClassXDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long classXDetailsId;

    @Column(unique = true)
    private String classXId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "panDetailsId")
    private List<ClassXImages> classXImages;
}
