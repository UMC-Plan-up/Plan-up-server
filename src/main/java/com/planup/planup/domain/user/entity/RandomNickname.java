package com.planup.planup.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomNickname {

    private String adjective;
    private String noun;
    private String fullNickname;

    public static RandomNickname of(String adjective, String noun) {
        return RandomNickname.builder()
                .adjective(adjective)
                .noun(noun)
                .fullNickname(adjective + noun)
                .build();
    }
}
