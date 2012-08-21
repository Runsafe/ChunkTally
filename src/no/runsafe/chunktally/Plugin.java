package no.runsafe.chunktally;

import no.runsafe.framework.RunsafeConfigurablePlugin;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(Accountant.class);
	}
}
