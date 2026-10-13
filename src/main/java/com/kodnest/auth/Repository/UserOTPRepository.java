
package com.kodnest.auth.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kodnest.auth.Entity.UserOTP;

public interface UserOTPRepository extends JpaRepository<UserOTP, Integer> {

    UserOTP findByOtp(String otp);

    List<UserOTP> findByUserId(Integer userId);
}
