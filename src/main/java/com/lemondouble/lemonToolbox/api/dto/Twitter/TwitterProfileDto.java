package com.lemondouble.lemonToolbox.api.dto.Twitter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwitterProfileDto {
    private String screenName;
    private String screenNickname;
    private String userBio;
    private String profileImageURL;
    private String bannerImageURL;
    private int followingCount;
    private int followerCount;
}
