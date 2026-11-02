# Building the self-hosted Android app

This branch is upstream `d6ff23af15` (2026-06-24, Rust core 0.168.2) plus the server picker & key pinning. It needs a rebuilt core, because the published core only accepts SRP moduli signed by Proton's key. Euroton ADR 259 has the reasons.

## Tools

Tested in a Debian 13 container with 14 GB RAM & 8 cores:

- Rust 1.93.0 with targets `x86_64-linux-android` & `aarch64-linux-android`
- Go 1.22.12 exactly; `build_android.sh` refuses anything else
- Android NDK 26.3.11579264, JDK 17, `clang`, `libclang-dev`, `cmake`, `jq`
- `cargo-ndk` 3.5.4. Version 4.1.2 breaks `gopenpgp-sys` 0.3.6, which builds its compiler path from `CARGO_NDK_ANDROID_PLATFORM`.

## Core

1. Clone `https://github.com/ProtonMail/clients` & check out `2ecb794`. The Rust code is under `project/mail/rust/`.
2. In the clone's `.cargo/config.toml`, delete the `nexus-crates-io-proxy` registry & the `[source.crates-io]` block that points at it. Proton's own registry, `rust-registry.proton.me`, is public & stays.
3. Run `python3 core/trim-workspace.py` from this folder. It drops the 78 workspace members that the public clone doesn't contain.
4. Copy `proton-srp` 0.8.2 out of `~/.cargo/registry/src/rust-registry.proton.me-*/` to `patches/proton-srp` in the clone & apply `core/proton-srp-0.8.2.diff`. Add `proton-srp = { path = "patches/proton-srp" }` under the existing `[patch.proton]` in the root `Cargo.toml`.
5. Apply `core/mail-uniffi.diff` in the clone.
6. From `project/mail/rust/`, with Go 1.22.12 first on `PATH` & `ANDROID_NDK_HOME` set:

   ```
   MAIL_ANDROID_ABIS=x86_64 CARGO_BUILD_JOBS=4 \
     bash rust-build/build_android.sh mail-uniffi ./mail/mail-uniffi/uniffi.toml ./mail/mail-uniffi/android/lib/src/main
   ```

   Use `arm64-v8a` for a phone. The x86_64 build took 22 minutes with 4 jobs.
7. In `mail/mail-uniffi/android/`, run `./gradlew :lib:assembleRelease` with `cargo` on `PATH`.
8. Copy `lib/build/outputs/aar/lib-release.aar` to `~/.m2/repository/me/proton/mail/common/lib/0.168.2-euroton1/lib-0.168.2-euroton1.aar`. Next to it, put Maven Central's `lib-0.168.2.pom` with the version changed to `0.168.2-euroton1`, so the app gets the same dependencies.

## App

`gradle/libs.versions.toml` already asks for `0.168.2-euroton1`. `app/google-services.json` must exist; any placeholder works, & push notifications through Firebase won't work with one.

```
./gradlew :app:assembleAlphaDebug --max-workers=4 -Dorg.gradle.jvmargs=-Xmx4g -Dkotlin.daemon.jvm.options=-Xmx3g
```

The server's TLS certificate must be trusted by the phone.

## What the app does

Add account shows `Server: default`. Tapping it takes a domain, fetches `https://mail-api.<domain>/core/v4/auth/modulus/key` & shows the key's fingerprint. Trust stores the domain & key, then restarts the app. From then on every modulus is checked against that key. A key that doesn't match stops the login before the password proof is sent.

Compare the fingerprint with the server's own copy before tapping Trust. On the server, `GET /core/v4/auth/modulus/key` from inside the network gives the same value.
