# Memory Match — Scaffold

Minimal scaffold. MVP game implementation will be filed as an issue.

## Environment

- Working directory: /home/scarolan.linux/memory-match
- The gradle build system is ALREADY set up (settings.gradle.kts, app/build.gradle.kts,
  AndroidManifest.xml) — do NOT modify any gradle file or the manifest.
- There is no local Android SDK. To build and test, run `./test-remote.sh`
  from the working directory. It syncs your code to the build host and runs
  the JVM unit tests there, and it is the ONLY way to build or test this
  project — never run ssh, rsync, or gradle commands yourself; the script
  handles all of that. Run it after writing your files and make the tests
  pass.
- Do NOT run git commit or git push.
