{
  inputs = {
    nixpkgs.url = "nixpkgs";
    flakelight = {
      url = "github:accelbread/flakelight";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    fenix = {
      url = "github:nix-community/fenix";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    android = {
      url = "github:tadfisher/android-nixpkgs/stable";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };
  outputs =
    {
      flakelight,
      nixpkgs,
      ...
    }:

    (flakelight ./. (
      { inputs, ... }:
      {
        inputs.nixpkgs = nixpkgs;
        nixpkgs.config.allowUnfree = true;
        nixpkgs.config.android_sdk.accept_license = true;
        devShell =
          { inputs', ... }:
          let
            android-sdk = (
              inputs'.android.sdk (
                sdkPkgs: with sdkPkgs; [
                  # Useful packages for building and testing.
                  build-tools-36-1-0
                  build-tools-35-0-0
                  cmdline-tools-latest
                  platform-tools
                  # platforms-android-34
                  platforms-android-35
                  platforms-android-36

                  # Other useful packages for a development environment.
                  # ndk-29-0-14206865
                  # skiaparser-3
                  # sources-android-34
                  sources-android-35
                  sources-android-36

                  # system-images-android-32-google-apis-x86-64
                  # emulator
                ]
              )
            );
            # rust-toolchain = (
            #   with inputs'.fenix.packages;
            #   combine [
            #     stable.toolchain
            #     targets.aarch64-linux-android.stable.rust-std
            #     targets.x86_64-linux-android.stable.rust-std
            #     targets.armv7-linux-androideabi.stable.rust-std
            #     targets.i686-linux-android.stable.rust-std

            #   ]
            # );
          in
          {
            packages = pkgs: [
              # rust-toolchain
              # android-sdk
              pkgs.android-studio
              pkgs.gradle
              pkgs.jdk
              # pkgs.rustup
              # pkgs.dioxus-cli
              # pkgs.wasm-bindgen-cli_0_2_100
              # pkgs.webkitgtk_6_0
              # pkgs.lld
              # pkgs.pkg-config
              # pkgs.curl
              # pkgs.wget
              # pkgs.file
              # pkgs.xdotool
              # pkgs.openssl
              # pkgs.libayatana-appindicator
              # pkgs.librsvg
              # pkgs.tailwindcss
            ];
            env = pkgs: {
              JAVA_HOME = pkgs.jdk.home;
              LD_LIBRARY_PATH = "${pkgs.libglvnd}/lib";
              ANDROID_HOME = "${android-sdk}/share/android-sdk";
              ANDROID_SDK_ROOT = "${android-sdk}/share/android-sdk";
            };
          };
      }
    ));
}
