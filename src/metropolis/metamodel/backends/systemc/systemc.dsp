# Microsoft Developer Studio Project File - Name="systemc" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=systemc - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "systemc.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "systemc.mak" CFG="systemc - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "systemc - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "systemc - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "systemc - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "systemc - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "systemc___Win32_Debug"
# PROP BASE Intermediate_Dir "systemc___Win32_Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "systemc___Win32_Debug"
# PROP Intermediate_Dir "systemc___Win32_Debug"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD CPP /nologo /W3 /Gm /GR /GX /ZI /Od /I "util" /I "ltl2ba-1.0" /I "zchaff" /I "..\..\..\..\..\..\systemc-2.0.1\src" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ENDIF 

# Begin Target

# Name "systemc - Win32 Release"
# Name "systemc - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\action.cpp
# End Source File
# Begin Source File

SOURCE=.\argsutil.cpp
# End Source File
# Begin Source File

SOURCE=.\behavior.cpp
# End Source File
# Begin Source File

SOURCE=.\buchiman.cpp
# End Source File
# Begin Source File

SOURCE=.\DefaultBehavior.cpp
# End Source File
# Begin Source File

SOURCE=.\event.cpp
# End Source File
# Begin Source File

SOURCE=.\global.cpp
# End Source File
# Begin Source File

SOURCE=.\GlobalTime.cpp
# End Source File
# Begin Source File

SOURCE=.\globaltimemanager.cpp
# End Source File
# Begin Source File

SOURCE=.\GlobalTimeRequestClass.cpp
# End Source File
# Begin Source File

SOURCE=.\manager.cpp
# End Source File
# Begin Source File

SOURCE=.\medium.cpp
# End Source File
# Begin Source File

SOURCE=.\netlist.cpp
# End Source File
# Begin Source File

SOURCE=.\netlist_b.cpp
# End Source File
# Begin Source File

SOURCE=.\node.cpp
# End Source File
# Begin Source File

SOURCE=.\nondeterminism.cpp
# End Source File
# Begin Source File

SOURCE=.\object.cpp
# End Source File
# Begin Source File

SOURCE=.\port_rusage.cpp
# End Source File
# Begin Source File

SOURCE=.\portmap.cpp
# End Source File
# Begin Source File

SOURCE=.\process.cpp
# End Source File
# Begin Source File

SOURCE=.\programcounter.cpp
# End Source File
# Begin Source File

SOURCE=.\quantity.cpp
# End Source File
# Begin Source File

SOURCE=.\quantitymanager.cpp
# End Source File
# Begin Source File

SOURCE=.\requestclass.cpp
# End Source File
# Begin Source File

SOURCE=.\sat_hook.cpp
# End Source File
# Begin Source File

SOURCE=.\scheduler.cpp
# End Source File
# Begin Source File

SOURCE=.\schedulingnetlist.cpp
# End Source File
# Begin Source File

SOURCE=.\scoreboard.cpp
# End Source File
# Begin Source File

SOURCE=.\statemedium.cpp
# End Source File
# Begin Source File

SOURCE=.\String.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\action.h
# End Source File
# Begin Source File

SOURCE=.\argsutil.h
# End Source File
# Begin Source File

SOURCE=.\behavior.h
# End Source File
# Begin Source File

SOURCE=.\buchiman.h
# End Source File
# Begin Source File

SOURCE=.\DefaultBehavior.h
# End Source File
# Begin Source File

SOURCE=.\event.h
# End Source File
# Begin Source File

SOURCE=.\global.h
# End Source File
# Begin Source File

SOURCE=.\GlobalTime.h
# End Source File
# Begin Source File

SOURCE=.\globaltimemanager.h
# End Source File
# Begin Source File

SOURCE=.\GlobalTimeRequestClass.h
# End Source File
# Begin Source File

SOURCE=.\library.h
# End Source File
# Begin Source File

SOURCE=.\macros.h
# End Source File
# Begin Source File

SOURCE=.\manager.h
# End Source File
# Begin Source File

SOURCE=.\medium.h
# End Source File
# Begin Source File

SOURCE=.\MetroString.h
# End Source File
# Begin Source File

SOURCE=.\netlist.h
# End Source File
# Begin Source File

SOURCE=.\netlist_b.h
# End Source File
# Begin Source File

SOURCE=.\node.h
# End Source File
# Begin Source File

SOURCE=.\nondeterminism.h
# End Source File
# Begin Source File

SOURCE=.\object.h
# End Source File
# Begin Source File

SOURCE=.\port_rusage.h
# End Source File
# Begin Source File

SOURCE=.\portmap.h
# End Source File
# Begin Source File

SOURCE=.\process.h
# End Source File
# Begin Source File

SOURCE=.\programcounter.h
# End Source File
# Begin Source File

SOURCE=.\psapi.h
# End Source File
# Begin Source File

SOURCE=.\quantity.h
# End Source File
# Begin Source File

SOURCE=.\quantitymanager.h
# End Source File
# Begin Source File

SOURCE=.\requestclass.h
# End Source File
# Begin Source File

SOURCE=.\sat_hook.h
# End Source File
# Begin Source File

SOURCE=.\scheduler.h
# End Source File
# Begin Source File

SOURCE=.\schedulingnetlist.h
# End Source File
# Begin Source File

SOURCE=.\scoreboard.h
# End Source File
# Begin Source File

SOURCE=.\statemediumDeclaration.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\util\Debug\util.lib
# End Source File
# Begin Source File

SOURCE=.\zchaff\Debug\zchaff.lib
# End Source File
# Begin Source File

SOURCE="..\..\..\..\..\..\systemc-2.0.1\msvc60\systemc\Debug\systemc.lib"
# End Source File
# Begin Source File

SOURCE=".\ltl2ba-1.0\Debug\ltl2ba.lib"
# End Source File
# End Target
# End Project
