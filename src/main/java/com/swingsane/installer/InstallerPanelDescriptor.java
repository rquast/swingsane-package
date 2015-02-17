package com.swingsane.installer;

import com.swingsane.wizard.WizardPanelDescriptor;

public class InstallerPanelDescriptor extends WizardPanelDescriptor {

  public static final String IDENTIFIER = "INSTALL_FILES_PANEL";

  InstallerPanel installerPanel;

  public InstallerPanelDescriptor() {

    installerPanel = new InstallerPanel();
    setPanelDescriptorIdentifier(InstallerPanelDescriptor.IDENTIFIER);
    setPanelComponent(installerPanel);

  }

  @Override
  public Object getNextPanelDescriptor() {
    return WizardPanelDescriptor.FINISH;
  }

  @Override
  public Object getBackPanelDescriptor() {
    return SetupOptionsPanelDescriptor.IDENTIFIER;
  }

  @Override
  public void aboutToDisplayPanel() {

    installerPanel.setProgressValue(0);
    installerPanel.setProgressText("Preparing for file extraction ...");

    getWizard().setNextFinishButtonEnabled(false);
    getWizard().setBackButtonEnabled(false);
    getWizard().setCancelButtonEnabled(false);

  }

  @Override
  public void displayingPanel() {

    Thread t = new Thread() {

      @Override
      public void run() {

        Main mainInstance = Main.getInstance();

        try {

          mainInstance.extract(mainInstance.jarFileName, installerPanel, mainInstance.outputDir);

          getWizard().setNextFinishButtonEnabled(true);
          installerPanel.setLaunchCheckBoxEnabled(true);
          installerPanel.setShortcutCheckBoxEnabled(true);

          installerPanel.setProgressValue(100);
          installerPanel.setProgressStatus("Installation Complete.");
          installerPanel.setProgressText("");
          mainInstance.launchCheckBox = installerPanel.getLaunchCheckBox();

        } catch (Exception e) {

          installerPanel.setProgressValue(0);
          installerPanel.setProgressStatus("An Error Has Occurred.");
          installerPanel.setProgressText("");
          System.out.println(e);
          e.printStackTrace();
          getWizard().setBackButtonEnabled(true);
          
        }

      }
    };

    t.start();
  }

}
