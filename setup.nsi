Name SwingSane

# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 0.2
!define COMPANY "Roland Quast"
!define URL http://www.swingsane.com

!define upgradecode {A69D7404-9F52-447A-AE0B-BD66F09D96CF} ; SwingSane MSI upgrade code

# MUI Symbol Definitions
!define MUI_ICON "icons\swingsane_package.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_LICENSEPAGE_RADIOBUTTONS
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY ${REGKEY}
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULTFOLDER SwingSane
!define MUI_UNICON "icons\swingsane_package.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define JRE_VERSION "1.7.0"

# Included files
!include Sections.nsh
!include MUI2.nsh

!include "WordFunc.nsh"
  !insertmacro VersionCompare

Var UNINSTALL_OLD_VERSION

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE LICENSE
Page custom CheckInstalledJRE
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
OutFile setup_unsigned.exe
InstallDir $PROGRAMFILES\SwingSane
CRCCheck on
XPStyle on
ShowInstDetails show
VIProductVersion 1.5.1.0
VIAddVersionKey ProductName SwingSane
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyName "${COMPANY}"
VIAddVersionKey CompanyWebsite "${URL}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Installer sections
Section "SwingSane Program" SEC0000
    SetOutPath $INSTDIR
    SetOverwrite on
    File target\swingsane.exe
    SetOutPath $INSTDIR\lib
    File /r target\appbundle\lib\*
    SetOutPath $INSTDIR
    File /r LICENSE
    File /r NOTICE
    SetOutPath $INSTDIR\icons
    File /r icons\*
    SetOutPath $DESKTOP
    CreateShortcut $DESKTOP\SwingSane.lnk $INSTDIR\swingsane.exe
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut $SMPROGRAMS\$StartMenuGroup\SwingSane.lnk $INSTDIR\swingsane.exe
    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section /o "Java JRE" SEC0001
    SetOutPath $INSTDIR\jre
    SetOverwrite on
    File /r target\jre\*
    WriteRegStr HKLM "${REGKEY}\Components" JRE 1
SectionEnd

Section -post SEC0002
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    !insertmacro MUI_STARTMENU_WRITE_END
    WriteRegStr HKLM "Software\${REGKEY}" "" $INSTDIR
    WriteRegStr HKLM "Software\${REGKEY}" "Version" "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" Publisher "${COMPANY}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" URLInfoAbout "${URL}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
    Delete /REBOOTOK $INSTDIR\icons\*.ico
    Delete /REBOOTOK $INSTDIR\icons\*.png
    Delete /REBOOTOK $INSTDIR\icons\*.icns
    Delete /REBOOTOK $INSTDIR\icons\*.svg
    RmDir /REBOOTOK $INSTDIR\icons
    Delete /REBOOTOK $INSTDIR\lib\*.jar
    Delete /REBOOTOK $INSTDIR\lib\*.jnilib
    Delete /REBOOTOK $INSTDIR\lib\*.dll
    RmDir /REBOOTOK $INSTDIR\lib
    Delete /REBOOTOK $INSTDIR\LICENSE
    Delete /REBOOTOK $INSTDIR\NOTICE
    Delete /REBOOTOK $INSTDIR\swingsane.exe
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

# Uninstaller sections
Section /o -un.JRE UNSEC0001
    RmDir /r /REBOOTOK $INSTDIR\jre
    DeleteRegValue HKLM "${REGKEY}\Components" JRE
SectionEnd

Section -un.post UNSEC0002
    DeleteRegKey HKLM "Software\${REGKEY}"
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    Delete /REBOOTOK $DESKTOP\SwingSane.lnk
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /r /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
SectionEnd

Function CheckInstalledJRE
  ; MessageBox MB_OK "Checking Installed JRE Version"
  Push "${JRE_VERSION}"
  Call DetectJRE
  ; Messagebox MB_OK "Done checking JRE version"
  Exch $0   ; Get return value from stack
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld
  Goto JREAlreadyInstalled
 
FoundOld:
  ; MessageBox MB_OK "Old JRE found"
  MessageBox MB_OK "An old version of Java was found your computer. Please select the Java JRE component in the following screen."
  Return
 
NoFound:
  ; MessageBox MB_OK "JRE not found"
  MessageBox MB_OK "Java was not found on your computer. Please select the Java JRE component in the following screen."
  Return

JREAlreadyInstalled:
  ; MessageBox MB_OK "JRE already installed"
  Return
 
FunctionEnd

; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE
 
; DetectJRE. Version requested is on the stack.
; Returns (on stack)    "0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!
 
Function DetectJRE
  Exch $0   ; Get version requested  
        ; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1   ; $1 = Java version string (ie 1.5.0)
  Push $2   ; $2 = Javahome
  Push $3   ; $3 and $4 are used for checking the major/minor version of java
  Push $4
  ; MessageBox MB_OK "Detecting JRE"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ; MessageBox MB_OK "Read : $1"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  ; MessageBox MB_OK "Read 3: $2"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ; MessageBox MB_OK "Detect Read : $1"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
 ; MessageBox MB_OK "Detect Read 3: $2"
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
  ; MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1            ; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1            ; $3 = major version requested, $4 = major version found
  ; MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2          ; Same as above. $3 is minor version requested, $4 is minor version installed
 ; MessageBox MB_OK "Want $3 , found $4" 
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
  MessageBox MB_OK "Java was not found on your computer. Please select the Java JRE component in the following screen."
  Push "0"
  Goto DetectJREEnd
 
FoundOld:
  ; MessageBox MB_OK "JRE too old: $3 is older than $4"
  MessageBox MB_OK "The Java version found on your computer is too old to run SwingSane. Please select the Java JRE component in the following screen."
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJREEnd  
FoundNew:
  ; MessageBox MB_OK "JRE is new: $3 is newer than $4"
 
  Push "$2\bin\java.exe"
;  Push "OK"
;  Return
   Goto DetectJREEnd
DetectJREEnd:
    ; Top of stack is return value, then r4,r3,r2,r1
    Exch    ; => r4,rv,r3,r2,r1,r0
    Pop $4  ; => rv,r3,r2,r1r,r0
    Exch    ; => r3,rv,r2,r1,r0
    Pop $3  ; => rv,r2,r1,r0
    Exch    ; => r2,rv,r1,r0
    Pop $2  ; => rv,r1,r0
    Exch    ; => r1,rv,r0
    Pop $1  ; => rv,r0
    Exch    ; => r0,rv
    Pop $0  ; => rv 
FunctionEnd

# Installer functions
Function .onInit

System::Call 'kernel32::CreateMutexA(i 0, i 0, t "SwingSaneInstaller") i .r1 ?e'
Pop $R0
StrCmp $R0 0 +3
   MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
   Abort

notRunning:
  InitPluginsDir
  ;Check earlier installation
  ClearErrors
  ReadRegStr $0 HKLM "Software\${REGKEY}" "Version"
  IfErrors init.uninst ; older versions might not have "Version" string set
  ${VersionCompare} $0 ${VERSION} $1
  IntCmp $1 2 init.uninst
    MessageBox MB_YESNO|MB_ICONQUESTION "SwingSane version $0 seems to be already installed on your system.$\nWould you like to proceed with the installation of version ${VERSION}?" \
        IDYES init.uninst
    Quit

init.uninst:
  ClearErrors
  ReadRegStr $0 HKLM "Software\${REGKEY}" ""
  IfErrors init.done
  StrCpy $UNINSTALL_OLD_VERSION '"$0\uninstall.exe" /S _?=$0'

init.done:

StrCpy $0 0

loop:

System::Call 'MSI::MsiEnumRelatedProducts(t "${upgradecode}",i0,i r0,t.r1)i.r2'

${If} $2 = 0
    MessageBox MB_YESNO|MB_ICONQUESTION "An older version of SwingSane was detected. Clicking yes will uninstall it first before continuing with this installation. Uninstalling will not remove your existing database or preferences. Would you like to continue?" \
        IDYES continue.detection
    Quit
continue.detection:
    ExecWait '"msiexec.exe" /x $1 /qb'
    IntOp $0 $0 + 1
    goto loop
${Endif}

FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuGroup
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
    !insertmacro SELECT_UNSECTION JRE ${UNSEC0001}
FunctionEnd

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT ${SEC0000} "SwingSane Program Files (required)."
!insertmacro MUI_DESCRIPTION_TEXT ${SEC0001} "Java JRE. Select this option if Java is not installed on your computer or if your Java version is too old."
!insertmacro MUI_FUNCTION_DESCRIPTION_END