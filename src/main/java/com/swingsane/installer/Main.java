package com.swingsane.installer;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.swingsane.wizard.Wizard;
import com.swingsane.wizard.WizardPanelDescriptor;

public class Main {

  private static Main instance = null;

  public String jarFileName;

  public File outputDir;

  public JCheckBox launchCheckBox;

  static String APPLICATION_NAME = "SwingSane";

  static String MANIFEST = "META-INF/MANIFEST.MF";

  static String OS_X_APP = "SWINGSANE.APP";

  static String WINDOWS_EXE = "SWINGSANE.EXE";

  static String UNIX_SCRIPT = "SWINGSANE.SH";

  public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase()
      .startsWith("mac os x"));

  public static boolean WINDOWS = (System.getProperty("os.name").toLowerCase()
      .startsWith("windows"));

  public static boolean LINUX = (System.getProperty("os.name").toLowerCase().startsWith("linux"));

  private Wizard wizard;

  private WizardPanelDescriptor welcomeDescriptor;

  private WizardPanelDescriptor licenseAgreementDescriptor;

  private WizardPanelDescriptor setupOptionsDescriptor;

  private WizardPanelDescriptor installerDescriptor;

  private static MultipleInstanceChecker multipleInstanceChecker;

  public Main() {

    Main.instance = this;

    String version = System.getProperty("java.version");
    String[] vparts = version.split("\\.");
    int major = Integer.parseInt(vparts[0]);
    int minor = Integer.parseInt(vparts[1]);

    if (major < 1 || minor < 6) {
      if (MAC_OS_X) {
        String macmsg = "SwingSane requires Mac OS X 10.7 or higher to run.\nIf you wish to use a version that will run with Mac OS X 10.5, download SwingSane version 1.3.1\nfrom the Past Versions download page on the SwingSane website.";
        JOptionPane.showMessageDialog(null, macmsg);
      } else {
        String msg = "SwingSane requires Java 1.7 or higher to be installed on your system for it to run.";
        JOptionPane.showMessageDialog(null, msg);
      }
      System.exit(0);
    }

    if (isInstanceRunning()) {
      JOptionPane.showMessageDialog(null,
          "SwingSane is running. Please exit it first before running this installer.");
      System.exit(0);
    }

    JFrame mainFrame = new JFrame();

    mainFrame.setIconImage((new ImageIcon(getClass().getResource(
        "/com/swingsane/installer/images/swingsane_package_16x16.png"))).getImage());

    Wizard wizard = new Wizard(mainFrame, this);
    wizard.getDialog().setTitle(Main.APPLICATION_NAME + " - Installation Wizard");
    createWizardPanels();

    if (Main.MAC_OS_X && isPrivilegedMode()) {
      wizard.setCurrentPanel(LicenseAgreementPanelDescriptor.IDENTIFIER);
    } else {
      wizard.setCurrentPanel(WelcomePanelDescriptor.IDENTIFIER);
    }
    Dimension dialogSize = new Dimension(600, 400);
    wizard.getDialog().setMinimumSize(dialogSize);
    wizard.getDialog().setPreferredSize(dialogSize);
    wizard.getDialog().pack();
    wizard.getDialog().setLocationRelativeTo(null);
    wizard.showModalDialog();

  }

  private boolean isInstanceRunning() {

    if (WINDOWS || MAC_OS_X) {
      return false;
    }

    final int PORT = 44593;
    final byte[] SIGNATURE = new byte[] { 0x34, 0x55, 0x7c, 0x03, 0x64, 0x22, 0x1e, 0x4a };
    multipleInstanceChecker = new MultipleInstanceChecker(SIGNATURE, PORT);
    try {
      int result = multipleInstanceChecker.check();
      switch (result) {
      case (MultipleInstanceChecker.STATUS_FIRST_INSTANCE): {
        return false;
      }
      case (MultipleInstanceChecker.STATUS_INSTANCE_EXISTS): {
        return true;
      }
      case (MultipleInstanceChecker.STATUS_SECURITY_EXCEPTION): {
        return false;
      }
      }
    } catch (Throwable th) {
      return false;
    }
    return false;
  }

  private void copyStream(OutputStream out, InputStream in) throws IOException {
    byte[] buffer = new byte[1024];
    int bytesRead = 0;
    while ((bytesRead = in.read(buffer)) >= 0) {
      out.write(buffer, 0, bytesRead);
    }
  }

  String getInstallerJar() {
    try {
      URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
      if (!"file".equals(uri.getScheme())) {
        throw new Exception("Unexpected scheme in JAR file URI: " + uri);
      }
      return new File(uri.getSchemeSpecificPart()).getCanonicalPath();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void checkForPrivilegedExecution() {
    if (!Main.MAC_OS_X || isPrivilegedMode()) {
      return;
    } else {
      try {
        relaunchWithElevatedRights();
      } catch (IOException e) {
      } catch (InterruptedException e) {
      }
      System.exit(0);
    }
  }

  public static boolean isPrivilegedMode() {
    return "privileged".equals(System.getenv("frinstaller.mode"))
        || "privileged".equals(System.getProperty("frinstaller.mode"));
  }

  private String getJavaCommand() {
    return new StringBuilder(System.getProperty("java.home")).append(File.separator).append("bin")
        .append(File.separator).append("java").toString();
  }

  public int relaunchWithElevatedRights() throws IOException, InterruptedException {
    String javaCommand = getJavaCommand();
    String installer = getInstallerJar();
    ProcessBuilder builder = new ProcessBuilder(getElevator(javaCommand, installer));
    builder.environment().put("frinstaller.mode", "privileged");
    return builder.start().waitFor();
  }

  private List<String> getElevator(String javaCommand, String installer) throws IOException,
      InterruptedException {
    List<String> elevator = new ArrayList<String>();

    if (Main.MAC_OS_X) {
      elevator.add(extractMacElevator().getCanonicalPath());
      elevator.add(javaCommand);
      elevator.add("-jar");
      elevator.add(installer);
    }

    return elevator;
  }

  private void makeExecutable(String path) throws InterruptedException, IOException {
    new ProcessBuilder("/bin/chmod", "+x", path).start().waitFor();
  }

  private File extractMacElevator() throws IOException, InterruptedException {
    String path = System.getProperty("java.io.tmpdir") + File.separator + "Installer";
    File elevator = new File(path);

    FileOutputStream out = new FileOutputStream(elevator);
    InputStream in = getClass().getResourceAsStream(
        "/com/swingsane/installer/osx/run-with-privileges-on-osx");
    copyStream(out, in);
    in.close();
    out.close();

    makeExecutable(path);

    elevator.deleteOnExit();
    return elevator;
  }

  public static void setInstance(Main mainInstance) {
    Main.instance = mainInstance;
  }

  public static Main getInstance() {
    return Main.instance;
  }

  public void createWizardPanels() {
    createWelcomePanel();
    createLicenseAgreementPanel();
    createSetupOptionsPanel();
    createInstallerPanel();
  }

  public void createWelcomePanel() {
    welcomeDescriptor = new WelcomePanelDescriptor();
    wizard.registerWizardPanel(WelcomePanelDescriptor.IDENTIFIER, welcomeDescriptor);
  }

  public void createLicenseAgreementPanel() {
    licenseAgreementDescriptor = new LicenseAgreementPanelDescriptor();
    wizard.registerWizardPanel(LicenseAgreementPanelDescriptor.IDENTIFIER,
        licenseAgreementDescriptor);
  }

  public void createSetupOptionsPanel() {
    setupOptionsDescriptor = new SetupOptionsPanelDescriptor();
    wizard.registerWizardPanel(SetupOptionsPanelDescriptor.IDENTIFIER, setupOptionsDescriptor);
  }

  public void createInstallerPanel() {
    installerDescriptor = new InstallerPanelDescriptor();
    wizard.registerWizardPanel(InstallerPanelDescriptor.IDENTIFIER, installerDescriptor);
  }

  public void extract(String zipfile, InstallerPanel installerPanel, File outputDir) {

    File currentArchive = new File(zipfile);

    byte[] buf = new byte[1024];
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mma", Locale.getDefault());

    boolean overwrite = false;
    ZipFile zf = null;
    FileOutputStream out = null;
    InputStream in = null;

    try {

      zf = new ZipFile(currentArchive);

      int size = zf.size();

      Enumeration entries = zf.entries();

      for (int i = 1; i <= size; i++) {

        ZipEntry entry = (ZipEntry) entries.nextElement();

        if (entry.isDirectory()) {
          continue;
        }

        String pathname = entry.getName();

        float floatSize = (new Float(size)).floatValue();
        float floatInc = (new Float(i)).floatValue();
        float floatPart = ((floatInc / floatSize) * 100.0f) * (100.0f / floatSize);
        installerPanel.setProgressValue((new Float(floatPart)).intValue());

        if (pathname.toUpperCase().startsWith("COM")
            || Main.MANIFEST.equals(pathname.toUpperCase())) {
          continue;
        }

        if (!Main.MAC_OS_X && pathname.toUpperCase().startsWith(Main.OS_X_APP)) {
          continue;
        }

        if (!Main.WINDOWS && pathname.toUpperCase().startsWith(Main.WINDOWS_EXE)) {
          continue;
        }

        if ((Main.WINDOWS || Main.MAC_OS_X) && pathname.toUpperCase().startsWith(Main.UNIX_SCRIPT)) {
          continue;
        }

        installerPanel.setProgressText(pathname);

        in = zf.getInputStream(entry);

        File outFile = new File(outputDir, pathname);
        Date archiveTime = new Date(entry.getTime());

        if (overwrite == false) {

          if (outFile.exists()) {

            Object[] options = { "Yes", "Yes To All", "No" };
            Date existTime = new Date(outFile.lastModified());
            Long archiveLen = new Long(entry.getSize());

            String msg = "File name conflict: There is already a file with that name on the disk!\n";
            msg += "\nFile name: " + outFile.getName() + "\nExisting file: "
                + formatter.format(existTime);
            msg += " (" + outFile.length() + " bytes)\nFile in archive:"
                + formatter.format(archiveTime) + " (" + archiveLen + " bytes)"
                + "\n\nWould you like to overwrite the file?";

            int result = JOptionPane.showOptionDialog(installerPanel, msg, "Warning",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

            if (result == 2) {
              // No
              continue;
            } else if (result == 1) {
              // YesToAll
              overwrite = true;
            }

          }

        }

        File parent = new File(outFile.getParent());
        if (parent != null && !parent.exists()) {
          parent.mkdirs();
        }

        out = new FileOutputStream(outFile);

        while (true) {
          int nRead = in.read(buf, 0, buf.length);
          if (nRead <= 0) {
            break;
          }
          out.write(buf, 0, nRead);
        }

        out.close();
        outFile.setLastModified(archiveTime.getTime());
      }

      if (Main.LINUX) {
        try {
          createDesktopLauncher(outputDir);
        } catch (Exception ex) {
          Main.displayError(ex);
        }
      }

      if (Main.MAC_OS_X) {

        try {
          File macOSDir = new File(outputDir.getPath() + "/SwingSane.app/Contents/MacOS");
          macOSDir.mkdirs();
        } catch (Exception ex) {
          Main.displayError(ex);
        }

        try {
          String[] cmdArray = { "ln", "-sf", "../../../lib",
              outputDir.getPath() + "/SwingSane.app/Contents/Resources/Java" };
          Runtime.getRuntime().exec(cmdArray);
        } catch (Exception ex) {
          Main.displayError(ex);
        }

        try {
          String[] cmdArray = { "cp", "-pf",
              "/System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub",
              outputDir.getPath() + "/SwingSane.app/Contents/MacOS/JavaApplicationStub" };
          Runtime.getRuntime().exec(cmdArray);
        } catch (Exception ex) {
          Main.displayError(ex);
        }

      }

      if (!Main.WINDOWS && !Main.MAC_OS_X) {
        try {
          String[] cmdArray = { "chmod", "+x", outputDir.getPath() + "/swingsane.sh" };
          Runtime.getRuntime().exec(cmdArray);
        } catch (Exception ex) {
          Main.displayError(ex);
        }
      }

      zf.close();

    } catch (Exception ex) {

      Main.displayError(ex);

      if (zf != null) {
        try {
          zf.close();
        } catch (IOException ioex) {
          Main.displayError(ioex);
        }
      }

      if (out != null) {
        try {
          out.close();
        } catch (IOException ioex) {
          Main.displayError(ioex);
        }
      }

      if (in != null) {
        try {
          in.close();
        } catch (IOException ioex) {
          Main.displayError(ioex);
        }
      }

    }

  }

  private void createDesktopLauncher(File outputDir) {

    String path = outputDir.getPath();

    StringBuffer sb = new StringBuffer();
    sb.append("#!/usr/bin/env xdg-open\n\n");
    sb.append("[Desktop Entry]\n");
    sb.append("Name=");
    sb.append(APPLICATION_NAME);
    sb.append("\n");
    sb.append("Exec=" + path + File.separator + "swingsane.sh %f\n");
    sb.append("Icon=" + path + File.separator + "icons" + File.separator + "swingsane_512x512.png\n");
    sb.append("Terminal=false\n");
    sb.append("MimeType=application/x-frs;application/x-frf;\n");
    sb.append("Type=Application\n");
    sb.append("Categories=Office;Application;\n");

    String filename = path + File.separator + "SwingSane.desktop";

    try {
      FileWriter fw = new FileWriter(filename);
      BufferedWriter bw = new BufferedWriter(fw);
      String outText = sb.toString();
      bw.write(outText);
      bw.close();
      fw.close();
    } catch (IOException ioex) {
      displayError(ioex);
    }

    try {
      new ProcessBuilder("/bin/chmod", "+x", filename).start().waitFor();
      new ProcessBuilder("xdg-desktop-menu", "install", "--novendor", filename).start().waitFor();
      new ProcessBuilder("xdg-desktop-icon", "install", "--novendor", filename).start().waitFor();
    } catch (InterruptedException e) {
      displayError(e);
    } catch (IOException e) {
      displayError(e);
    }

  }

  public static String implode(Object[] ary, String delim) {
    String out = "";
    for (int i = 0; i < ary.length; i++) {
      if (i != 0) {
        out += delim;
      }
      out += (String) ary[i];
    }
    return out;
  }

  public static void displayError(Exception ex) {
    System.out.println(ex.toString() + ": " + ex.getMessage());
    String caption = "Error";
    String message = ex.toString() + "\n" + implode(ex.getStackTrace(), "\n");
    javax.swing.JOptionPane.showConfirmDialog(null, message, caption,
        javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE);
  }

  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        String currentLAF = UIManager.getLookAndFeel().getClass().getName();

        if (Main.WINDOWS || Main.MAC_OS_X) {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          } catch (Exception ex) {
            System.out.println("Cannot set default PLAF: " + ex.getMessage());
          }
        } else {
          if (currentLAF.equals("javax.swing.plaf.metal")
              || currentLAF.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
            try {
              UIManager.setLookAndFeel("smooth.metal.SmoothLookAndFeel");
            } catch (Exception ex) {
              System.out.println("Cannot load smooth metal LAF: " + ex.getMessage());
            }
          }
        }
        new Main();
        System.exit(0);

      }
    });

  }

}
