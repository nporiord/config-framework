package me.oriordan.common.config.framework;

import java.util.ArrayList;
import java.util.List;

public class Product {

  protected String name;

  protected List<Template> templates;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Template> getTemplates() {
    return templates;
  }

  public void setTemplates(List<Template> templates) {
    this.templates = templates;
  }

  public void addTemplate(Template template) {
    if (this.templates == null) {
      this.templates = new ArrayList<Template>();
    }
    this.templates.add(template);
  }

}
