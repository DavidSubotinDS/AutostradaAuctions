# Android Studio Settings for File Locking Prevention
# Add these to your Android Studio settings:

## File > Settings > Build, Execution, Deployment > Gradle
- Use Gradle from: 'Specified location' (point to your Gradle installation)
- Gradle JVM: Use 'Project SDK' or specific JDK
- Command-line Options: --no-daemon --no-build-cache

## File > Settings > Build, Execution, Deployment > Compiler
- Build process heap size (Mbytes): 2048
- Additional command line parameters: -Djava.io.tmpdir=%TEMP%\android-tmp

## File > Settings > Appearance & Behavior > System Settings  
- Synchronize files on frame or editor tab activation: UNCHECKED
- Save files on frame deactivation: CHECKED
- Save files automatically if application is idle for: 15 sec

## Windows Registry Fix (Run as Administrator in PowerShell):
# This increases the number of file handles Windows allows:
New-ItemProperty -Path "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Session Manager\SubSystems" -Name "Windows" -Value "%SystemRoot%\system32\csrss.exe ObjectDirectory=\Windows SharedSection=1024,20480,768 Windows=On SubSystemType=Windows ServerDll=basesrv,1 ServerDll=winsrv:UserServerDllInitialization,3 ServerDll=winsrv:ConServerDllInitialization,2 ServerDll=sxssrv,4 ProfileControl=Off MaxRequestThreads=16" -PropertyType String -Force

## Alternative: Use Windows Subsystem for Linux (WSL)
# Install WSL2 and run Android builds there - eliminates Windows file locking entirely