package com.example.web_service.entity;

import java.io.Serializable;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {
    private UUID followerId;
    private UUID followeeId;
}
