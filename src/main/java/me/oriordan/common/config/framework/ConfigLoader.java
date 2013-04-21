package me.oriordan.common.config.framework;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.naming.InitialContext;

/**
 * Description. This loads the config.
 * 
 * @author tnxo
 * @param <T> The config class you want this to give back to you.
 * 
 */
public final class ConfigLoader<T> {

  public static final String JNDI_NAME = "config/ConfigPath";

  public static final String PROPERTY_KEY = "ORIORDAN_CONFIG";

  public static final String SUFFIX = "-config.xml";

  private final T configObject;

  private final Class<T> clazz;

  private final ConfigParser<T> configParser;

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

  /**
   * 
   * @param clazzName
   * @throws ConfigurationException
   */
  public ConfigLoader(String clazzName) throws ConfigurationException {
    this(clazzName, new ConfigAlias[] {});
  }

  /**
   * 
   * @param clazzName
   * @throws ConfigurationException
   */
  public ConfigLoader(Class clazz) throws ConfigurationException {
    this(clazz.getName(), new ConfigAlias[] {});
  }

  /**
   * 
   * @param clazzName
   * @throws ConfigurationException
   */
  public ConfigLoader(Class clazz, ConfigAlias... aliases) throws ConfigurationException {
    this(clazz.getName(), aliases);
  }

  /**
   * 
   * Well, this is the config loader.
   * 
   * Please see this package for unit tests that show how it's used.
   * 
   * @param clazzName
   * @throws ConfigurationException
   */
  public ConfigLoader(String clazzName, ConfigAlias... aliases) throws ConfigurationException {
    try {
      this.clazz = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(clazzName);
    }
    catch (ClassNotFoundException cnfe) {
      throw new ConfigurationException("Could not find class to instantiate.", cnfe);
    }

    try {
      checkforPrimitives();

      configParser = new ConfigParser<T>(this.clazz, aliases);

      T baseConfig = getClasspathResource();
      T jndiResource = getJNDIResource(translateClassNameToFilename(this.clazz));
      T tmp1 = getEnvironmentResource(1);
      T tmp2 = getEnvironmentResource(2);
      T tmp3 = getEnvironmentResource(3);

      baseConfig = combine(baseConfig, jndiResource);
      baseConfig = combine(baseConfig, tmp1);
      baseConfig = combine(baseConfig, tmp2);
      baseConfig = combine(baseConfig, tmp3);

      logResults(baseConfig, jndiResource, tmp1, tmp2, tmp3);

      configObject = baseConfig;
    }
    catch (Exception e) {
      LOG.error("configuration exception", e);
      throw new ConfigurationException("Invalid configuration initialisation", e);
    }
  }

  private void checkforPrimitives() throws IntrospectionException, ConfigurationException {
    BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
    for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
      if (descriptor.getPropertyType().isPrimitive()) {
        throw new ConfigurationException("Cannot use primitive types as they have default values inferred, please use wrappers. This one was: " + descriptor.getName());
      }
    }
  }

  private void logResults(T baseConfig, T jndiResource, T tmp1, T tmp2, T tmp3) {
    LOG.warn("####################################");
    LOG.warn("##Building config for: " + this.clazz.getName());
    if (baseConfig != null) {
      LOG.warn("## Built config from classpath.");
    }
    if (jndiResource != null) {
      LOG.warn("## Built config from JNDI.");
    }
    if (tmp1 != null) {
      LOG.warn("## Built config from " + (PROPERTY_KEY + 1) + ", " + System.getProperty(PROPERTY_KEY + 1));
    }
    if (tmp2 != null) {
      LOG.warn("## Built config from " + (PROPERTY_KEY + 2) + ", " + System.getProperty(PROPERTY_KEY + 2));
    }
    if (tmp3 != null) {
      LOG.warn("## Built config from " + (PROPERTY_KEY + 3) + ", " + System.getProperty(PROPERTY_KEY + 3));
    }
    LOG.warn("## Final configuration is: " + (ToStringBuilder.reflectionToString(baseConfig)));
    LOG.warn("####################################");
  }

  public String translateClassNameToFilename(Class<T> clazz) throws ConfigurationException {
    return translateClassName(clazz) + SUFFIX;
  }

  public String translateClassName(Class<T> clazz) throws ConfigurationException {
    String smallName = clazz.getSimpleName();
    if (smallName.endsWith("Config")) {
      return (smallName.substring(0, smallName.length() - 6)).toLowerCase();
    }
    else {
      return smallName.toLowerCase();
    }
  }

  T combine(T destination, T target) throws Exception {
    if (target == null) {
      return destination;
    }

    BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

    // Iterate over all the attributes
    for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

      // We cannot handle primitives, and we need things to be null if not
      // defined.

      // Only copy writable attributes
      if (descriptor.getWriteMethod() != null) {
        Object originalValue = descriptor.getReadMethod().invoke(target);

        // Only copy values values where the destination values is null
        if (originalValue == null) {
          Object defaultValue = descriptor.getReadMethod().invoke(destination);
          descriptor.getWriteMethod().invoke(target, defaultValue);
        }
      }
    }
    return target;
  }

  public T getConfigObject() {
    return configObject;
  }

  protected T getJNDIResource(String filename) {
    try {
      InitialContext iniCtx = new InitialContext();
      URL url = (URL) iniCtx.lookup(JNDI_NAME);
      if (url != null) {
        return configParser.parse(new File(url.getFile(), filename));
      }
    }
    catch (Exception e) {
      LOG.warn("Could not read JNDI environments file.");
      LOG.trace("Could not read JNDI environments file.", e);
    }
    return null;
  }

  protected T getEnvironmentResource(int i) throws ConfigurationException {
    String property = System.getProperty(PROPERTY_KEY + i);
    if (property == null) {
      return null;
    }
    File directory = new File(property);
    if (directory.exists() && directory.isDirectory()) {
      // Find the config file in here.
      File configFile = new File(directory, translateClassNameToFilename(clazz));
      if (configFile.exists()) {
        try {
          return configParser.parse(configFile);
        }
        catch (Exception e) {
          throw new ConfigurationException("Invalid configuration initialisation", e);
        }
      }
      else {
        return null;
      }
    }
    else {
      throw new ConfigurationException("Invalid configuration initialisation, environment variable could not resolve to directory: " + directory.getAbsolutePath());
    }
  }

  protected T getClasspathResource() throws ConfigurationException {
    InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(translateClassNameToFilename(clazz));
    if (resource == null) {
      throw new ConfigurationException("Invalid configuration initialisation, config file not found: " + translateClassNameToFilename(clazz));
    }
    try {
      return configParser.parse(resource);
    }
    catch (Exception e) {
      throw new ConfigurationException("Invalid configuration initialisation", e);
    }
  }
}
