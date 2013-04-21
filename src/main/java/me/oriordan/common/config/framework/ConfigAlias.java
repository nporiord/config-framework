package me.oriordan.common.config.framework;

/**
 * If you want to use a class inside your config file, create an alias to it using one of these.
 *
 * @author tnxo
 *
 */
public class ConfigAlias {

  private final String alias;
  private final Class aliasClass;
  
  /**
   * 
   * @param alias
   * @param aliasClass
   */
  public ConfigAlias(String alias, Class aliasClass) {
    this.alias = alias;
    this.aliasClass = aliasClass;
  }

  /**
   * 
   * @return
   */
  public String getAlias() {
    return alias;
  }

  /**
   * 
   * @return
   */
  public Class getAliasClass() {
    return aliasClass;
  }
}
