package ru.botcreators.telegramchatparsingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRecord {
    private String userId;
    private String username;
    private String fullName;
    private String bio;
    private LocalDate registrationDate;
    private Boolean hasChannelInProfile;
    private UserSource source;
}
