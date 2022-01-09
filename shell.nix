let
  pkgs = import <nixpkgs> {};
  jdk = pkgs.jdk8;
in
  pkgs.mkShell {
    buildInputs = [ jdk ];
    shellHook = ''
      export JAVA_HOME=${jdk}
    '';
  }
