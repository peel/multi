defmodule Blinks do
  use Application

  def start(_type, _args) do
    import Supervisor.Spec, warn: false

    children = [
      worker(Blinks.JavaNotifier, [Application.get_env(:blinks, :notifier)]),
      worker(Blinks.JsNotifier, [Application.get_env(:blinks, :js_notifier)])
    ]

    opts = [strategy: :one_for_one, name: Blinks.Supervisor]
    Supervisor.start_link(children, opts)

  end
end
