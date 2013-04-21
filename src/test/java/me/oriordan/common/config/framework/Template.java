package me.oriordan.common.config.framework;

public class Template {

  protected String name;

  protected String testcase;

  protected String soapUIPrjFile;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTestcase() {
    return testcase;
  }

  public void setTestcase(String testcase) {
    this.testcase = testcase;
  }

  public String getSoapUIPrjFile() {
    return soapUIPrjFile;
  }

  public void setSoapUIPrjFile(String soapUIPrjFile) {
    this.soapUIPrjFile = soapUIPrjFile;
  }
}
