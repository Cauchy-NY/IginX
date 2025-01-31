@REM
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM

@echo off

if "%OS%" == "Windows_NT" setlocal

pushd %~dp0..
if NOT DEFINED IGINX_HOME set IGINX_HOME=%CD%
popd

set PATH="%JAVA_HOME%\bin\";%PATH%
set "FULL_VERSION="
set "MAJOR_VERSION="
set "MINOR_VERSION="

for /f tokens^=2-5^ delims^=.-_+^" %%j in ('java -fullversion 2^>^&1') do (
	set "FULL_VERSION=%%j-%%k-%%l-%%m"
	IF "%%j" == "1" (
	    set "MAJOR_VERSION=%%k"
	    set "MINOR_VERSION=%%l"
	) else (
	    set "MAJOR_VERSION=%%j"
	    set "MINOR_VERSION=%%k"
	)
)

set JAVA_VERSION=%MAJOR_VERSION%

@REM we do not check jdk that version less than 1.6 because they are too stale...
IF "%JAVA_VERSION%" == "6" (
		echo IginX only supports jdk >= 8, please check your java version.
		goto finally
)
IF "%JAVA_VERSION%" == "7" (
		echo IginX only supports jdk >= 8, please check your java version.
		goto finally
)

if "%OS%" == "Windows_NT" setlocal

set IGINX_CONF=%IGINX_HOME%\conf\config.properties

@setlocal ENABLEDELAYEDEXPANSION ENABLEEXTENSIONS
set is_conf_path=false
for %%i in (%*) do (
	IF "%%i" == "-c" (
		set is_conf_path=true
	) ELSE IF "!is_conf_path!" == "true" (
		set is_conf_path=false
		set IGINX_CONF=%%i
	) ELSE (
		set CONF_PARAMS=!CONF_PARAMS! %%i
	)
)

if NOT DEFINED MAIN_CLASS set MAIN_CLASS=cn.edu.tsinghua.iginx.tools.csv.ExportCsv
if NOT DEFINED JAVA_HOME goto :err


@REM -----------------------------------------------------------------------------
@REM Compute Memory for JVM configurations

if ["%system_cpu_cores%"] LSS ["1"] set system_cpu_cores="1"

set liner=0
for /f  %%b in ('wmic ComputerSystem get TotalPhysicalMemory') do (
	set /a liner+=1
	if !liner!==2 set system_memory=%%b
)

echo wsh.echo FormatNumber(cdbl(%system_memory%)/(1024*1024), 0) > %temp%\tmp.vbs
for /f "tokens=*" %%a in ('cscript //nologo %temp%\tmp.vbs') do set system_memory_in_mb=%%a
del %temp%\tmp.vbs
set system_memory_in_mb=%system_memory_in_mb:,=%

set /a half_=%system_memory_in_mb%/4
set /a quarter_=%half_%/8

if ["%half_%"] GTR ["1024"] set half_=1024
if ["%quarter_%"] GTR ["8192"] set quarter_=8192

if ["%half_%"] GTR ["quarter_"] (
	set max_heap_size_in_mb=%half_%
) else set max_heap_size_in_mb=%quarter_%

set MAX_HEAP_SIZE=%max_heap_size_in_mb%M

@REM -----------------------------------------------------------------------------
@REM JVM Opts we'll use in legacy run or installation
set JAVA_OPTS=-ea^
 -DIGINX_HOME=%IGINX_HOME%^
 -DIGINX_DRIVER=%IGINX_HOME%\driver^
 -DIGINX_CONF=%IGINX_CONF%

set HEAP_OPTS=-Xmx%MAX_HEAP_SIZE% -Xms%MAX_HEAP_SIZE% -Xloggc:"%IGINX_HOME%\gc.log" -XX:+PrintGCDateStamps -XX:+PrintGCDetails

@REM ***** CLASSPATH library setting *****
@REM Ensure that any user defined CLASSPATH variables are not used on startup
set CLASSPATH="%IGINX_HOME%\lib\*"
goto okClasspath

@REM -----------------------------------------------------------------------------
:okClasspath

set PARAMETERS=%*

@REM set default parameters
set d_parameter=-d .
set h_parameter=-h 127.0.0.1
set p_parameter=-p 6888
set u_parameter=-u root
set pw_parameter=-pw root
set tf_parameter=-tf timestamp
set tp_parameter=-tp ms

@REM Added parameters when default parameters are missing
echo %PARAMETERS% | findstr /c:"-d ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%d_parameter% %PARAMETERS%)
echo %PARAMETERS% | findstr /c:"-pw ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%pw_parameter% %PARAMETERS%)
echo %PARAMETERS% | findstr /c:"-u ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%u_parameter% %PARAMETERS%)
echo %PARAMETERS% | findstr /c:"-p ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%p_parameter% %PARAMETERS%)
echo %PARAMETERS% | findstr /c:"-h ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%h_parameter% %PARAMETERS%)
echo %PARAMETERS% | findstr /c:"-tf ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%tf_parameter% %PARAMETERS%)
echo %PARAMETERS% | findstr /c:"-tp ">nul && (set PARAMETERS=%PARAMETERS%) || (set PARAMETERS=%tp_parameter% %PARAMETERS%)

echo %PARAMETERS%

"%JAVA_HOME%\bin\java" %JAVA_OPTS% %HEAP_OPTS% -cp %CLASSPATH% %MAIN_CLASS% %PARAMETERS%

@REM reg delete "HKEY_CURRENT_USER\Environment" /v "DRIVER" /f
@REM set DRIVER=

goto finally

:err
echo JAVA_HOME environment variable must be set!
pause


@REM -----------------------------------------------------------------------------
:finally

pause

ENDLOCAL
