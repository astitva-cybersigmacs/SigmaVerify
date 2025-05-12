package com.cybersigma.sigmaverify.auth.models;

import com.cybersigma.sigmaverify.utils.Tracker;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Entity
@Table(name = "User")
public class UserModel implements UserDetails {

    private static final int serialVersionUID = 1;

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Setter
    @NotBlank(message = "User Name should not be null")
    @Column(unique = true, nullable = false, updatable = false)
    @Size(max = 50)
    @Email
    private String userName;

    @Getter
    @Setter
    @Column(nullable = false, updatable = false)
    @NotBlank(message = "First Name should not be empty")
    @Size(max = 20, min = 2, message = "Name should be contain minimum 3 and maximum 20 digits")
    private String firstName;

    @Getter
    @Setter
    @Size(max = 20, message = "middle name should be contain minimum 3 and maximum 20 digits")
    private String middleName;

    @Getter
    @Setter
    @Size(max = 20, message = "Last Name should be contain minimum 3 and maximum 20 digits")
    private String lastName;

    @Setter
    @Getter
    @NotBlank(message = "Password should not be null")
    private String password;

    @Getter
    @Setter
    @Column(nullable = false, updatable = false)
    @NotBlank(message = "Email should not be null")
    @Email(message = "Email is not valid")
    @Size(max = 50)
    // @Pattern(regexp = emailRegX, message = "EmailId is not valid")
    private String email;

    @Getter
    @Setter
    @Column(nullable = false, updatable = false)
    @NotBlank(message = "phone no should not be null")
    @Size(min = 10, max = 16, message = "Phone Number should be contains 10-16 digits")
    private String phoneNo;

    private boolean status;



    public UserModel(int userId) {
        this.userId = userId;
    }

    public UserModel(int userId, String userName, String firstName, String middleName, String lastName, String password, String email, String phoneNo) {
        this.userId = userId;
        this.userName = userName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public UserModel(int userId, String userName, int otp, Date otpTime) {
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO Auto-generated method stub
//        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
//        for (UsersRolesModel roleName : userRole) {
//            GrantedAuthority grant = new SimpleGrantedAuthority(roleName.getRoles().getRoleType());
//            list.add(grant);
//        }
//        return list;
        return null;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return status;
    }
}
