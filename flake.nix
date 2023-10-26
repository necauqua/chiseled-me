{
  description = "Just a devShell with a JVM, to use on NixOS with direnv";
  inputs.basic-dev-shell.url = "github:necauqua/basic-dev-shell";
  outputs = { self, basic-dev-shell }: basic-dev-shell.make (pkgs: [ pkgs.jdk17 ]);
}
