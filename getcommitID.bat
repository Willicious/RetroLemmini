@echo off
setlocal
for /f %%i in ('git rev-parse HEAD') do set COMMIT_ID=%%i

(
echo package lemmini.tools;
echo.
echo public class CommitID {
echo     public static final String ID = "%COMMIT_ID%";
echo }
) > src\lemmini\tools\CommitID.java