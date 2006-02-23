@echo off
cls

set CP=target\classes;target\test-classes
set J_PARAMS=%PANMX_J_PARAMS%

:checkJMX
if ""%PANMX_NOJMX%""=="""" goto jmxEnable
goto doneStart

:jmxEnable
echo JMX Remote RMI Connector Enabled
set J_PARAMS=%J_PARAMS% -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.ssl=false

:doneStart

echo Starting PanMX Test
java %J_PARAMS% -classpath %CP% panmx.rmx.RMXBeanTest %*
