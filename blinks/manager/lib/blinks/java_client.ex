defmodule Blinks.JavaClient do

  def say(timeout \\ 5000) do
    server = get_java_server()
    GenServer.call(server, {:say}, timeout)
  end

  def blink do
    server = get_java_server()
    GenServer.cast(server, {:blink})
  end

  defp get_java_server() do
    java_node = "__blinks__" <> Atom.to_string(Kernel.node())
    {:blinks_java_server, String.to_atom(java_node)}
  end
end
