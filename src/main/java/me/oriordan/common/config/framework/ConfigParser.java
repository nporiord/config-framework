package me.oriordan.common.config.framework;

import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.InputStream;

/**
 * 
 * Parses the config files..
 * 
 * @author tnxo
 * @param <T> Eh, the class you want this to give back to you?
 * 
 */
public class ConfigParser<T> {

  private XStream xStream;

  public ConfigParser(Class<T> clazz, ConfigAlias... aliases) throws ConfigurationException {
    xStream = new XStream();
    xStream.alias(translateClassName(clazz), clazz);
    for (ConfigAlias alias : aliases) {
      xStream.alias(alias.getAlias(), alias.getAliasClass());
    }
  }

  public T parse(File file) {
    return (T) xStream.fromXML(file);
  }

  public T parse(InputStream resource) {
    return (T) xStream.fromXML(resource);
  }

  public final String translateClassName(Class<T> clazz) throws ConfigurationException {
    String smallName = clazz.getSimpleName();
    if (smallName.endsWith("Config")) {
      return (smallName.substring(0, smallName.length() - 6)).toLowerCase();
    }
    else {
      return smallName.toLowerCase();
    }
  }
}
