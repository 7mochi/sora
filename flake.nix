{
  description = "sora";

  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";

  outputs =
    { nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = with pkgs; [
          git
          just
          jdk25
          gradle
          mariadb.client
          redis
        ];

        shellHook = ''
          echo "sora"
          echo "  just run"
          echo "  just lint"
          echo "  just check"
          echo "  just test"
        '';
      };
    };
}
