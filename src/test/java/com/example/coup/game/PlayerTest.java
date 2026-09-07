package com.example.coup.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class PlayerTest {
    Player player;
    @BeforeEach
    void setUp(){
        player = new Player("1", "test");
    }

    @Test
    void startsWithoutInfluences(){

        assertThat(player.getAliveInfluences()).isEmpty();
        assertThat(player.getDeadInfluences()).isEmpty();
        assertThat(player.getRemainingInfluences()).isEqualTo(0);
    }

    @Test
    void canReceiveNotRevealedInfluences(){
        player.dealInitialInfluence(new Influence(CharacterType.DUKE));
        player.dealInitialInfluence(new Influence(CharacterType.CAPTAIN));

        assertThat(player.getRemainingInfluences()).isEqualTo(2);
        assertThat(player.getDeadInfluences()).isEmpty();
        assertThat(player.getAliveInfluences()).size().isEqualTo(2);
        assertThat(player.isAlive()).isTrue();
    }

    @Test
    void canNotReceiveMoreThanTwoInfluences(){
        player.dealInitialInfluence(new Influence(CharacterType.DUKE));
        player.dealInitialInfluence(new Influence(CharacterType.CAPTAIN));

        assertThatThrownBy(() -> player.dealInitialInfluence(new Influence(CharacterType.DUKE)))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Player can not hold more than two cards");
    }

    @Test
    void doesNotAcceptRevealedInfluencesAsAliveInfluence(){
        Influence influence = new Influence(CharacterType.DUKE);
        influence.reveal();
        assertThatThrownBy(() -> player.dealInitialInfluence(influence))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Influence is already revealed");
    }

    @Test
    void influencesCannotBeAltered(){
        player.dealInitialInfluence(new Influence(CharacterType.DUKE));
        player.dealInitialInfluence(new Influence(CharacterType.CONTESSA));

        assertThatThrownBy(() -> player.getAliveInfluences().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void loseAnInfluenceMovesItToDeadInfluencesAndRevealsIt(){
        player.dealInitialInfluence(new Influence(CharacterType.DUKE));
        Influence contessa = new Influence(CharacterType.CONTESSA);
        player.dealInitialInfluence(contessa);
        player.loseInfluence(1);


        assertThat(player.getAliveInfluences().size()).isEqualTo(1);
        assertThat(player.getDeadInfluences().size()).isEqualTo(1);
        assertThat(player.getRemainingInfluences()).isEqualTo(1);
        assertThat(player.isAlive()).isTrue();
        assertThat(player.getDeadInfluences().getFirst()).isSameAs(contessa);
        assertThat(player.getDeadInfluences().getFirst().isRevealed()).isTrue();
    }
    @Test
    void loseTwoCardsMustKillThePlayer(){
        player.dealInitialInfluence(new Influence(CharacterType.ASSASSIN));
        player.dealInitialInfluence(new Influence(CharacterType.AMBASSADOR));

        player.loseInfluence(1);

        assertThat(player.getRemainingInfluences()).isEqualTo(1);

        player.loseInfluence(0);

        assertThat(player.getAliveInfluences()).isEmpty();
        assertThat(player.getDeadInfluences()).hasSize(2);
        assertThat(player.getRemainingInfluences()).isEqualTo(0);
        assertThat(player.isAlive()).isFalse();
    }

    @Test
    void invalidIndexIsNotAccepted(){
        player.dealInitialInfluence(new Influence(CharacterType.AMBASSADOR));
        player.dealInitialInfluence(new Influence(CharacterType.CONTESSA));

        assertThatThrownBy(() -> player.loseInfluence(3))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Invalid influence index");
    }

    @Test
    void canNotReceiveTheFirstHandAfterLosingAnInfluence(){
        player.dealInitialInfluence(new Influence(CharacterType.CONTESSA));

        player.loseInfluence(0);

        assertThatThrownBy(()-> player.dealInitialInfluence(new Influence(CharacterType.DUKE)))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Cannot deal an initial influence after the game has started");
    }

    @Test
    void canReplaceAnActiveInfluence(){
        Influence influenceToReplace = new Influence(CharacterType.CONTESSA);
        player.dealInitialInfluence(influenceToReplace);
        Influence influenceUntouched = new Influence(CharacterType.CONTESSA);
        player.dealInitialInfluence(influenceUntouched);

        int qtdBefore = player.getRemainingInfluences();
        Influence replacement = new Influence(CharacterType.CAPTAIN);

        player.replaceInfluence(influenceToReplace, replacement);

        assertThat(player.getRemainingInfluences()).isEqualTo(qtdBefore);
        assertThat(player.getAliveInfluences()).doesNotContain(influenceToReplace);
        assertThat(player.getAliveInfluences()).contains(influenceUntouched, replacement);
        assertThat(player.getDeadInfluences()).isEmpty();
        assertThat(influenceToReplace.isRevealed()).isFalse();
        assertThat(replacement.isRevealed()).isFalse();
        assertThat(influenceUntouched.isRevealed()).isFalse();
    }

    @Test
    void cannotReplaceWithLostInfluence() {
        Influence influenceToReplace =
                new Influence(CharacterType.DUKE);
        Influence untouchedInfluence =
                new Influence(CharacterType.CAPTAIN);

        player.dealInitialInfluence(influenceToReplace);
        player.dealInitialInfluence(untouchedInfluence);

        List<Influence> handBeforeReplacement =
                player.getAliveInfluences();

        Influence lostReplacement =
                new Influence(CharacterType.CONTESSA);
        lostReplacement.reveal();

        assertThatThrownBy(() ->
                player.replaceInfluence(
                        influenceToReplace,
                        lostReplacement
                )
        )
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Replacement influence cannot be lost");

        assertThat(player.getAliveInfluences())
                .containsExactlyElementsOf(handBeforeReplacement);
    }

    @Test
    void cannotReplaceInfluenceThatIsNotActive(){
        Influence thirdInfluence = new  Influence(CharacterType.CONTESSA);
        Influence firstCard = new Influence(CharacterType.AMBASSADOR);
        Influence secondCard = new Influence(CharacterType.CONTESSA);
        player.dealInitialInfluence(firstCard);
        player.dealInitialInfluence(secondCard);
        int qtdBefore = player.getRemainingInfluences();

        Influence replacement = new Influence(CharacterType.ASSASSIN);
        assertThatThrownBy(()-> player.replaceInfluence(thirdInfluence, replacement))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Influence is not active");
        assertThat(player.getAliveInfluences()).doesNotContain(thirdInfluence);
        assertThat(player.getAliveInfluences()).contains(firstCard, secondCard);
        assertThat(player.getDeadInfluences()).isEmpty();
        assertThat(player.getRemainingInfluences()).isEqualTo(qtdBefore);
    }

    @Test
    void cannotReplaceNullInfluence(){
        Influence firstCard = new Influence(CharacterType.CONTESSA);
        Influence secondCard = new Influence(CharacterType.AMBASSADOR);
        player.dealInitialInfluence(firstCard);
        player.dealInitialInfluence(secondCard);
        List<Influence> handbefore = player.getAliveInfluences();

        Influence replacement = new Influence(CharacterType.CONTESSA);

        assertThatThrownBy(()-> player.replaceInfluence(null, replacement))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Influence to replace is required");
        assertThat(player.getAliveInfluences()).doesNotContain(replacement);
        assertThat(player.getAliveInfluences()).containsExactlyElementsOf(handbefore);
        assertThat(player.getDeadInfluences()).isEmpty();
    }

    @Test
    void cannotUseNullInfluenceAsReplacement(){
        Influence firstCard = new Influence(CharacterType.CONTESSA);
        Influence secondCard = new Influence(CharacterType.AMBASSADOR);
        player.dealInitialInfluence(firstCard);
        player.dealInitialInfluence(secondCard);
        List<Influence> handbefore = player.getAliveInfluences();


        assertThatThrownBy(()-> player.replaceInfluence(firstCard, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Replacement influence is required");
        assertThat(player.getAliveInfluences()).containsExactlyElementsOf(handbefore);
        assertThat(player.getDeadInfluences()).isEmpty();
    }
}
