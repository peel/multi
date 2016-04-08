defmodule Blinks.JsNotifier do
  use GenServer

  def start_link(args) do
    GenServer.start_link(__MODULE__, {"sudo node #{:code.priv_dir(:blinks) ++ '/notifier-js/server.js'}", "not-blinking", args}, name: __MODULE__)
  end

  def init({cmd,init,opts}) do
    port = Port.open({:spawn,'#{cmd}'}, [:binary,:exit_status, packet: 4] ++ opts)
    send(port, {self, {:command,:erlang.term_to_binary(init) }})
    {:ok, port}
  end

  def handle_info({port,{:exit_status,0}},port), do: {:stop,:normal,port}
  def handle_info({port,{:exit_status,_}},port), do: {:stop,:port_terminated,port}
  def handle_info(_,port), do: {:noreply,port}

  def handle_cast(term,port) do
    send(port,{self,{:command,:erlang.term_to_binary(term)}})
    {:noreply,port}
  end

  def handle_call(term,_reply_to,port) do
    send(port,{self,{:command,:erlang.term_to_binary(term)}})
    res = receive do {^port,{:data,b}} -> :erlang.binary_to_term(b) end
    {:reply,res,port}
  end

end
