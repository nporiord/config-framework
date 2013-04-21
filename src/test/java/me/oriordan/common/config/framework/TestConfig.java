package me.oriordan.common.config.framework;

import java.util.List;
import java.util.Map;

public class TestConfig {

  
  private String tmp;
  private String another;
  private String also;

  private Map<String, String> niallMap;
  private List<String> niallList;
  
  public String getTmp() {
    return tmp;
  }
  
  public void setTmp(String tmp) {
    this.tmp = tmp;
  }
  
  public String getAnother() {
    return another;
  }
  
  public void setAnother(String another) {
    this.another = another;
  }

  public String getAlso() {
    return also;
  }

  public void setAlso(String also) {
    this.also = also;
  }

  public Map<String, String> getNiallMap() {
    return niallMap;
  }
  
  public void setNiallMap(Map<String, String> niallMap) {
    this.niallMap = niallMap;
  }

  public List<String> getNiallList() {
    return niallList;
  }

  public void setNiallList(List<String> niallList) {
    this.niallList = niallList;
  }
}
