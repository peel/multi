defmodule Blinks.JavaNotifier do
  use GenServer
  require Logger

  defstruct node: nil, port: nil
  @type t :: %__MODULE__{node: String.t, port: port()}

  @registered_proc_name :blinks_java_server

  def start_link(args) do
    GenServer.start_link(__MODULE__, args, name: __MODULE__)
  end

  def init(args), do: init(args, System.find_executable("java"))

  defp init(_args, nil) do
    {:stop, "Cannot locate Java executable on PATH environment variable."}
  end
  defp init(args, exec) do
    self_node = Atom.to_string(Kernel.node())
    java_node = "__blinks__" <> self_node
    cookie = Node.get_cookie()
    jarfile = :code.priv_dir(:blinks) ++ '/led-notifier.jar'
    jvm_args = ['-jar', jarfile, '-s', java_node, '-c', cookie, '-p', @registered_proc_name]
    port = Port.open({:spawn_executable, exec},[{:line, 1000},:stderr_to_stdout,{:args, jvm_args}])
    state = Kernel.struct(__MODULE__, [node: String.to_atom(java_node), port: port])
    sync_with_java_startup(state)
  end

  defp sync_with_java_startup(state) do
    port = state.port
    receive do
      {^port, {:data, {:eol, 'READY'}}} ->
        Logger.info("Successfully started Java server process.")
        {_, pid} = GenServer.call({@registered_proc_name, state.node}, {:pid})
        true = Process.link(pid)
        Logger.info("Java server now linked.")
        true = Node.monitor(state.node, true)
        {:ok, state}
      {^port, {:data, {:eol, stdout}}} ->
        {:stop, stdout}
      msg ->
        {:stop, msg}
    end
  end

  def handle_info({:nodedown, node}, %__MODULE__{node: node} = state) do
    Logger.error("Java server process is down.")
    {:stop, :nodedown, state}
  end
  def handle_info({port, {:data, {:eol, msg}}}, %__MODULE__{port: port} = state) do
    Logger.info(msg)
    {:noreply, state}
  end
  def handle_info(_msg, state) do
    {:noreply, state}
  end
  def terminate(_reason, state), do: Port.close(state.port); :ok
end
