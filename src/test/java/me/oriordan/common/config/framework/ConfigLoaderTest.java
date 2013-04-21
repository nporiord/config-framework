package me.oriordan.common.config.framework;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static me.oriordan.common.config.framework.ConfigLoader.JNDI_NAME;
import static me.oriordan.common.config.framework.ConfigLoader.PROPERTY_KEY;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import me.oriordan.common.config.framework.ConfigAlias;
import me.oriordan.common.config.framework.ConfigLoader;
import me.oriordan.common.config.framework.ConfigurationException;

import java.io.File;
import java.net.ContentHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testTypedSpring.xml"})
public class ConfigLoaderTest {

  private ConfigLoader<TestConfig> loader;

  @Autowired
  public TestConfig config;

  private ConfigAlias alias = new ConfigAlias("tmp", Object.class);

  @Before
  @DirtiesContext
  public void before() {
    clearProperties();
    disableInitialContext();
  }

  @Test
  public void testException1() {
    try {
      new ConfigLoader<TestConfig>("somethingstupid");
    }
    catch (ConfigurationException e) {
      return;
    }
    fail("Expected an exception in this case.");
  }

  @Test
  public void testException2() {
    try {
      new ConfigLoader<ContentHandler>(ContentHandler.class.getName());
    }
    catch (ConfigurationException e) {
      return;
    }
    fail("Expected an exception in this case.");
  }

  @Test
  @DirtiesContext
  public void test1() throws ConfigurationException {
    assertProperties(Arrays.asList(new Object[]{""}) );
    assertNotNull(config);
    assertNotNull(config.getNiallMap());
    assertNotNull(config.getNiallList());
  }

  private void assertProperties(List<Object> matchingSet) {
    Properties properties = System.getProperties();
    Enumeration<Object> keys = properties.keys();
    while(keys.hasMoreElements()) {
      Object key = keys.nextElement(); 
      if(key.toString().startsWith(PROPERTY_KEY))
        assertTrue(key.toString(), matchingSet.contains(key));
    }
  }

  @Test
  @DirtiesContext
  public void testExternal1() throws ConfigurationException {
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "test1");
    assertProperties(Arrays.asList(new Object[]{PROPERTY_KEY + "1"}) );
    loader = new ConfigLoader<TestConfig>(TestConfig.class.getName());
    assertConfig("1. This is the external value of tmp.", "1. This is the external value of another.", null);
  }
  
  @Test
  @DirtiesContext
  public void testConstructor1() throws ConfigurationException {
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "test1");
    assertProperties(Arrays.asList(new Object[]{PROPERTY_KEY + "1"}) );
    loader = new ConfigLoader<TestConfig>(TestConfig.class);
    assertConfig("1. This is the external value of tmp.", "1. This is the external value of another.", null);
  }
  
  @Test
  @DirtiesContext
  public void testConstructor2() throws ConfigurationException {
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "test1");
    assertProperties(Arrays.asList(new Object[]{PROPERTY_KEY + "1"}) );
    loader = new ConfigLoader<TestConfig>(TestConfig.class, new ConfigAlias[]{alias });
    assertConfig("1. This is the external value of tmp.", "1. This is the external value of another.", null);
  }

  @Test
  @DirtiesContext
  public void testExternal2() throws ConfigurationException {
    System.setProperty(PROPERTY_KEY + "2", "." + File.separator + "other" + File.separator + "test2");
    assertProperties(Arrays.asList(new Object[]{PROPERTY_KEY + "2"}) );
    loader = new ConfigLoader<TestConfig>(TestConfig.class.getName());
    assertConfig("2. This is the external value of tmp.", "This is the value of another.", null);
  }

  @Test
  @DirtiesContext
  public void testExternal3() throws ConfigurationException {
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "test1");
    System.setProperty(PROPERTY_KEY + "2", "." + File.separator + "other" + File.separator + "test2");
    System.setProperty(PROPERTY_KEY + "3", "." + File.separator + "other" + File.separator + "test3");
    assertProperties(Arrays.asList(new Object[]{PROPERTY_KEY + "1", PROPERTY_KEY + "2", PROPERTY_KEY + "3"}) );
    loader = new ConfigLoader<TestConfig>(TestConfig.class.getName());
    assertConfig("2. This is the external value of tmp.", "1. This is the external value of another.", "3. This is the external value of also.");
  }

  @Test
  @DirtiesContext
  public void testJNDI() throws ConfigurationException, NamingException, MalformedURLException {
    enableInitialContext();

    InitialContext initialContext = new InitialContext();
    initialContext.bind(JNDI_NAME, new URL(new URL("file:"), "./other/jndi/"));

    assertProperties(Arrays.asList(new Object[]{PROPERTY_KEY + "1", PROPERTY_KEY + "2", PROPERTY_KEY + "3"}) );
    loader = new ConfigLoader<TestConfig>(TestConfig.class.getName());
    assertConfig("JNDI TMP", "JNDI ANOTHER", null);

    disableInitialContext();
  }

  private void assertConfig(String tmp, String another, String also) {
    TestConfig config = loader.getConfigObject();
    assertEquals(tmp, config.getTmp());
    assertEquals(another, config.getAnother());
    assertEquals(also, config.getAlso());
  }

  @Test
  public void testEnvironmentResource() {
    ConfigLoader<TestConfig> loader2;
    try {
      loader2 = new ConfigLoader<TestConfig>(TestConfig.class.getName());
      assertNull(loader2.getEnvironmentResource(9));
    }
    catch (ConfigurationException e) {
      fail("Did not expect this exception.");
    }

    //file does not exist
    clearProperties();
    System.setProperty(PROPERTY_KEY + "1", "somestupidfilethatdoesnotexist.");
    try {
      loader2 = new ConfigLoader<TestConfig>(TestConfig.class.getName());
      assertNull(loader2.getEnvironmentResource(1));
      fail("Expected to throw an exception in this instance.");
    }
    catch (ConfigurationException e) {
    }

    //exists but is not directory
    clearProperties();
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "test1" + File.separator + "test-config.xml");
    try {
      loader2 = new ConfigLoader<TestConfig>(TestConfig.class.getName());
      assertNull(loader2.getEnvironmentResource(1));
      fail("Expected an exception in this scenario.");
    }
    catch (ConfigurationException e) {
    }

    //directory exists but file doesn't
    clearProperties();
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "nofiles");
    try {
      loader2 = new ConfigLoader<TestConfig>(TestConfig.class.getName());
      assertNull(loader2.getEnvironmentResource(1));
    }
    catch (ConfigurationException e) {
      fail("Did not expect this exception.");
    }

    //parser fails
    clearProperties();
    System.setProperty(PROPERTY_KEY + "1", "." + File.separator + "other" + File.separator + "parserfail");
    try {
      loader2 = new ConfigLoader<TestConfig>(TestConfig.class.getName());
      fail("Did not expect this exception.");
      assertNull(loader2.getEnvironmentResource(1));
      fail("Expected an exception in this scenario.");
    }
    catch (ConfigurationException e) {
    }
  }

  @Test
  public void testGetClasspathResourceParserFail() {
    ConfigLoader<BrokenConfig> loader2;
    try {
      loader2 = new ConfigLoader<BrokenConfig>(BrokenConfig.class.getName());
      assertNull(loader2.getClasspathResource());
      fail("Expected an exception in this scenario.");
    }
    catch (ConfigurationException e) {
    }
  }
  
  @Test
  public void testCombine() throws Exception {

    TestConfig destination = new TestConfig();
    destination.setAlso("destinationAlso");
    destination.setAnother("destinationAnother");

    TestConfig target = new TestConfig();
    target.setAlso("targetAlso");
    target.setAnother("targetAnother");
    target.setTmp("tmp");

    ConfigLoader<TestConfig> loader2 = new ConfigLoader<TestConfig>(TestConfig.class.getName());
    TestConfig combined = loader2.combine(destination, target);

    assertEquals("targetAlso", combined.getAlso());
    assertEquals("targetAnother", combined.getAnother());
    assertEquals("tmp", combined.getTmp());

  }

  private void disableInitialContext() {
    System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
  }

  private void enableInitialContext() {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestContextFactory.class.getName());
  }

  private void clearProperties() {
    System.clearProperty(PROPERTY_KEY + "1");
    System.clearProperty(PROPERTY_KEY + "2");
    System.clearProperty(PROPERTY_KEY + "3");
    System.clearProperty(PROPERTY_KEY + "4");
    System.clearProperty(PROPERTY_KEY + "5");
  }
}
