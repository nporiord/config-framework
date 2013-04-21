package me.oriordan.common.config.framework;

import static me.oriordan.common.config.framework.ConfigLoader.PROPERTY_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import me.oriordan.common.config.framework.ConfigLoader;
import me.oriordan.common.config.framework.ConfigurationException;

import javax.naming.InitialContext;


public class TestConfigTest {
  
  @Before
  public void before() {
    System.clearProperty(InitialContext.INITIAL_CONTEXT_FACTORY);
    System.clearProperty(PROPERTY_KEY + "1");
    System.clearProperty(PROPERTY_KEY + "2");
    System.clearProperty(PROPERTY_KEY + "3");
    System.clearProperty(PROPERTY_KEY + "4");
    System.clearProperty(PROPERTY_KEY + "5");    
  }
  
  @Test
  public void testConfig()  throws ConfigurationException {
    ConfigLoader<TestConfig> loader = new ConfigLoader<TestConfig>(TestConfig.class.getName());
    TestConfig configObject = loader.getConfigObject();
    assertNotNull(configObject);
    assertNull(configObject.getAlso());
    assertEquals("This is the value of another.", configObject.getAnother());
    assertEquals("This is the value of tmp.", configObject.getTmp());
  }    
}
