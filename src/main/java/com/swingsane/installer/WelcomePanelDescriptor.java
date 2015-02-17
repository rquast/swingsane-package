package com.swingsane.installer;

import com.swingsane.wizard.WizardPanelDescriptor;

public class WelcomePanelDescriptor extends WizardPanelDescriptor {

  public static final String IDENTIFIER = "INTRODUCTION_PANEL";

  public WelcomePanelDescriptor() {
    super(WelcomePanelDescriptor.IDENTIFIER, new WelcomePanel());
  }

  @Override
  public Object getNextPanelDescriptor() {
    return LicenseAgreementPanelDescriptor.IDENTIFIER;
  }

  @Override
  public Object getBackPanelDescriptor() {
    return null;
  }

}
