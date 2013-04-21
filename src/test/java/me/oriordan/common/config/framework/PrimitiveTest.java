package me.oriordan.common.config.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import me.oriordan.common.config.framework.ConfigLoader;
import me.oriordan.common.config.framework.ConfigurationException;


public class PrimitiveTest {

  @Test
  public void test() throws ConfigurationException {
    try {
      new ConfigLoader<PrimitiveConfig>(PrimitiveConfig.class);
      fail("should have failed.");
    }
    catch(ConfigurationException e) {
      assertEquals("Invalid configuration initialisation", e.getMessage());
      assertEquals("Cannot use primitive types as they have default values inferred, please use wrappers. This one was: number", e.getCause().getMessage());
      
    }
  }
  
}