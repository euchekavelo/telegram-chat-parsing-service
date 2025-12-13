package ru.botcreators.telegramchatparsingservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
public class UserRecord {
    private String username;
    private String fullName;
    private String bio;
    private LocalDate registrationDate;
    private Boolean hasChannelInProfile;
    private UserSource source;

    public UserRecord(String username, String fullName, String bio,
                      LocalDate registrationDate, Boolean hasChannelInProfile,
                      UserSource source) {
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.registrationDate = registrationDate;
        this.hasChannelInProfile = hasChannelInProfile;
        this.source = source;
    }
}