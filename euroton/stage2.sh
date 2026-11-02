# Package the patched core, publish it to ~/.m2, then build the app against it.
R=~/stage2.log; : > $R
export ANDROID_HOME=$HOME/android JAVA_HOME=$HOME/jdk17 PATH=$HOME/jdk17/bin:$PATH
cd ~/pc/project/mail/rust/mail/mail-uniffi/android
./gradlew :lib:assembleRelease --console=plain --max-workers=4 -Dorg.gradle.jvmargs=-Xmx2g >>$R 2>&1; echo "aar-exit $?" >>$R
AAR=$(ls lib/build/outputs/aar/lib-release.aar 2>/dev/null)
V=0.168.2-euroton1; D=~/.m2/repository/me/proton/mail/common/lib/$V
mkdir -p $D; cp "$AAR" $D/lib-$V.aar && sed "s#<version>0.168.2</version>#<version>$V</version>#" ~/m2tmp/lib-0.168.2.pom > $D/lib-$V.pom
ls -la $D >>$R
pkill -9 -f "[G]radleDaemon"
cd ~/amw
[ -f app/google-services.json ] || cp /home/dev/code/EurotonFoss/android-mail/app/google-services.json app/
./gradlew :app:assembleAlphaDebug --console=plain --max-workers=4 -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs=-Xmx4g -Dkotlin.daemon.jvm.options=-Xmx3g -Porg.gradle.java.installations.paths=$HOME/jdk17 >>$R 2>&1
echo "app-exit $?" >>$R
pkill -9 -f "[G]radleDaemon"; pkill -9 -f "[K]otlinCompileDaemon"
ls -la app/build/outputs/apk/alpha/debug/*.apk >>$R 2>&1
echo STAGE2DONE >>$R
