package me.oriordan.common.config.framework;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import me.oriordan.common.config.framework.ConfigParser;
import me.oriordan.common.config.framework.ConfigurationException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigParserTest {

  ConfigParser<TestConfig> parser;

  private Map<String, String> niallMap;

  @Test
  public void test() throws ConfigurationException {
    parser = new ConfigParser<TestConfig>(TestConfig.class);
    InputStream resource = getClass().getClassLoader().getResourceAsStream("test-config.xml");
    assertNotNull(resource);
    parser.parse(resource);

  }

  @Test
  public void testOutput() throws ConfigurationException {
    TestConfig config = new TestConfig();
    niallMap = new HashMap<String, String>();
    niallMap.put("key", "value");
    config.setAlso("also");
    config.setAnother("another");
    config.setNiallMap(niallMap);
    config.setTmp("tmp");

    parser = new ConfigParser<TestConfig>(TestConfig.class);
    InputStream resource = getClass().getClassLoader().getResourceAsStream("test-config.xml");
    assertNotNull(resource);
  }
}
