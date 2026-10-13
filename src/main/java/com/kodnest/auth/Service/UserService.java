package com.kodnest.auth.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kodnest.auth.Entity.User;
import com.kodnest.auth.Entity.UserOTP;
import com.kodnest.auth.Repository.UserOTPRepository;
import com.kodnest.auth.Repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserOTPRepository userOtpRepo;

    @Autowired
    private JavaMailSender mailSender;

    private final SecureRandom random = new SecureRandom();
    
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

 // Register a new user
    public void register(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPwd());
        user.setPwd(hashedPassword);
        userRepo.save(user);
    }

    // Validate login and generate OTP
    public boolean loginAndGenerateOTP(
            String email, String password) {

        User user = userRepo.findByEmail(email);

        // Check whether user exists
        if (user == null) {
            return false;
        }

        // Your User entity uses the field "pwd"
        if (user.getPwd() == null
                || !passwordEncoder.matches(password, user.getPwd())) {
            return false;
        }

        // Generate a six-digit OTP
        String otp = String.valueOf(
                100000 + random.nextInt(900000));
        

        // Delete previous OTPs for this user
        List<UserOTP> previousOtps =
                userOtpRepo.findByUserId(user.getId());

        if (!previousOtps.isEmpty()) {
            userOtpRepo.deleteAll(previousOtps);
        }


        // Save OTP and creation time
        UserOTP userOtp = new UserOTP();
        userOtp.setOtp(otp);
        userOtp.setUserId(user.getId());
        userOtp.setCreatedTime(LocalDateTime.now());

        userOtpRepo.save(userOtp);

        // Send OTP to the registered email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your OTP Code");
        message.setText(
                "Hello from 107 ," + user.getName()
                + ", your OTP is " + otp
                + ". It is valid for 2 minutes."
        );

        mailSender.send(message);

        return true;
    }



@Transactional
public boolean verifyOtp(String email, String otp) {

    User user = userRepo.findByEmail(email);

    if (user == null) {
        return false;
    }

    List<UserOTP> otps =
            userOtpRepo.findByUserId(user.getId());

    LocalDateTime now = LocalDateTime.now();

    for (UserOTP candidate : otps) {

        // Expire OTPs older than two minutes
        if (candidate.getCreatedTime() == null
                || !now.isBefore(
                        candidate.getCreatedTime().plusMinutes(2))) {

            userOtpRepo.delete(candidate);
            continue;
        }

        // Ignore OTPs that have already reached the attempt limit
        if (candidate.getFailedAttempts() >= 5) {
            userOtpRepo.delete(candidate);
            continue;
        }

        // Check the submitted OTP
        if (candidate.getOtp() != null
                && candidate.getOtp().equals(otp)) {

            // A valid OTP can only be used once
            userOtpRepo.delete(candidate);

            return true;
        }

        // Count an incorrect attempt
        candidate.setFailedAttempts(
                candidate.getFailedAttempts() + 1);

        // Invalidate the OTP after five incorrect attempts
        if (candidate.getFailedAttempts() >= 5) {
            userOtpRepo.delete(candidate);
        } else {
            userOtpRepo.save(candidate);
        }
    }

    return false;
}


}