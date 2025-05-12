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
@Table(name = "class_xii_details")
public class ClassXIIDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long classXIIDetailsId;

    @Column(unique = true)
    private String classXIIRollNo;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "classXIIDetailsId")
    private List<ClassXIIDocs> classXIIDocs;
}
