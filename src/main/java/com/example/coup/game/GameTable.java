package com.example.coup.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameTable {

    private static final int MINIMUM_PLAYERS = 3;
    private static final int MAXIMUM_PLAYERS = 4;
    private static final int COUP_COST = 7;
    private static final int TAX_COINS = 3;
    private static final int MANDATORY_COUP_COINS = 10;

    private final String id;
    private final List<Player> players = new ArrayList<>();
    private GamePhase phase = GamePhase.WAITING_FOR_PLAYERS;
    private int currentPlayerIndex;
    private PendingAction pendingAction;
    private final CourtDeck courtDeck = new CourtDeck();
    private String winnerId;

    public GameTable(String id) {
        if (Objects.requireNonNull(id, "Table id is required").isBlank()) {
            throw new IllegalArgumentException("Table id is required");
        }
        this.id = id;
    }

    public void join(Player player) {
        requirePhase(GamePhase.WAITING_FOR_PLAYERS);
        Objects.requireNonNull(player, "Player is required");

        if (players.size() == MAXIMUM_PLAYERS) {
            throw new GameRuleException("Table is full");
        }
        if (findPlayerOrNull(player.getId()) != null) {
            throw new GameRuleException("Player is already at the table");
        }
        players.add(player);
    }

    public void start() {
        requirePhase(GamePhase.WAITING_FOR_PLAYERS);
        if (players.size() < MINIMUM_PLAYERS) {
            throw new GameRuleException("At least three players are required");
        }
        for(Player player: players){
            player.dealInitialInfluence(courtDeck.draw());
            player.dealInitialInfluence(courtDeck.draw());
        }
        currentPlayerIndex = 0;
        phase = GamePhase.AWAITING_ACTION;
    }

    public void performAction(String actorId, ActionType actionType, String targetId) {
        requirePhase(GamePhase.AWAITING_ACTION);
        Player actor = requireCurrentPlayer(actorId);
        Objects.requireNonNull(actionType, "Action type is required");

        if (actor.getCoins() >= MANDATORY_COUP_COINS && actionType != ActionType.COUP) {
            throw new GameRuleException("A player with ten or more coins must perform a coup");
        }

        switch (actionType) {
            case INCOME -> resolveIncome(actor);
            case FOREIGN_AID -> openForeignAidBlockWindow(actor);
            case COUP -> startCoup(actor, targetId);
            case TAX -> openTaxChallengeWindow(actor);
        }
    }

    public void blockForeignAid(String blockerId) {
        requirePhase(GamePhase.AWAITING_FOREIGN_AID_BLOCK);
        Player blocker = requireAlivePlayer(blockerId);
        if (blocker.getId().equals(pendingAction.actorId())) {
            throw new GameRuleException("The acting player cannot block their own action");
        }
        finishTurn();
    }

    public void resolveForeignAidWithoutBlock() {
        requirePhase(GamePhase.AWAITING_FOREIGN_AID_BLOCK);
        Player actor = requireAlivePlayer(pendingAction.actorId());
        actor.receiveCoins(2);
        finishTurn();
    }

    public void chooseInfluenceToLose(String playerId, int influenceIndex) {
        requirePhase(GamePhase.AWAITING_INFLUENCE_LOSS);
        if (!pendingAction.targetId().equals(playerId)) {
            throw new GameRuleException("Only the targeted player can lose influence");
        }

        Player target = requireAlivePlayer(playerId);
        target.loseInfluence(influenceIndex);

        List<Player> alivePlayers = alivePlayers();
        if (alivePlayers.size() == 1) {
            phase = GamePhase.GAME_OVER;
            winnerId = alivePlayers.getFirst().getId();
            pendingAction = null;
            return;
        }
        finishTurn();
    }

    public String getId() {
        return id;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getCurrentPlayer() {
        if (phase == GamePhase.WAITING_FOR_PLAYERS || phase == GamePhase.GAME_OVER) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public PendingAction getPendingAction() {
        return pendingAction;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public int getRemainingCards(){
        return courtDeck.getRemainingCards();
    }

    private void resolveIncome(Player actor) {
        actor.receiveCoins(1);
        finishTurn();
    }

    private void openForeignAidBlockWindow(Player actor) {
        pendingAction = new PendingAction(ActionType.FOREIGN_AID, actor.getId(), null);
        phase = GamePhase.AWAITING_FOREIGN_AID_BLOCK;
    }

    private void openTaxChallengeWindow(Player actor){
        pendingAction = new PendingAction(ActionType.TAX, actor.getId(), null);
        phase = GamePhase.AWAITING_ACTION_CHALLENGE;
    }

    public void resolveTaxWithoutChallenge(){
        requirePhase(GamePhase.AWAITING_ACTION_CHALLENGE);
        Player actor = requireAlivePlayer(pendingAction.actorId());
        actor.receiveCoins(TAX_COINS);
        finishTurn();
    }

    private void startCoup(Player actor, String targetId) {
        Player target = requireAlivePlayer(targetId);
        if (actor.getId().equals(target.getId())) {
            throw new GameRuleException("A player cannot target themselves");
        }
        actor.payCoins(COUP_COST);
        pendingAction = new PendingAction(ActionType.COUP, actor.getId(), target.getId());
        phase = GamePhase.AWAITING_INFLUENCE_LOSS;
    }

    private void finishTurn() {
        pendingAction = null;
        moveToNextAlivePlayer();
        phase = GamePhase.AWAITING_ACTION;
    }

    private void moveToNextAlivePlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isAlive());
    }

    private Player requireCurrentPlayer(String playerId) {
        Player player = requireAlivePlayer(playerId);
        if (!player.getId().equals(getCurrentPlayer().getId())) {
            throw new GameRuleException("It is not this player's turn");
        }
        return player;
    }

    private Player requireAlivePlayer(String playerId) {
        Player player = findPlayerOrNull(playerId);
        if (player == null) {
            throw new GameRuleException("Player is not at the table");
        }
        if (!player.isAlive()) {
            throw new GameRuleException("Player has been eliminated");
        }
        return player;
    }

    private Player findPlayerOrNull(String playerId) {
        return players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    private List<Player> alivePlayers() {
        return players.stream()
                .filter(Player::isAlive)
                .toList();
    }

    private void requirePhase(GamePhase expectedPhase) {
        if (phase != expectedPhase) {
            throw new GameRuleException(
                    "Expected phase " + expectedPhase + ", but current phase is " + phase
            );
        }
    }
}
