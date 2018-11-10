package com.syswin.temail.gateway.codec;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.GROUP_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import com.syswin.temail.gateway.TemailGatewayProperties;
import java.util.Random;
import org.junit.Test;

public class CommandAwarePredicateTest {

  private final TemailGatewayProperties properties = new TemailGatewayProperties();
  private final CommandAwarePredicate predicate = new CommandAwarePredicate(properties);

  @Test
  public void trueIfPrivateMessage() {
    assertThat(predicate.check(SINGLE_MESSAGE_CODE, (short) 1)).isTrue();
  }

  @Test
  public void trueIfGroupMessage() {
    properties.setGroupPacketEnabled(true);
    assertThat(predicate.check(GROUP_MESSAGE_CODE, (short) 1)).isTrue();
  }

  @Test
  public void falseIfGroupMessageToggledOff() {
    properties.setGroupPacketEnabled(false);
    assertThat(predicate.check(GROUP_MESSAGE_CODE, (short) 1)).isFalse();
  }

  @Test
  public void falseIfAnyOtherPrivateMessage() {
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != 1) {
        assertThat(predicate.check(SINGLE_MESSAGE_CODE, i)).isFalse();
      }
    }
  }

  @Test
  public void falseIfAnyOtherGroupMessage() {
    properties.setGroupPacketEnabled(true);
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != 1) {
        assertThat(predicate.check(GROUP_MESSAGE_CODE, i)).isFalse();
      }
    }
  }

  @Test
  public void falseIfAnythingElse() {
    final Random random = new Random();
    properties.setGroupPacketEnabled(true);
    for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
      if (i != SINGLE_MESSAGE_CODE && i != GROUP_MESSAGE_CODE) {
        assertThat(predicate.check(i, (short) random.nextInt())).isFalse();
      }
    }
  }
}
