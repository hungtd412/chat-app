package com.hungtd.chatapp.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ParticipantId implements Serializable {
    private String conversationId;
    private String userId;
}
