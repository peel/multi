defmodule Blinks.Mixfile do
  use Mix.Project

  def project do
    [app: :blinks,
     version: "0.0.1",
     elixir: "~> 1.2",
     build_embedded: Mix.env == :prod,
     start_permanent: Mix.env == :prod,
     deps: deps]
  end

  def application do
    [applications: [:logger],
     env: [notifier: [],
           jvm_args: ["-Xms512m","-Xmx1024m"]],
     mod: {Blinks, []}]
  end

  defp deps do
    []
  end
end
