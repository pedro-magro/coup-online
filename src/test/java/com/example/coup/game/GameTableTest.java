package com.example.coup.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTableTest {

    private GameTable table;
    private Player ana;
    private Player bruno;
    private Player carla;

    @BeforeEach
    void setUp() {
        table = new GameTable("table-1");
        ana = new Player("ana", "Ana");
        bruno = new Player("bruno", "Bruno");
        carla = new Player("carla", "Carla");
        table.join(ana);
        table.join(bruno);
        table.join(carla);
        table.start();
    }

    @Test
    void startDealsTwoInfluencesToEveryPlayer(){
        assertThat(ana.getAliveInfluences()).hasSize(2);
        assertThat(ana.getAliveInfluences()).allMatch(influence -> !influence.isRevealed());
        assertThat(bruno.getAliveInfluences()).hasSize(2);
        assertThat(bruno.getAliveInfluences()).allMatch(influence -> !influence.isRevealed());
        assertThat(carla.getAliveInfluences()).hasSize(2);
        assertThat(carla.getAliveInfluences()).allMatch(influence -> !influence.isRevealed());
    }

    @Test
    void startRemovesDealtInfluencesFromTheCourtDeck(){
        assertThat(table.getRemainingCards()).isEqualTo(9);
    }

    @Test
    void incomeAddsOneCoinAndMovesToTheNextPlayer() {
        table.performAction("ana", ActionType.INCOME, null);

        assertThat(ana.getCoins()).isEqualTo(3);
        assertThat(table.getCurrentPlayer()).isSameAs(bruno);
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_ACTION);
    }

    @Test
    void foreignAidWaitsForAReactionBeforeGivingCoins() {
        table.performAction("ana", ActionType.FOREIGN_AID, null);

        assertThat(ana.getCoins()).isEqualTo(2);
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_FOREIGN_AID_BLOCK);
        assertThat(table.getPendingAction().actorId()).isEqualTo("ana");

        table.resolveForeignAidWithoutBlock();

        assertThat(ana.getCoins()).isEqualTo(4);
        assertThat(table.getCurrentPlayer()).isSameAs(bruno);
    }

    @Test
    void anotherPlayerCanBlockForeignAid() {
        table.performAction("ana", ActionType.FOREIGN_AID, null);

        table.blockForeignAid("carla");

        assertThat(ana.getCoins()).isEqualTo(2);
        assertThat(table.getCurrentPlayer()).isSameAs(bruno);
        assertThat(table.getPendingAction()).isNull();
    }

    @Test
    void coupCostsSevenCoinsAndMakesTheTargetLoseInfluence() {
        playIncomeRounds(5);

        table.performAction("ana", ActionType.COUP, "bruno");

        assertThat(ana.getCoins()).isZero();
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_INFLUENCE_LOSS);

        Influence chosenInfluence = bruno.getAliveInfluences().getLast();

        table.chooseInfluenceToLose("bruno", 1);

        assertThat(bruno.getRemainingInfluences()).isEqualTo(1);
        assertThat(table.getCurrentPlayer()).isSameAs(bruno);
        assertThat(bruno.getDeadInfluences().getFirst()).isSameAs(chosenInfluence);
        assertThat(chosenInfluence.isRevealed()).isTrue();
    }

    @Test
    void anotherPlayerCannotChooseAnInfluenceToBeLost(){
        playIncomeRounds(5);

        table.performAction("ana", ActionType.COUP, "bruno");

        assertThat(ana.getCoins()).isZero();
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_INFLUENCE_LOSS);

        assertThatThrownBy(()-> table.chooseInfluenceToLose("carla", 1))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Only the targeted player can lose influence");
        assertThat(bruno.getAliveInfluences().size()).isEqualTo(2);
        assertThat(bruno.getDeadInfluences()).isEmpty();
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_INFLUENCE_LOSS);

    }

    @Test
    void rejectsAnActionFromAPlayerWhoseTurnHasNotArrived() {
        assertThatThrownBy(() -> table.performAction("bruno", ActionType.INCOME, null))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("It is not this player's turn");
    }

    @Test
    void playerWithTenCoinsMustPerformACoup() {
        playIncomeRounds(8);

        assertThatThrownBy(() -> table.performAction("ana", ActionType.INCOME, null))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("A player with ten or more coins must perform a coup");
    }

    private void playIncomeRounds(int rounds) {
        for (int round = 0; round < rounds; round++) {
            table.performAction("ana", ActionType.INCOME, null);
            table.performAction("bruno", ActionType.INCOME, null);
            table.performAction("carla", ActionType.INCOME, null);
        }
    }

    @Test
    void taxWaitsForChallengeBeforeGivingCoins(){
        table.performAction("ana", ActionType.TAX, null);

        assertThat(ana.getCoins()).isEqualTo(2);
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_ACTION_CHALLENGE);
        assertThat(table.getPendingAction()).isNotNull();
        assertThat(table.getPendingAction().type()).isEqualTo(ActionType.TAX);
        assertThat(table.getPendingAction().actorId()).isEqualTo("ana");
        assertThat(table.getPendingAction().targetId()).isNull();
    }

    @Test
    void taxGivesThreeCoinsWhenNobodyChallenges(){
        table.performAction("ana", ActionType.TAX, null);
        table.resolveTaxWithoutChallenge();

        assertThat(ana.getCoins()).isEqualTo(5);
        assertThat(table.getPendingAction()).isNull();
        assertThat(table.getPhase()).isEqualTo(GamePhase.AWAITING_ACTION);
        assertThat(table.getCurrentPlayer()).isSameAs(bruno);
    }

    @Test
    void cannotResolveTaxWithoutOpenChallengeWindow(){
        assertThatThrownBy(() -> table.resolveTaxWithoutChallenge())
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Expected phase AWAITING_ACTION_CHALLENGE, but current phase is AWAITING_ACTION");
    }
}
