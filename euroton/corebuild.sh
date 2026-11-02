# Build the patched mail core for the x86_64 emulator. Capped at 4 jobs.
R=~/corebuild.log; : > $R
sudo apt-get install -y -qq clang libclang-dev cmake protobuf-compiler >>$R 2>&1; echo "apt $?" >>$R
. ~/.cargo/env
export PATH=$HOME/go1.22/bin:$PATH ANDROID_HOME=$HOME/android ANDROID_NDK_HOME=$HOME/android/ndk/26.3.11579264
export JAVA_HOME=$HOME/jdk17 CARGO_BUILD_JOBS=4 MAIL_ANDROID_ABIS=x86_64
command -v cargo-ndk >/dev/null || cargo install cargo-ndk --locked >>$R 2>&1
cargo ndk --version >>$R 2>&1
cd ~/pc/project/mail/rust
timeout 9000 bash rust-build/build_android.sh mail-uniffi ./mail/mail-uniffi/uniffi.toml ./mail/mail-uniffi/android/lib/src/main >>$R 2>&1
echo "core-exit $?" >>$R
ls -la mail/mail-uniffi/android/lib/src/main/jniLibs/x86_64 >>$R 2>&1
echo COREDONE >>$R
