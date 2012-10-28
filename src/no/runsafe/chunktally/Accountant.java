package no.runsafe.chunktally;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.world.IChunkLoad;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.chunk.RunsafeChunk;
import no.runsafe.framework.server.entity.RunsafeEntity;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.logging.Level;

public class Accountant implements IChunkLoad, IConfigurationChanged
{
	public Accountant(IConfiguration config, IOutput output)
	{
		this.config = config;
		this.console = output;
	}

	@Override
	public void OnChunkLoad(RunsafeChunk chunk)
	{
		if (chunk.getEntities().size() > chunkEntityAudit)
		{
			if (!auditedWorlds.contains(chunk.getWorld().getName()))
				return;
			AuditEntitiesAboveLimit(chunk);
		}
	}

	private void AuditEntitiesAboveLimit(RunsafeChunk chunk)
	{
		List<RunsafeEntity> entities = chunk.getEntities();
		if (entities == null)
			return;
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		for (RunsafeEntity entity : entities)
		{
			String name = entity.getRaw().getClass().getSimpleName();
			if (!counts.containsKey(name))
				counts.put(name, 0);
			else
				counts.put(name, counts.get(name) + 1);
		}
		ArrayList<Map.Entry<String, Integer>> as = new ArrayList<Map.Entry<String, Integer>>(counts.entrySet());
		Collections.sort(as, new Comparator<Map.Entry<String, Integer>>()
		{
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		Map.Entry<String, Integer> max = as.get(0);
		console.outputColoredToConsole(
			String.format(
				"%sChunk [%s,%d,%d] is above entity limit! %d > %d (%d %s)%s",
				ChatColor.RED,
				chunk.getWorld().getName(),
				chunk.getX(),
				chunk.getZ(),
				entities.size(),
				chunkEntityAudit,
				max.getValue(),
				max.getKey(),
				ChatColor.RESET
			),
			Level.WARNING
		);
	}

	@Override
	public void OnConfigurationChanged()
	{
		chunkEntityAudit = config.getConfigValueAsInt("audit.entity.limit");
		auditedWorlds.clear();
		auditedWorlds.addAll(config.getConfigValueAsList("audit.entity.worlds"));
	}

	private final IConfiguration config;
	private final IOutput console;
	private final ArrayList<String> auditedWorlds = new ArrayList<String>();
	private int chunkEntityAudit;
}
