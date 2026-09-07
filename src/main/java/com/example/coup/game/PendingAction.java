package com.example.coup.game;

public record PendingAction(
        ActionType type,
        String actorId,
        String targetId
) {
}
